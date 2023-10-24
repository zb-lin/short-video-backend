package com.lzb.shortvideo.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lzb.shortvideo.model.dto.comment.CommentQueryRequest;
import com.lzb.shortvideo.model.entity.Comment;
import com.lzb.shortvideo.model.vo.CommentVO;

import javax.servlet.http.HttpServletRequest;


public interface CommentService extends IService<Comment> {
    /**
     * 校验
     *
     * @param comment
     * @param add
     */
    void validComment(Comment comment, boolean add);

    /**
     * 获取查询条件
     *
     * @param commentQueryRequest
     * @return
     */
    QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest);


    /**
     * 获取评论封装
     *
     * @param comment
     * @param request
     * @return
     */
    CommentVO getCommentVO(Comment comment, HttpServletRequest request);

    /**
     * 分页获取评论封装
     *
     * @param commentPage
     * @param request
     * @return
     */
    Page<CommentVO> getCommentVOPage(Page<Comment> commentPage, HttpServletRequest request);
}
