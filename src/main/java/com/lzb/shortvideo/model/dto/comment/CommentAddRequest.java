package com.lzb.shortvideo.model.dto.comment;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentAddRequest implements Serializable {

    /**
     * 内容
     */
    private String content;

    /**
     * 视频id
     */
    private Long videoId;

    private static final long serialVersionUID = 1L;

}
