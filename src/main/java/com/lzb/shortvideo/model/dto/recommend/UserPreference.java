package com.lzb.shortvideo.model.dto.recommend;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 个性化推荐工具实体类
 */
@Data
public class UserPreference implements Serializable {
    private Long userId;
    private Long videoId;
    private Float value;
}
