package com.lzb.shortvideo.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzb.shortvideo.model.entity.Video;
import com.lzb.shortvideo.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lzb.shortvideo.model.entity.VideoFavour;


public interface VideoFavourService extends IService<VideoFavour> {
    /**
     * 视频收藏
     *
     * @param videoId
     * @param loginUser
     * @return
     */
    int doVideoFavour(long videoId, User loginUser);

    /**
     * 分页获取用户收藏的视频列表
     *
     * @param page
     * @param queryWrapper
     * @param favourUserId
     * @return
     */
    Page<Video> listFavourVideoByPage(IPage<Video> page, Wrapper<Video> queryWrapper,
                                    long favourUserId);

    /**
     * 视频收藏（内部服务）
     *
     * @param userId
     * @param videoId
     * @return
     */
    int doVideoFavourInner(long userId, long videoId);
}
