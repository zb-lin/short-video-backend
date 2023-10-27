package com.lzb.shortvideo.model.dto.log;

import lombok.Data;

/**
 * web请求信息收集类
 */
@Data
public class RequestInfo {
    private Long uid;
    private String ip;
}
