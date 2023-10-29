package com.lzb.shortvideo;

import com.lzb.shortvideo.model.dto.recommend.UserPreference;
import com.lzb.shortvideo.model.entity.VideoFavour;
import com.lzb.shortvideo.model.entity.VideoThumb;
import com.lzb.shortvideo.service.VideoFavourService;
import com.lzb.shortvideo.service.VideoThumbService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主类测试
 */
@SpringBootTest
@Slf4j
class MainApplicationTests {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private VideoFavourService videoFavourService;
    @Resource
    private VideoThumbService videoThumbService;

    @Test
    void testJob() {
        doCacheRecommendUser();
    }

    /**
     * 每一小时执行任务
     */
    public void doCacheRecommendUser() {
        Pair<Long, Long> pair = new Pair<>(1L, 1L);
        Pair<Long, Long> pair1 = new Pair<>(1L, 1L);
    }

    private List<UserPreference> getAllUserPreference() {
        // 查询每一个用户视频收藏情况
        List<VideoFavour> videoFavours = videoFavourService.list();
        List<VideoThumb> videoThumbs = videoThumbService.list();
        Map<String, Integer> map = new HashMap<>();
        for (VideoFavour videoFavour : videoFavours) {
            Long videoId = videoFavour.getVideoId();
            Long userId = videoFavour.getUserId();
            map.put(videoId + " " + userId, 6);
        }
        for (VideoThumb videoThumb : videoThumbs) {
            Long videoId = videoThumb.getVideoId();
            Long userId = videoThumb.getUserId();
            if (map.get(videoId + " " + userId) == null) {
                map.put(videoId + " " + userId, 4);
            } else {
                map.put(videoId + " " + userId, 10);
            }
        }
        List<UserPreference> userPreferenceList = new ArrayList<>();
        map.keySet().forEach(key -> {
            String[] strings = key.split(" ");
            String videoId = strings[0];
            String userId = strings[1];
            UserPreference userPreference = new UserPreference();
            userPreference.setUserId(Long.valueOf(userId));
            userPreference.setVideoId(Long.valueOf(videoId));
            userPreference.setValue(map.get(key).floatValue());
            userPreferenceList.add(userPreference);
        });
        return userPreferenceList;
    }


}
