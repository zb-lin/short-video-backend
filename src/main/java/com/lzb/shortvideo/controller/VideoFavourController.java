package com.lzb.shortvideo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzb.shortvideo.common.BaseResponse;
import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.common.ResultUtils;
import com.lzb.shortvideo.exception.BusinessException;
import com.lzb.shortvideo.exception.ThrowUtils;
import com.lzb.shortvideo.model.dto.comment.CommentQueryRequest;
import com.lzb.shortvideo.model.dto.commentfavour.CommentFavourAddRequest;
import com.lzb.shortvideo.model.dto.commentfavour.CommentFavourQueryRequest;
import com.lzb.shortvideo.model.entity.Comment;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.model.vo.CommentVO;
import com.lzb.shortvideo.service.CommentFavourService;
import com.lzb.shortvideo.service.CommentService;
import com.lzb.shortvideo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子收藏接口
 */
@RestController
@RequestMapping("/comment_favour")
@Slf4j
public class VideoFavourController {

    @Resource
    private CommentFavourService commentFavourService;

    @Resource
    private CommentService commentService;

    @Resource
    private UserService userService;

    /**
     * 收藏 / 取消收藏
     *
     * @param commentFavourAddRequest
     * @param request
     * @return resultNum 收藏变化数
     */
    @PostMapping("/")
    public BaseResponse<Integer> doCommentFavour(@RequestBody CommentFavourAddRequest commentFavourAddRequest,
                                              HttpServletRequest request) {
        if (commentFavourAddRequest == null || commentFavourAddRequest.getCommentId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能操作
        final User loginUser = userService.getLoginUser(request);
        long commentId = commentFavourAddRequest.getCommentId();
        int result = commentFavourService.doCommentFavour(commentId, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取我收藏的帖子列表
     *
     * @param commentQueryRequest
     * @param request
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<CommentVO>> listMyFavourCommentByPage(@RequestBody CommentQueryRequest commentQueryRequest,
                                                             HttpServletRequest request) {
        if (commentQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Comment> commentPage = commentFavourService.listFavourCommentByPage(new Page<>(current, size),
                commentService.getQueryWrapper(commentQueryRequest), loginUser.getId());
        return ResultUtils.success(commentService.getCommentVOPage(commentPage, request));
    }

    /**
     * 获取用户收藏的帖子列表
     *
     * @param commentFavourQueryRequest
     * @param request
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<CommentVO>> listFavourCommentByPage(@RequestBody CommentFavourQueryRequest commentFavourQueryRequest,
                                                           HttpServletRequest request) {
        if (commentFavourQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = commentFavourQueryRequest.getCurrent();
        long size = commentFavourQueryRequest.getPageSize();
        Long userId = commentFavourQueryRequest.getUserId();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20 || userId == null, ErrorCode.PARAMS_ERROR);
        Page<Comment> commentPage = commentFavourService.listFavourCommentByPage(new Page<>(current, size),
                commentService.getQueryWrapper(commentFavourQueryRequest.getCommentQueryRequest()), userId);
        return ResultUtils.success(commentService.getCommentVOPage(commentPage, request));
    }
}
