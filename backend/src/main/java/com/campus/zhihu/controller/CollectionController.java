package com.campus.zhihu.controller;

import com.campus.zhihu.common.Result;
import com.campus.zhihu.service.CollectionService;
import com.campus.zhihu.vo.CollectionVO;
import com.campus.zhihu.vo.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 收藏控制器
 * 处理收藏相关的 HTTP 请求
 *
 * @author CampusZhihu Team
 */
@Slf4j
@RestController
@RequestMapping("/collection")
@RequiredArgsConstructor
public class CollectionController extends BaseController {

    private final CollectionService collectionService;

    /**
     * 收藏
     *
     * @param targetType 目标类型（1-问题，2-回答）
     * @param targetId   目标ID
     * @param request    HTTP 请求
     * @return 操作结果
     */
    @PostMapping
    public Result<Boolean> collect(
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("用户收藏: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
        Boolean result = collectionService.collect(userId, targetType, targetId);
        return Result.success("收藏成功", result);
    }

    /**
     * 取消收藏
     *
     * @param targetType 目标类型（1-问题，2-回答）
     * @param targetId   目标ID
     * @param request    HTTP 请求
     * @return 操作结果
     */
    @DeleteMapping
    public Result<Boolean> uncollect(
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("用户取消收藏: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
        Boolean result = collectionService.uncollect(userId, targetType, targetId);
        return Result.success("取消收藏成功", result);
    }

    /**
     * 切换收藏状态
     *
     * @param targetType 目标类型（1-问题，2-回答）
     * @param targetId   目标ID
     * @param request    HTTP 请求
     * @return 收藏后的状态（true-已收藏，false-未收藏）
     */
    @PostMapping("/toggle")
    public Result<Boolean> toggleCollect(
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("切换收藏状态: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
        Boolean isCollected = collectionService.toggleCollect(userId, targetType, targetId);
        String message = isCollected ? "收藏成功" : "取消收藏成功";
        return Result.success(message, isCollected);
    }

    /**
     * 检查用户是否已收藏
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @param request    HTTP 请求
     * @return 是否已收藏
     */
    @GetMapping("/check")
    public Result<Boolean> checkUserCollected(
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("检查用户是否已收藏: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
        Boolean isCollected = collectionService.checkUserCollected(userId, targetType, targetId);
        return Result.success(isCollected);
    }

    /**
     * 批量检查用户是否已收藏
     *
     * @param targetType 目标类型
     * @param targetIds  目标ID列表
     * @param request    HTTP 请求
     * @return 目标ID -> 是否已收藏的映射
     */
    @PostMapping("/check-batch")
    public Result<Map<Long, Boolean>> batchCheckUserCollected(
            @RequestParam Integer targetType,
            @RequestBody List<Long> targetIds,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("批量检查用户是否已收藏: userId={}, targetType={}, targetIds.size={}",
                userId, targetType, targetIds != null ? targetIds.size() : 0);
        Map<Long, Boolean> result = collectionService.batchCheckUserCollected(userId, targetType, targetIds);
        return Result.success(result);
    }

    /**
     * 获取用户的收藏列表
     *
     * @param targetType 目标类型（可选）
     * @param request    HTTP 请求
     * @return 收藏列表
     */
    @GetMapping("/my")
    public Result<PageResponse<CollectionVO>> getMyCollections(
            @RequestParam(required = false) Integer targetType,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("获取我的收藏列表: userId={}, targetType={}", userId, targetType);
        List<CollectionVO> collections = collectionService.getUserCollections(userId, targetType);
        PageResponse<CollectionVO> response = PageResponse.of(collections, (long) collections.size(), 1L, (long) collections.size());
        return Result.success(response);
    }

    /**
     * 获取用户的收藏列表（根据用户ID）
     *
     * @param userId     用户ID
     * @param targetType 目标类型（可选）
     * @return 收藏列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<CollectionVO>> getUserCollections(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer targetType
    ) {
        log.info("获取用户的收藏列表: userId={}, targetType={}", userId, targetType);
        List<CollectionVO> collections = collectionService.getUserCollections(userId, targetType);
        return Result.success(collections);
    }

    /**
     * 获取目标的收藏列表
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 收藏列表
     */
    @GetMapping("/target")
    public Result<List<CollectionVO>> getTargetCollections(
            @RequestParam Integer targetType,
            @RequestParam Long targetId
    ) {
        log.info("获取目标的收藏列表: targetType={}, targetId={}", targetType, targetId);
        List<CollectionVO> collections = collectionService.getTargetCollections(targetType, targetId);
        return Result.success(collections);
    }

    /**
     * 统计用户的收藏数量
     *
     * @param userId     用户ID
     * @param targetType 目标类型（可选）
     * @return 收藏数量
     */
    @GetMapping("/count/user/{userId}")
    public Result<Integer> countUserCollections(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer targetType
    ) {
        log.info("统计用户收藏数量: userId={}, targetType={}", userId, targetType);
        Integer count = collectionService.countUserCollections(userId, targetType);
        return Result.success(count);
    }

    /**
     * 统计当前用户的收藏数量
     *
     * @param targetType 目标类型（可选）
     * @param request    HTTP 请求
     * @return 收藏数量
     */
    @GetMapping("/count/my")
    public Result<Integer> countMyCollections(
            @RequestParam(required = false) Integer targetType,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("统计我的收藏数量: userId={}, targetType={}", userId, targetType);
        Integer count = collectionService.countUserCollections(userId, targetType);
        return Result.success(count);
    }

    /**
     * 统计目标的收藏数量
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 收藏数量
     */
    @GetMapping("/count/target")
    public Result<Integer> countTargetCollections(
            @RequestParam Integer targetType,
            @RequestParam Long targetId
    ) {
        log.info("统计目标收藏数量: targetType={}, targetId={}", targetType, targetId);
        Integer count = collectionService.countTargetCollections(targetType, targetId);
        return Result.success(count);
    }

    /**
     * 获取用户收藏的目标ID列表
     *
     * @param targetType 目标类型
     * @param request    HTTP 请求
     * @return 目标ID列表
     */
    @GetMapping("/my/target-ids")
    public Result<List<Long>> getMyCollectedTargetIds(
            @RequestParam Integer targetType,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("获取用户收藏的目标ID列表: userId={}, targetType={}", userId, targetType);
        List<Long> targetIds = collectionService.getUserCollectedTargetIds(userId, targetType);
        return Result.success(targetIds);
    }
}
