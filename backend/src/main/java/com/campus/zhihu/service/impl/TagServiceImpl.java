package com.campus.zhihu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.entity.BizTag;
import com.campus.zhihu.entity.BizUserTag;
import com.campus.zhihu.mapper.TagMapper;
import com.campus.zhihu.mapper.UserTagMapper;
import com.campus.zhihu.service.TagService;
import com.campus.zhihu.vo.TagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 *
 * @author CampusZhihu Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final UserTagMapper userTagMapper;

    @Override
    public BizTag getById(Long id) {
        log.debug("根据ID获取标签: id={}", id);
        return tagMapper.selectById(id);
    }

    @Override
    @Cacheable(value = "tags", key = "'hot:' + #limit", unless = "#result == null || #result.isEmpty()")
    public List<TagVO> getHotTags(Integer limit) {
        log.debug("获取热门标签: limit={}", limit);

        // 使用 MyBatis-Plus 分页 API 替代字符串拼接，避免 SQL 注入风险
        Page<BizTag> page = new Page<>(1, limit);
        LambdaQueryWrapper<BizTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(BizTag::getUseCount);

        IPage<BizTag> tagPage = tagMapper.selectPage(page, queryWrapper);
        List<BizTag> tags = tagPage.getRecords();

        // 转换为VO
        return tags.stream()
                .map(tag -> convertToVO(tag, null))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "tags", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<TagVO> getAllTags() {
        log.debug("获取所有标签");

        List<BizTag> tags = tagMapper.selectList(new LambdaQueryWrapper<>());

        return tags.stream()
                .map(tag -> convertToVO(tag, null))
                .collect(Collectors.toList());
    }

    @Override
    public IPage<TagVO> getTagList(Integer page, Integer size, String category, String sortBy) {
        log.debug("获取标签列表: page={}, size={}, category={}, sortBy={}", page, size, category, sortBy);

        LambdaQueryWrapper<BizTag> queryWrapper = new LambdaQueryWrapper<>();
        
        // 分类筛选
        if (category != null && !category.isEmpty()) {
            queryWrapper.eq(BizTag::getCategory, category);
        }
        
        // 排序
        switch (sortBy) {
            case "new":
                queryWrapper.orderByDesc(BizTag::getCreateTime);
                break;
            case "name":
                queryWrapper.orderByAsc(BizTag::getName);
                break;
            case "hot":
            default:
                queryWrapper.orderByDesc(BizTag::getUseCount);
                break;
        }

        // 使用分页API
        Page<BizTag> pageParam = new Page<>(page, size);
        IPage<BizTag> tagPage = tagMapper.selectPage(pageParam, queryWrapper);

        // 转换为VO
        IPage<TagVO> voPage = new Page<>(tagPage.getCurrent(), tagPage.getSize(), tagPage.getTotal());
        List<TagVO> tagVOList = tagPage.getRecords().stream()
                .map(tag -> convertToVO(tag, null))
                .collect(Collectors.toList());
        voPage.setRecords(tagVOList);

        return voPage;
    }

    @Override
    public IPage<TagVO> searchTags(String keyword, Integer page, Integer size, String sortBy) {
        log.debug("搜索标签: keyword={}, page={}, size={}, sortBy={}", keyword, page, size, sortBy);

        LambdaQueryWrapper<BizTag> queryWrapper = new LambdaQueryWrapper<>();
        
        // 搜索条件：标签名称或描述包含关键词
        queryWrapper.and(wrapper -> wrapper
                .like(BizTag::getName, keyword)
                .or()
                .like(BizTag::getDescription, keyword));
        
        // 排序
        switch (sortBy) {
            case "new":
                queryWrapper.orderByDesc(BizTag::getCreateTime);
                break;
            case "name":
                queryWrapper.orderByAsc(BizTag::getName);
                break;
            case "hot":
            default:
                queryWrapper.orderByDesc(BizTag::getUseCount);
                break;
        }

        // 使用分页API
        Page<BizTag> pageParam = new Page<>(page, size);
        IPage<BizTag> tagPage = tagMapper.selectPage(pageParam, queryWrapper);

        // 转换为VO
        IPage<TagVO> voPage = new Page<>(tagPage.getCurrent(), tagPage.getSize(), tagPage.getTotal());
        List<TagVO> tagVOList = tagPage.getRecords().stream()
                .map(tag -> convertToVO(tag, null))
                .collect(Collectors.toList());
        voPage.setRecords(tagVOList);

        return voPage;
    }

    @Override
    public TagVO convertToVO(BizTag tag, Long userId) {
        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setDescription(tag.getDescription());
        vo.setIcon(tag.getIcon());
        vo.setColor(tag.getColor());
        vo.setCategory(tag.getCategory());
        vo.setQuestionCount(tag.getUseCount());

        // 检查是否已关注
        if (userId != null) {
            LambdaQueryWrapper<BizUserTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizUserTag::getUserId, userId).eq(BizUserTag::getTagId, tag.getId());
            boolean isFollowed = userTagMapper.selectCount(wrapper) > 0;
            vo.setFollowCount(userTagMapper
                    .selectCount(new LambdaQueryWrapper<BizUserTag>().eq(BizUserTag::getTagId, tag.getId()))
                    .intValue());
            vo.setIsFollowed(isFollowed);
        } else {
            vo.setFollowCount(0);
            vo.setIsFollowed(false);
        }

        vo.setCreateTime(tag.getCreateTime().toString());
        vo.setUpdateTime(tag.getUpdateTime().toString());
        return vo;
    }

    @Override
    public boolean isFollowed(Long userId, Long tagId) {
        if (userId == null || tagId == null) {
            return false;
        }
        LambdaQueryWrapper<BizUserTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizUserTag::getUserId, userId)
               .eq(BizUserTag::getTagId, tagId);
        return userTagMapper.selectCount(wrapper) > 0;
    }

    @Override
    public int getQuestionCountByTagId(Long tagId) {
        if (tagId == null) {
            return 0;
        }
        BizTag tag = tagMapper.selectById(tagId);
        return tag != null && tag.getUseCount() != null ? tag.getUseCount() : 0;
    }
}