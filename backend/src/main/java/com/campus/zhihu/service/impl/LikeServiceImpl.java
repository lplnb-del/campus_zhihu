package com.campus.zhihu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.common.ResultCode;
import com.campus.zhihu.entity.BizLikeRecord;
import com.campus.zhihu.mapper.LikeRecordMapper;
import com.campus.zhihu.service.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞服务实现类
 * 实现点赞相关的业务逻辑
 *
 * @author CampusZhihu Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRecordMapper likeRecordMapper;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final CommentService commentService;

    /**
     * 目标类型常量
     */
    private static final int TARGET_TYPE_QUESTION = 1;
    private static final int TARGET_TYPE_ANSWER = 2;
    private static final int TARGET_TYPE_COMMENT = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean like(Long userId, Integer targetType, Long targetId) {
        log.info(
            "用户点赞: userId={}, targetType={}, targetId={}",
            userId,
            targetType,
            targetId
        );

        // 验证参数
        validateLikeParams(userId, targetType, targetId);

        // 验证目标是否存在
        validateTargetExists(targetType, targetId);

        // 检查是否已点赞
        if (checkUserLiked(userId, targetType, targetId)) {
            log.warn(
                "用户已点赞，无需重复点赞: userId={}, targetType={}, targetId={}",
                userId,
                targetType,
                targetId
            );
            return true;
        }

        // 创建点赞记录
        BizLikeRecord likeRecord = new BizLikeRecord();
        likeRecord.setUserId(userId);
        likeRecord.setTargetType(targetType);
        likeRecord.setTargetId(targetId);
        likeRecord.setIsDeleted(0);
        likeRecordMapper.insert(likeRecord);
        log.info(
            "创建点赞记录: userId={}, targetType={}, targetId={}",
            userId,
            targetType,
            targetId
        );

        // 更新目标的点赞数
        updateTargetLikeCount(targetType, targetId, 1);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unlike(Long userId, Integer targetType, Long targetId) {
        log.info(
            "用户取消点赞: userId={}, targetType={}, targetId={}",
            userId,
            targetType,
            targetId
        );

        // 验证参数
        validateLikeParams(userId, targetType, targetId);

        // 检查是否已点赞
        if (!checkUserLiked(userId, targetType, targetId)) {
            log.warn(
                "用户未点赞，无需取消: userId={}, targetType={}, targetId={}",
                userId,
                targetType,
                targetId
            );
            return true;
        }

        // 物理删除点赞记录（避免逻辑删除导致的唯一约束冲突）
        int rows = likeRecordMapper.physicalDelete(userId, targetType, targetId);
        if (rows > 0) {
            // 更新目标的点赞数
            updateTargetLikeCount(targetType, targetId, -1);
            log.info(
                "取消点赞成功: userId={}, targetType={}, targetId={}",
                userId,
                targetType,
                targetId
            );
            return true;
        }

        throw new BusinessException(ResultCode.ERROR.getCode(), "取消点赞失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleLike(Long userId, Integer targetType, Long targetId) {
        log.info(
            "切换点赞状态: userId={}, targetType={}, targetId={}",
            userId,
            targetType,
            targetId
        );

        // 验证参数
        validateLikeParams(userId, targetType, targetId);

        // 检查当前点赞状态
        boolean isLiked = checkUserLiked(userId, targetType, targetId);

        if (isLiked) {
            // 已点赞，执行取消点赞
            unlike(userId, targetType, targetId);
            return false;
        } else {
            // 未点赞，执行点赞
            like(userId, targetType, targetId);
            return true;
        }
    }

    @Override
    public Boolean checkUserLiked(
        Long userId,
        Integer targetType,
        Long targetId
    ) {
        if (userId == null) {
            return false;
        }

        Integer count = likeRecordMapper.checkUserLiked(
            userId,
            targetType,
            targetId
        );
        return count != null && count > 0;
    }

    @Override
    public Map<Long, Boolean> batchCheckUserLiked(
        Long userId,
        Integer targetType,
        List<Long> targetIds
    ) {
        Map<Long, Boolean> resultMap = new HashMap<>();

        if (userId == null || targetIds == null || targetIds.isEmpty()) {
            // 如果用户未登录或目标列表为空，返回全部未点赞
            if (targetIds != null) {
                targetIds.forEach(id -> resultMap.put(id, false));
            }
            return resultMap;
        }

        // 初始化所有目标为未点赞
        targetIds.forEach(id -> resultMap.put(id, false));

        // 查询已点赞的目标ID列表
        List<Long> likedIds = likeRecordMapper.selectLikedTargetIds(
            userId,
            targetType,
            targetIds
        );

        // 更新已点赞的目标
        if (likedIds != null && !likedIds.isEmpty()) {
            likedIds.forEach(id -> resultMap.put(id, true));
        }

        return resultMap;
    }

    @Override
    public List<BizLikeRecord> getUserLikeRecords(
        Long userId,
        Integer targetType
    ) {
        return likeRecordMapper.selectByUserId(userId, targetType);
    }

    @Override
    public List<BizLikeRecord> getTargetLikeRecords(
        Integer targetType,
        Long targetId
    ) {
        return likeRecordMapper.selectByTarget(targetType, targetId);
    }

    @Override
    public Integer countUserLikes(Long userId, Integer targetType) {
        return likeRecordMapper.countByUserId(userId, targetType);
    }

    @Override
    public Integer countTargetLikes(Integer targetType, Long targetId) {
        return likeRecordMapper.countByTarget(targetType, targetId);
    }

    @Override
    public List<Long> getUserLikedTargetIds(Long userId, Integer targetType) {
        List<BizLikeRecord> records = likeRecordMapper.selectByUserId(
            userId,
            targetType
        );
        return records
            .stream()
            .map(BizLikeRecord::getTargetId)
            .collect(Collectors.toList());
    }

    /**
     * 验证点赞参数
     */
    private void validateLikeParams(
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
        if (targetType < 1 || targetType > 3) {
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
            case TARGET_TYPE_COMMENT:
                exists = commentService.getCommentById(targetId, null) != null;
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
     * 更新目标的点赞数
     */
    private void updateTargetLikeCount(
        Integer targetType,
        Long targetId,
        Integer delta
    ) {
        try {
            switch (targetType) {
                case TARGET_TYPE_QUESTION:
                    questionService.updateLikeCount(targetId, delta);
                    break;
                case TARGET_TYPE_ANSWER:
                    answerService.updateLikeCount(targetId, delta);
                    break;
                case TARGET_TYPE_COMMENT:
                    commentService.updateLikeCount(targetId, delta);
                    break;
                default:
                    log.warn("未知的目标类型: {}", targetType);
            }
        } catch (Exception e) {
            log.error(
                "更新点赞数失败: targetType={}, targetId={}, delta={}",
                targetType,
                targetId,
                delta,
                e
            );
            // 不抛出异常，避免影响点赞记录的创建/删除
        }
    }
}
