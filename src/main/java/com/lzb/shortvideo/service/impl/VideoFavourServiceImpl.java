package com.lzb.shortvideo.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.exception.BusinessException;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.model.entity.Video;
import com.lzb.shortvideo.model.entity.VideoFavour;
import com.lzb.shortvideo.model.entity.VideoFavour;
import com.lzb.shortvideo.service.VideoFavourService;
import com.lzb.shortvideo.mapper.VideoFavourMapper;
import com.lzb.shortvideo.service.VideoService;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class VideoFavourServiceImpl extends ServiceImpl<VideoFavourMapper, VideoFavour>
    implements VideoFavourService{
    @Resource
    private VideoService videoService;

    /**
     * 视频收藏
     *
     * @param videoId
     * @param loginUser
     * @return
     */
    @Override
    public int doVideoFavour(long videoId, User loginUser) {
        // 判断是否存在
        Video video = videoService.getById(videoId);
        if (video == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已收藏
        long userId = loginUser.getId();
        // 每个用户串行
        // 锁必须要包裹住事务方法
        VideoFavourService videoFavourService = (VideoFavourService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return videoFavourService.doVideoFavourInner(userId, videoId);
        }
    }

    @Override
    public Page<Video> listFavourVideoByPage(IPage<Video> page, Wrapper<Video> queryWrapper, long favourUserId) {
        if (favourUserId <= 0) {
            return new Page<>();
        }
        return baseMapper.listFavourVideoByPage(page, queryWrapper, favourUserId);
    }

    /**
     * 封装了事务的方法
     *
     * @param userId
     * @param videoId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doVideoFavourInner(long userId, long videoId) {
        VideoFavour videoFavour = new VideoFavour();
        videoFavour.setUserId(userId);
        videoFavour.setVideoId(videoId);
        QueryWrapper<VideoFavour> videoFavourQueryWrapper = new QueryWrapper<>(videoFavour);
        VideoFavour oldVideoFavour = this.getOne(videoFavourQueryWrapper);
        boolean result;
        // 已收藏
        if (oldVideoFavour != null) {
            result = this.remove(videoFavourQueryWrapper);
            if (result) {
                // 视频收藏数 - 1
                result = videoService.update()
                        .eq("id", videoId)
                        .gt("favourNum", 0)
                        .setSql("favourNum = favourNum - 1")
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未视频收藏
            result = this.save(videoFavour);
            if (result) {
                // 视频收藏数 + 1
                result = videoService.update()
                        .eq("id", videoId)
                        .setSql("favourNum = favourNum + 1")
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }


}




