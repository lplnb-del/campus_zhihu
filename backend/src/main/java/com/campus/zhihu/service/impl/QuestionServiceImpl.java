package com.campus.zhihu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.common.ResultCode;
import com.campus.zhihu.dto.QuestionPublishDTO;
import com.campus.zhihu.dto.QuestionQueryDTO;
import com.campus.zhihu.dto.QuestionUpdateDTO;
import com.campus.zhihu.entity.BizQuestion;
import com.campus.zhihu.entity.BizQuestionTag;
import com.campus.zhihu.entity.BizTag;
import com.campus.zhihu.entity.SysUser;
import com.campus.zhihu.mapper.QuestionMapper;
import com.campus.zhihu.mapper.QuestionTagMapper;
import com.campus.zhihu.mapper.TagMapper;
import com.campus.zhihu.mapper.UserMapper;
import com.campus.zhihu.service.QuestionService;
import com.campus.zhihu.vo.QuestionVO;
import com.campus.zhihu.vo.UserVO;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 问题服务实现类
 *
 * @author CampusZhihu Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final TagMapper tagMapper;
    private final QuestionTagMapper questionTagMapper;
    private final UserMapper userMapper;

    /**
     * 最大重试次数（乐观锁）
     */
    private static final int MAX_RETRY_TIMES = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "questions", allEntries = true)
    public QuestionVO publishQuestion(
        Long userId,
        QuestionPublishDTO publishDTO
    ) {
        log.info(
            "用户发布问题: userId={}, title={}",
            userId,
            publishDTO.getTitle()
        );

        // 1. 校验用户是否存在
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 2. 校验标签是否存在
        List<Long> tagIds = publishDTO.getTagIds();
        if (!CollectionUtils.isEmpty(tagIds)) {
            List<BizTag> tags = tagMapper.selectByIds(tagIds);
            if (tags.size() != tagIds.size()) {
                throw new BusinessException(
                    ResultCode.PARAM_ERROR.getCode(),
                    "部分标签不存在"
                );
            }
        }

        // 2.5. 校验积分是否足够（仅当发布正式问题时）
        if (Boolean.TRUE.equals(publishDTO.getPublish())) {
            Integer rewardPoints = publishDTO.getRewardPoints() != null
                ? publishDTO.getRewardPoints()
                : 0;
            if (user.getPoints() == null || user.getPoints() < rewardPoints) {
                throw new BusinessException(
                    ResultCode.INSUFFICIENT_POINTS.getCode(),
                    "积分不足，无法发布问题"
                );
            }
        }

        // 3. 创建问题实体
        BizQuestion question = new BizQuestion();
        question.setUserId(userId);
        question.setTitle(publishDTO.getTitle());
        question.setContent(publishDTO.getContent());

        // 处理图片列表
        if (!CollectionUtils.isEmpty(publishDTO.getImages())) {
            question.setImages(String.join(",", publishDTO.getImages()));
        }

        question.setRewardPoints(
            publishDTO.getRewardPoints() != null
                ? publishDTO.getRewardPoints()
                : 0
        );
        question.setStatus(
            Boolean.TRUE.equals(publishDTO.getPublish())
                ? BizQuestion.STATUS_PUBLISHED
                : BizQuestion.STATUS_DRAFT
        );
        question.setIsSolved(BizQuestion.RESOLVED_NO);
        question.setViewCount(0);
        question.setAnswerCount(0);
        question.setCollectionCount(0);
        question.setLikeCount(0);

        // 4. 插入问题
        int rows = questionMapper.insert(question);
        if (rows <= 0) {
            throw new BusinessException(
                ResultCode.DATABASE_ERROR.getCode(),
                "问题发布失败"
            );
        }

        // 4.5. 扣除积分（仅当发布正式问题时）
        if (Boolean.TRUE.equals(publishDTO.getPublish()) && question.getRewardPoints() > 0) {
            int updateRows = userMapper.deductPoints(
                userId,
                question.getRewardPoints()
            );
            if (updateRows <= 0) {
                throw new BusinessException(
                    ResultCode.INSUFFICIENT_POINTS.getCode(),
                    "积分不足，无法发布问题"
                );
            }
        }

        // 5. 插入问题-标签关联
        if (!CollectionUtils.isEmpty(tagIds)) {
            questionTagMapper.batchInsert(question.getId(), tagIds);
            // 增加标签使用次数
            tagMapper.batchUpdateUseCount(tagIds, 1);
        }

        log.info(
            "问题发布成功: questionId={}, userId={}",
            question.getId(),
            userId
        );

        // 6. 返回问题详情
        return getQuestionByIdWithoutView(question.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "questions", allEntries = true)
    public QuestionVO updateQuestion(
        Long userId,
        Long questionId,
        QuestionUpdateDTO updateDTO
    ) {
        log.info("用户更新问题: userId={}, questionId={}", userId, questionId);

        // 1. 查询问题
        BizQuestion question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(
                ResultCode.PARAM_ERROR.getCode(),
                "问题不存在"
            );
        }

        // 2. 校验权限（只有问题发布者才能更新）
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(
                ResultCode.FORBIDDEN.getCode(),
                "无权限修改此问题"
            );
        }

        // 3. 更新字段
        if (StringUtils.hasText(updateDTO.getTitle())) {
            question.setTitle(updateDTO.getTitle());
        }
        if (StringUtils.hasText(updateDTO.getContent())) {
            question.setContent(updateDTO.getContent());
        }
        if (updateDTO.getImages() != null) {
            question.setImages(
                CollectionUtils.isEmpty(updateDTO.getImages())
                    ? null
                    : String.join(",", updateDTO.getImages())
            );
        }
        if (updateDTO.getRewardPoints() != null) {
            question.setRewardPoints(updateDTO.getRewardPoints());
        }
        if (updateDTO.getStatus() != null) {
            question.setStatus(updateDTO.getStatus());
        }

        // 4. 更新问题
        int rows = questionMapper.updateById(question);
        if (rows <= 0) {
            throw new BusinessException(
                ResultCode.DATABASE_ERROR.getCode(),
                "问题更新失败"
            );
        }

        // 5. 更新标签关联（如果提供了新的标签列表）
        if (!CollectionUtils.isEmpty(updateDTO.getTagIds())) {
            // 获取旧标签
            List<Long> oldTagIds = questionTagMapper.selectTagIdsByQuestionId(
                questionId
            );

            // 删除旧关联
            questionTagMapper.deleteByQuestionId(questionId);

            // 插入新关联
            questionTagMapper.batchInsert(questionId, updateDTO.getTagIds());

            // 更新标签使用次数
            if (!CollectionUtils.isEmpty(oldTagIds)) {
                tagMapper.batchUpdateUseCount(oldTagIds, -1);
            }
            tagMapper.batchUpdateUseCount(updateDTO.getTagIds(), 1);
        }

        log.info("问题更新成功: questionId={}, userId={}", questionId, userId);

        // 6. 返回更新后的问题详情
        return getQuestionByIdWithoutView(questionId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "questions", allEntries = true)
    public Boolean deleteQuestion(Long userId, Long questionId) {
        log.info("用户删除问题: userId={}, questionId={}", userId, questionId);

        // 1. 查询问题
        BizQuestion question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(
                ResultCode.PARAM_ERROR.getCode(),
                "问题不存在"
            );
        }

        // 2. 校验权限（只有问题发布者才能删除）
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(
                ResultCode.FORBIDDEN.getCode(),
                "无权限删除此问题"
            );
        }

        // 3. 逻辑删除问题
        int rows = questionMapper.deleteById(questionId);
        if (rows <= 0) {
            throw new BusinessException(
                ResultCode.DATABASE_ERROR.getCode(),
                "问题删除失败"
            );
        }

        // 4. 删除问题-标签关联，并减少标签使用次数
        List<Long> tagIds = questionTagMapper.selectTagIdsByQuestionId(
            questionId
        );
        if (!CollectionUtils.isEmpty(tagIds)) {
            questionTagMapper.deleteByQuestionId(questionId);
            tagMapper.batchUpdateUseCount(tagIds, -1);
        }

        log.info("问题删除成功: questionId={}, userId={}", questionId, userId);
        return true;
    }

    @Override
    public QuestionVO getQuestionById(Long questionId, Long userId) {
        log.debug("获取问题详情: questionId={}, userId={}", questionId, userId);

        // 1. 增加浏览次数
        questionMapper.incrementViewCount(questionId);

        // 2. 查询问题详情
        return getQuestionByIdWithoutView(questionId, userId);
    }

    @Override
    public QuestionVO getQuestionByIdWithoutView(Long questionId, Long userId) {
        log.debug(
            "获取问题详情（不增加浏览）: questionId={}, userId={}",
            questionId,
            userId
        );

        // 1. 查询问题
        BizQuestion question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(
                ResultCode.PARAM_ERROR.getCode(),
                "问题不存在"
            );
        }

        // 2. 转换为VO
        return convertToVO(question, userId);
    }

    @Override
    public IPage<QuestionVO> getQuestionPage(
        QuestionQueryDTO queryDTO,
        Long userId
    ) {
        log.debug("分页查询问题: queryDTO={}", queryDTO);

        // 1. 构建分页对象
        Page<BizQuestion> page = new Page<>(
            queryDTO.getPageNum(),
            queryDTO.getPageSize()
        );

        // 2. 构建查询条件
        LambdaQueryWrapper<BizQuestion> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w ->
                w
                    .like(BizQuestion::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(BizQuestion::getContent, queryDTO.getKeyword())
            );
        }

        // 用户ID
        if (queryDTO.getUserId() != null) {
            wrapper.eq(BizQuestion::getUserId, queryDTO.getUserId());
        }

        // 状态
        if (queryDTO.getStatus() != null) {
            wrapper.eq(BizQuestion::getStatus, queryDTO.getStatus());
        }

        // 是否已解决
        if (queryDTO.getIsResolved() != null) {
        wrapper.eq(BizQuestion::getIsSolved, queryDTO.getIsResolved());        }

        // 悬赏筛选
        if (Boolean.TRUE.equals(queryDTO.getHasReward())) {
            wrapper.gt(BizQuestion::getRewardPoints, 0);
        }
        if (queryDTO.getMinRewardPoints() != null) {
            wrapper.ge(
                BizQuestion::getRewardPoints,
                queryDTO.getMinRewardPoints()
            );
        }
        if (queryDTO.getMaxRewardPoints() != null) {
            wrapper.le(
                BizQuestion::getRewardPoints,
                queryDTO.getMaxRewardPoints()
            );
        }

        // 标签筛选
        if (queryDTO.getTagId() != null) {
            List<Long> questionIds = questionTagMapper.selectQuestionIdsByTagId(
                queryDTO.getTagId()
            );
            if (CollectionUtils.isEmpty(questionIds)) {
                return new Page<>(
                    queryDTO.getPageNum(),
                    queryDTO.getPageSize()
                );
            }
            wrapper.in(BizQuestion::getId, questionIds);
        }

        // 排序
        if (StringUtils.hasText(queryDTO.getOrderBy())) {
            boolean isAsc = "asc".equalsIgnoreCase(
                queryDTO.getOrderDirection()
            );
            switch (queryDTO.getOrderBy()) {
                case "create_time":
                    wrapper.orderBy(true, isAsc, BizQuestion::getCreateTime);
                    break;
                case "view_count":
                    wrapper.orderBy(true, isAsc, BizQuestion::getViewCount);
                    break;
                case "answer_count":
                    wrapper.orderBy(true, isAsc, BizQuestion::getAnswerCount);
                    break;
                case "like_count":
                    wrapper.orderBy(true, isAsc, BizQuestion::getLikeCount);
                    break;
                default:
                    wrapper.orderByDesc(BizQuestion::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(BizQuestion::getCreateTime);
        }

        // 3. 执行查询
        IPage<BizQuestion> questionPage = questionMapper.selectPage(
            page,
            wrapper
        );

        // 4. 转换为VO
        IPage<QuestionVO> voPage = new Page<>(
            questionPage.getCurrent(),
            questionPage.getSize(),
            questionPage.getTotal()
        );
        voPage.setRecords(convertToVOList(questionPage.getRecords(), userId));

        return voPage;
    }

    @Override
    public List<QuestionVO> getQuestionsByUserId(Long userId) {
        log.debug("查询用户的问题列表: userId={}", userId);

        List<BizQuestion> questions = questionMapper.selectByUserId(userId);
        return convertToVOList(questions, userId);
    }

    @Override
    public List<QuestionVO> getQuestionsByTagId(Long tagId, Long userId) {
        log.debug("查询标签的问题列表: tagId={}", tagId);

        List<BizQuestion> questions = questionMapper.selectByTagId(tagId);
        return convertToVOList(questions, userId);
    }

    @Override
    public IPage<QuestionVO> getQuestionsByTag(Long tagId, Integer page, Integer size, String orderBy, Long userId) {
        log.debug("根据标签分页查询问题: tagId={}, page={}, size={}, orderBy={}", tagId, page, size, orderBy);

        Page<BizQuestion> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BizQuestion> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关联标签表查询（使用安全的参数化查询，避免SQL注入风险）
        queryWrapper.eq(BizQuestion::getStatus, 1); // 只查询已发布的问题

        // 根据标签ID查询（使用已有的安全方法）
        List<Long> questionIds = questionTagMapper.selectQuestionIdsByTagId(tagId);
        if (questionIds.isEmpty()) {
            return new Page<>(page, size);
        }
        queryWrapper.in(BizQuestion::getId, questionIds);
        
        // 根据排序方式排序
        switch (orderBy) {
            case "latest":
                queryWrapper.orderByDesc(BizQuestion::getCreateTime);
                break;
            case "unsolved":
                queryWrapper.orderByDesc(BizQuestion::getCreateTime)
                    .eq(BizQuestion::getIsSolved, 0);
                break;
            case "reward":
                queryWrapper.orderByDesc(BizQuestion::getCreateTime)
                    .gt(BizQuestion::getRewardPoints, 0);
                break;
            case "hot":
            default:
                queryWrapper.orderByDesc(BizQuestion::getViewCount);
                break;
        }
        
        IPage<BizQuestion> questionPage = questionMapper.selectPage(pageParam, queryWrapper);
        
        // 转换为VO并添加标签信息
        List<QuestionVO> voList = convertToVOList(questionPage.getRecords(), userId);
        
        // 查询每个问题的标签
        for (QuestionVO vo : voList) {
            List<BizQuestionTag> questionTags = questionTagMapper.selectList(
                new LambdaQueryWrapper<BizQuestionTag>()
                    .eq(BizQuestionTag::getQuestionId, vo.getId())
            );
            if (!questionTags.isEmpty()) {
                List<Long> tagIds = questionTags.stream()
                    .map(BizQuestionTag::getTagId)
                    .collect(Collectors.toList());
                List<BizTag> tags = tagMapper.selectBatchIds(tagIds);
                vo.setTags(tags.stream()
                    .map(tag -> {
                        QuestionVO.TagVO tagVO = new QuestionVO.TagVO();
                        tagVO.setId(tag.getId());
                        tagVO.setName(tag.getName());
                        return tagVO;
                    })
                    .collect(Collectors.toList()));
            }
        }
        
        // 构建返回的分页对象
        IPage<QuestionVO> result = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        result.setRecords(voList);
        
        return result;
    }

    @Override
    public IPage<QuestionVO> searchQuestions(
        String keyword,
        Integer pageNum,
        Integer pageSize,
        Long userId
    ) {
        log.debug("搜索问题: keyword={}", keyword);

        Page<BizQuestion> page = new Page<>(pageNum, pageSize);
        IPage<BizQuestion> questionPage = questionMapper.searchQuestions(
            page,
            keyword
        );

        IPage<QuestionVO> voPage = new Page<>(
            questionPage.getCurrent(),
            questionPage.getSize(),
            questionPage.getTotal()
        );
        voPage.setRecords(convertToVOList(questionPage.getRecords(), userId));

        return voPage;
    }

    @Override
    @Cacheable(value = "questions", key = "'hot:' + #limit", unless = "#result == null || #result.isEmpty()")
    public List<QuestionVO> getHotQuestions(Integer limit, Long userId) {
        log.debug("获取热门问题: limit={}", limit);

        List<BizQuestion> questions = questionMapper.selectHotQuestions(limit);
        return convertToVOList(questions, userId);
    }

    @Override
    @Cacheable(value = "questions", key = "'latest:' + #limit", unless = "#result == null || #result.isEmpty()")
    public List<QuestionVO> getLatestQuestions(Integer limit, Long userId) {
        log.debug("获取最新问题: limit={}", limit);

        List<BizQuestion> questions = questionMapper.selectLatestQuestions(
            limit
        );
        return convertToVOList(questions, userId);
    }

    @Override
    public List<QuestionVO> getUnsolvedQuestions(Integer limit, Long userId) {
        log.debug("获取待解决问题: limit={}", limit);

        List<BizQuestion> questions = questionMapper.selectUnsolvedQuestions(
            limit
        );
        return convertToVOList(questions, userId);
    }

    @Override
    public List<QuestionVO> getRewardQuestions(
        Integer minRewardPoints,
        Integer limit,
        Long userId
    ) {
        log.debug(
            "获取悬赏问题: minRewardPoints={}, limit={}",
            minRewardPoints,
            limit
        );

        List<BizQuestion> questions = questionMapper.selectRewardQuestions(
            minRewardPoints,
            limit
        );
        return convertToVOList(questions, userId);
    }

    @Override
    public List<QuestionVO> getRecommendedQuestions(
        Long userId,
        Integer limit
    ) {
        log.debug("获取推荐问题: userId={}, limit={}", userId, limit);

        // TODO: 实现基于用户关注标签的推荐逻辑
        // 暂时返回最新问题作为推荐
        return getLatestQuestions(limit, userId);
    }

    @Override
    public List<QuestionVO> getRelatedQuestions(
        Long questionId,
        Integer limit,
        Long userId
    ) {
        log.debug("获取相关问题: questionId={}, limit={}", questionId, limit);

        // 1. 获取相关问题ID列表
        List<Long> relatedQuestionIds =
            questionTagMapper.selectRelatedQuestionIds(questionId, limit);

        if (CollectionUtils.isEmpty(relatedQuestionIds)) {
            return Collections.emptyList();
        }

        // 2. 批量查询问题
        List<BizQuestion> questions = questionMapper.selectBatchIds(
            relatedQuestionIds
        );

        // 3. 转换为VO
        return convertToVOList(questions, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean closeQuestion(Long userId, Long questionId) {
        log.info("关闭问题: userId={}, questionId={}", userId, questionId);

        // 1. 查询问题
        BizQuestion question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(
                ResultCode.PARAM_ERROR.getCode(),
                "问题不存在"
            );
        }

        // 2. 校验权限
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(
                ResultCode.FORBIDDEN.getCode(),
                "无权限关闭此问题"
            );
        }

        // 3. 更新状态
        question.setStatus(BizQuestion.STATUS_CLOSED);
        int rows = questionMapper.updateById(question);
        if (rows <= 0) {
            throw new BusinessException(
                ResultCode.DATABASE_ERROR.getCode(),
                "问题关闭失败"
            );
        }

        log.info("问题关闭成功: questionId={}", questionId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markQuestionAsResolved(Long userId, Long questionId) {
        log.info(
            "标记问题已解决: userId={}, questionId={}",
            userId,
            questionId
        );

        // 1. 查询问题
        BizQuestion question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(
                ResultCode.PARAM_ERROR.getCode(),
                "问题不存在"
            );
        }

        // 2. 校验权限
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(
                ResultCode.FORBIDDEN.getCode(),
                "无权限操作此问题"
            );
        }

        // 3. 更新解决状态
        int rows = questionMapper.updateResolvedStatus(
            questionId,
            BizQuestion.RESOLVED_YES
        );
        if (rows <= 0) {
            throw new BusinessException(
                ResultCode.DATABASE_ERROR.getCode(),
                "更新失败"
            );
        }

        log.info("问题已标记为已解决: questionId={}", questionId);
        return true;
    }

    @Override
    public Boolean incrementViewCount(Long questionId) {
        int rows = questionMapper.incrementViewCount(questionId);
        return rows > 0;
    }

    @Override
    public Boolean updateAnswerCount(Long questionId, Integer delta) {
        int rows = questionMapper.updateAnswerCount(questionId, delta);
        return rows > 0;
    }

    @Override
    public Boolean updateCollectCount(Long questionId, Integer delta) {
        int rows = questionMapper.updateCollectCount(questionId, delta);
        return rows > 0;
    }

    @Override
    public Boolean updateLikeCount(Long questionId, Integer delta) {
        int rows = questionMapper.updateLikeCount(questionId, delta);
        return rows > 0;
    }

    @Override
    public Integer countQuestionsByUserId(Long userId) {
        return questionMapper.countByUserId(userId);
    }

    @Override
    public Long countQuestions(Long userId) {
        LambdaQueryWrapper<BizQuestion> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(BizQuestion::getUserId, userId);
        }
        return questionMapper.selectCount(queryWrapper);
    }

    @Override
    public Integer countQuestionsByStatus(Integer status) {
        return questionMapper.countByStatus(status);
    }

    @Override
    public QuestionVO convertToVO(BizQuestion question, Long userId) {
        if (question == null) {
            return null;
        }

        QuestionVO vo = new QuestionVO();
        BeanUtils.copyProperties(question, vo);

        // 处理图片列表
        if (StringUtils.hasText(question.getImages())) {
            vo.setImages(Arrays.asList(question.getImages().split(",")));
        } else {
            vo.setImages(Collections.emptyList());
        }

        // 查询用户信息
        SysUser user = userMapper.selectById(question.getUserId());
        if (user != null) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            vo.setUser(userVO);
        }

        // 查询标签列表
        List<Long> tagIds = questionTagMapper.selectTagIdsByQuestionId(
            question.getId()
        );
        if (!CollectionUtils.isEmpty(tagIds)) {
            List<BizTag> tags = tagMapper.selectByIds(tagIds);
            List<QuestionVO.TagVO> tagVOs = tags
                .stream()
                .map(tag -> {
                    QuestionVO.TagVO tagVO = new QuestionVO.TagVO();
                    tagVO.setId(tag.getId());
                    tagVO.setName(tag.getName());
                    tagVO.setDescription(tag.getDescription());
                    tagVO.setIcon(tag.getIcon());
                    tagVO.setColor(tag.getColor());
                    tagVO.setUseCount(tag.getUseCount());
                    return tagVO;
                })
                .collect(Collectors.toList());
            vo.setTags(tagVOs);
        } else {
            vo.setTags(Collections.emptyList());
        }

        // 设置状态文本
        vo.setStatusText(getStatusText(question.getStatus()));

        // 设置是否已解决
        vo.setIsResolved(question.getIsSolved() != null ? question.getIsSolved() : BizQuestion.RESOLVED_NO);
        vo.setResolvedText(
            question.getIsSolved() != null &&
                    question.getIsSolved() == BizQuestion.RESOLVED_YES
                ? "已解决"
                : "未解决"
        );

        // TODO: 查询当前用户是否点赞/收藏
        vo.setHasLiked(false);
        vo.setHasCollected(false);

        return vo;
    }

    @Override
    public List<QuestionVO> convertToVOList(
        List<BizQuestion> questions,
        Long userId
    ) {
        if (CollectionUtils.isEmpty(questions)) {
            return Collections.emptyList();
        }

        // 批量查询用户信息
        Set<Long> userIds = questions.stream()
                .map(BizQuestion::getUserId)
                .collect(Collectors.toSet());
        List<SysUser> users = userMapper.selectBatchIds(userIds);
        Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        // 批量查询问题-标签关联
        Set<Long> questionIds = questions.stream()
                .map(BizQuestion::getId)
                .collect(Collectors.toSet());
        Map<Long, List<Long>> questionTagMap = new HashMap<>();
        for (Long qId : questionIds) {
            List<Long> tagIds = questionTagMapper.selectTagIdsByQuestionId(qId);
            if (!CollectionUtils.isEmpty(tagIds)) {
                questionTagMap.put(qId, tagIds);
            }
        }

        // 批量查询标签信息
        Set<Long> allTagIds = questionTagMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        List<BizTag> tags = tagMapper.selectByIds(new ArrayList<>(allTagIds));
        Map<Long, BizTag> tagMap = tags.stream()
                .collect(Collectors.toMap(BizTag::getId, t -> t));

        // 转换为VO
        return questions.stream()
                .map(question -> {
                    QuestionVO vo = new QuestionVO();
                    BeanUtils.copyProperties(question, vo);

                    // 处理图片列表
                    if (StringUtils.hasText(question.getImages())) {
                        vo.setImages(Arrays.asList(question.getImages().split(",")));
                    } else {
                        vo.setImages(Collections.emptyList());
                    }

                    // 从Map中获取用户信息（避免重复查询）
                    SysUser user = userMap.get(question.getUserId());
                    if (user != null) {
                        UserVO userVO = new UserVO();
                        BeanUtils.copyProperties(user, userVO);
                        vo.setUser(userVO);
                    }

                    // 从Map中获取标签列表（避免重复查询）
                    List<Long> tagIds = questionTagMap.get(question.getId());
                    if (!CollectionUtils.isEmpty(tagIds)) {
                        List<QuestionVO.TagVO> tagVOs = tagIds.stream()
                                .map(tagId -> {
                                    BizTag tag = tagMap.get(tagId);
                                    if (tag == null) {
                                        return null;
                                    }
                                    QuestionVO.TagVO tagVO = new QuestionVO.TagVO();
                                    tagVO.setId(tag.getId());
                                    tagVO.setName(tag.getName());
                                    tagVO.setDescription(tag.getDescription());
                                    tagVO.setIcon(tag.getIcon());
                                    tagVO.setColor(tag.getColor());
                                    tagVO.setUseCount(tag.getUseCount());
                                    return tagVO;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        vo.setTags(tagVOs);
                    } else {
                        vo.setTags(Collections.emptyList());
                    }

                    // 设置状态文本
                    vo.setStatusText(getStatusText(question.getStatus()));

                    // 设置是否已解决
                    vo.setIsResolved(question.getIsSolved() != null ? question.getIsSolved() : BizQuestion.RESOLVED_NO);
                    vo.setResolvedText(
                    question.getIsSolved() != null &&
                    question.getIsSolved() == BizQuestion.RESOLVED_YES
                    ? "已解决"
                    : "未解决"
                    );

                    // TODO: 查询当前用户是否点赞/收藏
                    vo.setHasLiked(false);
                    vo.setHasCollected(false);

                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "草稿";
            case 1:
                return "已发布";
            case 2:
                return "已关闭";
            default:
                return "未知";
        }
    }
}
