package com.campus.zhihu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.CommentPublishDTO;
import com.campus.zhihu.service.CommentService;
import com.campus.zhihu.vo.CommentVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 * 处理评论相关的 HTTP 请求
 *
 * @author CampusZhihu Team
 */
@Slf4j
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController extends BaseController {

    private final CommentService commentService;

    /**
     * 发布评论
     *
     * @param publishDTO 评论发布信息
     * @param request    HTTP 请求
     * @return 评论详情
     */
    @PostMapping("/publish")
    public Result<CommentVO> publishComment(
            @Validated @RequestBody CommentPublishDTO publishDTO,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("用户发布评论: userId={}, targetType={}, targetId={}",
                userId, publishDTO.getTargetType(), publishDTO.getTargetId());
        CommentVO commentVO = commentService.publishComment(userId, publishDTO);
        return Result.success("评论发布成功", commentVO);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param request   HTTP 请求
     * @return 操作结果
     */
    @DeleteMapping("/{commentId}")
    public Result<Boolean> deleteComment(
            @PathVariable Long commentId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("用户删除评论: userId={}, commentId={}", userId, commentId);
        Boolean result = commentService.deleteComment(userId, commentId);
        return Result.success("评论删除成功", result);
    }

    /**
     * 获取评论详情
     *
     * @param commentId 评论ID
     * @param request   HTTP 请求
     * @return 评论详情
     */
    @GetMapping("/{commentId}")
    public Result<CommentVO> getCommentById(
            @PathVariable Long commentId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取评论详情: commentId={}", commentId);
        CommentVO commentVO = commentService.getCommentById(commentId, userId);
        return Result.success(commentVO);
    }

    /**
     * 获取目标的评论列表（树形结构）
     *
     * @param targetType 目标类型（1-问题，2-回答）
     * @param targetId   目标ID
     * @param request    HTTP 请求
     * @return 评论列表
     */
    @GetMapping("/target")
    public Result<List<CommentVO>> getCommentsByTarget(
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取目标的评论列表: targetType={}, targetId={}", targetType, targetId);
        List<CommentVO> comments = commentService.getCommentsByTarget(targetType, targetId, userId);
        return Result.success(comments);
    }

    /**
     * 获取目标的顶级评论列表
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @param request    HTTP 请求
     * @return 顶级评论列表
     */
    @GetMapping("/target/top-level")
    public Result<List<CommentVO>> getTopLevelComments(
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取顶级评论列表: targetType={}, targetId={}", targetType, targetId);
        List<CommentVO> comments = commentService.getTopLevelComments(targetType, targetId, userId);
        return Result.success(comments);
    }

    /**
     * 获取父评论的回复列表
     *
     * @param parentId 父评论ID
     * @param request  HTTP 请求
     * @return 回复列表
     */
    @GetMapping("/replies/{parentId}")
    public Result<List<CommentVO>> getRepliesByParentId(
            @PathVariable Long parentId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取评论的回复列表: parentId={}", parentId);
        List<CommentVO> replies = commentService.getRepliesByParentId(parentId, userId);
        return Result.success(replies);
    }

    /**
     * 获取用户的评论列表
     *
     * @param userId  用户ID
     * @param request HTTP 请求
     * @return 评论列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<CommentVO>> getUserComments(
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserIdOrNull(request);
        log.info("获取用户的评论列表: userId={}", userId);
        List<CommentVO> comments = commentService.getCommentsByUserId(userId, currentUserId);
        return Result.success(comments);
    }

    /**
     * 获取当前用户的评论列表
     *
     * @param request HTTP 请求
     * @return 评论列表
     */
    @GetMapping("/my")
    public Result<List<CommentVO>> getMyComments(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        log.info("获取我的评论列表: userId={}", userId);
        List<CommentVO> comments = commentService.getCommentsByUserId(userId, userId);
        return Result.success(comments);
    }

    /**
     * 分页查询评论
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param userId   用户ID（可选）
     * @param status   状态（可选）
     * @param request  HTTP 请求
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<IPage<CommentVO>> getCommentPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserIdOrNull(request);
        log.info("分页查询评论: pageNum={}, pageSize={}, userId={}, status={}",
                pageNum, pageSize, userId, status);
        IPage<CommentVO> page = commentService.getCommentPage(pageNum, pageSize, userId, status, currentUserId);
        return Result.success(page);
    }

    /**
     * 搜索评论
     *
     * @param keyword  搜索关键词
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param request  HTTP 请求
     * @return 分页结果
     */
    @GetMapping("/search")
    public Result<IPage<CommentVO>> searchComments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("搜索评论: keyword={}", keyword);
        IPage<CommentVO> page = commentService.searchComments(keyword, pageNum, pageSize, userId);
        return Result.success(page);
    }

    /**
     * 统计目标的评论数量
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 评论数量
     */
    @GetMapping("/count/target")
    public Result<Integer> countCommentsByTarget(
            @RequestParam Integer targetType,
            @RequestParam Long targetId
    ) {
        log.info("统计目标评论数量: targetType={}, targetId={}", targetType, targetId);
        Integer count = commentService.countCommentsByTarget(targetType, targetId);
        return Result.success(count);
    }

    /**
     * 统计用户的评论数量
     *
     * @param userId 用户ID
     * @return 评论数量
     */
    @GetMapping("/count/user/{userId}")
    public Result<Integer> countUserComments(@PathVariable Long userId) {
        log.info("统计用户评论数量: userId={}", userId);
        Integer count = commentService.countCommentsByUserId(userId);
        return Result.success(count);
    }

    /**
     * 统计父评论的回复数量
     *
     * @param parentId 父评论ID
     * @return 回复数量
     */
    @GetMapping("/count/replies/{parentId}")
    public Result<Integer> countRepliesByParentId(@PathVariable Long parentId) {
        log.info("统计评论的回复数量: parentId={}", parentId);
        Integer count = commentService.countRepliesByParentId(parentId);
        return Result.success(count);
    }

    /**
     * 获取用户的最新评论
     *
     * @param userId  用户ID
     * @param limit   数量限制
     * @param request HTTP 请求
     * @return 最新评论列表
     */
    @GetMapping("/user/{userId}/latest")
    public Result<List<CommentVO>> getLatestComments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserIdOrNull(request);
        log.info("获取用户最新评论: userId={}, limit={}", userId, limit);
        List<CommentVO> comments = commentService.getLatestCommentsByUserId(userId, limit, currentUserId);
        return Result.success(comments);
    }

    /**
     * 获取热门评论
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @param limit      数量限制
     * @param request    HTTP 请求
     * @return 热门评论列表
     */
    @GetMapping("/hot")
    public Result<List<CommentVO>> getHotComments(
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取热门评论: targetType={}, targetId={}, limit={}", targetType, targetId, limit);
        List<CommentVO> comments = commentService.getHotComments(targetType, targetId, limit, userId);
        return Result.success(comments);
    }

    /**
     * 获取全站最新评论
     *
     * @param limit   数量限制
     * @param request HTTP 请求
     * @return 最新评论列表
     */
    @GetMapping("/latest")
    public Result<List<CommentVO>> getLatestCommentsGlobal(
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取全站最新评论: limit={}", limit);
        List<CommentVO> comments = commentService.getLatestComments(limit, userId);
        return Result.success(comments);
    }

    /**
     * 屏蔽评论（管理员功能）
     *
     * @param commentId 评论ID
     * @param request   HTTP 请求
     * @return 操作结果
     */
    @PutMapping("/{commentId}/block")
    public Result<Boolean> blockComment(
            @PathVariable Long commentId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("管理员屏蔽评论: adminId={}, commentId={}", userId, commentId);
        Boolean result = commentService.blockComment(commentId);
        return Result.success("评论已屏蔽", result);
    }

    /**
     * 审核通过评论（管理员功能）
     *
     * @param commentId 评论ID
     * @param request   HTTP 请求
     * @return 操作结果
     */
    @PutMapping("/{commentId}/approve")
    public Result<Boolean> approveComment(
            @PathVariable Long commentId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("管理员审核通过评论: adminId={}, commentId={}", userId, commentId);
        Boolean result = commentService.approveComment(commentId);
        return Result.success("评论已审核通过", result);
    }
}
