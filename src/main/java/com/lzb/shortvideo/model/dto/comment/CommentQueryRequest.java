package com.lzb.shortvideo.model.dto.comment;

import com.lzb.shortvideo.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommentQueryRequest extends PageRequest implements Serializable {


    /**
     * 创建用户 id
     */
    private Long userId;


    /**
     * 视频id
     */
    private Long videoId;

    private static final long serialVersionUID = 1L;
}