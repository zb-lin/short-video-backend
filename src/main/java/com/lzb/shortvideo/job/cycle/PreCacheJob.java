package com.lzb.shortvideo.job.cycle;

import com.lzb.shortvideo.annotation.RedissonLock;
import com.lzb.shortvideo.model.dto.recommend.UserPreference;
import com.lzb.shortvideo.model.entity.VideoFavour;
import com.lzb.shortvideo.model.entity.VideoThumb;
import com.lzb.shortvideo.service.VideoFavourService;
import com.lzb.shortvideo.service.VideoThumbService;
import com.lzb.shortvideo.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lzb.shortvideo.constant.RedisConstant.VIDEO_RECOMMEND_KEY;
import static com.lzb.shortvideo.constant.RedisConstant.VIDEO_RECOMMEND_LOCK;

/**
 * 缓存预热任务
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private VideoFavourService videoFavourService;
    @Resource
    private VideoThumbService videoThumbService;

    /**
     * 每一小时执行任务
     */
    @Scheduled(cron = "0 0 */1 * * ?")
    @RedissonLock(prefixKey = VIDEO_RECOMMEND_LOCK)
    public void doCacheRecommend() {
        List<UserPreference> allUserPreference = getAllUserPreference();
        try {
            RedisUtils.set(VIDEO_RECOMMEND_KEY, allUserPreference);
        } catch (Exception e) {
            log.error("redis set key error", e);
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

}
