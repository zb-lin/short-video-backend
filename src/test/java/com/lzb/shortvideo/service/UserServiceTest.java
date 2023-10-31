package com.lzb.shortvideo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.model.entity.Video;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务测试
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Resource
    private VideoService videoService;

    @Resource
    private VideoFavourService videoFavourService;

    @Resource
    private VideoThumbService videoThumbService;

    @Test
    void userRegister() {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", 1719245355229995009L);
        queryWrapper.select("id");
        List<Long> videoIdList = videoService.list(queryWrapper).stream()
                .map(Video::getId).collect(Collectors.toList());
        User user = userService.getById(1719245269766856705L);
        videoIdList.forEach(videoId -> {
            videoThumbService.doVideoThumb(videoId, user);
            if (videoId % 2 == 0) {
                videoFavourService.doVideoFavour(videoId, user);
            }
        });
    }

    @Test
    void userRegiste() {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        List<Long> videoIdList = videoService.list(queryWrapper).stream()
                .map(Video::getId).collect(Collectors.toList());
        videoIdList.forEach(videoId -> {
            Video video = videoService.getById(videoId);
            video.setUrl("http://s32x7tly6.hn-bkt.clouddn.com/video/1719245355229995009/5gLHiZCS-Download.mp4");
            videoService.updateById(video);
        });
    }


}