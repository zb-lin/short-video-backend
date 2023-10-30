package com.lzb.shortvideo.model.dto.video;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 */
@Data
public class VideoAddRequest implements Serializable {

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;
    /**
     * 视频路径
     */
    private String url;
    /**
     * 封面
     */
    private String cover;
    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}