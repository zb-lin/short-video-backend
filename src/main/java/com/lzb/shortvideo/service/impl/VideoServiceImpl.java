package com.lzb.shortvideo.service.impl;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.constant.CommonConstant;
import com.lzb.shortvideo.exception.BusinessException;
import com.lzb.shortvideo.exception.ThrowUtils;
import com.lzb.shortvideo.mapper.VideoFavourMapper;
import com.lzb.shortvideo.mapper.VideoMapper;
import com.lzb.shortvideo.mapper.VideoThumbMapper;
import com.lzb.shortvideo.model.dto.recommend.UserPreference;
import com.lzb.shortvideo.model.dto.video.VideoEsDTO;
import com.lzb.shortvideo.model.dto.video.VideoQueryRequest;
import com.lzb.shortvideo.model.dto.video.VideoSearchRequest;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.model.entity.Video;
import com.lzb.shortvideo.model.entity.VideoFavour;
import com.lzb.shortvideo.model.entity.VideoThumb;
import com.lzb.shortvideo.model.vo.UserVO;
import com.lzb.shortvideo.model.vo.VideoVO;
import com.lzb.shortvideo.service.UserService;
import com.lzb.shortvideo.service.VideoService;
import com.lzb.shortvideo.utils.SqlUtils;
import com.lzb.shortvideo.utils.sensitive.sensitiveWord.SensitiveWordBs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.lzb.shortvideo.constant.RedisConstant.VIDEO_RECOMMEND_KEY;

@Slf4j
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video>
        implements VideoService {
    private final static Gson GSON = new Gson();

    @Resource
    private UserService userService;

    @Resource
    private VideoThumbMapper videoThumbMapper;

    @Resource
    private VideoFavourMapper videoFavourMapper;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Resource
    private SensitiveWordBs sensitiveWordBs;

    @Resource
    private BitMapBloomFilter bloomFilter;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void validVideo(Video video, boolean add) {
        if (video == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = video.getTitle();
        String content = video.getContent();
        String tags = video.getTags();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
        ThrowUtils.throwIf(sensitiveWordBs.hasSensitiveWord(content, title, tags), ErrorCode.SENSITIVE_WORD_ERROR);
    }

    /**
     * 获取查询包装类
     *
     * @param videoQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Video> getQueryWrapper(VideoQueryRequest videoQueryRequest) {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        if (videoQueryRequest == null) {
            return queryWrapper;
        }
        Long userId = videoQueryRequest.getUserId();
        String sortField = videoQueryRequest.getSortField();
        String sortOrder = videoQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<Video> searchFromEs(VideoSearchRequest videoSearchRequest) {
        String searchText = videoSearchRequest.getSearchText();
        String tag = videoSearchRequest.getTag();
        // es 起始页为 0
        long current = videoSearchRequest.getCurrent() - 1;
        long pageSize = videoSearchRequest.getPageSize();
        String sortField = videoSearchRequest.getSortField();
        String sortOrder = videoSearchRequest.getSortOrder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
        // 必须包含的标签
        if (StringUtils.isNotBlank(tag)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag));
        }
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("tags", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 分页
        PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withPageable(pageRequest).withSorts(sortBuilder).build();
        SearchHits<VideoEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, VideoEsDTO.class);
        Page<Video> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Video> resourceList = new ArrayList<>();
        // 查出结果后，从 db 获取最新动态数据（比如点赞数, 收藏数）
        if (searchHits.hasSearchHits()) {
            List<SearchHit<VideoEsDTO>> searchHitList = searchHits.getSearchHits();
            List<Long> videoIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
                    .collect(Collectors.toList());
            List<Video> videoList = baseMapper.selectBatchIds(videoIdList);
            if (videoList != null) {
                Map<Long, List<Video>> idVideoMap = videoList.stream().collect(Collectors.groupingBy(Video::getId));
                videoIdList.forEach(videoId -> {
                    if (idVideoMap.containsKey(videoId)) {
                        resourceList.add(idVideoMap.get(videoId).get(0));
                    } else {
                        // 从 es 清空 db 已物理删除的数据
                        String delete = elasticsearchRestTemplate.delete(String.valueOf(videoId), VideoEsDTO.class);
                        log.info("delete video {}", delete);
                    }
                });
            }
        }
        page.setRecords(resourceList);
        return page;
    }

    @Override
    public VideoVO getVideoVO(Video video, HttpServletRequest request) {
        VideoVO videoVO = VideoVO.objToVo(video);
        long videoId = video.getId();
        // 1. 关联查询用户信息
        Long userId = video.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        videoVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<VideoThumb> videoThumbQueryWrapper = new QueryWrapper<>();
            videoThumbQueryWrapper.in("videoId", videoId);
            videoThumbQueryWrapper.eq("userId", loginUser.getId());
            VideoThumb videoThumb = videoThumbMapper.selectOne(videoThumbQueryWrapper);
            videoVO.setHasThumb(videoThumb != null);
            // 获取收藏
            QueryWrapper<VideoFavour> videoFavourQueryWrapper = new QueryWrapper<>();
            videoFavourQueryWrapper.in("videoId", videoId);
            videoFavourQueryWrapper.eq("userId", loginUser.getId());
            VideoFavour videoFavour = videoFavourMapper.selectOne(videoFavourQueryWrapper);
            videoVO.setHasFavour(videoFavour != null);
        }
        return videoVO;
    }

    @Override
    public Page<VideoVO> getVideoVOPage(Page<Video> videoPage, HttpServletRequest request) {
        List<Video> videoList = videoPage.getRecords();
        Page<VideoVO> videoVOPage = new Page<>(videoPage.getCurrent(), videoPage.getSize(), videoPage.getTotal());
        if (CollectionUtils.isEmpty(videoList)) {
            return videoVOPage;
        }
        videoVOPage.setRecords(getVideoVOList(videoList, request));
        return videoVOPage;
    }


    /**
     * 推荐和自己相似的用户看过的视频
     *
     * @param id
     * @param request
     * @return
     */
    @Override
    public List<VideoVO> recommend(Long id, HttpServletRequest request) {
        try {
            // 从缓存中取
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            List<UserPreference> userPreferenceList = (List<UserPreference>) valueOperations.get(VIDEO_RECOMMEND_KEY);
            // 创建数据模型
            DataModel dataModel = this.createDataModel(userPreferenceList);
            // 获取用户相似度
            UserSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
            // 获取用户邻居
            UserNeighborhood userNeighborhood = new NearestNUserNeighborhood(3, similarity, dataModel);
            long[] ar = userNeighborhood.getUserNeighborhood(id);
            // 构建推荐器
            Recommender recommender = new GenericUserBasedRecommender(dataModel, userNeighborhood, similarity);
            // 推荐商品
            List<RecommendedItem> recommendedItems = recommender.recommend(id, 20);
            List<Long> idList = recommendedItems.stream().map(RecommendedItem::getItemID).collect(Collectors.toList());
            // 布隆过滤  去除重复视频
            Iterator<Long> iterator = idList.iterator();
            while (iterator.hasNext()) {
                Long videoId = iterator.next();
                String key = id + "-" + videoId;
                if (bloomFilter.contains(key)) {
                    iterator.remove();
                } else {
                    bloomFilter.add(key);
                }
            }
            List<Video> videoList = new ArrayList<>();
            // 无推荐视频
            if (CollectionUtils.isNotEmpty(idList)) {
                videoList = this.listByIds(idList);
            }
            // todo 无推荐补偿
            List<VideoVO> videoVOList = new ArrayList<>();
            if (CollectionUtils.isEmpty(videoList)) {
                return videoVOList;
            }
            videoVOList = getVideoVOList(videoList, request);
            return videoVOList;
        } catch (TasteException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "推荐算法出错");
        }
    }

    private List<VideoVO> getVideoVOList(List<Video> videoList, HttpServletRequest request) {
        // 1. 关联查询用户信息
        Set<Long> userIdSet = videoList.stream().map(Video::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> videoIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> videoIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> videoIdSet = videoList.stream().map(Video::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<VideoThumb> videoThumbQueryWrapper = new QueryWrapper<>();
            videoThumbQueryWrapper.in("videoId", videoIdSet);
            videoThumbQueryWrapper.eq("userId", loginUser.getId());
            List<VideoThumb> videoVideoThumbList = videoThumbMapper.selectList(videoThumbQueryWrapper);
            videoVideoThumbList.forEach(videoVideoThumb -> videoIdHasThumbMap.put(videoVideoThumb.getVideoId(), true));
            // 获取收藏
            QueryWrapper<VideoFavour> videoFavourQueryWrapper = new QueryWrapper<>();
            videoFavourQueryWrapper.in("videoId", videoIdSet);
            videoFavourQueryWrapper.eq("userId", loginUser.getId());
            List<VideoFavour> videoFavourList = videoFavourMapper.selectList(videoFavourQueryWrapper);
            videoFavourList.forEach(videoFavour -> videoIdHasFavourMap.put(videoFavour.getVideoId(), true));
        }
        // 填充信息
        return videoList.stream().map(video -> {
            VideoVO videoVO = VideoVO.objToVo(video);
            Long userId = video.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            videoVO.setUser(userService.getUserVO(user));
            videoVO.setHasThumb(videoIdHasThumbMap.getOrDefault(video.getId(), false));
            videoVO.setHasFavour(videoIdHasFavourMap.getOrDefault(video.getId(), false));
            return videoVO;
        }).collect(Collectors.toList());
    }

    private DataModel createDataModel(List<UserPreference> userPreferenceList) {
        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();
        Map<Long, List<UserPreference>> map = userPreferenceList.stream().collect(Collectors.groupingBy(UserPreference::getUserId));
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

}




