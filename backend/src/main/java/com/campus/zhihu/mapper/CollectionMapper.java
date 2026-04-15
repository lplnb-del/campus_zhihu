package com.campus.zhihu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.zhihu.entity.BizCollection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏 Mapper 接口
 * 提供收藏的数据库访问操作
 *
 * @author CampusZhihu Team
 */
@Mapper
public interface CollectionMapper extends BaseMapper<BizCollection> {

    /**
     * 检查用户是否已收藏
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 收藏记录数量
     */
    Integer checkUserCollected(@Param("userId") Long userId,
                               @Param("targetType") Integer targetType,
                               @Param("targetId") Long targetId);

    /**
     * 获取用户的收藏列表
     *
     * @param userId     用户ID
     * @param targetType 目标类型（可为空）
     * @return 收藏列表
     */
    List<BizCollection> selectByUserId(@Param("userId") Long userId,
                                       @Param("targetType") Integer targetType);

    /**
     * 获取目标的收藏列表
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 收藏列表
     */
    List<BizCollection> selectByTarget(@Param("targetType") Integer targetType,
                                       @Param("targetId") Long targetId);

    /**
     * 统计用户的收藏数量
     *
     * @param userId     用户ID
     * @param targetType 目标类型（可为空）
     * @return 收藏数量
     */
    Integer countByUserId(@Param("userId") Long userId,
                          @Param("targetType") Integer targetType);

    /**
     * 统计目标的收藏数量
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 收藏数量
     */
    Integer countByTarget(@Param("targetType") Integer targetType,
                          @Param("targetId") Long targetId);

    /**
     * 批量检查用户是否收藏了指定目标列表
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param targetIds  目标ID列表
     * @return 已收藏的目标ID列表
     */
    List<Long> selectCollectedTargetIds(@Param("userId") Long userId,
                                        @Param("targetType") Integer targetType,
                                        @Param("targetIds") List<Long> targetIds);

    /**
     * 物理删除收藏记录
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 删除的行数
     */
    Integer physicalDelete(@Param("userId") Long userId,
                          @Param("targetType") Integer targetType,
                          @Param("targetId") Long targetId);
}
