package com.lzb.shortvideo.model.dto.videofavour;

import lombok.Data;

import java.io.Serializable;

/**
 * 视频收藏 / 取消收藏请求
 */
@Data
public class VideoFavourAddRequest implements Serializable {

    /**
     * 视频 id
     */
    private Long videoId;

    private static final long serialVersionUID = 1L;
}