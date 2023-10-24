package com.lzb.shortvideo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzb.shortvideo.model.entity.Comment;

import java.util.Date;
import java.util.List;

/**
* @author lzb
* @createDate 2023-10-24 22:15:27
*/
public interface CommentMapper extends BaseMapper<Comment> {
    /**
     * 查询评论列表（包括已被删除的数据）
     */
    List<Comment> listCommentWithDelete(Date minUpdateTime);
}




