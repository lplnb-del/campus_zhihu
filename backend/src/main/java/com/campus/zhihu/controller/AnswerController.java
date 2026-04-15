package com.campus.zhihu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.AnswerPublishDTO;
import com.campus.zhihu.dto.AnswerQueryDTO;
import com.campus.zhihu.dto.AnswerUpdateDTO;
import com.campus.zhihu.service.AnswerService;
import com.campus.zhihu.vo.AnswerVO;
import com.campus.zhihu.vo.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 回答控制器
 * 处理回答相关的 HTTP 请求
 *
 * @author CampusZhihu Team
 */
@Slf4j
@RestController
@RequestMapping("/answer")
@RequiredArgsConstructor
@Tag(name = "回答管理", description = "回答的发布、查询、编辑、删除、采纳")
public class AnswerController extends BaseController {

    private final AnswerService answerService;

    /**
     * 发布回答
     *
     * @param publishDTO 回答发布信息
     * @param request    HTTP 请求
     * @return 回答详情
     */
    @Operation(summary = "发布回答", description = "发布新回答，支持Markdown和图片上传")
    @PostMapping("/publish")
    public Result<AnswerVO> publishAnswer(
        @Validated @RequestBody AnswerPublishDTO publishDTO,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("用户发布回答: userId={}, questionId={}", userId, publishDTO.getQuestionId());
        AnswerVO answerVO = answerService.publishAnswer(userId, publishDTO);
        return Result.success("回答发布成功", answerVO);
    }

    /**
     * 更新回答
     *
     * @param answerId  回答ID
     * @param updateDTO 回答更新信息
     * @param request   HTTP 请求
     * @return 回答详情
     */
    @PutMapping("/{answerId}")
    public Result<AnswerVO> updateAnswer(
        @PathVariable Long answerId,
        @Validated @RequestBody AnswerUpdateDTO updateDTO,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("用户更新回答: userId={}, answerId={}", userId, answerId);
        AnswerVO answerVO = answerService.updateAnswer(userId, answerId, updateDTO);
        return Result.success("回答更新成功", answerVO);
    }

    /**
     * 删除回答
     *
     * @param answerId 回答ID
     * @param request  HTTP 请求
     * @return 操作结果
     */
    @DeleteMapping("/{answerId}")
    public Result<Boolean> deleteAnswer(
        @PathVariable Long answerId,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("用户删除回答: userId={}, answerId={}", userId, answerId);
        Boolean result = answerService.deleteAnswer(userId, answerId);
        return Result.success("回答删除成功", result);
    }

    /**
     * 获取回答详情
     *
     * @param answerId 回答ID
     * @param request  HTTP 请求
     * @return 回答详情
     */
    @GetMapping("/{answerId}")
    public Result<AnswerVO> getAnswerById(
        @PathVariable Long answerId,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取回答详情: answerId={}", answerId);
        AnswerVO answerVO = answerService.getAnswerById(answerId, userId);
        return Result.success(answerVO);
    }

    /**
     * 分页查询回答列表
     *
     * @param queryDTO 查询条件
     * @param request  HTTP 请求
     * @return 分页结果
     */
    @PostMapping("/page")
    public Result<IPage<AnswerVO>> getAnswerPage(
        @RequestBody AnswerQueryDTO queryDTO,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("分页查询回答: queryDTO={}", queryDTO);
        IPage<AnswerVO> page = answerService.getAnswerPage(queryDTO, userId);
        return Result.success(page);
    }

    /**
     * 搜索回答
     *
     * @param keyword  搜索关键词
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param request  HTTP 请求
     * @return 分页结果
     */
    @GetMapping("/search")
    public Result<IPage<AnswerVO>> searchAnswers(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("搜索回答: keyword={}", keyword);
        IPage<AnswerVO> page = answerService.searchAnswers(keyword, pageNum, pageSize, userId);
        return Result.success(page);
    }

    /**
     * 获取问题的回答列表（支持分页）
     *
     * @param questionId 问题ID
     * @param page       页码（默认1）
     * @param size       每页大小（默认20）
     * @param request    HTTP 请求
     * @return 分页结果
     */
    @GetMapping("/question/{questionId}")
    public Result<PageResponse<AnswerVO>> getAnswersByQuestion(
        @PathVariable Long questionId,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "20") Integer size,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取问题的回答列表: questionId={}, page={}, size={}", questionId, page, size);
        IPage<AnswerVO> answers = answerService.getAnswersByQuestionIdPage(questionId, page, size, userId);
        PageResponse<AnswerVO> response = PageResponse.of(answers);
        return Result.success(response);
    }

    /**
     * 获取用户的回答列表
     *
     * @param userId  用户ID
     * @param request HTTP 请求
     * @return 回答列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<AnswerVO>> getUserAnswers(
        @PathVariable Long userId,
        HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserIdOrNull(request);
        log.info("获取用户的回答列表: userId={}", userId);
        List<AnswerVO> answers = answerService.getAnswersByUserId(userId, currentUserId);
        return Result.success(answers);
    }

    /**
     * 获取当前用户的回答列表
     *
     * @param request HTTP 请求
     * @return 回答列表
     */
    @GetMapping("/my")
    public Result<PageResponse<AnswerVO>> getMyAnswers(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        log.info("获取我的回答列表: userId={}", userId);
        List<AnswerVO> answers = answerService.getAnswersByUserId(userId, userId);
        PageResponse<AnswerVO> response = PageResponse.of(answers, (long) answers.size(), 1L, (long) answers.size());
        return Result.success(response);
    }

    /**
     * 采纳回答
     *
     * @param answerId   回答ID
     * @param questionId 问题ID
     * @param request    HTTP 请求
     * @return 操作结果
     */
    @Operation(summary = "采纳回答", description = "问题创建者采纳最佳回答，并给予悬赏积分")
    @PutMapping("/{answerId}/accept")
    public Result<Boolean> acceptAnswer(
            @Parameter(description = "回答ID", required = true, example = "1")
            @PathVariable Long answerId,
            @Parameter(description = "问题ID", required = true, example = "1")
            @RequestParam Long questionId,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("采纳回答: userId={}, answerId={}, questionId={}", userId, answerId, questionId);
        Boolean result = answerService.acceptAnswer(userId, answerId, questionId);
        return Result.success("回答已采纳", result);
    }

    /**
     * 取消采纳回答
     *
     * @param answerId   回答ID
     * @param questionId 问题ID
     * @param request    HTTP 请求
     * @return 操作结果
     */
    @PutMapping("/{answerId}/cancel-accept")
    public Result<Boolean> cancelAcceptAnswer(
        @PathVariable Long answerId,
        @RequestParam Long questionId,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("取消采纳回答: userId={}, answerId={}, questionId={}", userId, answerId, questionId);
        Boolean result = answerService.cancelAcceptAnswer(userId, answerId, questionId);
        return Result.success("已取消采纳", result);
    }

    /**
     * 获取问题的被采纳回答
     *
     * @param questionId 问题ID
     * @param request    HTTP 请求
     * @return 被采纳的回答
     */
    @GetMapping("/question/{questionId}/accepted")
    public Result<AnswerVO> getAcceptedAnswer(
        @PathVariable Long questionId,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取问题的被采纳回答: questionId={}", questionId);
        AnswerVO answerVO = answerService.getAcceptedAnswer(questionId, userId);
        return Result.success(answerVO);
    }

    /**
     * 获取用户的最新回答
     *
     * @param userId  用户ID
     * @param limit   数量限制
     * @param request HTTP 请求
     * @return 最新回答列表
     */
    @GetMapping("/user/{userId}/latest")
    public Result<List<AnswerVO>> getLatestAnswers(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "10") Integer limit,
        HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserIdOrNull(request);
        log.info("获取用户最新回答: userId={}, limit={}", userId, limit);
        List<AnswerVO> answers = answerService.getLatestAnswersByUserId(userId, limit, currentUserId);
        return Result.success(answers);
    }

    /**
     * 获取用户的热门回答
     *
     * @param userId  用户ID
     * @param limit   数量限制
     * @param request HTTP 请求
     * @return 热门回答列表
     */
    @GetMapping("/user/{userId}/hot")
    public Result<List<AnswerVO>> getHotAnswers(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "10") Integer limit,
        HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserIdOrNull(request);
        log.info("获取用户热门回答: userId={}, limit={}", userId, limit);
        List<AnswerVO> answers = answerService.getHotAnswersByUserId(userId, limit, currentUserId);
        return Result.success(answers);
    }

    /**
     * 获取全站高赞回答
     *
     * @param limit   数量限制
     * @param request HTTP 请求
     * @return 高赞回答列表
     */
    @GetMapping("/top")
    public Result<List<AnswerVO>> getTopAnswers(
        @RequestParam(defaultValue = "10") Integer limit,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserIdOrNull(request);
        log.info("获取全站高赞回答: limit={}", limit);
        List<AnswerVO> answers = answerService.getTopAnswers(limit, userId);
        return Result.success(answers);
    }

    /**
     * 统计用户的回答数量
     *
     * @param userId 用户ID
     * @return 回答数量
     */
    @GetMapping("/count/user/{userId}")
    public Result<Integer> countUserAnswers(@PathVariable Long userId) {
        log.info("统计用户回答数量: userId={}", userId);
        Integer count = answerService.countAnswersByUserId(userId);
        return Result.success(count);
    }

    /**
     * 统计问题的回答数量
     *
     * @param questionId 问题ID
     * @return 回答数量
     */
    @GetMapping("/count/question/{questionId}")
    public Result<Integer> countQuestionAnswers(@PathVariable Long questionId) {
        log.info("统计问题回答数量: questionId={}", questionId);
        Integer count = answerService.countAnswersByQuestionId(questionId);
        return Result.success(count);
    }

    /**
     * 检查用户是否已回答该问题
     *
     * @param questionId 问题ID
     * @param request    HTTP 请求
     * @return 是否已回答
     */
    @GetMapping("/check-answered")
    public Result<Boolean> checkUserAnswered(
        @RequestParam Long questionId,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        log.info("检查用户是否已回答: userId={}, questionId={}", userId, questionId);
        Boolean answered = answerService.checkUserAnswered(questionId, userId);
        return Result.success(answered);
    }
}
