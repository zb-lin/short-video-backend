package com.lzb.shortvideo;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.lzb.shortvideo.model.dto.recommend.UserPreference;
import com.lzb.shortvideo.model.entity.VideoFavour;
import com.lzb.shortvideo.model.entity.VideoThumb;
import com.lzb.shortvideo.service.VideoFavourService;
import com.lzb.shortvideo.service.VideoThumbService;
import com.lzb.shortvideo.utils.RedisKeyUtils;
import com.lzb.shortvideo.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lzb.shortvideo.constant.RedisConstant.VIDEO_RECOMMEND_KEY;
import static com.lzb.shortvideo.constant.RedisConstant.VIDEO_RECOMMEND_LOCK;

/**
 * 主类测试
 */
@SpringBootTest
@Slf4j
class MainApplicationTests {
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private VideoFavourService videoFavourService;
    @Resource
    private VideoThumbService videoThumbService;

    private static final Gson GSON = new Gson();

    @Test
    void tets() throws TasteException {
        long id = 1717188659762479105L;
        // 从缓存中取
        String uuid = RedisKeyUtils.getUUID();
        Map<Object, Object> userPreferenceMap = RedisUtils.hmget(VIDEO_RECOMMEND_KEY + uuid);
        List<UserPreference> userPreferenceList = userPreferenceMap.values().stream()
                .map(userPreferenceJson -> JSONUtil.toBean((String) userPreferenceJson, UserPreference.class)).collect(Collectors.toList());
        // 创建数据模型
        DataModel dataModel = this.createDataModel(userPreferenceList);
        // 获取用户相似度
        UserSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        // 获取用户邻居
        UserNeighborhood userNeighborhood = new NearestNUserNeighborhood(20, similarity, dataModel);
        long[] ar = userNeighborhood.getUserNeighborhood(id);
        // 构建推荐器
        Recommender recommender = new GenericUserBasedRecommender(dataModel, userNeighborhood, similarity);
        // 推荐商品
        List<RecommendedItem> recommendedItems = recommender.recommend(id, 10);
        for (RecommendedItem recommendedItem : recommendedItems) {
            System.out.println(recommendedItem.getItemID());
            for (long l : ar) {
                userPreferenceMap.remove(l + "-" + recommendedItem.getItemID());
            }
        }
        String simpleUUID = IdUtil.simpleUUID();
        RedisUtils.hmset(VIDEO_RECOMMEND_KEY + simpleUUID, userPreferenceMap);
        RedisKeyUtils.setUUID(simpleUUID);
        RedisUtils.del(VIDEO_RECOMMEND_KEY + uuid);
        System.out.println("------------------------");
        userPreferenceMap = RedisUtils.hmget(VIDEO_RECOMMEND_KEY + RedisKeyUtils.getUUID());
        userPreferenceList = userPreferenceMap.values().stream()
                .map(userPreferenceJson -> JSONUtil.toBean((String) userPreferenceJson, UserPreference.class)).collect(Collectors.toList());
        // 创建数据模型
        dataModel = this.createDataModel(userPreferenceList);
        // 获取用户相似度
        similarity = new UncenteredCosineSimilarity(dataModel);
        // 获取用户邻居
        userNeighborhood = new NearestNUserNeighborhood(20, similarity, dataModel);
        userNeighborhood.getUserNeighborhood(id);
        // 构建推荐器
        recommender = new GenericUserBasedRecommender(dataModel, userNeighborhood, similarity);
        // 推荐商品
        recommendedItems = recommender.recommend(id, 10);
        for (RecommendedItem recommendedItem : recommendedItems) {
            System.out.println(recommendedItem.getItemID());
            userPreferenceMap.remove(id + "-" + recommendedItem.getItemID());
        }
    }

    private DataModel createDataModel(List<UserPreference> userPreferenceList) {
        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();
        Map<Long, List<UserPreference>> map = userPreferenceList.stream()
                .collect(Collectors.groupingBy(UserPreference::getUserId));
        Collection<List<UserPreference>> list = map.values();
        for (List<UserPreference> userPreferences : list) {
            GenericPreference[] preferences = new GenericPreference[userPreferences.size()];
            for (int i = 0; i < userPreferences.size(); i++) {
                UserPreference userPreference = userPreferences.get(i);
                GenericPreference item = new GenericPreference(userPreference.getUserId(), userPreference.getVideoId(), userPreference.getValue());
                preferences[i] = item;
            }
            fastByIdMap.put(preferences[0].getUserID(), new GenericUserPreferenceArray(Arrays.asList(preferences)));
        }
        return new GenericDataModel(fastByIdMap);
    }

    /**
     * 每一小时执行任务
     */
    @Test
    public void doCacheRecommend() {
        RLock lock = redissonClient.getLock(VIDEO_RECOMMEND_LOCK);
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                String simpleUUID = IdUtil.simpleUUID();
                System.out.println(simpleUUID);
                List<UserPreference> allUserPreference = getAllUserPreference();
                Map<Object, Object> userPreferenceMap = new HashMap<>();
                for (UserPreference userPreference : allUserPreference) {
                    String key = userPreference.getUserId() + "-" + userPreference.getVideoId();
                    String json = GSON.toJson(userPreference);
                    userPreferenceMap.put(key, json);
                }
                try {
                    RedisUtils.hmset(VIDEO_RECOMMEND_KEY + simpleUUID, userPreferenceMap);
                } catch (Exception e) {
                    log.error("redis set key error", e);
                }
                String uuid = RedisKeyUtils.getUUID();
                RedisKeyUtils.setUUID(simpleUUID);
                if (uuid != null) {
                    RedisUtils.del(VIDEO_RECOMMEND_KEY + uuid);
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


}
