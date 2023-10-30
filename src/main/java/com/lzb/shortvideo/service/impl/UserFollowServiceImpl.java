package com.lzb.shortvideo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzb.shortvideo.annotation.Cache;
import com.lzb.shortvideo.constant.CommonConstant;
import com.lzb.shortvideo.mapper.UserFollowMapper;
import com.lzb.shortvideo.model.dto.userfollow.UserFollowQueryRequest;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.model.entity.UserFollow;
import com.lzb.shortvideo.model.entity.Video;
import com.lzb.shortvideo.model.vo.UserFollowVO;
import com.lzb.shortvideo.service.UserFollowService;
import com.lzb.shortvideo.service.UserService;
import com.lzb.shortvideo.service.VideoService;
import com.lzb.shortvideo.utils.SpringContextUtils;
import com.lzb.shortvideo.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.lzb.shortvideo.constant.RedisConstant.USER_FOLLOW_ID_KEY;

/**
 * @author 86177
 * @description 针对表【user_follow(用户关注)】的数据库操作Service实现
 * @createDate 2023-10-30 16:47:22
 */
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow>
        implements UserFollowService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private VideoService videoService;

    @Override
    public QueryWrapper<UserFollow> getQueryWrapper(UserFollowQueryRequest userFollowQueryRequest) {
        QueryWrapper<UserFollow> queryWrapper = new QueryWrapper<>();
        Long userId = userFollowQueryRequest.getUserId();
        String sortField = userFollowQueryRequest.getSortField();
        String sortOrder = userFollowQueryRequest.getSortOrder();
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<UserFollowVO> getUserFollowVOPage(Page<UserFollow> userFollowPage, HttpServletRequest request) {
        List<UserFollow> userFollows = userFollowPage.getRecords();
        Page<UserFollowVO> videoVOPage = new Page<>(userFollowPage.getCurrent(), userFollowPage.getSize(), userFollowPage.getTotal());
        if (CollectionUtils.isEmpty(userFollows)) {
            return videoVOPage;
        }
        List<UserFollowVO> userFollowVOList = userFollows.stream().map(userFollow -> {
            UserFollowVO userFollowVO = new UserFollowVO();
            BeanUtil.copyProperties(userFollow, userFollowVO);
            Long followerId = userFollow.getFollowerId();
            User user = userService.getById(followerId);
            userFollowVO.setUserVO(userService.getUserVO(user));
            return userFollowVO;
        }).collect(Collectors.toList());
        videoVOPage.setRecords(userFollowVOList);
        return videoVOPage;
    }

    @Override
    public List<Long> getFollowVideoIdList(Long userId) {
        List<Long> userFollowIdList = SpringContextUtils.getBean(UserFollowService.class).getUserFollowIdList(userId);
        List<Long> idList = new ArrayList<>();
        userFollowIdList.forEach(userFollowId -> {
            QueryWrapper<Video> videoQueryWrapper = new QueryWrapper<>();
            videoQueryWrapper.eq("userId", userFollowId);
            videoQueryWrapper.select("id");
            List<Long> tempIdList = videoService.list(videoQueryWrapper).stream().map(Video::getId).collect(Collectors.toList());
            idList.addAll(tempIdList);
        });
        return idList;
    }


    @Cache(prefixKey = USER_FOLLOW_ID_KEY, spEl = "#userId", isList = true, resultClass = Long.class)
    @Override
    public List<Long> getUserFollowIdList(Long userId) {
        QueryWrapper<UserFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.select("followerId");
        return this.list(queryWrapper).stream().map(UserFollow::getFollowerId).collect(Collectors.toList());
    }
}
