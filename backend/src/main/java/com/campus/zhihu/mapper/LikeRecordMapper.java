package com.campus.zhihu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.zhihu.entity.BizLikeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 点赞记录 Mapper 接口
 * 提供点赞记录的数据库访问操作
 *
 * @author CampusZhihu Team
 */
@Mapper
public interface LikeRecordMapper extends BaseMapper<BizLikeRecord> {

    /**
     * 检查用户是否已点赞
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 点赞记录数量
     */
    Integer checkUserLiked(@Param("userId") Long userId,
                           @Param("targetType") Integer targetType,
                           @Param("targetId") Long targetId);

    /**
     * 获取用户的点赞记录列表
     *
     * @param userId     用户ID
     * @param targetType 目标类型（可为空）
     * @return 点赞记录列表
     */
    List<BizLikeRecord> selectByUserId(@Param("userId") Long userId,
                                       @Param("targetType") Integer targetType);

    /**
     * 获取目标的点赞记录列表
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 点赞记录列表
     */
    List<BizLikeRecord> selectByTarget(@Param("targetType") Integer targetType,
                                       @Param("targetId") Long targetId);

    /**
     * 统计用户的点赞数量
     *
     * @param userId     用户ID
     * @param targetType 目标类型（可为空）
     * @return 点赞数量
     */
    Integer countByUserId(@Param("userId") Long userId,
                          @Param("targetType") Integer targetType);

    /**
     * 统计目标的点赞数量
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 点赞数量
     */
    Integer countByTarget(@Param("targetType") Integer targetType,
                          @Param("targetId") Long targetId);

    /**
     * 批量检查用户是否点赞了指定目标列表
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param targetIds  目标ID列表
     * @return 已点赞的目标ID列表
     */
    List<Long> selectLikedTargetIds(@Param("userId") Long userId,
                                    @Param("targetType") Integer targetType,
                                    @Param("targetIds") List<Long> targetIds);

    /**
     * 物理删除点赞记录
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
