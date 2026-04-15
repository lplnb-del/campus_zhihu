package com.campus.zhihu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.common.ResultCode;
import com.campus.zhihu.entity.BizCollection;
import com.campus.zhihu.mapper.CollectionMapper;
import com.campus.zhihu.service.*;
import com.campus.zhihu.vo.AnswerVO;
import com.campus.zhihu.vo.CollectionVO;
import com.campus.zhihu.vo.QuestionVO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 收藏服务实现类
 * 实现收藏相关的业务逻辑
 *
 * @author CampusZhihu Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionMapper collectionMapper;
    private final QuestionService questionService;
    private final AnswerService answerService;

    /**
     * 目标类型常量
     */
    private static final int TARGET_TYPE_QUESTION = 1;
    private static final int TARGET_TYPE_ANSWER = 2;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean collect(Long userId, Integer targetType, Long targetId) {
        log.info(
            "用户收藏: userId={}, targetType={}, targetId={}",
            userId,
            targetType,
            targetId
        );

        // 验证参数
        validateCollectionParams(userId, targetType, targetId);

        // 验证目标是否存在
        validateTargetExists(targetType, targetId);

        // 检查是否已收藏
        if (checkUserCollected(userId, targetType, targetId)) {
            log.warn(
                "用户已收藏，无需重复收藏: userId={}, targetType={}, targetId={}",
                userId,
                targetType,
                targetId
            );
            return true;
        }

        // 创建收藏记录
        BizCollection collection = new BizCollection();
        collection.setUserId(userId);
        collection.setTargetType(targetType);
        collection.setTargetId(targetId);
        collection.setIsDeleted(0);
        collectionMapper.insert(collection);
        log.info(
            "创建收藏记录: userId={}, targetType={}, targetId={}",
            userId,
            targetType,
            targetId
        );

        // 更新目标的收藏数
        updateTargetCollectionCount(targetType, targetId, 1);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean uncollect(Long userId, Integer targetType, Long targetId) {
        log.info(
            "用户取消收藏: userId={}, targetType={}, targetId={}",
            userId,
            targetType,
            targetId
        );

        // 验证参数
        validateCollectionParams(userId, targetType, targetId);

        // 检查是否已收藏
        if (!checkUserCollected(userId, targetType, targetId)) {
            log.warn(
                "用户未收藏，无需取消: userId={}, targetType={}, targetId={}",
                userId,
                targetType,
                targetId
            );
            return true;
        }

        // 物理删除收藏记录（避免逻辑删除导致的唯一约束冲突）
        int rows = collectionMapper.physicalDelete(userId, targetType, targetId);
        if (rows > 0) {
            // 更新目标的收藏数
            updateTargetCollectionCount(targetType, targetId, -1);
            log.info(
                "取消收藏成功: userId={}, targetType={}, targetId={}",
                userId,
                targetType,
                targetId
            );
            return true;
        }

        throw new BusinessException(ResultCode.ERROR.getCode(), "取消收藏失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleCollect(
        Long userId,
        Integer targetType,
        Long targetId
    ) {
        log.info(
            "切换收藏状态: userId={}, targetType={}, targetId={}",
            userId,
            targetType,
            targetId
        );

        // 验证参数
        validateCollectionParams(userId, targetType, targetId);

        // 检查当前收藏状态
        boolean isCollected = checkUserCollected(userId, targetType, targetId);

        if (isCollected) {
            // 已收藏，执行取消收藏
            uncollect(userId, targetType, targetId);
            return false;
        } else {
            // 未收藏，执行收藏
            collect(userId, targetType, targetId);
            return true;
        }
    }

    @Override
    public Boolean checkUserCollected(
        Long userId,
        Integer targetType,
        Long targetId
    ) {
        if (userId == null) {
            return false;
        }

        Integer count = collectionMapper.checkUserCollected(
            userId,
            targetType,
            targetId
        );
        return count != null && count > 0;
    }

    @Override
    public Map<Long, Boolean> batchCheckUserCollected(
        Long userId,
        Integer targetType,
        List<Long> targetIds
    ) {
        Map<Long, Boolean> resultMap = new HashMap<>();

        if (userId == null || targetIds == null || targetIds.isEmpty()) {
            // 如果用户未登录或目标列表为空，返回全部未收藏
            if (targetIds != null) {
                targetIds.forEach(id -> resultMap.put(id, false));
            }
            return resultMap;
        }

        // 初始化所有目标为未收藏
        targetIds.forEach(id -> resultMap.put(id, false));

        // 查询已收藏的目标ID列表
        List<Long> collectedIds = collectionMapper.selectCollectedTargetIds(
            userId,
            targetType,
            targetIds
        );

        // 更新已收藏的目标
        if (collectedIds != null && !collectedIds.isEmpty()) {
            collectedIds.forEach(id -> resultMap.put(id, true));
        }

        return resultMap;
    }

    @Override
    public List<CollectionVO> getUserCollections(
        Long userId,
        Integer targetType
    ) {
        List<BizCollection> collections = collectionMapper.selectByUserId(userId, targetType);
        return collections.stream().map(collection -> {
            CollectionVO vo = new CollectionVO();
            vo.setId(collection.getId());
            vo.setUserId(collection.getUserId());
            vo.setTargetType(collection.getTargetType());
            vo.setTargetId(collection.getTargetId());
            vo.setCreateTime(collection.getCreateTime());
            
            if (collection.getTargetType() == TARGET_TYPE_QUESTION) {
                QuestionVO question = questionService.getQuestionById(collection.getTargetId(), userId);
                vo.setQuestion(question);
            } else if (collection.getTargetType() == TARGET_TYPE_ANSWER) {
                AnswerVO answer = answerService.getAnswerById(collection.getTargetId(), userId);
                vo.setAnswer(answer);
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CollectionVO> getTargetCollections(
        Integer targetType,
        Long targetId
    ) {
        List<BizCollection> collections = collectionMapper.selectByTarget(targetType, targetId);
        return collections.stream().map(collection -> {
            CollectionVO vo = new CollectionVO();
            vo.setId(collection.getId());
            vo.setUserId(collection.getUserId());
            vo.setTargetType(collection.getTargetType());
            vo.setTargetId(collection.getTargetId());
            vo.setCreateTime(collection.getCreateTime());
            
            if (collection.getTargetType() == TARGET_TYPE_QUESTION) {
                QuestionVO question = questionService.getQuestionById(collection.getTargetId(), null);
                vo.setQuestion(question);
            } else if (collection.getTargetType() == TARGET_TYPE_ANSWER) {
                AnswerVO answer = answerService.getAnswerById(collection.getTargetId(), null);
                vo.setAnswer(answer);
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Integer countUserCollections(Long userId, Integer targetType) {
        return collectionMapper.countByUserId(userId, targetType);
    }

    @Override
    public Integer countTargetCollections(Integer targetType, Long targetId) {
        return collectionMapper.countByTarget(targetType, targetId);
    }

    @Override
    public List<Long> getUserCollectedTargetIds(
        Long userId,
        Integer targetType
    ) {
        List<BizCollection> collections = collectionMapper.selectByUserId(
            userId,
            targetType
        );
        return collections
            .stream()
            .map(BizCollection::getTargetId)
            .collect(Collectors.toList());
    }

    /**
     * 验证收藏参数
     */
    private void validateCollectionParams(
        Long userId,
        Integer targetType,
        Long targetId
    ) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (targetType == null || targetId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (targetType < 1 || targetType > 2) {
            throw new BusinessException(
                ResultCode.PARAM_ERROR.getCode(),
                "目标类型错误"
            );
        }
    }

    /**
     * 验证目标是否存在
     */
    private void validateTargetExists(Integer targetType, Long targetId) {
        boolean exists;
        switch (targetType) {
            case TARGET_TYPE_QUESTION:
                exists =
                    questionService.getQuestionById(targetId, null) != null;
                break;
            case TARGET_TYPE_ANSWER:
                exists = answerService.getAnswerById(targetId, null) != null;
                break;
            default:
                throw new BusinessException(
                    ResultCode.PARAM_ERROR.getCode(),
                    "不支持的目标类型"
                );
        }

        if (!exists) {
            throw new BusinessException(
                ResultCode.QUESTION_NOT_EXIST.getCode(),
                "目标不存在"
            );
        }
    }

    /**
     * 更新目标的收藏数
     */
    private void updateTargetCollectionCount(
        Integer targetType,
        Long targetId,
        Integer delta
    ) {
        try {
            switch (targetType) {
                case TARGET_TYPE_QUESTION:
                    questionService.updateCollectCount(targetId, delta);
                    break;
                case TARGET_TYPE_ANSWER:
                    // Answer 模块暂未实现 updateCollectionCount
                    // answerService.updateCollectionCount(targetId, delta);
                    break;
                default:
                    log.warn("未知的目标类型: {}", targetType);
            }
        } catch (Exception e) {
            log.error(
                "更新收藏数失败: targetType={}, targetId={}, delta={}",
                targetType,
                targetId,
                delta,
                e
            );
            // 不抛出异常，避免影响收藏记录的创建/删除
        }
    }
}
