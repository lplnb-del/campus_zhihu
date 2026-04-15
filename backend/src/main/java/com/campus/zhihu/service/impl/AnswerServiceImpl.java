package com.campus.zhihu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.common.ResultCode;
import com.campus.zhihu.dto.AnswerPublishDTO;
import com.campus.zhihu.dto.AnswerQueryDTO;
import com.campus.zhihu.dto.AnswerUpdateDTO;
import com.campus.zhihu.entity.BizAnswer;
import com.campus.zhihu.entity.BizQuestion;
import com.campus.zhihu.entity.SysUser;
import com.campus.zhihu.mapper.AnswerMapper;
import com.campus.zhihu.mapper.QuestionMapper;
import com.campus.zhihu.mapper.UserMapper;
import com.campus.zhihu.service.AnswerService;
import com.campus.zhihu.vo.AnswerVO;
import com.campus.zhihu.vo.UserVO;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 回答服务实现类
 *
 * @author CampusZhihu Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerMapper answerMapper;
    private final QuestionMapper questionMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnswerVO publishAnswer(Long userId, AnswerPublishDTO publishDTO) {
        log.info("用户发布回答: userId={}, questionId={}", userId, publishDTO.getQuestionId());

        // 1. 校验用户是否存在
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 2. 校验问题是否存在
        BizQuestion question = questionMapper.selectById(publishDTO.getQuestionId());
        if (question == null) {
            throw new BusinessException(ResultCode.QUESTION_NOT_EXIST);
        }

        // 3. 检查问题是否已关闭
        if (question.getStatus() == BizQuestion.STATUS_CLOSED) {
            throw new BusinessException(ResultCode.QUESTION_CLOSED);
        }

        // 4. 创建回答实体
        BizAnswer answer = new BizAnswer();
        answer.setQuestionId(publishDTO.getQuestionId());
        answer.setUserId(userId);
        answer.setContent(publishDTO.getContent());

        // 处理图片列表
        if (!CollectionUtils.isEmpty(publishDTO.getImages())) {
            answer.setImages(String.join(",", publishDTO.getImages()));
        }

        answer.setStatus(Boolean.TRUE.equals(publishDTO.getPublish()) ? BizAnswer.STATUS_PUBLISHED : BizAnswer.STATUS_DRAFT);
        answer.setIsAccepted(BizAnswer.NOT_ACCEPTED);
        answer.setLikeCount(0);
        answer.setCommentCount(0);

        // 5. 插入回答
        int rows = answerMapper.insert(answer);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.DATABASE_ERROR.getCode(), "回答发布失败");
        }

        // 6. 更新问题的回答数量（只有发布状态才更新）
        if (answer.getStatus() == BizAnswer.STATUS_PUBLISHED) {
            questionMapper.updateAnswerCount(publishDTO.getQuestionId(), 1);
        }

        log.info("回答发布成功: answerId={}, userId={}", answer.getId(), userId);

        // 7. 返回回答详情
        return getAnswerById(answer.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnswerVO updateAnswer(Long userId, Long answerId, AnswerUpdateDTO updateDTO) {
        log.info("用户更新回答: userId={}, answerId={}", userId, answerId);

        // 1. 查询回答
        BizAnswer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw new BusinessException(ResultCode.ANSWER_NOT_EXIST);
        }

        // 2. 校验权限（只有回答作者才能更新）
        if (!answer.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限修改此回答");
        }

        // 3. 不允许修改已被采纳的回答
        if (answer.getIsAccepted() == BizAnswer.ACCEPTED) {
            throw new BusinessException(ResultCode.ANSWER_ALREADY_ACCEPTED);
        }

        // 4. 记录旧状态
        Integer oldStatus = answer.getStatus();

        // 5. 更新字段
        if (StringUtils.hasText(updateDTO.getContent())) {
            answer.setContent(updateDTO.getContent());
        }
        if (updateDTO.getImages() != null) {
            answer.setImages(CollectionUtils.isEmpty(updateDTO.getImages()) ? null : String.join(",", updateDTO.getImages()));
        }
        if (updateDTO.getStatus() != null) {
            answer.setStatus(updateDTO.getStatus());
        }

        // 6. 更新回答
        int rows = answerMapper.updateById(answer);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.DATABASE_ERROR.getCode(), "回答更新失败");
        }

        // 7. 如果状态从草稿变为发布，更新问题的回答数
        if (oldStatus == BizAnswer.STATUS_DRAFT && answer.getStatus() == BizAnswer.STATUS_PUBLISHED) {
            questionMapper.updateAnswerCount(answer.getQuestionId(), 1);
        } else if (oldStatus == BizAnswer.STATUS_PUBLISHED && answer.getStatus() == BizAnswer.STATUS_DRAFT) {
            questionMapper.updateAnswerCount(answer.getQuestionId(), -1);
        }

        log.info("回答更新成功: answerId={}, userId={}", answerId, userId);

        // 8. 返回更新后的回答详情
        return getAnswerById(answerId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAnswer(Long userId, Long answerId) {
        log.info("用户删除回答: userId={}, answerId={}", userId, answerId);

        // 1. 查询回答
        BizAnswer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw new BusinessException(ResultCode.ANSWER_NOT_EXIST);
        }

        // 2. 校验权限（只有回答作者才能删除）
        if (!answer.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限删除此回答");
        }

        // 3. 不允许删除已被采纳的回答
        if (answer.getIsAccepted() == BizAnswer.ACCEPTED) {
            throw new BusinessException(ResultCode.ANSWER_ALREADY_ACCEPTED);
        }

        // 4. 逻辑删除回答
        int rows = answerMapper.deleteById(answerId);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.DATABASE_ERROR.getCode(), "回答删除失败");
        }

        // 5. 如果是已发布状态，更新问题的回答数量
        if (answer.getStatus() == BizAnswer.STATUS_PUBLISHED) {
            questionMapper.updateAnswerCount(answer.getQuestionId(), -1);
        }

        log.info("回答删除成功: answerId={}, userId={}", answerId, userId);
        return true;
    }

    @Override
    public AnswerVO getAnswerById(Long answerId, Long userId) {
        log.debug("获取回答详情: answerId={}, userId={}", answerId, userId);

        // 1. 查询回答
        BizAnswer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "回答不存在");
        }

        // 2. 转换为VO
        return convertToVO(answer, userId);
    }

    @Override
    public IPage<AnswerVO> getAnswerPage(AnswerQueryDTO queryDTO, Long userId) {
        log.debug("分页查询回答: queryDTO={}", queryDTO);

        // 1. 构建分页对象
        Page<BizAnswer> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<BizAnswer> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(BizAnswer::getContent, queryDTO.getKeyword());
        }

        // 问题ID
        if (queryDTO.getQuestionId() != null) {
            wrapper.eq(BizAnswer::getQuestionId, queryDTO.getQuestionId());
        }

        // 用户ID
        if (queryDTO.getUserId() != null) {
            wrapper.eq(BizAnswer::getUserId, queryDTO.getUserId());
        }

        // 状态
        if (queryDTO.getStatus() != null) {
            wrapper.eq(BizAnswer::getStatus, queryDTO.getStatus());
        }

        // 是否被采纳
        if (queryDTO.getIsAccepted() != null) {
            wrapper.eq(BizAnswer::getIsAccepted, queryDTO.getIsAccepted());
        }

        // 点赞数筛选
        if (queryDTO.getMinLikeCount() != null) {
            wrapper.ge(BizAnswer::getLikeCount, queryDTO.getMinLikeCount());
        }
        if (queryDTO.getMaxLikeCount() != null) {
            wrapper.le(BizAnswer::getLikeCount, queryDTO.getMaxLikeCount());
        }

        // 排序
        if (StringUtils.hasText(queryDTO.getOrderBy())) {
            boolean isAsc = "asc".equalsIgnoreCase(queryDTO.getOrderDirection());
            switch (queryDTO.getOrderBy()) {
                case "create_time":
                    wrapper.orderBy(true, isAsc, BizAnswer::getCreateTime);
                    break;
                case "like_count":
                    wrapper.orderBy(true, isAsc, BizAnswer::getLikeCount);
                    break;
                case "comment_count":
                    wrapper.orderBy(true, isAsc, BizAnswer::getCommentCount);
                    break;
                default:
                    wrapper.orderByDesc(BizAnswer::getCreateTime);
            }
        } else {
            // 默认排序：采纳状态 > 点赞数 > 创建时间
            wrapper.orderByDesc(BizAnswer::getIsAccepted).orderByDesc(BizAnswer::getLikeCount).orderByDesc(BizAnswer::getCreateTime);
        }

        // 3. 执行查询
        IPage<BizAnswer> answerPage = answerMapper.selectPage(page, wrapper);

        // 4. 转换为VO
        IPage<AnswerVO> voPage = new Page<>(answerPage.getCurrent(), answerPage.getSize(), answerPage.getTotal());
        voPage.setRecords(convertToVOList(answerPage.getRecords(), userId));

        return voPage;
    }

    @Override
    public List<AnswerVO> getAnswersByQuestionId(Long questionId, Long userId) {
        log.debug("查询问题的回答列表: questionId={}", questionId);

        List<BizAnswer> answers = answerMapper.selectByQuestionId(questionId);
        return convertToVOList(answers, userId);
    }

    @Override
    public IPage<AnswerVO> getAnswersByQuestionIdPage(Long questionId, Integer page, Integer size, Long userId) {
        log.debug("分页查询问题的回答列表: questionId={}, page={}, size={}", questionId, page, size);

        Page<BizAnswer> answerPage = new Page<>(page, size);
        IPage<BizAnswer> resultPage = answerMapper.selectPageByQuestionId(answerPage, questionId);

        IPage<AnswerVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(convertToVOList(resultPage.getRecords(), userId));

        return voPage;
    }

    @Override
    public List<AnswerVO> getAnswersByUserId(Long userId, Long currentUserId) {
        log.debug("查询用户的回答列表: userId={}", userId);

        List<BizAnswer> answers = answerMapper.selectByUserId(userId);
        return convertToVOList(answers, currentUserId);
    }

    @Override
    public IPage<AnswerVO> searchAnswers(String keyword, Integer pageNum, Integer pageSize, Long userId) {
        log.debug("搜索回答: keyword={}", keyword);

        Page<BizAnswer> page = new Page<>(pageNum, pageSize);
        IPage<BizAnswer> answerPage = answerMapper.searchAnswers(page, keyword);

        IPage<AnswerVO> voPage = new Page<>(answerPage.getCurrent(), answerPage.getSize(), answerPage.getTotal());
        voPage.setRecords(convertToVOList(answerPage.getRecords(), userId));

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean acceptAnswer(Long userId, Long answerId, Long questionId) {
        log.info("采纳回答: userId={}, answerId={}, questionId={}", userId, answerId, questionId);

        // 1. 查询问题
        BizQuestion question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "问题不存在");
        }

        // 2. 校验权限（只有问题作者才能采纳）
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只有问题作者才能采纳回答");
        }

        // 3. 查询回答
        BizAnswer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "回答不存在");
        }

        // 4. 校验回答是否属于该问题
        if (!answer.getQuestionId().equals(questionId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "回答不属于该问题");
        }

        // 5. 取消该问题的其他所有采纳状态
        answerMapper.cancelAllAcceptedByQuestionId(questionId);

        // 6. 采纳当前回答
        int rows = answerMapper.updateAcceptedStatus(answerId, BizAnswer.ACCEPTED);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.DATABASE_ERROR.getCode(), "采纳失败");
        }

        // 7. 标记问题为已解决，并设置采纳的回答ID
        questionMapper.updateResolvedStatus(questionId, BizQuestion.RESOLVED_YES);
        questionMapper.updateAcceptedAnswerId(questionId, answerId);

        // 8. 处理积分：提问者扣除悬赏分，回答者获得悬赏分+额外奖励
        Integer rewardPoints = question.getRewardPoints();
        Integer bonusPoints = 10; // 额外奖励10分
        
        // 提问者扣除悬赏分（使用乐观锁）
        userMapper.deductPoints(question.getUserId(), rewardPoints);
        
        // 回答者获得悬赏分+额外奖励（使用乐观锁）
        userMapper.addPoints(answer.getUserId(), rewardPoints + bonusPoints);

        log.info("采纳回答成功: answerId={}, questionId={}, rewardPoints={}, bonusPoints={}", 
                answerId, questionId, rewardPoints, bonusPoints);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelAcceptAnswer(Long userId, Long answerId, Long questionId) {
        log.info("取消采纳回答: userId={}, answerId={}, questionId={}", userId, answerId, questionId);

        // 1. 查询问题
        BizQuestion question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "问题不存在");
        }

        // 2. 校验权限（只有问题作者才能取消采纳）
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只有问题作者才能取消采纳");
        }

        // 3. 查询回答
        BizAnswer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "回答不存在");
        }

        // 4. 取消采纳
        int rows = answerMapper.updateAcceptedStatus(answerId, BizAnswer.NOT_ACCEPTED);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.DATABASE_ERROR.getCode(), "取消采纳失败");
        }

        // 5. 标记问题为未解决，并清除采纳的回答ID
        questionMapper.updateResolvedStatus(questionId, BizQuestion.RESOLVED_NO);
        questionMapper.updateAcceptedAnswerId(questionId, null);

        log.info("取消采纳回答成功: answerId={}, questionId={}", answerId, questionId);
        return true;
    }

    @Override
    public Boolean updateLikeCount(Long answerId, Integer delta) {
        int rows = answerMapper.updateLikeCount(answerId, delta);
        return rows > 0;
    }

    @Override
    public Boolean updateCommentCount(Long answerId, Integer delta) {
        int rows = answerMapper.updateCommentCount(answerId, delta);
        return rows > 0;
    }

    @Override
    public Integer countAnswersByUserId(Long userId) {
        return answerMapper.countByUserId(userId);
    }

    @Override
    public Integer countAnswersByQuestionId(Long questionId) {
        return answerMapper.countByQuestionId(questionId);
    }

    @Override
    public List<AnswerVO> getLatestAnswersByUserId(Long userId, Integer limit, Long currentUserId) {
        log.debug("获取用户最新回答: userId={}, limit={}", userId, limit);

        List<BizAnswer> answers = answerMapper.selectLatestAnswersByUserId(userId, limit);
        return convertToVOList(answers, currentUserId);
    }

    @Override
    public List<AnswerVO> getHotAnswersByUserId(Long userId, Integer limit, Long currentUserId) {
        log.debug("获取用户热门回答: userId={}, limit={}", userId, limit);

        List<BizAnswer> answers = answerMapper.selectHotAnswersByUserId(userId, limit);
        return convertToVOList(answers, currentUserId);
    }

    @Override
    public AnswerVO getAcceptedAnswer(Long questionId, Long userId) {
        log.debug("获取问题的被采纳回答: questionId={}", questionId);

        BizAnswer answer = answerMapper.selectAcceptedAnswer(questionId);
        if (answer == null) {
            return null;
        }
        return convertToVO(answer, userId);
    }

    @Override
    public List<AnswerVO> getTopAnswers(Integer limit, Long userId) {
        log.debug("获取高赞回答列表: limit={}", limit);

        List<BizAnswer> answers = answerMapper.selectTopAnswers(limit);
        return convertToVOList(answers, userId);
    }

    @Override
    public Boolean checkUserAnswered(Long questionId, Long userId) {
        int count = answerMapper.checkUserAnswered(questionId, userId);
        return count > 0;
    }

    @Override
    public AnswerVO convertToVO(BizAnswer answer, Long userId) {
        if (answer == null) {
            return null;
        }

        AnswerVO vo = new AnswerVO();
        BeanUtils.copyProperties(answer, vo);

        // 处理图片列表
        if (StringUtils.hasText(answer.getImages())) {
            vo.setImages(Arrays.asList(answer.getImages().split(",")));
        } else {
            vo.setImages(Collections.emptyList());
        }

        // 查询用户信息
        SysUser user = userMapper.selectById(answer.getUserId());
        if (user != null) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            vo.setUser(userVO);
        }

        // 查询问题标题
        BizQuestion question = questionMapper.selectById(answer.getQuestionId());
        if (question != null) {
            vo.setQuestionTitle(question.getTitle());
        }

        // 设置状态文本
        vo.setStatusText(getStatusText(answer.getStatus()));
        vo.setAcceptedText(answer.getIsAccepted() != null && answer.getIsAccepted() == BizAnswer.ACCEPTED ? "已采纳" : "未采纳");

        // TODO: 查询当前用户是否点赞
        vo.setHasLiked(false);

        return vo;
    }

    @Override
    public List<AnswerVO> convertToVOList(List<BizAnswer> answers, Long userId) {
        if (CollectionUtils.isEmpty(answers)) {
            return Collections.emptyList();
        }

        // 批量查询用户信息
        Set<Long> userIds = answers.stream()
                .map(BizAnswer::getUserId)
                .collect(Collectors.toSet());
        List<SysUser> users = userMapper.selectBatchIds(userIds);
        Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        // 批量查询问题信息
        Set<Long> questionIds = answers.stream()
                .map(BizAnswer::getQuestionId)
                .collect(Collectors.toSet());
        List<BizQuestion> questions = questionMapper.selectBatchIds(questionIds);
        Map<Long, BizQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(BizQuestion::getId, q -> q));

        // 转换为VO
        return answers.stream()
                .map(answer -> {
                    AnswerVO vo = new AnswerVO();
                    BeanUtils.copyProperties(answer, vo);

                    // 处理图片列表
                    if (StringUtils.hasText(answer.getImages())) {
                        vo.setImages(Arrays.asList(answer.getImages().split(",")));
                    } else {
                        vo.setImages(Collections.emptyList());
                    }

                    // 从Map中获取用户信息（避免重复查询）
                    SysUser user = userMap.get(answer.getUserId());
                    if (user != null) {
                        UserVO userVO = new UserVO();
                        BeanUtils.copyProperties(user, userVO);
                        vo.setUser(userVO);
                    }

                    // 从Map中获取问题标题（避免重复查询）
                    BizQuestion question = questionMap.get(answer.getQuestionId());
                    if (question != null) {
                        vo.setQuestionTitle(question.getTitle());
                    }

                    // 设置状态文本
                    vo.setStatusText(getStatusText(answer.getStatus()));
                    vo.setAcceptedText(answer.getIsAccepted() != null && answer.getIsAccepted() == BizAnswer.ACCEPTED ? "已采纳" : "未采纳");

                    // TODO: 查询当前用户是否点赞
                    vo.setHasLiked(false);

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
            default:
                return "未知";
        }
    }
}
