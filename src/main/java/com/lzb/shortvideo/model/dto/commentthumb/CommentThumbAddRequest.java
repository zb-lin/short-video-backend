package com.lzb.shortvideo.model.dto.commentthumb;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论点赞请求
 */
@Data
public class CommentThumbAddRequest implements Serializable {

    /**
     * 评论 id
     */
    private Long commentId;


    private static final long serialVersionUID = 1L;
}