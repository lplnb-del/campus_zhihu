package com.campus.zhihu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.common.Result;
import com.campus.zhihu.entity.BizTag;
import com.campus.zhihu.entity.BizUserTag;
import com.campus.zhihu.mapper.TagMapper;
import com.campus.zhihu.mapper.UserTagMapper;
import com.campus.zhihu.service.TagService;
import com.campus.zhihu.util.JwtUtil;
import com.campus.zhihu.vo.PageResponse;
import com.campus.zhihu.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签控制器
 *
 * @author CampusZhihu Team
 */
@Slf4j
@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
@Tag(name = "标签管理", description = "标签的查询、关注")
public class TagController {

    private final TagService tagService;
    private final TagMapper tagMapper;
    private final UserTagMapper userTagMapper;
    private final JwtUtil jwtUtil;

    /**
     * 获取标签详情
     *
     * @param id      标签ID
     * @param request HTTP请求
     * @return 标签详情
     */
    @Operation(summary = "获取标签详情", description = "根据ID获取标签详细信息")
    @GetMapping("/{id}")
    public Result<TagVO> getTagById(
            @Parameter(description = "标签ID", example = "1")
            @PathVariable Long id,
            HttpServletRequest request) {
        log.info("获取标签详情：id={}", id);

        Long userId = getUserIdFromRequest(request);
        TagVO tagVO = tagService.convertToVO(tagService.getById(id), userId);
        return Result.success(tagVO);
    }

    /**
     * 获取热门标签
     *
     * @param limit   返回数量限制
     * @param request HTTP请求
     * @return 标签列表
     */
    @Operation(summary = "获取热门标签", description = "获取使用次数最多的标签列表")
    @GetMapping("/hot")
    public Result<List<TagVO>> getHotTags(
            @Parameter(description = "返回数量限制", example = "20")
            @RequestParam(defaultValue = "20") Integer limit,
            HttpServletRequest request) {
        // 参数验证：限制最大值为100，防止恶意请求
        limit = Math.min(Math.max(limit, 1), 100);
        log.info("获取热门标签，limit={}", limit);

        List<TagVO> tagVOList = tagService.getHotTags(limit);
        return Result.success(tagVOList);
    }

    /**
     * 获取所有标签
     *
     * @param request HTTP请求
     * @return 标签列表
     */
    @GetMapping("/all")
    public Result<List<TagVO>> getAllTags(HttpServletRequest request) {
        log.info("获取所有标签");

        List<TagVO> tagVOList = tagService.getAllTags();
        return Result.success(tagVOList);
    }

    /**
     * 获取标签列表（分页）
     *
     * @param page     页码
     * @param size     每页大小
     * @param category 分类
     * @param sortBy   排序方式：hot-按热度，new-按最新，name-按名称
     * @param request  HTTP请求
     * @return 标签列表
     */
    @GetMapping("/list")
    public Result<PageResponse<TagVO>> getTagList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "hot") String sortBy,
            HttpServletRequest request) {
        // 参数验证：限制分页参数范围
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 100);

        log.info("获取标签列表，page={}, size={}, category={}, sortBy={}", page, size, category, sortBy);

        IPage<TagVO> voPage = tagService.getTagList(page, size, category, sortBy);
        
        // 转换为前端期望的格式
        PageResponse<TagVO> response = PageResponse.of(voPage);
        return Result.success(response);
    }

    /**
     * 搜索标签
     *
     * @param keyword  搜索关键词
     * @param page     页码
     * @param size     每页大小
     * @param sortBy   排序方式：hot-按热度，new-按最新，name-按名称
     * @param request  HTTP请求
     * @return 标签列表
     */
    @GetMapping("/search")
    public Result<PageResponse<TagVO>> searchTags(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "hot") String sortBy,
            HttpServletRequest request) {
        // 参数验证：限制分页参数范围
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 100);

        log.info("搜索标签，keyword={}, page={}, size={}, sortBy={}", keyword, page, size, sortBy);

        IPage<TagVO> voPage = tagService.searchTags(keyword, page, size, sortBy);
        PageResponse<TagVO> response = PageResponse.of(voPage);
        return Result.success(response);
    }

    /**
     * 获取用户ID（从请求中）
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }

    /**
     * 关注标签
     *
     * @param tagId   标签ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/{tagId}/follow")
    public Result<String> followTag(@PathVariable Long tagId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(403, "请先登录");
        }

        log.info("用户关注标签: userId={}, tagId={}", userId, tagId);

        // 检查是否已关注
        LambdaQueryWrapper<BizUserTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizUserTag::getUserId, userId).eq(BizUserTag::getTagId, tagId);
        if (userTagMapper.selectCount(wrapper) > 0) {
            return Result.error("已关注该标签");
        }

        // 添加关注
        BizUserTag userTag = new BizUserTag();
        userTag.setUserId(userId);
        userTag.setTagId(tagId);
        userTagMapper.insert(userTag);

        return Result.success("关注成功");
    }

    /**
     * 取消关注标签
     *
     * @param tagId   标签ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @DeleteMapping("/{tagId}/follow")
    public Result<String> unfollowTag(@PathVariable Long tagId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(403, "请先登录");
        }

        log.info("用户取消关注标签: userId={}, tagId={}", userId, tagId);

        // 删除关注
        LambdaQueryWrapper<BizUserTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizUserTag::getUserId, userId).eq(BizUserTag::getTagId, tagId);
        userTagMapper.delete(wrapper);

        return Result.success("取消关注成功");
    }

    /**
     * 获取用户关注的标签列表
     *
     * @param userId  用户ID
     * @param request HTTP请求
     * @return 标签列表
     */
    @GetMapping("/user/{userId}/followed")
    public Result<List<TagVO>> getUserFollowedTags(@PathVariable Long userId, HttpServletRequest request) {
        log.info("获取用户关注的标签列表: userId={}", userId);

        // 查询用户关注的标签ID列表
        LambdaQueryWrapper<BizUserTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizUserTag::getUserId, userId);
        List<BizUserTag> userTags = userTagMapper.selectList(wrapper);

        // 如果没有关注任何标签，返回空列表
        if (userTags.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 获取标签详情
        List<Long> tagIds = userTags.stream().map(BizUserTag::getTagId).collect(Collectors.toList());
        List<BizTag> tags = tagMapper.selectBatchIds(tagIds);

        // 转换为VO
        Long currentUserId = getUserIdFromRequest(request);
        List<TagVO> tagVOList = tags.stream()
                .map(tag -> tagService.convertToVO(tag, currentUserId))
                .collect(Collectors.toList());

        return Result.success(tagVOList);
    }

    /**
     * 获取当前用户关注的标签列表
     *
     * @param request HTTP请求
     * @return 标签列表
     */
    @GetMapping("/my/followed")
    public Result<List<TagVO>> getMyFollowedTags(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(403, "请先登录");
        }

        return getUserFollowedTags(userId, request);
    }

    /**
     * 检查当前用户是否已关注指定标签
     *
     * @param tagId 标签ID
     * @param request HTTP请求
     * @return 是否已关注
     */
    @GetMapping("/{tagId}/followed")
    public Result<Boolean> checkFollowed(
            @PathVariable Long tagId,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        boolean followed = false;

        if (userId != null) {
            followed = tagService.isFollowed(userId, tagId);
        }

        return Result.success(followed);
    }

    /**
     * 获取标签下的问题数量
     *
     * @param tagId 标签ID
     * @return 问题数量
     */
    @GetMapping("/{tagId}/question-count")
    public Result<Integer> getQuestionCount(@PathVariable Long tagId) {
        int count = tagService.getQuestionCountByTagId(tagId);
        return Result.success(count);
    }
}