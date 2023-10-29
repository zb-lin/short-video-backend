package com.lzb.shortvideo.job.cycle;

import com.lzb.shortvideo.model.dto.recommend.UserPreference;
import com.lzb.shortvideo.model.entity.VideoFavour;
import com.lzb.shortvideo.model.entity.VideoThumb;
import com.lzb.shortvideo.service.VideoFavourService;
import com.lzb.shortvideo.service.VideoThumbService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.lzb.shortvideo.constant.RedisConstant.VIDEO_RECOMMEND_KEY;
import static com.lzb.shortvideo.constant.RedisConstant.VIDEO_RECOMMEND_LOCK;

/**
 * 缓存预热任务
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private VideoFavourService videoFavourService;
    @Resource
    private VideoThumbService videoThumbService;

    /**
     * 每一小时执行任务
     */
    @Scheduled(cron = "0 0 */1 * * ?")
    public void doCacheRecommend() {
        RLock lock = redissonClient.getLock(VIDEO_RECOMMEND_LOCK);
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                List<UserPreference> allUserPreference = getAllUserPreference();
                ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                try {
                    valueOperations.set(VIDEO_RECOMMEND_KEY, allUserPreference);
                } catch (Exception e) {
                    log.error("redis set key error", e);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
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

    @Deprecated
    private List<UserPreference> getAllUserPreferenceByLongMap() {
        // 查询每一个用户视频收藏情况
        List<VideoFavour> videoFavours = videoFavourService.list();
        List<VideoThumb> videoThumbs = videoThumbService.list();
        Map<Long, Map<Long, Integer>> map = new HashMap<>();
        Map<Long, Integer> stringMap = new HashMap<>();
        videoFavours.forEach(videoFavour -> {
            Long videoId = videoFavour.getVideoId();
            Long userId = videoFavour.getUserId();
            stringMap.put(videoId, 4);
            map.put(userId, stringMap);
            stringMap.clear();
        });
        videoThumbs.forEach(videoThumb -> {
            Long videoId = videoThumb.getVideoId();
            Long userId = videoThumb.getUserId();
            Map<Long, Integer> longIntegerMap = map.getOrDefault(userId, null);
            if (longIntegerMap == null) {
                stringMap.put(videoId, 2);
                map.put(userId, stringMap);
                stringMap.clear();
            } else {
                Integer userIdByMap = longIntegerMap.get(videoId);
                if (userIdByMap != null) {
                    longIntegerMap.put(videoId, 6);
                } else {
                    longIntegerMap.put(videoId, 2);
                }
                map.put(userId, longIntegerMap);
            }
        });
        List<UserPreference> userPreferenceList = new ArrayList<>();
        map.keySet().forEach(keyKey -> {
            Map<Long, Integer> longIntegerMap = map.get(keyKey);
            longIntegerMap.keySet().forEach(valueKey -> {
                Integer value = longIntegerMap.get(valueKey);
                UserPreference userPreference = new UserPreference();
                userPreference.setUserId(keyKey);
                userPreference.setVideoId(valueKey);
                userPreference.setValue(Float.valueOf(value));
                userPreferenceList.add(userPreference);
            });
        });
        return userPreferenceList;
    }

}
