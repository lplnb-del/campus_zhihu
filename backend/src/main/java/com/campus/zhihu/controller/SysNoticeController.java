package com.campus.zhihu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.zhihu.common.Result;
import com.campus.zhihu.entity.SysNotice;
import com.campus.zhihu.service.SysNoticeService;
import com.campus.zhihu.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知控制器
 *
 * @author CampusZhihu Team
 */
@Slf4j
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class SysNoticeController {

    private final SysNoticeService noticeService;
    private final JwtUtil jwtUtil;

    /**
     * 获取通知列表（分页）
     *
     * @param page    页码
     * @param size    每页大小
     * @param request HTTP请求
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<IPage<SysNotice>> getNoticeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("获取通知列表: userId={}, page={}, size={}", userId, page, size);
        
        IPage<SysNotice> noticePage = noticeService.getUserNoticePage(userId, page, size);
        return Result.success(noticePage);
    }

    /**
     * 获取未读通知数量
     *
     * @param request HTTP请求
     * @return 未读数量
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("获取未读通知数量: userId={}", userId);
        
        Integer count = noticeService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记通知为已读
     *
     * @param noticeId 通知ID
     * @param request  HTTP请求
     * @return 操作结果
     */
    @PutMapping("/read/{noticeId}")
    public Result<Boolean> markAsRead(
            @PathVariable Long noticeId,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("标记通知为已读: noticeId={}, userId={}", noticeId, userId);
        
        Boolean result = noticeService.markAsRead(noticeId, userId);
        return Result.success("标记成功", result);
    }

    /**
     * 标记所有通知为已读
     *
     * @param request HTTP请求
     * @return 操作结果
     */
    @PutMapping("/read-all")
    public Result<Boolean> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("标记所有通知为已读: userId={}", userId);
        
        Boolean result = noticeService.markAllAsRead(userId);
        return Result.success("标记成功", result);
    }

    /**
     * 删除通知
     *
     * @param noticeId 通知ID
     * @param request  HTTP请求
     * @return 操作结果
     */
    @DeleteMapping("/{noticeId}")
    public Result<Boolean> deleteNotice(
            @PathVariable Long noticeId,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("删除通知: noticeId={}, userId={}", noticeId, userId);
        
        Boolean result = noticeService.deleteNotice(noticeId, userId);
        return Result.success("删除成功", result);
    }

    /**
     * 从请求中获取用户ID
     *
     * @param request HTTP请求
     * @return 用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        return jwtUtil.getUserIdFromToken(token);
    }

    /**
     * 从请求头中获取Token
     *
     * @param request HTTP请求
     * @return Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}