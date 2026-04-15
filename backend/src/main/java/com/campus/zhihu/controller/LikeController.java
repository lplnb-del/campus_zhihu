package com.campus.zhihu.controller;

import com.campus.zhihu.common.Result;
import com.campus.zhihu.entity.BizLikeRecord;
import com.campus.zhihu.service.LikeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 点赞控制器
 * 处理点赞相关的 HTTP 请求
 *
 * @author CampusZhihu Team
 */
@Slf4j
@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController extends BaseController {

    private final LikeService likeService;

    /**
     * 点赞
     *
     * @param targetType 目标类型（1-问题，2-回答，3-评论）
     * @param targetId   目标ID
     * @param request    HTTP 请求
     * @return 操作结果
     */
    @PostMapping
    public Result<Boolean> like(
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("用户点赞: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
        Boolean result = likeService.like(userId, targetType, targetId);
        return Result.success("点赞成功", result);
    }

    /**
     * 取消点赞
     *
     * @param targetType 目标类型（1-问题，2-回答，3-评论）
     * @param targetId   目标ID
     * @param request    HTTP 请求
     * @return 操作结果
     */
    @DeleteMapping
    public Result<Boolean> unlike(
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("用户取消点赞: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
        Boolean result = likeService.unlike(userId, targetType, targetId);
        return Result.success("取消点赞成功", result);
    }

    /**
     * 切换点赞状态
     *
     * @param targetType 目标类型（1-问题，2-回答，3-评论）
     * @param targetId   目标ID
     * @param request    HTTP 请求
     * @return 点赞后的状态（true-已点赞，false-未点赞）
     */
    @PostMapping("/toggle")
    public Result<Boolean> toggleLike(
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("切换点赞状态: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
        Boolean isLiked = likeService.toggleLike(userId, targetType, targetId);
        String message = isLiked ? "点赞成功" : "取消点赞成功";
        return Result.success(message, isLiked);
    }

    /**
     * 检查用户是否已点赞
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @param request    HTTP 请求
     * @return 是否已点赞
     */
    @GetMapping("/check")
    public Result<Boolean> checkUserLiked(
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("检查用户是否已点赞: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
        Boolean isLiked = likeService.checkUserLiked(userId, targetType, targetId);
        return Result.success(isLiked);
    }

    /**
     * 批量检查用户是否已点赞
     *
     * @param targetType 目标类型
     * @param targetIds  目标ID列表
     * @param request    HTTP 请求
     * @return 目标ID -> 是否已点赞的映射
     */
    @PostMapping("/check-batch")
    public Result<Map<Long, Boolean>> batchCheckUserLiked(
            @RequestParam Integer targetType,
            @RequestBody List<Long> targetIds,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("批量检查用户是否已点赞: userId={}, targetType={}, targetIds.size={}",
                userId, targetType, targetIds != null ? targetIds.size() : 0);
        Map<Long, Boolean> result = likeService.batchCheckUserLiked(userId, targetType, targetIds);
        return Result.success(result);
    }

    /**
     * 获取用户的点赞记录列表
     *
     * @param targetType 目标类型（可选）
     * @param request    HTTP 请求
     * @return 点赞记录列表
     */
    @GetMapping("/my")
    public Result<List<BizLikeRecord>> getMyLikeRecords(
            @RequestParam(required = false) Integer targetType,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("获取我的点赞记录: userId={}, targetType={}", userId, targetType);
        List<BizLikeRecord> records = likeService.getUserLikeRecords(userId, targetType);
        return Result.success(records);
    }

    /**
     * 获取用户的点赞记录列表（根据用户ID）
     *
     * @param userId     用户ID
     * @param targetType 目标类型（可选）
     * @return 点赞记录列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<BizLikeRecord>> getUserLikeRecords(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer targetType
    ) {
        log.info("获取用户的点赞记录: userId={}, targetType={}", userId, targetType);
        List<BizLikeRecord> records = likeService.getUserLikeRecords(userId, targetType);
        return Result.success(records);
    }

    /**
     * 获取目标的点赞记录列表
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 点赞记录列表
     */
    @GetMapping("/target")
    public Result<List<BizLikeRecord>> getTargetLikeRecords(
            @RequestParam Integer targetType,
            @RequestParam Long targetId
    ) {
        log.info("获取目标的点赞记录: targetType={}, targetId={}", targetType, targetId);
        List<BizLikeRecord> records = likeService.getTargetLikeRecords(targetType, targetId);
        return Result.success(records);
    }

    /**
     * 统计用户的点赞数量
     *
     * @param userId     用户ID
     * @param targetType 目标类型（可选）
     * @return 点赞数量
     */
    @GetMapping("/count/user/{userId}")
    public Result<Integer> countUserLikes(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer targetType
    ) {
        log.info("统计用户点赞数量: userId={}, targetType={}", userId, targetType);
        Integer count = likeService.countUserLikes(userId, targetType);
        return Result.success(count);
    }

    /**
     * 统计当前用户的点赞数量
     *
     * @param targetType 目标类型（可选）
     * @param request    HTTP 请求
     * @return 点赞数量
     */
    @GetMapping("/count/my")
    public Result<Integer> countMyLikes(
            @RequestParam(required = false) Integer targetType,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("统计我的点赞数量: userId={}, targetType={}", userId, targetType);
        Integer count = likeService.countUserLikes(userId, targetType);
        return Result.success(count);
    }

    /**
     * 统计目标的点赞数量
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 点赞数量
     */
    @GetMapping("/count/target")
    public Result<Integer> countTargetLikes(
            @RequestParam Integer targetType,
            @RequestParam Long targetId
    ) {
        log.info("统计目标点赞数量: targetType={}, targetId={}", targetType, targetId);
        Integer count = likeService.countTargetLikes(targetType, targetId);
        return Result.success(count);
    }

    /**
     * 获取用户点赞的目标ID列表
     *
     * @param targetType 目标类型
     * @param request    HTTP 请求
     * @return 目标ID列表
     */
    @GetMapping("/my/target-ids")
    public Result<List<Long>> getMyLikedTargetIds(
            @RequestParam Integer targetType,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("获取用户点赞的目标ID列表: userId={}, targetType={}", userId, targetType);
        List<Long> targetIds = likeService.getUserLikedTargetIds(userId, targetType);
        return Result.success(targetIds);
    }
}
