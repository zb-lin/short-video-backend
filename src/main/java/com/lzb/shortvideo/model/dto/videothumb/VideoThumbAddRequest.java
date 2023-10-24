package com.lzb.shortvideo.model.dto.videothumb;

import lombok.Data;

import java.io.Serializable;

/**
 * 视频点赞请求
 */
@Data
public class VideoThumbAddRequest implements Serializable {

    /**
     * 视频 id
     */
    private Long videoId;

    private static final long serialVersionUID = 1L;
}