package com.lzb.shortvideo.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UploadFileVO implements Serializable {

    /**
     * 文件路径
     */
    private String filepath;
    /**
     * 缩略图路径
     */
    private String thumbnailPath;
    private static final long serialVersionUID = 1L;

    public UploadFileVO(String cosHost, String filepath, String thumbnailPath) {
        this.filepath = cosHost + filepath;
        if (thumbnailPath != null) {
            this.thumbnailPath = cosHost + thumbnailPath;
        } else {
            this.thumbnailPath = null;
        }
    }
}
