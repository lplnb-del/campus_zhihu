package com.campus.zhihu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.zhihu.entity.BizTag;
import com.campus.zhihu.vo.TagVO;

import java.util.List;

/**
 * 标签服务接口
 *
 * @author CampusZhihu Team
 */
public interface TagService {

    /**
     * 根据ID获取标签
     *
     * @param id 标签ID
     * @return 标签实体
     */
    BizTag getById(Long id);

    /**
     * 获取热门标签
     *
     * @param limit 返回数量限制
     * @return 标签列表
     */
    List<TagVO> getHotTags(Integer limit);

    /**
     * 获取所有标签
     *
     * @return 标签列表
     */
    List<TagVO> getAllTags();

    /**
     * 获取标签列表（分页）
     *
     * @param page     页码
     * @param size     每页大小
     * @param category 分类
     * @param sortBy   排序方式：hot-按热度，new-按最新，name-按名称
     * @return 标签列表
     */
    IPage<TagVO> getTagList(Integer page, Integer size, String category, String sortBy);

    /**
     * 搜索标签
     *
     * @param keyword  搜索关键词
     * @param page     页码
     * @param size     每页大小
     * @param sortBy   排序方式：hot-按热度，new-按最新，name-按名称
     * @return 标签列表
     */
    IPage<TagVO> searchTags(String keyword, Integer page, Integer size, String sortBy);

    /**
     * 转换为VO
     *
     * @param tag    标签实体
     * @param userId 用户ID
     * @return 标签VO
     */
    TagVO convertToVO(BizTag tag, Long userId);

    /**
     * 检查用户是否关注了标签
     *
     * @param userId 用户ID
     * @param tagId  标签ID
     * @return true: 已关注, false: 未关注
     */
    boolean isFollowed(Long userId, Long tagId);

    /**
     * 获取标签下的问题数量
     *
     * @param tagId 标签ID
     * @return 问题数量
     */
    int getQuestionCountByTagId(Long tagId);
}