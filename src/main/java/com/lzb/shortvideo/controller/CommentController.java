package com.lzb.shortvideo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.lzb.shortvideo.common.BaseResponse;
import com.lzb.shortvideo.common.DeleteRequest;
import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.common.ResultUtils;
import com.lzb.shortvideo.exception.BusinessException;
import com.lzb.shortvideo.exception.ThrowUtils;
import com.lzb.shortvideo.model.dto.comment.CommentAddRequest;
import com.lzb.shortvideo.model.dto.comment.CommentQueryRequest;
import com.lzb.shortvideo.model.entity.Comment;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.model.vo.CommentVO;
import com.lzb.shortvideo.service.CommentService;
import com.lzb.shortvideo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子接口
 */
@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private UserService userService;



    /**
     * 创建
     *
     * @param commentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addComment(@RequestBody CommentAddRequest commentAddRequest, HttpServletRequest request) {
        if (commentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentAddRequest, comment);
        commentService.validComment(comment, true);
        User loginUser = userService.getLoginUser(request);
        comment.setUserId(loginUser.getId());
        comment.setThumbNum(0);
        boolean result = commentService.save(comment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newCommentId = comment.getId();
        return ResultUtils.success(newCommentId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Comment oldComment = commentService.getById(id);
        ThrowUtils.throwIf(oldComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldComment.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = commentService.removeById(id);
        return ResultUtils.success(b);
    }


    /**
     * 分页获取列表（封装类）
     *
     * @param commentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CommentVO>> listCommentVOByPage(@RequestBody CommentQueryRequest commentQueryRequest,
                                                             HttpServletRequest request) {
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Comment> commentPage = commentService.page(new Page<>(current, size),
                commentService.getQueryWrapper(commentQueryRequest));
        return ResultUtils.success(commentService.getCommentVOPage(commentPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param commentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<CommentVO>> listMyCommentVOByPage(@RequestBody CommentQueryRequest commentQueryRequest,
                                                               HttpServletRequest request) {
        if (commentQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        commentQueryRequest.setUserId(loginUser.getId());
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Comment> commentPage = commentService.page(new Page<>(current, size),
                commentService.getQueryWrapper(commentQueryRequest));
        return ResultUtils.success(commentService.getCommentVOPage(commentPage, request));
    }

}
