package com.lzb.shortvideo.model.dto.video;

import com.lzb.shortvideo.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VideoSearchRequest extends PageRequest implements Serializable {


    /**
     * 搜索框 搜索词
     */
    private String searchText;


    /**
     * 侧边栏标签
     */
    private String tag;


    private static final long serialVersionUID = 1L;
}