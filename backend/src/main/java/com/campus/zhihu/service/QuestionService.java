package com.campus.zhihu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.zhihu.dto.QuestionPublishDTO;
import com.campus.zhihu.dto.QuestionQueryDTO;
import com.campus.zhihu.dto.QuestionUpdateDTO;
import com.campus.zhihu.entity.BizQuestion;
import com.campus.zhihu.vo.QuestionVO;

import java.util.List;

/**
 * 问题服务接口
 * 提供问题相关的业务逻辑处理
 *
 * @author CampusZhihu Team
 */
public interface QuestionService {

    /**
     * 发布问题
     *
     * @param userId     用户ID
     * @param publishDTO 问题发布DTO
     * @return 问题VO
     */
    QuestionVO publishQuestion(Long userId, QuestionPublishDTO publishDTO);

    /**
     * 更新问题
     *
     * @param userId    用户ID
     * @param questionId 问题ID
     * @param updateDTO 问题更新DTO
     * @return 问题VO
     */
    QuestionVO updateQuestion(Long userId, Long questionId, QuestionUpdateDTO updateDTO);

    /**
     * 删除问题（逻辑删除）
     *
     * @param userId     用户ID
     * @param questionId 问题ID
     * @return 是否成功
     */
    Boolean deleteQuestion(Long userId, Long questionId);

    /**
     * 根据ID获取问题详情（自动增加浏览次数）
     *
     * @param questionId 问题ID
     * @param userId     当前用户ID（可为空，用于判断是否点赞/收藏）
     * @return 问题VO
     */
    QuestionVO getQuestionById(Long questionId, Long userId);

    /**
     * 根据ID获取问题详情（不增加浏览次数）
     *
     * @param questionId 问题ID
     * @param userId     当前用户ID（可为空）
     * @return 问题VO
     */
    QuestionVO getQuestionByIdWithoutView(Long questionId, Long userId);

    /**
     * 分页查询问题列表
     *
     * @param queryDTO 查询条件
     * @param userId   当前用户ID（可为空）
     * @return 分页结果
     */
    IPage<QuestionVO> getQuestionPage(QuestionQueryDTO queryDTO, Long userId);

    /**
     * 根据用户ID查询问题列表
     *
     * @param userId 用户ID
     * @return 问题列表
     */
    List<QuestionVO> getQuestionsByUserId(Long userId);

    /**
     * 根据标签ID查询问题列表
     *
     * @param tagId  标签ID
     * @param userId 当前用户ID（可为空）
     * @return 问题列表
     */
    List<QuestionVO> getQuestionsByTagId(Long tagId, Long userId);

    /**
     * 根据标签ID分页查询问题列表
     *
     * @param tagId  标签ID
     * @param page   页码
     * @param size   每页大小
     * @param orderBy 排序方式：hot-按热度，latest-按最新，unsolved-待解决，reward-有悬赏
     * @param userId 当前用户ID（可为空）
     * @return 分页结果
     */
    IPage<QuestionVO> getQuestionsByTag(Long tagId, Integer page, Integer size, String orderBy, Long userId);

    /**
     * 搜索问题
     *
     * @param keyword 搜索关键词
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId  当前用户ID（可为空）
     * @return 分页结果
     */
    IPage<QuestionVO> searchQuestions(String keyword, Integer pageNum, Integer pageSize, Long userId);

    /**
     * 获取热门问题列表
     *
     * @param limit  数量限制
     * @param userId 当前用户ID（可为空）
     * @return 热门问题列表
     */
    List<QuestionVO> getHotQuestions(Integer limit, Long userId);

    /**
     * 获取最新问题列表
     *
     * @param limit  数量限制
     * @param userId 当前用户ID（可为空）
     * @return 最新问题列表
     */
    List<QuestionVO> getLatestQuestions(Integer limit, Long userId);

    /**
     * 获取待解决问题列表
     *
     * @param limit  数量限制
     * @param userId 当前用户ID（可为空）
     * @return 待解决问题列表
     */
    List<QuestionVO> getUnsolvedQuestions(Integer limit, Long userId);

    /**
     * 获取有悬赏的问题列表
     *
     * @param minRewardPoints 最小悬赏积分
     * @param limit           数量限制
     * @param userId          当前用户ID（可为空）
     * @return 有悬赏的问题列表
     */
    List<QuestionVO> getRewardQuestions(Integer minRewardPoints, Integer limit, Long userId);

    /**
     * 获取推荐问题列表（基于用户关注的标签）
     *
     * @param userId 用户ID
     * @param limit  数量限制
     * @return 推荐问题列表
     */
    List<QuestionVO> getRecommendedQuestions(Long userId, Integer limit);

    /**
     * 获取相关问题列表（基于标签相似度）
     *
     * @param questionId 问题ID
     * @param limit      数量限制
     * @param userId     当前用户ID（可为空）
     * @return 相关问题列表
     */
    List<QuestionVO> getRelatedQuestions(Long questionId, Integer limit, Long userId);

    /**
     * 关闭问题
     *
     * @param userId     用户ID
     * @param questionId 问题ID
     * @return 是否成功
     */
    Boolean closeQuestion(Long userId, Long questionId);

    /**
     * 标记问题为已解决
     *
     * @param userId     用户ID
     * @param questionId 问题ID
     * @return 是否成功
     */
    Boolean markQuestionAsResolved(Long userId, Long questionId);

    /**
     * 增加问题浏览次数
     *
     * @param questionId 问题ID
     * @return 是否成功
     */
    Boolean incrementViewCount(Long questionId);

    /**
     * 更新问题回答数量
     *
     * @param questionId 问题ID
     * @param delta      变化量（正数为增加，负数为减少）
     * @return 是否成功
     */
    Boolean updateAnswerCount(Long questionId, Integer delta);

    /**
     * 更新问题收藏次数
     *
     * @param questionId 问题ID
     * @param delta      变化量（正数为增加，负数为减少）
     * @return 是否成功
     */
    Boolean updateCollectCount(Long questionId, Integer delta);

    /**
     * 更新问题点赞次数
     *
     * @param questionId 问题ID
     * @param delta      变化量（正数为增加，负数为减少）
     * @return 是否成功
     */
    Boolean updateLikeCount(Long questionId, Integer delta);

    /**
     * 统计某个用户的问题数量
     *
     * @param userId 用户ID
     * @return 问题数量
     */
    Integer countQuestionsByUserId(Long userId);

    /**
     * 统计问题总数
     *
     * @param userId 用户ID（可为空）
     * @return 问题数量
     */
    Long countQuestions(Long userId);

    /**
     * 统计某个状态的问题数量
     *
     * @param status 状态
     * @return 问题数量
     */
    Integer countQuestionsByStatus(Integer status);

    /**
     * 根据实体转换为VO
     *
     * @param question 问题实体
     * @param userId   当前用户ID（可为空）
     * @return 问题VO
     */
    QuestionVO convertToVO(BizQuestion question, Long userId);

    /**
     * 批量转换为VO
     *
     * @param questions 问题实体列表
     * @param userId    当前用户ID（可为空）
     * @return 问题VO列表
     */
    List<QuestionVO> convertToVOList(List<BizQuestion> questions, Long userId);
}
