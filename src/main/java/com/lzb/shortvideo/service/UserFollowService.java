package com.lzb.shortvideo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lzb.shortvideo.model.dto.userfollow.UserFollowQueryRequest;
import com.lzb.shortvideo.model.entity.UserFollow;
import com.lzb.shortvideo.model.vo.UserFollowVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 86177
 * @description 针对表【user_follow(用户关注)】的数据库操作Service
 * @createDate 2023-10-30 16:47:22
 */
public interface UserFollowService extends IService<UserFollow> {

    QueryWrapper<UserFollow> getQueryWrapper(UserFollowQueryRequest userFollowQueryRequest);

    Page<UserFollowVO> getUserFollowVOPage(Page<UserFollow> userFollowPage, HttpServletRequest request);

    List<Long> getFollowVideoIdList(Long userId);

    List<Long> getUserFollowIdList(Long userId);
}
