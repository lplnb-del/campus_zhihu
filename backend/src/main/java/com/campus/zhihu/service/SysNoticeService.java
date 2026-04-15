package com.campus.zhihu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.zhihu.entity.SysNotice;

/**
 * 消息通知 Service 接口
 *
 * @author CampusZhihu Team
 */
public interface SysNoticeService extends IService<SysNotice> {

    /**
     * 获取用户的通知列表（分页）
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 分页结果
     */
    IPage<SysNotice> getUserNoticePage(Long userId, Integer page, Integer size);

    /**
     * 获取用户的未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    Integer getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     *
     * @param noticeId 通知ID
     * @param userId   用户ID
     * @return 是否成功
     */
    Boolean markAsRead(Long noticeId, Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean markAllAsRead(Long userId);

    /**
     * 删除通知
     *
     * @param noticeId 通知ID
     * @param userId   用户ID
     * @return 是否成功
     */
    Boolean deleteNotice(Long noticeId, Long userId);
}