package com.campus.zhihu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.QuestionPublishDTO;
import com.campus.zhihu.dto.QuestionQueryDTO;
import com.campus.zhihu.dto.QuestionUpdateDTO;
import com.campus.zhihu.service.QuestionService;
import com.campus.zhihu.vo.PageResponse;
import com.campus.zhihu.vo.QuestionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 问题控制器
 * 处理问题相关的 HTTP 请求
 *
 * @author CampusZhihu Team
 */
@Slf4j
@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
@Tag(name = "问题管理", description = "问题的发布、查询、编辑、删除")
public class QuestionController extends BaseController {

    private final QuestionService questionService;

    /**
     * 发布问题
     *
     * @param publishDTO 问题发布信息
     * @param request    HTTP 请求
     * @return 问题详情
     */
    @Operation(summary = "发布问题", description = "发布新问题，支持Markdown和图片上传")
    @PostMapping("/publish")
    public Result<QuestionVO> publishQuestion(@Validated @RequestBody QuestionPublishDTO publishDTO,
                                             HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        log.info("用户发布问题: userId={}, title={}", userId, publishDTO.getTitle());
        QuestionVO questionVO = questionService.publishQuestion(userId, publishDTO);
        return Result.success("问题发布成功", questionVO);
    }

    /**
     * 更新问题
     *
     * @param questionId 问题ID
     * @param updateDTO  问题更新信息
     * @param request    HTTP 请求
     * @return 问题详情
     */
    @PutMapping("/{questionId}")
    public Result<QuestionVO> updateQuestion(@PathVariable Long questionId,
                                              @Validated @RequestBody QuestionUpdateDTO updateDTO,
                                              HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        log.info("用户更新问题: userId={}, questionId={}", userId, questionId);
        QuestionVO questionVO = questionService.updateQuestion(userId, questionId, updateDTO);
        return Result.success("问题更新成功", questionVO);
    }

    /**
     * 删除问题
     *
     * @param questionId 问题ID
     * @param request    HTTP 请求
     * @return 操作结果
     */
    @DeleteMapping("/{questionId}")
    public Result<Boolean> deleteQuestion(@PathVariable Long questionId,
                                           HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        log.info("用户删除问题: userId={}, questionId={}", userId, questionId);
        Boolean result = questionService.deleteQuestion(userId, questionId);
        return Result.success("问题删除成功", result);
    }

    /**
     * 获取问题详情
     *
     * @param questionId 问题ID
     * @param request    HTTP 请求
     * @return 问题详情
     */
    @Operation(summary = "获取问题详情", description = "根据问题ID获取问题详情，包含回答列表")
    @GetMapping("/{questionId}")
    public Result<QuestionVO> getQuestionById(
            @Parameter(description = "问题ID", required = true, example = "1")
            @PathVariable Long questionId,
            HttpServletRequest request) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取问题详情: questionId={}, userId={}", questionId, userId);
        QuestionVO questionVO = questionService.getQuestionById(questionId, userId);
        return Result.success(questionVO);
    }

    /**
     * 获取问题详情（不增加浏览次数，用于编辑）
     *
     * @param questionId 问题ID
     * @param request    HTTP 请求
     * @return 问题详情
     */
    @GetMapping("/{questionId}/detail")
    public Result<QuestionVO> getQuestionDetail(@PathVariable Long questionId,
                                                 HttpServletRequest request) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取问题详情（编辑）: questionId={}, userId={}", questionId, userId);
        QuestionVO questionVO = questionService.getQuestionByIdWithoutView(questionId, userId);
        return Result.success(questionVO);
    }

    /**
     * 分页查询问题列表
     *
     * @param queryDTO 查询条件
     * @param request  HTTP 请求
     * @return 分页结果
     */
    @PostMapping("/page")
    public Result<IPage<QuestionVO>> getQuestionPage(@RequestBody QuestionQueryDTO queryDTO,
                                                      HttpServletRequest request) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("分页查询问题: queryDTO={}, userId={}", queryDTO, userId);
        IPage<QuestionVO> page = questionService.getQuestionPage(queryDTO, userId);
        return Result.success(page);
    }

    /**
     * 获取问题列表（分页，GET方式）
     *
     * @param keyword  搜索关键词
     * @param tagId    标签ID
     * @param userId   用户ID
     * @param status   状态
     * @param orderBy  排序字段
     * @param page     页码
     * @param size     每页大小
     * @param request  HTTP 请求
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResponse<QuestionVO>> getQuestionList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderBy,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long currentUserId = getCurrentUserIdOrNull(request);
        log.info("获取问题列表: keyword={}, tagId={}, userId={}, status={}, orderBy={}, page={}, size={}",
                 keyword, tagId, userId, status, orderBy, page, size);

        QuestionQueryDTO queryDTO = new QuestionQueryDTO();
        queryDTO.setKeyword(keyword);
        queryDTO.setTagId(tagId);
        queryDTO.setUserId(userId);
        queryDTO.setStatus(status);
        queryDTO.setPageNum(page);
        queryDTO.setPageSize(size);

        // 处理orderBy参数
        if (orderBy != null) {
            switch (orderBy) {
                case "latest":
                    queryDTO.setOrderBy("create_time");
                    queryDTO.setOrderDirection("desc");
                    break;
                case "hot":
                    queryDTO.setOrderBy("view_count");
                    queryDTO.setOrderDirection("desc");
                    break;
                case "reward":
                    queryDTO.setOrderBy("reward_points");
                    queryDTO.setOrderDirection("desc");
                    break;
                case "unsolved":
                    queryDTO.setIsResolved(0);
                    queryDTO.setOrderBy("reward_points");
                    queryDTO.setOrderDirection("desc");
                    break;
            }
        }

        IPage<QuestionVO> pageResult = questionService.getQuestionPage(queryDTO, currentUserId);
        PageResponse<QuestionVO> response = PageResponse.of(pageResult);
        return Result.success(response);
    }

    /**
     * 搜索问题
     *
     * @param keyword  搜索关键词
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param request  HTTP 请求
     * @return 分页结果
     */
    @GetMapping("/search")
    public Result<IPage<QuestionVO>> searchQuestions(@RequestParam String keyword,
                                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                                      HttpServletRequest request) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("搜索问题: keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);
        IPage<QuestionVO> page = questionService.searchQuestions(keyword, pageNum, pageSize, userId);
        return Result.success(page);
    }

    /**
     * 根据标签ID获取问题列表
     *
     * @param tagId    标签ID
     * @param page     页码
     * @param size     每页大小
     * @param orderBy  排序方式：hot-按热度，latest-按最新，unsolved-待解决，reward-有悬赏
     * @param request  HTTP 请求
     * @return 问题列表
     */
    @Operation(summary = "根据标签获取问题", description = "根据标签ID获取问题列表，支持多种排序方式")
    @GetMapping("/tag/{tagId}")
    public Result<PageResponse<QuestionVO>> getQuestionsByTag(
            @Parameter(description = "标签ID", required = true, example = "1")
            @PathVariable Long tagId,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "排序方式：hot-按热度，latest-按最新，unsolved-待解决，reward-有悬赏", example = "hot")
            @RequestParam(defaultValue = "hot") String orderBy,
            HttpServletRequest request) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("根据标签获取问题: tagId={}, page={}, size={}, orderBy={}", tagId, page, size, orderBy);
        IPage<QuestionVO> questionPage = questionService.getQuestionsByTag(tagId, page, size, orderBy, userId);
        PageResponse<QuestionVO> response = PageResponse.of(questionPage);
        return Result.success(response);
    }

    /**
     * 获取热门问题列表
     *
     * @param page    页码
     * @param size    每页大小
     * @param request HTTP 请求
     * @return 热门问题列表
     */
    @GetMapping("/hot")
    public Result<PageResponse<QuestionVO>> getHotQuestions(@RequestParam(defaultValue = "1") Integer page,
                                                             @RequestParam(defaultValue = "20") Integer size,
                                                             HttpServletRequest request) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取热门问题: page={}, size={}", page, size);
        List<QuestionVO> questions = questionService.getHotQuestions(size, userId);
        Long total = questionService.countQuestions(userId);
        PageResponse<QuestionVO> response = PageResponse.of(questions, total, (long) page, (long) size);
        return Result.success(response);
    }

    /**
     * 获取最新问题列表
     *
     * @param page    页码
     * @param size    每页大小
     * @param request HTTP 请求
     * @return 最新问题列表
     */
    @GetMapping("/latest")
    public Result<PageResponse<QuestionVO>> getLatestQuestions(@RequestParam(defaultValue = "1") Integer page,
                                                                @RequestParam(defaultValue = "20") Integer size,
                                                                HttpServletRequest request) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取最新问题: page={}, size={}", page, size);
        List<QuestionVO> questions = questionService.getLatestQuestions(size, userId);
        Long total = questionService.countQuestions(userId);
        PageResponse<QuestionVO> response = PageResponse.of(questions, total, (long) page, (long) size);
        return Result.success(response);
    }

    /**
     * 获取待解决问题列表
     *
     * @param page    页码
     * @param size    每页大小
     * @param request HTTP 请求
     * @return 待解决问题列表
     */
    @GetMapping("/unsolved")
    public Result<PageResponse<QuestionVO>> getUnsolvedQuestions(@RequestParam(defaultValue = "1") Integer page,
                                                                  @RequestParam(defaultValue = "20") Integer size,
                                                                  HttpServletRequest request) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取待解决问题: page={}, size={}", page, size);
        List<QuestionVO> questions = questionService.getUnsolvedQuestions(size, userId);
        Long total = questionService.countQuestions(userId);
        PageResponse<QuestionVO> response = PageResponse.of(questions, total, (long) page, (long) size);
        return Result.success(response);
    }

    /**
     * 获取有悬赏的问题列表
     *
     * @param page            页码
     * @param size            每页大小
     * @param request         HTTP 请求
     * @return 有悬赏的问题列表
     */
    @GetMapping("/reward")
    public Result<PageResponse<QuestionVO>> getRewardQuestions(@RequestParam(defaultValue = "1") Integer page,
                                                                @RequestParam(defaultValue = "20") Integer size,
                                                                HttpServletRequest request) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取悬赏问题: page={}, size={}", page, size);
        List<QuestionVO> questions = questionService.getRewardQuestions(0, size, userId);
        Long total = questionService.countQuestions(userId);
        PageResponse<QuestionVO> response = PageResponse.of(questions, total, (long) page, (long) size);
        return Result.success(response);
    }

    /**
     * 获取推荐问题列表（基于用户关注的标签）
     *
     * @param limit   数量限制
     * @param request HTTP 请求
     * @return 推荐问题列表
     */
    @GetMapping("/recommended")
    public Result<List<QuestionVO>> getRecommendedQuestions(@RequestParam(defaultValue = "10") Integer limit,
                                                             HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        log.info("获取推荐问题: userId={}, limit={}", userId, limit);
        List<QuestionVO> questions = questionService.getRecommendedQuestions(userId, limit);
        return Result.success(questions);
    }

    /**
     * 获取相关问题列表（基于标签相似度）
     *
     * @param questionId 问题ID
     * @param limit      数量限制
     * @param request    HTTP 请求
     * @return 相关问题列表
     */
    @GetMapping("/{questionId}/related")
    public Result<List<QuestionVO>> getRelatedQuestions(@PathVariable Long questionId,
                                                         @RequestParam(defaultValue = "5") Integer limit,
                                                         HttpServletRequest request) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取相关问题: questionId={}, limit={}", questionId, limit);
        List<QuestionVO> questions = questionService.getRelatedQuestions(questionId, limit, userId);
        return Result.success(questions);
    }

    /**
     * 获取用户的问题列表
     *
     * @param userId  用户ID
     * @param request HTTP 请求
     * @return 问题列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<QuestionVO>> getUserQuestions(@PathVariable Long userId,
                                                      HttpServletRequest request) {
        Long currentUserId = getCurrentUserIdOrNull(request);
        log.info("获取用户问题列表: userId={}", userId);
        List<QuestionVO> questions = questionService.getQuestionsByUserId(userId);
        return Result.success(questions);
    }

    /**
     * 获取当前登录用户的问题列表
     *
     * @param request HTTP 请求
     * @return 问题列表
     */
    @GetMapping("/my")
    public Result<PageResponse<QuestionVO>> getMyQuestions(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        log.info("获取我的问题列表: userId={}", userId);
        List<QuestionVO> questions = questionService.getQuestionsByUserId(userId);
        PageResponse<QuestionVO> response = PageResponse.of(questions, (long) questions.size(), 1L, (long) questions.size());
        return Result.success(response);
    }

    /**
     * 关闭问题
     *
     * @param questionId 问题ID
     * @param request    HTTP 请求
     * @return 操作结果
     */
    @PutMapping("/{questionId}/close")
    public Result<Boolean> closeQuestion(@PathVariable Long questionId,
                                          HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        log.info("关闭问题: userId={}, questionId={}", userId, questionId);
        Boolean result = questionService.closeQuestion(userId, questionId);
        return Result.success("问题已关闭", result);
    }

    /**
     * 标记问题为已解决
     *
     * @param questionId 问题ID
     * @param request    HTTP 请求
     * @return 操作结果
     */
    @PutMapping("/{questionId}/resolve")
    public Result<Boolean> markQuestionAsResolved(@PathVariable Long questionId,
                                                   HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        log.info("标记问题已解决: userId={}, questionId={}", userId, questionId);
        Boolean result = questionService.markQuestionAsResolved(userId, questionId);
        return Result.success("问题已标记为已解决", result);
    }

    /**
     * 统计用户的问题数量
     *
     * @param userId 用户ID
     * @return 问题数量
     */
    @GetMapping("/count/user/{userId}")
    public Result<Integer> countUserQuestions(@PathVariable Long userId) {
        log.info("统计用户问题数量: userId={}", userId);
        Integer count = questionService.countQuestionsByUserId(userId);
        return Result.success(count);
    }

    /**
     * 统计某个状态的问题数量
     *
     * @param status 状态
     * @return 问题数量
     */
    @GetMapping("/count/status/{status}")
    public Result<Integer> countQuestionsByStatus(@PathVariable Integer status) {
        log.info("统计问题数量: status={}", status);
        Integer count = questionService.countQuestionsByStatus(status);
        return Result.success(count);
    }
}
