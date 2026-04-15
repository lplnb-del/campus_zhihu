package com.campus.zhihu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.zhihu.dto.AnswerPublishDTO;
import com.campus.zhihu.dto.AnswerQueryDTO;
import com.campus.zhihu.dto.AnswerUpdateDTO;
import com.campus.zhihu.entity.BizAnswer;
import com.campus.zhihu.vo.AnswerVO;

import java.util.List;

/**
 * 回答服务接口
 * 提供回答相关的业务逻辑处理
 *
 * @author CampusZhihu Team
 */
public interface AnswerService {

    /**
     * 发布回答
     *
     * @param userId     用户ID
     * @param publishDTO 回答发布DTO
     * @return 回答VO
     */
    AnswerVO publishAnswer(Long userId, AnswerPublishDTO publishDTO);

    /**
     * 更新回答
     *
     * @param userId    用户ID
     * @param answerId  回答ID
     * @param updateDTO 回答更新DTO
     * @return 回答VO
     */
    AnswerVO updateAnswer(Long userId, Long answerId, AnswerUpdateDTO updateDTO);

    /**
     * 删除回答（逻辑删除）
     *
     * @param userId   用户ID
     * @param answerId 回答ID
     * @return 是否成功
     */
    Boolean deleteAnswer(Long userId, Long answerId);

    /**
     * 根据ID获取回答详情
     *
     * @param answerId 回答ID
     * @param userId   当前用户ID（可为空，用于判断是否点赞）
     * @return 回答VO
     */
    AnswerVO getAnswerById(Long answerId, Long userId);

    /**
     * 分页查询回答列表
     *
     * @param queryDTO 查询条件
     * @param userId   当前用户ID（可为空）
     * @return 分页结果
     */
    IPage<AnswerVO> getAnswerPage(AnswerQueryDTO queryDTO, Long userId);

    /**
     * 根据问题ID查询回答列表
     *
     * @param questionId 问题ID
     * @param userId     当前用户ID（可为空）
     * @return 回答列表
     */
    List<AnswerVO> getAnswersByQuestionId(Long questionId, Long userId);

    /**
     * 根据问题ID分页查询回答列表
     *
     * @param questionId 问题ID
     * @param page       页码
     * @param size       每页大小
     * @param userId     当前用户ID（可为空）
     * @return 分页结果
     */
    IPage<AnswerVO> getAnswersByQuestionIdPage(Long questionId, Integer page, Integer size, Long userId);

    /**
     * 根据用户ID查询回答列表
     *
     * @param userId        用户ID
     * @param currentUserId 当前用户ID（可为空）
     * @return 回答列表
     */
    List<AnswerVO> getAnswersByUserId(Long userId, Long currentUserId);

    /**
     * 搜索回答
     *
     * @param keyword  搜索关键词
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param userId   当前用户ID（可为空）
     * @return 分页结果
     */
    IPage<AnswerVO> searchAnswers(String keyword, Integer pageNum, Integer pageSize, Long userId);

    /**
     * 采纳回答
     *
     * @param userId     用户ID（问题作者）
     * @param answerId   回答ID
     * @param questionId 问题ID
     * @return 是否成功
     */
    Boolean acceptAnswer(Long userId, Long answerId, Long questionId);

    /**
     * 取消采纳回答
     *
     * @param userId     用户ID（问题作者）
     * @param answerId   回答ID
     * @param questionId 问题ID
     * @return 是否成功
     */
    Boolean cancelAcceptAnswer(Long userId, Long answerId, Long questionId);

    /**
     * 更新点赞次数
     *
     * @param answerId 回答ID
     * @param delta    变化量（正数为增加，负数为减少）
     * @return 是否成功
     */
    Boolean updateLikeCount(Long answerId, Integer delta);

    /**
     * 更新评论次数
     *
     * @param answerId 回答ID
     * @param delta    变化量（正数为增加，负数为减少）
     * @return 是否成功
     */
    Boolean updateCommentCount(Long answerId, Integer delta);

    /**
     * 统计用户的回答数量
     *
     * @param userId 用户ID
     * @return 回答数量
     */
    Integer countAnswersByUserId(Long userId);

    /**
     * 统计问题的回答数量
     *
     * @param questionId 问题ID
     * @return 回答数量
     */
    Integer countAnswersByQuestionId(Long questionId);

    /**
     * 获取用户的最新回答列表
     *
     * @param userId        用户ID
     * @param limit         数量限制
     * @param currentUserId 当前用户ID（可为空）
     * @return 最新回答列表
     */
    List<AnswerVO> getLatestAnswersByUserId(Long userId, Integer limit, Long currentUserId);

    /**
     * 获取用户的热门回答列表
     *
     * @param userId        用户ID
     * @param limit         数量限制
     * @param currentUserId 当前用户ID（可为空）
     * @return 热门回答列表
     */
    List<AnswerVO> getHotAnswersByUserId(Long userId, Integer limit, Long currentUserId);

    /**
     * 获取问题的被采纳回答
     *
     * @param questionId 问题ID
     * @param userId     当前用户ID（可为空）
     * @return 被采纳的回答
     */
    AnswerVO getAcceptedAnswer(Long questionId, Long userId);

    /**
     * 获取高赞回答列表（全站）
     *
     * @param limit  数量限制
     * @param userId 当前用户ID（可为空）
     * @return 高赞回答列表
     */
    List<AnswerVO> getTopAnswers(Integer limit, Long userId);

    /**
     * 检查用户是否已回答该问题
     *
     * @param questionId 问题ID
     * @param userId     用户ID
     * @return 是否已回答
     */
    Boolean checkUserAnswered(Long questionId, Long userId);

    /**
     * 根据实体转换为VO
     *
     * @param answer 回答实体
     * @param userId 当前用户ID（可为空）
     * @return 回答VO
     */
    AnswerVO convertToVO(BizAnswer answer, Long userId);

    /**
     * 批量转换为VO
     *
     * @param answers 回答实体列表
     * @param userId  当前用户ID（可为空）
     * @return 回答VO列表
     */
    List<AnswerVO> convertToVOList(List<BizAnswer> answers, Long userId);
}
