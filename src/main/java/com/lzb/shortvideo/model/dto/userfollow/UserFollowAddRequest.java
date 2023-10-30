package com.lzb.shortvideo.model.dto.userfollow;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户关注
 *
 * @TableName user_follow
 */
@Data
public class UserFollowAddRequest implements Serializable {


    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 关注用户 id
     */
    private Long followerId;


    private static final long serialVersionUID = 1L;
}