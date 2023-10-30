package com.lzb.shortvideo.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户关注
 *
 * @TableName user_follow
 */
@Data
public class UserFollowVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 关注用户
     */
    private UserVO userVO;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    private static final long serialVersionUID = 1L;
}