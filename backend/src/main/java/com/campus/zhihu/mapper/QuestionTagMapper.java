package com.campus.zhihu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.zhihu.entity.BizQuestionTag;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 问题-标签关联 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础 CRUD 操作
 *
 * @author CampusZhihu Team
 */
@Mapper
public interface QuestionTagMapper extends BaseMapper<BizQuestionTag> {

    /**
     * 根据问题ID查询所有关联的标签ID
     *
     * @param questionId 问题ID
     * @return 标签ID列表
     */
    @Select("SELECT tag_id FROM biz_question_tag WHERE question_id = #{questionId}")
    List<Long> selectTagIdsByQuestionId(@Param("questionId") Long questionId);

    /**
     * 根据标签ID查询所有关联的问题ID
     *
     * @param tagId 标签ID
     * @return 问题ID列表
     */
    @Select("SELECT question_id FROM biz_question_tag WHERE tag_id = #{tagId}")
    List<Long> selectQuestionIdsByTagId(@Param("tagId") Long tagId);

    /**
     * 根据问题ID删除所有关联的标签
     *
     * @param questionId 问题ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM biz_question_tag WHERE question_id = #{questionId}")
    int deleteByQuestionId(@Param("questionId") Long questionId);

    /**
     * 根据标签ID删除所有关联的问题
     *
     * @param tagId 标签ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM biz_question_tag WHERE tag_id = #{tagId}")
    int deleteByTagId(@Param("tagId") Long tagId);

    /**
     * 批量插入问题-标签关联
     *
     * @param questionId 问题ID
     * @param tagIds     标签ID列表
     * @return 影响的行数
     */
    @Insert("<script>" +
            "INSERT INTO biz_question_tag (question_id, tag_id) VALUES " +
            "<foreach item='tagId' collection='tagIds' separator=','>" +
            "(#{questionId}, #{tagId})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("questionId") Long questionId, @Param("tagIds") List<Long> tagIds);

    /**
     * 检查问题-标签关联是否存在
     *
     * @param questionId 问题ID
     * @param tagId      标签ID
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(*) FROM biz_question_tag WHERE question_id = #{questionId} AND tag_id = #{tagId}")
    int exists(@Param("questionId") Long questionId, @Param("tagId") Long tagId);

    /**
     * 根据问题ID统计标签数量
     *
     * @param questionId 问题ID
     * @return 标签数量
     */
    @Select("SELECT COUNT(*) FROM biz_question_tag WHERE question_id = #{questionId}")
    Integer countTagsByQuestionId(@Param("questionId") Long questionId);

    /**
     * 根据标签ID统计问题数量
     *
     * @param tagId 标签ID
     * @return 问题数量
     */
    @Select("SELECT COUNT(*) FROM biz_question_tag WHERE tag_id = #{tagId}")
    Integer countQuestionsByTagId(@Param("tagId") Long tagId);

    /**
     * 批量删除问题-标签关联
     *
     * @param questionId 问题ID
     * @param tagIds     标签ID列表
     * @return 影响的行数
     */
    @Delete("<script>" +
            "DELETE FROM biz_question_tag " +
            "WHERE question_id = #{questionId} AND tag_id IN " +
            "<foreach item='tagId' collection='tagIds' open='(' separator=',' close=')'>" +
            "#{tagId}" +
            "</foreach>" +
            "</script>")
    int batchDelete(@Param("questionId") Long questionId, @Param("tagIds") List<Long> tagIds);

    /**
     * 根据问题ID列表批量删除关联
     *
     * @param questionIds 问题ID列表
     * @return 影响的行数
     */
    @Delete("<script>" +
            "DELETE FROM biz_question_tag " +
            "WHERE question_id IN " +
            "<foreach item='questionId' collection='questionIds' open='(' separator=',' close=')'>" +
            "#{questionId}" +
            "</foreach>" +
            "</script>")
    int batchDeleteByQuestionIds(@Param("questionIds") List<Long> questionIds);

    /**
     * 获取与指定问题有相同标签的其他问题ID（用于推荐相关问题）
     *
     * @param questionId 问题ID
     * @param limit      数量限制
     * @return 相关问题ID列表
     */
    @Select("SELECT DISTINCT qt2.question_id " +
            "FROM biz_question_tag qt1 " +
            "INNER JOIN biz_question_tag qt2 ON qt1.tag_id = qt2.tag_id " +
            "WHERE qt1.question_id = #{questionId} AND qt2.question_id != #{questionId} " +
            "LIMIT #{limit}")
    List<Long> selectRelatedQuestionIds(@Param("questionId") Long questionId,
                                        @Param("limit") Integer limit);

    /**
     * 根据多个标签ID查询包含所有这些标签的问题ID
     *
     * @param tagIds 标签ID列表
     * @return 问题ID列表
     */
    @Select("<script>" +
            "SELECT question_id FROM biz_question_tag " +
            "WHERE tag_id IN " +
            "<foreach item='tagId' collection='tagIds' open='(' separator=',' close=')'>" +
            "#{tagId}" +
            "</foreach>" +
            "GROUP BY question_id " +
            "HAVING COUNT(DISTINCT tag_id) = #{tagCount}" +
            "</script>")
    List<Long> selectQuestionIdsByAllTags(@Param("tagIds") List<Long> tagIds,
                                          @Param("tagCount") Integer tagCount);

    /**
     * 根据多个标签ID查询包含任意一个这些标签的问题ID
     *
     * @param tagIds 标签ID列表
     * @return 问题ID列表
     */
    @Select("<script>" +
            "SELECT DISTINCT question_id FROM biz_question_tag " +
            "WHERE tag_id IN " +
            "<foreach item='tagId' collection='tagIds' open='(' separator=',' close=')'>" +
            "#{tagId}" +
            "</foreach>" +
            "</script>")
    List<Long> selectQuestionIdsByAnyTag(@Param("tagIds") List<Long> tagIds);
}
