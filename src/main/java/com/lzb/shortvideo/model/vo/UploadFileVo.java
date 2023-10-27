package com.lzb.shortvideo.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class UploadFileVo implements Serializable {

    /**
     * 文件路径
     */
    private String filepath;
    /**
     * 缩略图路径
     */
    private String thumbnailPath;
    private static final long serialVersionUID = 1L;


}
