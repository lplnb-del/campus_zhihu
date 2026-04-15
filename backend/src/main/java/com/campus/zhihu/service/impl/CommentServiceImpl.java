package com.campus.zhihu.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.common.ResultCode;
import com.campus.zhihu.dto.CommentPublishDTO;
import com.campus.zhihu.entity.*;
import com.campus.zhihu.mapper.*;
import com.campus.zhihu.service.CommentService;
import com.campus.zhihu.vo.CommentVO;
import com.campus.zhihu.vo.UserVO;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 评论服务实现类
 *
 * @author CampusZhihu Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO publishComment(Long userId, CommentPublishDTO publishDTO) {
        log.info(
            "用户发布评论: userId={}, targetType={}, targetId={}",
            userId,
            publishDTO.getTargetType(),
            publishDTO.getTargetId()
        );

        // 验证用户
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证目标存在
        validateTarget(publishDTO.getTargetType(), publishDTO.getTargetId());

        // 验证父评论（如果是回复）
        if (publishDTO.getParentId() != null && publishDTO.getParentId() > 0) {
            BizComment parentComment = commentMapper.selectById(
                publishDTO.getParentId()
            );
            if (parentComment == null) {
                throw new BusinessException(
                    ResultCode.PARAM_ERROR.getCode(),
                    "父评论不存在"
                );
            }
        }

        // 创建评论
        BizComment comment = new BizComment();
        comment.setUserId(userId);
        comment.setTargetType(publishDTO.getTargetType());
        comment.setTargetId(publishDTO.getTargetId());
        comment.setParentId(
        publishDTO.getParentId() != null ? publishDTO.getParentId() : 0L
        );
        // comment.setReplyToUserId(publishDTO.getReplyToUserId()); // 数据库表中无此字段
        comment.setContent(publishDTO.getContent());
        // comment.setLikeCount(0); // 数据库表中无此字段
        // comment.setStatus(BizComment.STATUS_PUBLISHED); // 数据库表中无此字段
        commentMapper.insert(comment);

        // 更新目标的评论数
        updateTargetCommentCount(
            publishDTO.getTargetType(),
            publishDTO.getTargetId(),
            1
        );

        log.info("评论发布成功: commentId={}", comment.getId());
        return getCommentById(comment.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteComment(Long userId, Long commentId) {
        log.info("用户删除评论: userId={}, commentId={}", userId, commentId);

        BizComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(
                ResultCode.PARAM_ERROR.getCode(),
                "评论不存在"
            );
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(
                ResultCode.FORBIDDEN.getCode(),
                "无权删除此评论"
            );
        }

        commentMapper.deleteById(commentId);
        updateTargetCommentCount(
            comment.getTargetType(),
            comment.getTargetId(),
            -1
        );

        log.info("评论删除成功: commentId={}", commentId);
        return true;
    }

    @Override
    public CommentVO getCommentById(Long commentId, Long userId) {
        BizComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(
                ResultCode.PARAM_ERROR.getCode(),
                "评论不存在"
            );
        }
        return convertToVO(comment, userId);
    }

    @Override
    public List<CommentVO> getCommentsByTarget(
        Integer targetType,
        Long targetId,
        Long userId
    ) {
        log.debug(
            "获取目标的评论: targetType={}, targetId={}",
            targetType,
            targetId
        );

        List<BizComment> topLevelComments = commentMapper.selectTopLevelComments(
            targetType,
            targetId
        );
        List<CommentVO> result = new ArrayList<>();

        for (BizComment comment : topLevelComments) {
            CommentVO vo = convertToVO(comment, userId);
            List<BizComment> replies = commentMapper.selectRepliesByParentId(
                comment.getId()
            );
            vo.setReplies(convertToVOList(replies, userId));
            vo.setReplyCount(replies.size());
            result.add(vo);
        }

        return result;
    }

    @Override
    public List<CommentVO> getTopLevelComments(
        Integer targetType,
        Long targetId,
        Long userId
    ) {
        List<BizComment> comments = commentMapper.selectTopLevelComments(
            targetType,
            targetId
        );
        return convertToVOList(comments, userId);
    }

    @Override
    public List<CommentVO> getRepliesByParentId(Long parentId, Long userId) {
        List<BizComment> replies = commentMapper.selectRepliesByParentId(
            parentId
        );
        return convertToVOList(replies, userId);
    }

    @Override
    public List<CommentVO> getCommentsByUserId(
        Long userId,
        Long currentUserId
    ) {
        List<BizComment> comments = commentMapper.selectByUserId(userId);
        return convertToVOList(comments, currentUserId);
    }

    @Override
    public IPage<CommentVO> getCommentPage(
        Integer pageNum,
        Integer pageSize,
        Long userId,
        Integer status,
        Long currentUserId
    ) {
        Page<BizComment> page = new Page<>(pageNum, pageSize);
        IPage<BizComment> commentPage = commentMapper.selectPageByCondition(
            page,
            userId,
            status
        );

        IPage<CommentVO> voPage = new Page<>(
            commentPage.getCurrent(),
            commentPage.getSize(),
            commentPage.getTotal()
        );
        voPage.setRecords(convertToVOList(commentPage.getRecords(), currentUserId));
        return voPage;
    }

    @Override
    public IPage<CommentVO> searchComments(
        String keyword,
        Integer pageNum,
        Integer pageSize,
        Long userId
    ) {
        Page<BizComment> page = new Page<>(pageNum, pageSize);
        IPage<BizComment> commentPage = commentMapper.searchComments(
            page,
            keyword
        );

        IPage<CommentVO> voPage = new Page<>(
            commentPage.getCurrent(),
            commentPage.getSize(),
            commentPage.getTotal()
        );
        voPage.setRecords(convertToVOList(commentPage.getRecords(), userId));
        return voPage;
    }

    @Override
    public Boolean updateLikeCount(Long commentId, Integer delta) {
        return commentMapper.updateLikeCount(commentId, delta) > 0;
    }

    @Override
    public Integer countCommentsByTarget(Integer targetType, Long targetId) {
        return commentMapper.countByTarget(targetType, targetId);
    }

    @Override
    public Integer countCommentsByUserId(Long userId) {
        return commentMapper.countByUserId(userId);
    }

    @Override
    public Integer countRepliesByParentId(Long parentId) {
        return commentMapper.countRepliesByParentId(parentId);
    }

    @Override
    public List<CommentVO> getLatestCommentsByUserId(
        Long userId,
        Integer limit,
        Long currentUserId
    ) {
        List<BizComment> comments = commentMapper.selectLatestCommentsByUserId(
            userId,
            limit
        );
        return convertToVOList(comments, currentUserId);
    }

    @Override
    public List<CommentVO> getHotComments(
        Integer targetType,
        Long targetId,
        Integer limit,
        Long userId
    ) {
        List<BizComment> comments = commentMapper.selectHotComments(
            targetType,
            targetId,
            limit
        );
        return convertToVOList(comments, userId);
    }

    @Override
    public List<CommentVO> getLatestComments(Integer limit, Long userId) {
        List<BizComment> comments = commentMapper.selectLatestComments(limit);
        return convertToVOList(comments, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean blockComment(Long commentId) {
        log.info("屏蔽评论: commentId={}", commentId);
        // 数据库中没有status字段，暂时使用逻辑删除
        return commentMapper.deleteById(commentId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveComment(Long commentId) {
        log.info("审核通过评论: commentId={}", commentId);
        // 数据库中没有status字段，暂时不做任何操作
        return true;
    }

    @Override
    public CommentVO convertToVO(BizComment comment, Long userId) {
        if (comment == null) {
            return null;
        }

        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);

        // 用户信息
        SysUser user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            vo.setUser(userVO);
        }

        // 回复用户信息
        // if (comment.getReplyToUserId() != null) {
        //     SysUser replyToUser = userMapper.selectById(
        //         comment.getReplyToUserId()
        //     );
        //     if (replyToUser != null) {
        //         UserVO replyToUserVO = new UserVO();
        //         BeanUtils.copyProperties(replyToUser, replyToUserVO);
        //         vo.setReplyToUser(replyToUserVO);
        //     }
        // }
        
        // 状态文本
        vo.setTargetTypeText(getTargetTypeText(comment.getTargetType()));
        // vo.setStatusText(getStatusText(comment.getStatus())); // 数据库表中无status字段
        // TODO: 点赞标志
        vo.setHasLiked(false);

        return vo;
    }

    @Override
    public List<CommentVO> convertToVOList(
        List<BizComment> comments,
        Long userId
    ) {
        if (CollectionUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }
        return comments
            .stream()
            .map(c -> convertToVO(c, userId))
            .collect(Collectors.toList());
    }

    /**
     * 验证目标存在
     */
    private void validateTarget(Integer targetType, Long targetId) {
        if (targetType == BizComment.TARGET_TYPE_QUESTION) {
            BizQuestion question = questionMapper.selectById(targetId);
            if (question == null) {
                throw new BusinessException(
                    ResultCode.PARAM_ERROR.getCode(),
                    "问题不存在"
                );
            }
        } else if (targetType == BizComment.TARGET_TYPE_ANSWER) {
            BizAnswer answer = answerMapper.selectById(targetId);
            if (answer == null) {
                throw new BusinessException(
                    ResultCode.PARAM_ERROR.getCode(),
                    "回答不存在"
                );
            }
        } else {
            throw new BusinessException(
                ResultCode.PARAM_ERROR.getCode(),
                "不支持的目标类型"
            );
        }
    }

    /**
     * 更新目标的评论数
     */
    private void updateTargetCommentCount(
        Integer targetType,
        Long targetId,
        Integer delta
    ) {
        if (targetType == BizComment.TARGET_TYPE_ANSWER) {
            answerMapper.updateCommentCount(targetId, delta);
        }
        // 问题的评论数暂不统计
    }

    /**
     * 获取目标类型文本
     */
    private String getTargetTypeText(Integer targetType) {
        if (targetType == null) {
            return "未知";
        }
        return targetType == 1 ? "问题" : "回答";
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "审核中";
            case 1:
                return "已发布";
            case 2:
                return "已屏蔽";
            default:
                return "未知";
        }
    }
}
