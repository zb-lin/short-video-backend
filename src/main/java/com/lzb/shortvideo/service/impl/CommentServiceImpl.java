package com.lzb.shortvideo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.constant.CommonConstant;
import com.lzb.shortvideo.exception.BusinessException;
import com.lzb.shortvideo.exception.ThrowUtils;
import com.lzb.shortvideo.mapper.CommentMapper;
import com.lzb.shortvideo.mapper.CommentThumbMapper;
import com.lzb.shortvideo.model.dto.comment.CommentQueryRequest;
import com.lzb.shortvideo.model.entity.Comment;
import com.lzb.shortvideo.model.entity.CommentThumb;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.model.vo.CommentVO;
import com.lzb.shortvideo.model.vo.UserVO;
import com.lzb.shortvideo.service.CommentService;
import com.lzb.shortvideo.service.UserService;
import com.lzb.shortvideo.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {

    @Resource
    private UserService userService;

    @Resource
    private CommentThumbMapper commentThumbMapper;


    @Override
    public void validComment(Comment comment, boolean add) {
        if (comment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String content = comment.getContent();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(content), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(content) && content.length() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能发布空评论");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param commentQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        if (commentQueryRequest == null) {
            return queryWrapper;
        }
        Long id = commentQueryRequest.getId();
        Long userId = commentQueryRequest.getUserId();
        String sortField = commentQueryRequest.getSortField();
        String sortOrder = commentQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    @Override
    public CommentVO getCommentVO(Comment comment, HttpServletRequest request) {
        CommentVO commentVO = CommentVO.objToVo(comment);
        long commentId = comment.getId();
        // 1. 关联查询用户信息
        Long userId = comment.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        commentVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<CommentThumb> commentThumbQueryWrapper = new QueryWrapper<>();
            commentThumbQueryWrapper.in("commentId", commentId);
            commentThumbQueryWrapper.eq("userId", loginUser.getId());
            CommentThumb commentThumb = commentThumbMapper.selectOne(commentThumbQueryWrapper);
            commentVO.setHasThumb(commentThumb != null);
        }
        return commentVO;
    }

    @Override
    public Page<CommentVO> getCommentVOPage(Page<Comment> commentPage, HttpServletRequest request) {
        List<Comment> commentList = commentPage.getRecords();
        Page<CommentVO> commentVOPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        if (CollectionUtils.isEmpty(commentList)) {
            return commentVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = commentList.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> commentIdHasThumbMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> commentIdSet = commentList.stream().map(Comment::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<CommentThumb> commentThumbQueryWrapper = new QueryWrapper<>();
            commentThumbQueryWrapper.in("commentId", commentIdSet);
            commentThumbQueryWrapper.eq("userId", loginUser.getId());
            List<CommentThumb> commentCommentThumbList = commentThumbMapper.selectList(commentThumbQueryWrapper);
            commentCommentThumbList.forEach(commentCommentThumb -> commentIdHasThumbMap.put(commentCommentThumb.getCommentId(), true));
        }
        // 填充信息
        List<CommentVO> commentVOList = commentList.stream().map(comment -> {
            CommentVO commentVO = CommentVO.objToVo(comment);
            Long userId = comment.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            commentVO.setUser(userService.getUserVO(user));
            commentVO.setHasThumb(commentIdHasThumbMap.getOrDefault(comment.getId(), false));
            return commentVO;
        }).collect(Collectors.toList());
        commentVOPage.setRecords(commentVOList);
        return commentVOPage;
    }

}




