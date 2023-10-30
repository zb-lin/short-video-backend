package com.lzb.shortvideo.model.dto.userfollow;

import com.lzb.shortvideo.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户关注
 *
 * @TableName user_follow
 */
@Data
public class UserFollowQueryRequest extends PageRequest implements Serializable {


    /**
     * 创建用户 id
     */
    private Long userId;


    private static final long serialVersionUID = 1L;
}