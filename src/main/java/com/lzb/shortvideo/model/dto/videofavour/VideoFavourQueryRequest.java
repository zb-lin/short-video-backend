package com.lzb.shortvideo.model.dto.videofavour;

import com.lzb.shortvideo.common.PageRequest;
import com.lzb.shortvideo.model.dto.video.VideoQueryRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 视频收藏查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VideoFavourQueryRequest extends PageRequest implements Serializable {

    /**
     * 视频查询请求
     */
    private VideoQueryRequest videoQueryRequest;

    /**
     * 用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}