package com.lzb.shortvideo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.exception.BusinessException;
import com.lzb.shortvideo.mapper.VideoThumbMapper;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.model.entity.Video;
import com.lzb.shortvideo.model.entity.VideoThumb;
import com.lzb.shortvideo.service.VideoService;
import com.lzb.shortvideo.service.VideoThumbService;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author 86177
 * @description 针对表【video_thumb(帖子点赞)】的数据库操作Service实现
 * @createDate 2023-10-24 22:15:27
 */
@Service
public class VideoThumbServiceImpl extends ServiceImpl<VideoThumbMapper, VideoThumb>
        implements VideoThumbService {
    @Resource
    private VideoService videoService;

    /**
     * 点赞
     *
     * @param videoId
     * @param loginUser
     * @return
     */
    @Override
    public int doVideoThumb(long videoId, User loginUser) {
        // 判断实体是否存在，根据类别获取实体
        Video video = videoService.getById(videoId);
        if (video == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已点赞
        long userId = loginUser.getId();
        // 每个用户串行点赞
        // 锁必须要包裹住事务方法
        VideoThumbService videoThumbService = (VideoThumbService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return videoThumbService.doVideoThumbInner(userId, videoId);
        }
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
    public int doVideoThumbInner(long userId, long videoId) {
        VideoThumb videoThumb = new VideoThumb();
        videoThumb.setUserId(userId);
        videoThumb.setVideoId(videoId);
        QueryWrapper<VideoThumb> thumbQueryWrapper = new QueryWrapper<>(videoThumb);
        VideoThumb oldVideoThumb = this.getOne(thumbQueryWrapper);
        boolean result;
        // 已点赞
        if (oldVideoThumb != null) {
            result = this.remove(thumbQueryWrapper);
            if (result) {
                // 点赞数 - 1
                result = videoService.update()
                        .eq("id", videoId)
                        .gt("thumbNum", 0)
                        .setSql("thumbNum = thumbNum - 1")
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未点赞
            result = this.save(videoThumb);
            if (result) {
                // 点赞数 + 1
                result = videoService.update()
                        .eq("id", videoId)
                        .setSql("thumbNum = thumbNum + 1")
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
}




