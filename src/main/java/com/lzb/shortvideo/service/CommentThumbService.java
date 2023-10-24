package com.lzb.shortvideo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lzb.shortvideo.model.entity.CommentThumb;
import com.lzb.shortvideo.model.entity.User;


public interface CommentThumbService extends IService<CommentThumb> {
    /**
     * 点赞
     *
     * @param commentId
     * @param loginUser
     * @return
     */
    int doCommentThumb(long commentId, User loginUser);

    /**
     * 评论点赞（内部服务）
     *
     * @param userId
     * @param commentId
     * @return
     */
    int doCommentThumbInner(long userId, long commentId);
}
