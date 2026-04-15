package com.campus.zhihu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.zhihu.dto.CommentPublishDTO;
import com.campus.zhihu.entity.BizComment;
import com.campus.zhihu.vo.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 * 提供评论相关的业务逻辑处理
 *
 * @author CampusZhihu Team
 */
public interface CommentService {

    /**
     * 发布评论
     *
     * @param userId     用户ID
     * @param publishDTO 评论发布DTO
     * @return 评论VO
     */
    CommentVO publishComment(Long userId, CommentPublishDTO publishDTO);

    /**
     * 删除评论（逻辑删除）
     *
     * @param userId    用户ID
     * @param commentId 评论ID
     * @return 是否成功
     */
    Boolean deleteComment(Long userId, Long commentId);

    /**
     * 根据ID获取评论详情
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID（可为空）
     * @return 评论VO
     */
    CommentVO getCommentById(Long commentId, Long userId);

    /**
     * 根据目标获取评论列表（树形结构）
     *
     * @param targetType 目标类型（1-问题，2-回答）
     * @param targetId   目标ID
     * @param userId     当前用户ID（可为空）
     * @return 评论列表（包含子评论）
     */
    List<CommentVO> getCommentsByTarget(Integer targetType, Long targetId, Long userId);

    /**
     * 根据目标获取顶级评论列表
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @param userId     当前用户ID（可为空）
     * @return 顶级评论列表
     */
    List<CommentVO> getTopLevelComments(Integer targetType, Long targetId, Long userId);

    /**
     * 根据父评论ID获取回复列表
     *
     * @param parentId 父评论ID
     * @param userId   当前用户ID（可为空）
     * @return 回复列表
     */
    List<CommentVO> getRepliesByParentId(Long parentId, Long userId);

    /**
     * 根据用户ID查询评论列表
     *
     * @param userId        用户ID
     * @param currentUserId 当前用户ID（可为空）
     * @return 评论列表
     */
    List<CommentVO> getCommentsByUserId(Long userId, Long currentUserId);

    /**
     * 分页查询评论
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId  用户ID（可为空）
     * @param status  状态（可为空）
     * @param currentUserId 当前用户ID（可为空）
     * @return 分页结果
     */
    IPage<CommentVO> getCommentPage(Integer pageNum, Integer pageSize, Long userId, Integer status, Long currentUserId);

    /**
     * 搜索评论
     *
     * @param keyword  搜索关键词
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param userId   当前用户ID（可为空）
     * @return 分页结果
     */
    IPage<CommentVO> searchComments(String keyword, Integer pageNum, Integer pageSize, Long userId);

    /**
     * 更新点赞次数
     *
     * @param commentId 评论ID
     * @param delta     变化量
     * @return 是否成功
     */
    Boolean updateLikeCount(Long commentId, Integer delta);

    /**
     * 统计目标的评论数量
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 评论数量
     */
    Integer countCommentsByTarget(Integer targetType, Long targetId);

    /**
     * 统计用户的评论数量
     *
     * @param userId 用户ID
     * @return 评论数量
     */
    Integer countCommentsByUserId(Long userId);

    /**
     * 统计父评论的回复数量
     *
     * @param parentId 父评论ID
     * @return 回复数量
     */
    Integer countRepliesByParentId(Long parentId);

    /**
     * 获取用户最新评论列表
     *
     * @param userId        用户ID
     * @param limit         数量限制
     * @param currentUserId 当前用户ID（可为空）
     * @return 最新评论列表
     */
    List<CommentVO> getLatestCommentsByUserId(Long userId, Integer limit, Long currentUserId);

    /**
     * 获取热门评论列表
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @param limit      数量限制
     * @param userId     当前用户ID（可为空）
     * @return 热门评论列表
     */
    List<CommentVO> getHotComments(Integer targetType, Long targetId, Integer limit, Long userId);

    /**
     * 获取全站最新评论
     *
     * @param limit  数量限制
     * @param userId 当前用户ID（可为空）
     * @return 最新评论列表
     */
    List<CommentVO> getLatestComments(Integer limit, Long userId);

    /**
     * 屏蔽评论（管理员功能）
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    Boolean blockComment(Long commentId);

    /**
     * 审核通过评论（管理员功能）
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    Boolean approveComment(Long commentId);

    /**
     * 根据实体转换为VO
     *
     * @param comment 评论实体
     * @param userId  当前用户ID（可为空）
     * @return 评论VO
     */
    CommentVO convertToVO(BizComment comment, Long userId);

    /**
     * 批量转换为VO
     *
     * @param comments 评论实体列表
     * @param userId   当前用户ID（可为空）
     * @return 评论VO列表
     */
    List<CommentVO> convertToVOList(List<BizComment> comments, Long userId);
}
