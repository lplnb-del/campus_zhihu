package com.campus.zhihu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.entity.BizAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 回答 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础 CRUD 操作
 *
 * @author CampusZhihu Team
 */
@Mapper
public interface AnswerMapper extends BaseMapper<BizAnswer> {

    /**
     * 根据问题ID查询回答列表
     *
     * @param questionId 问题ID
     * @return 回答列表
     */
    @Select("SELECT * FROM biz_answer WHERE question_id = #{questionId} AND is_deleted = 0 ORDER BY is_accepted DESC, like_count DESC, create_time DESC")
    List<BizAnswer> selectByQuestionId(@Param("questionId") Long questionId);

    /**
     * 根据问题ID分页查询回答列表
     *
     * @param page      分页对象
     * @param questionId 问题ID
     * @return 分页结果
     */
    @Select("SELECT * FROM biz_answer WHERE question_id = #{questionId} AND is_deleted = 0 ORDER BY is_accepted DESC, like_count DESC, create_time DESC")
    IPage<BizAnswer> selectPageByQuestionId(Page<BizAnswer> page, @Param("questionId") Long questionId);

    /**
     * 根据用户ID查询回答列表
     *
     * @param userId 用户ID
     * @return 回答列表
     */
    @Select("SELECT * FROM biz_answer WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY create_time DESC")
    List<BizAnswer> selectByUserId(@Param("userId") Long userId);

    /**
     * 分页查询回答列表（支持关键词搜索）
     *
     * @param page    分页对象
     * @param keyword 关键词（搜索内容）
     * @param status  状态
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM biz_answer " +
            "WHERE is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND content LIKE CONCAT('%', #{keyword}, '%') " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<BizAnswer> selectPageWithKeyword(Page<BizAnswer> page,
                                            @Param("keyword") String keyword,
                                            @Param("status") Integer status);

    /**
     * 增加点赞次数
     *
     * @param answerId 回答ID
     * @param delta    变化量（正数为增加，负数为减少）
     * @return 影响的行数
     */
    @Update("UPDATE biz_answer SET like_count = like_count + #{delta} WHERE id = #{answerId} AND is_deleted = 0")
    int updateLikeCount(@Param("answerId") Long answerId, @Param("delta") Integer delta);

    /**
     * 增加评论次数
     *
     * @param answerId 回答ID
     * @param delta    变化量（正数为增加，负数为减少）
     * @return 影响的行数
     */
    @Update("UPDATE biz_answer SET comment_count = comment_count + #{delta} WHERE id = #{answerId} AND is_deleted = 0")
    int updateCommentCount(@Param("answerId") Long answerId, @Param("delta") Integer delta);

    /**
     * 更新采纳状态
     *
     * @param answerId   回答ID
     * @param isAccepted 是否采纳
     * @return 影响的行数
     */
    @Update("UPDATE biz_answer SET is_accepted = #{isAccepted} WHERE id = #{answerId} AND is_deleted = 0")
    int updateAcceptedStatus(@Param("answerId") Long answerId, @Param("isAccepted") Integer isAccepted);

    /**
     * 取消问题的所有采纳状态（用于更换采纳答案）
     *
     * @param questionId 问题ID
     * @return 影响的行数
     */
    @Update("UPDATE biz_answer SET is_accepted = 0 WHERE question_id = #{questionId} AND is_deleted = 0")
    int cancelAllAcceptedByQuestionId(@Param("questionId") Long questionId);

    /**
     * 根据问题ID统计回答数量
     *
     * @param questionId 问题ID
     * @return 回答数量
     */
    @Select("SELECT COUNT(*) FROM biz_answer WHERE question_id = #{questionId} AND status = 1 AND is_deleted = 0")
    Integer countByQuestionId(@Param("questionId") Long questionId);

    /**
     * 根据用户ID统计回答数量
     *
     * @param userId 用户ID
     * @return 回答数量
     */
    @Select("SELECT COUNT(*) FROM biz_answer WHERE user_id = #{userId} AND is_deleted = 0")
    Integer countByUserId(@Param("userId") Long userId);

    /**
     * 根据问题ID和用户ID查询回答
     *
     * @param questionId 问题ID
     * @param userId     用户ID
     * @return 回答列表
     */
    @Select("SELECT * FROM biz_answer WHERE question_id = #{questionId} AND user_id = #{userId} AND is_deleted = 0 ORDER BY create_time DESC")
    List<BizAnswer> selectByQuestionIdAndUserId(@Param("questionId") Long questionId, @Param("userId") Long userId);

    /**
     * 获取问题的被采纳回答
     *
     * @param questionId 问题ID
     * @return 被采纳的回答
     */
    @Select("SELECT * FROM biz_answer WHERE question_id = #{questionId} AND is_accepted = 1 AND is_deleted = 0 LIMIT 1")
    BizAnswer selectAcceptedAnswer(@Param("questionId") Long questionId);

    /**
     * 获取用户的最新回答列表
     *
     * @param userId 用户ID
     * @param limit  数量限制
     * @return 最新回答列表
     */
    @Select("SELECT * FROM biz_answer WHERE user_id = #{userId} AND status = 1 AND is_deleted = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<BizAnswer> selectLatestAnswersByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 获取用户的热门回答列表（按点赞数排序）
     *
     * @param userId 用户ID
     * @param limit  数量限制
     * @return 热门回答列表
     */
    @Select("SELECT * FROM biz_answer WHERE user_id = #{userId} AND status = 1 AND is_deleted = 0 " +
            "ORDER BY like_count DESC, create_time DESC LIMIT #{limit}")
    List<BizAnswer> selectHotAnswersByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 搜索回答（全文搜索）
     *
     * @param page    分页对象
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    @Select("SELECT * FROM biz_answer " +
            "WHERE content LIKE CONCAT('%', #{keyword}, '%') " +
            "AND status = 1 AND is_deleted = 0 " +
            "ORDER BY like_count DESC, create_time DESC")
    IPage<BizAnswer> searchAnswers(Page<BizAnswer> page, @Param("keyword") String keyword);

    /**
     * 获取问题的回答数（包括草稿和已发布）
     *
     * @param questionId 问题ID
     * @return 回答数量
     */
    @Select("SELECT COUNT(*) FROM biz_answer WHERE question_id = #{questionId} AND is_deleted = 0")
    Integer countAllByQuestionId(@Param("questionId") Long questionId);

    /**
     * 获取高赞回答列表（全站）
     *
     * @param limit 数量限制
     * @return 高赞回答列表
     */
    @Select("SELECT * FROM biz_answer WHERE status = 1 AND is_deleted = 0 " +
            "ORDER BY like_count DESC LIMIT #{limit}")
    List<BizAnswer> selectTopAnswers(@Param("limit") Integer limit);

    /**
     * 检查用户是否已回答该问题
     *
     * @param questionId 问题ID
     * @param userId     用户ID
     * @return 是否已回答
     */
    @Select("SELECT COUNT(*) FROM biz_answer WHERE question_id = #{questionId} AND user_id = #{userId} AND is_deleted = 0")
    int checkUserAnswered(@Param("questionId") Long questionId, @Param("userId") Long userId);
}
