package com.campus.zhihu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.entity.BizQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 问题 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础 CRUD 操作
 *
 * @author CampusZhihu Team
 */
@Mapper
public interface QuestionMapper extends BaseMapper<BizQuestion> {

    /**
     * 根据用户ID查询问题列表
     *
     * @param userId 用户ID
     * @return 问题列表
     */
    @Select("SELECT * FROM biz_question WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY create_time DESC")
    List<BizQuestion> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据标签ID查询问题列表
     *
     * @param tagId 标签ID
     * @return 问题列表
     */
    @Select("SELECT q.* FROM biz_question q " +
            "INNER JOIN biz_question_tag qt ON q.id = qt.question_id " +
            "WHERE qt.tag_id = #{tagId} AND q.is_deleted = 0 " +
            "ORDER BY q.create_time DESC")
    List<BizQuestion> selectByTagId(@Param("tagId") Long tagId);

    /**
     * 分页查询问题列表（支持关键词搜索）
     *
     * @param page    分页对象
     * @param keyword 关键词（搜索标题和内容）
     * @param status  状态
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM biz_question " +
            "WHERE is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<BizQuestion> selectPageWithKeyword(Page<BizQuestion> page,
                                              @Param("keyword") String keyword,
                                              @Param("status") Integer status);

    /**
     * 增加浏览次数
     *
     * @param questionId 问题ID
     * @return 影响的行数
     */
    @Update("UPDATE biz_question SET view_count = view_count + 1 WHERE id = #{questionId} AND is_deleted = 0")
    int incrementViewCount(@Param("questionId") Long questionId);

    /**
     * 增加回答数量
     *
     * @param questionId 问题ID
     * @param delta      变化量（正数为增加，负数为减少）
     * @return 影响的行数
     */
    @Update("UPDATE biz_question SET answer_count = answer_count + #{delta} WHERE id = #{questionId} AND is_deleted = 0")
    int updateAnswerCount(@Param("questionId") Long questionId, @Param("delta") Integer delta);

    /**
     * 增加收藏次数
     *
     * @param questionId 问题ID
     * @param delta      变化量（正数为增加，负数为减少）
     * @return 影响的行数
     */
    @Update("UPDATE biz_question SET collection_count = collection_count + #{delta} WHERE id = #{questionId} AND is_deleted = 0")
    int updateCollectCount(@Param("questionId") Long questionId, @Param("delta") Integer delta);

    /**
     * 增加点赞次数
     *
     * @param questionId 问题ID
     * @param delta      变化量（正数为增加，负数为减少）
     * @return 影响的行数
     */
    @Update("UPDATE biz_question SET like_count = like_count + #{delta} WHERE id = #{questionId} AND is_deleted = 0")
    int updateLikeCount(@Param("questionId") Long questionId, @Param("delta") Integer delta);

    /**
     * 更新问题解决状态
     *
     * @param questionId 问题ID
     * @param isResolved 是否已解决
     * @return 影响的行数
     */
    @Update("UPDATE biz_question SET is_solved = #{isResolved} WHERE id = #{questionId} AND is_deleted = 0")
    int updateResolvedStatus(@Param("questionId") Long questionId, @Param("isResolved") Integer isResolved);

    /**
     * 更新问题的采纳回答ID
     *
     * @param questionId 问题ID
     * @param answerId 采纳的回答ID
     * @return 影响的行数
     */
    @Update("UPDATE biz_question SET accepted_answer_id = #{answerId} WHERE id = #{questionId} AND is_deleted = 0")
    int updateAcceptedAnswerId(@Param("questionId") Long questionId, @Param("answerId") Long answerId);

    /**
     * 根据状态统计问题数量
     *
     * @param status 状态
     * @return 问题数量
     */
    @Select("SELECT COUNT(*) FROM biz_question WHERE status = #{status} AND is_deleted = 0")
    Integer countByStatus(@Param("status") Integer status);

    /**
     * 统计某个用户的问题数量
     *
     * @param userId 用户ID
     * @return 问题数量
     */
    @Select("SELECT COUNT(*) FROM biz_question WHERE user_id = #{userId} AND is_deleted = 0")
    Integer countByUserId(@Param("userId") Long userId);

    /**
     * 获取热门问题列表（按浏览次数排序）
     *
     * @param limit 数量限制
     * @return 热门问题列表
     */
    @Select("SELECT * FROM biz_question WHERE status = 1 AND is_deleted = 0 " +
            "ORDER BY view_count DESC, like_count DESC LIMIT #{limit}")
    List<BizQuestion> selectHotQuestions(@Param("limit") Integer limit);

    /**
     * 获取最新问题列表
     *
     * @param limit 数量限制
     * @return 最新问题列表
     */
    @Select("SELECT * FROM biz_question WHERE status = 1 AND is_deleted = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<BizQuestion> selectLatestQuestions(@Param("limit") Integer limit);

    /**
     * 获取待解决问题列表
     *
     * @param limit 数量限制
     * @return 待解决问题列表
     */
    @Select("SELECT * FROM biz_question WHERE status = 1 AND is_solved = 0 AND is_deleted = 0 " +
            "ORDER BY reward_points DESC, create_time DESC LIMIT #{limit}")
    List<BizQuestion> selectUnsolvedQuestions(@Param("limit") Integer limit);

    /**
     * 获取有悬赏的问题列表
     *
     * @param minRewardPoints 最小悬赏积分
     * @param limit           数量限制
     * @return 有悬赏的问题列表
     */
    @Select("SELECT * FROM biz_question WHERE status = 1 AND reward_points >= #{minRewardPoints} AND is_deleted = 0 " +
            "ORDER BY reward_points DESC, create_time DESC LIMIT #{limit}")
    List<BizQuestion> selectRewardQuestions(@Param("minRewardPoints") Integer minRewardPoints,
                                            @Param("limit") Integer limit);

    /**
     * 搜索问题（全文搜索）
     *
     * @param page    分页对象
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    @Select("SELECT * FROM biz_question " +
            "WHERE (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND status = 1 AND is_deleted = 0 " +
            "ORDER BY create_time DESC")
    IPage<BizQuestion> searchQuestions(Page<BizQuestion> page, @Param("keyword") String keyword);

    /**
     * 获取用户关注的标签相关的问题（推荐问题）
     *
     * @param tagIds 标签ID列表
     * @param limit  数量限制
     * @return 推荐问题列表
     */
    @Select("<script>" +
            "SELECT DISTINCT q.* FROM biz_question q " +
            "INNER JOIN biz_question_tag qt ON q.id = qt.question_id " +
            "WHERE qt.tag_id IN " +
            "<foreach item='tagId' collection='tagIds' open='(' separator=',' close=')'>" +
            "#{tagId}" +
            "</foreach>" +
            "AND q.status = 1 AND q.is_deleted = 0 " +
            "ORDER BY q.create_time DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<BizQuestion> selectRecommendedQuestions(@Param("tagIds") List<Long> tagIds,
                                                  @Param("limit") Integer limit);
}
