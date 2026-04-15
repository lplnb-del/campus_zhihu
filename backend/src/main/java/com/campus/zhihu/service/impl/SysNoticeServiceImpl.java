package com.campus.zhihu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.zhihu.entity.SysNotice;
import com.campus.zhihu.mapper.SysNoticeMapper;
import com.campus.zhihu.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消息通知 Service 实现类
 *
 * @author CampusZhihu Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements SysNoticeService {

    @Override
    public IPage<SysNotice> getUserNoticePage(Long userId, Integer page, Integer size) {
        log.info("获取用户通知列表: userId={}, page={}, size={}", userId, page, size);
        
        Page<SysNotice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getReceiverId, userId)
                .orderByDesc(SysNotice::getCreateTime);
        
        return baseMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        log.info("获取用户未读通知数量: userId={}", userId);
        
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getReceiverId, userId)
                .eq(SysNotice::getIsRead, 0);
        
        return Math.toIntExact(baseMapper.selectCount(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markAsRead(Long noticeId, Long userId) {
        log.info("标记通知为已读: noticeId={}, userId={}", noticeId, userId);
        
        SysNotice notice = baseMapper.selectById(noticeId);
        if (notice == null || !notice.getReceiverId().equals(userId)) {
            log.warn("通知不存在或无权操作: noticeId={}", noticeId);
            return false;
        }
        
        notice.setIsRead(1);
        return baseMapper.updateById(notice) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markAllAsRead(Long userId) {
        log.info("标记所有通知为已读: userId={}", userId);
        
        SysNotice update = new SysNotice();
        update.setIsRead(1);
        
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getReceiverId, userId)
                .eq(SysNotice::getIsRead, 0);
        
        return baseMapper.update(update, wrapper) >= 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteNotice(Long noticeId, Long userId) {
        log.info("删除通知: noticeId={}, userId={}", noticeId, userId);
        
        SysNotice notice = baseMapper.selectById(noticeId);
        if (notice == null || !notice.getReceiverId().equals(userId)) {
            log.warn("通知不存在或无权操作: noticeId={}", noticeId);
            return false;
        }
        
        return baseMapper.deleteById(noticeId) > 0;
    }
}