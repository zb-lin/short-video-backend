package com.lzb.shortvideo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.model.entity.VideoThumb;

/**
* @author 86177
* @description 针对表【video_thumb(视频点赞)】的数据库操作Service
* @createDate 2023-10-24 22:15:27
*/
public interface VideoThumbService extends IService<VideoThumb> {
    /**
     * 点赞
     *
     * @param videoId
     * @param loginUser
     * @return
     */
    int doVideoThumb(long videoId, User loginUser);

    /**
     * 视频点赞（内部服务）
     *
     * @param userId
     * @param videoId
     * @return
     */
    int doVideoThumbInner(long userId, long videoId);
}
