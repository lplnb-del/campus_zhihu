package com.campus.zhihu.service;

import com.campus.zhihu.entity.BizLikeRecord;

import java.util.List;
import java.util.Map;

/**
 * 点赞服务接口
 * 提供点赞相关的业务逻辑处理
 *
 * @author CampusZhihu Team
 */
public interface LikeService {

    /**
     * 点赞
     *
     * @param userId     用户ID
     * @param targetType 目标类型（1-问题，2-回答，3-评论）
     * @param targetId   目标ID
     * @return 是否成功
     */
    Boolean like(Long userId, Integer targetType, Long targetId);

    /**
     * 取消点赞
     *
     * @param userId     用户ID
     * @param targetType 目标类型（1-问题，2-回答，3-评论）
     * @param targetId   目标ID
     * @return 是否成功
     */
    Boolean unlike(Long userId, Integer targetType, Long targetId);

    /**
     * 切换点赞状态（如果已点赞则取消，如果未点赞则点赞）
     *
     * @param userId     用户ID
     * @param targetType 目标类型（1-问题，2-回答，3-评论）
     * @param targetId   目标ID
     * @return 点赞后的状态（true-已点赞，false-未点赞）
     */
    Boolean toggleLike(Long userId, Integer targetType, Long targetId);

    /**
     * 检查用户是否已点赞
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 是否已点赞
     */
    Boolean checkUserLiked(Long userId, Integer targetType, Long targetId);

    /**
     * 批量检查用户是否点赞了指定目标列表
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param targetIds  目标ID列表
     * @return 目标ID -> 是否已点赞的映射
     */
    Map<Long, Boolean> batchCheckUserLiked(Long userId, Integer targetType, List<Long> targetIds);

    /**
     * 获取用户的点赞记录列表
     *
     * @param userId     用户ID
     * @param targetType 目标类型（可为空）
     * @return 点赞记录列表
     */
    List<BizLikeRecord> getUserLikeRecords(Long userId, Integer targetType);

    /**
     * 获取目标的点赞记录列表
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 点赞记录列表
     */
    List<BizLikeRecord> getTargetLikeRecords(Integer targetType, Long targetId);

    /**
     * 统计用户的点赞数量
     *
     * @param userId     用户ID
     * @param targetType 目标类型（可为空）
     * @return 点赞数量
     */
    Integer countUserLikes(Long userId, Integer targetType);

    /**
     * 统计目标的点赞数量
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 点赞数量
     */
    Integer countTargetLikes(Integer targetType, Long targetId);

    /**
     * 获取用户点赞的目标ID列表
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @return 目标ID列表
     */
    List<Long> getUserLikedTargetIds(Long userId, Integer targetType);
}
