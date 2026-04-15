package com.campus.zhihu.service;

import com.campus.zhihu.entity.BizCollection;
import com.campus.zhihu.vo.CollectionVO;

import java.util.List;
import java.util.Map;

/**
 * 收藏服务接口
 * 提供收藏相关的业务逻辑处理
 *
 * @author CampusZhihu Team
 */
public interface CollectionService {

    /**
     * 收藏
     *
     * @param userId     用户ID
     * @param targetType 目标类型（1-问题，2-回答）
     * @param targetId   目标ID
     * @return 是否成功
     */
    Boolean collect(Long userId, Integer targetType, Long targetId);

    /**
     * 取消收藏
     *
     * @param userId     用户ID
     * @param targetType 目标类型（1-问题，2-回答）
     * @param targetId   目标ID
     * @return 是否成功
     */
    Boolean uncollect(Long userId, Integer targetType, Long targetId);

    /**
     * 切换收藏状态（如果已收藏则取消，如果未收藏则收藏）
     *
     * @param userId     用户ID
     * @param targetType 目标类型（1-问题，2-回答）
     * @param targetId   目标ID
     * @return 收藏后的状态（true-已收藏，false-未收藏）
     */
    Boolean toggleCollect(Long userId, Integer targetType, Long targetId);

    /**
     * 检查用户是否已收藏
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 是否已收藏
     */
    Boolean checkUserCollected(Long userId, Integer targetType, Long targetId);

    /**
     * 批量检查用户是否收藏了指定目标列表
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param targetIds  目标ID列表
     * @return 目标ID -> 是否已收藏的映射
     */
    Map<Long, Boolean> batchCheckUserCollected(Long userId, Integer targetType, List<Long> targetIds);

    /**
     * 获取用户的收藏列表
     *
     * @param userId     用户ID
     * @param targetType 目标类型（可为空）
     * @return 收藏列表
     */
    List<CollectionVO> getUserCollections(Long userId, Integer targetType);

    /**
     * 获取目标的收藏列表
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 收藏列表
     */
    List<CollectionVO> getTargetCollections(Integer targetType, Long targetId);

    /**
     * 统计用户的收藏数量
     *
     * @param userId     用户ID
     * @param targetType 目标类型（可为空）
     * @return 收藏数量
     */
    Integer countUserCollections(Long userId, Integer targetType);

    /**
     * 统计目标的收藏数量
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 收藏数量
     */
    Integer countTargetCollections(Integer targetType, Long targetId);

    /**
     * 获取用户收藏的目标ID列表
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @return 目标ID列表
     */
    List<Long> getUserCollectedTargetIds(Long userId, Integer targetType);
}
