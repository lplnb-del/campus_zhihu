package com.campus.zhihu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.entity.BizComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 评论 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础 CRUD 操作
 *
 * @author CampusZhihu Team
 */
@Mapper
public interface CommentMapper extends BaseMapper<BizComment> {

    /**
     * 根据目标类型和目标ID查询评论列表
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 评论列表
     */
    @Select("SELECT * FROM biz_comment WHERE target_type = #{targetType} AND target_id = #{targetId} AND is_deleted = 0 ORDER BY create_time ASC")
    List<BizComment> selectByTarget(@Param("targetType") Integer targetType, @Param("targetId") Long targetId);

    /**
     * 根据目标类型和目标ID查询顶级评论列表
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 顶级评论列表
     */
    @Select("SELECT * FROM biz_comment WHERE target_type = #{targetType} AND target_id = #{targetId} AND parent_id = 0 AND is_deleted = 0 ORDER BY create_time DESC")
    List<BizComment> selectTopLevelComments(@Param("targetType") Integer targetType, @Param("targetId") Long targetId);

    /**
     * 根据父评论ID查询回复列表
     *
     * @param parentId 父评论ID
     * @return 回复列表
     */
    @Select("SELECT * FROM biz_comment WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY create_time ASC")
    List<BizComment> selectRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * 根据用户ID查询评论列表
     *
     * @param userId 用户ID
     * @return 评论列表
     */
    @Select("SELECT * FROM biz_comment WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY create_time DESC")
    List<BizComment> selectByUserId(@Param("userId") Long userId);

    /**
     * 分页查询评论列表
     *
     * @param page   分页对象
     * @param userId 用户ID
     * @param status 状态
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM biz_comment " +
            "WHERE is_deleted = 0 " +
            "<if test='userId != null'>" +
            "AND user_id = #{userId} " +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<BizComment> selectPageByCondition(Page<BizComment> page,
                                             @Param("userId") Long userId,
                                             @Param("status") Integer status);

    /**
     * 增加点赞次数（数据库中没有like_count字段，此方法暂时不可用）
     *
     * @param commentId 评论ID
     * @param delta     变化量（正数为增加，负数为减少）
     * @return 影响的行数
     */
    @Update("UPDATE biz_comment SET is_deleted = is_deleted WHERE id = #{commentId} AND is_deleted = 0")
    int updateLikeCount(@Param("commentId") Long commentId, @Param("delta") Integer delta);

    /**
     * 根据目标类型和目标ID统计评论数量
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 评论数量
     */
    @Select("SELECT COUNT(*) FROM biz_comment WHERE target_type = #{targetType} AND target_id = #{targetId} AND is_deleted = 0")
    Integer countByTarget(@Param("targetType") Integer targetType, @Param("targetId") Long targetId);

    /**
     * 根据用户ID统计评论数量
     *
     * @param userId 用户ID
     * @return 评论数量
     */
    @Select("SELECT COUNT(*) FROM biz_comment WHERE user_id = #{userId} AND is_deleted = 0")
    Integer countByUserId(@Param("userId") Long userId);

    /**
     * 根据父评论ID统计回复数量
     *
     * @param parentId 父评论ID
     * @return 回复数量
     */
    @Select("SELECT COUNT(*) FROM biz_comment WHERE parent_id = #{parentId} AND is_deleted = 0")
    Integer countRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * 获取用户的最新评论列表
     *
     * @param userId 用户ID
     * @param limit  数量限制
     * @return 最新评论列表
     */
    @Select("SELECT * FROM biz_comment WHERE user_id = #{userId} AND is_deleted = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<BizComment> selectLatestCommentsByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 获取热门评论列表（按创建时间排序）
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @param limit      数量限制
     * @return 热门评论列表
     */
    @Select("SELECT * FROM biz_comment WHERE target_type = #{targetType} AND target_id = #{targetId} " +
            "AND is_deleted = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<BizComment> selectHotComments(@Param("targetType") Integer targetType,
                                       @Param("targetId") Long targetId,
                                       @Param("limit") Integer limit);

    /**
     * 批量删除目标的所有评论（用于删除问题或回答时）
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 影响的行数
     */
    @Update("UPDATE biz_comment SET is_deleted = 1 WHERE target_type = #{targetType} AND target_id = #{targetId}")
    int deleteByTarget(@Param("targetType") Integer targetType, @Param("targetId") Long targetId);

    /**
     * 更新评论状态（数据库中没有status字段，此方法暂时不可用）
     *
     * @param commentId 评论ID
     * @param status    状态
     * @return 影响的行数
     */
    @Update("UPDATE biz_comment SET is_deleted = is_deleted WHERE id = #{commentId} AND is_deleted = 0")
    int updateStatus(@Param("commentId") Long commentId, @Param("status") Integer status);

    /**
     * 搜索评论内容
     *
     * @param page    分页对象
     * @param keyword 关键词
     * @return 分页结果
     */
    @Select("SELECT * FROM biz_comment " +
            "WHERE content LIKE CONCAT('%', #{keyword}, '%') " +
            "AND is_deleted = 0 " +
            "ORDER BY create_time DESC")
    IPage<BizComment> searchComments(Page<BizComment> page, @Param("keyword") String keyword);

    /**
     * 获取全站最新评论
     *
     * @param limit 数量限制
     * @return 最新评论列表
     */
    @Select("SELECT * FROM biz_comment WHERE is_deleted = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<BizComment> selectLatestComments(@Param("limit") Integer limit);

    /**
     * 获取用户在指定目标下的评论
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 评论列表
     */
    @Select("SELECT * FROM biz_comment WHERE user_id = #{userId} " +
            "AND target_type = #{targetType} AND target_id = #{targetId} " +
            "AND is_deleted = 0 ORDER BY create_time DESC")
    List<BizComment> selectByUserAndTarget(@Param("userId") Long userId,
                                            @Param("targetType") Integer targetType,
                                            @Param("targetId") Long targetId);
}