package com.lzb.shortvideo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.lzb.shortvideo.common.BaseResponse;
import com.lzb.shortvideo.common.DeleteRequest;
import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.common.ResultUtils;
import com.lzb.shortvideo.exception.BusinessException;
import com.lzb.shortvideo.exception.ThrowUtils;
import com.lzb.shortvideo.model.dto.userfollow.UserFollowAddRequest;
import com.lzb.shortvideo.model.dto.userfollow.UserFollowQueryRequest;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.model.entity.UserFollow;
import com.lzb.shortvideo.model.vo.UserFollowVO;
import com.lzb.shortvideo.service.UserFollowService;
import com.lzb.shortvideo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 视频接口
 */
@RestController
@RequestMapping("/userFollow")
@Slf4j
public class UserFollowController {

    @Resource
    private UserFollowService userFollowService;

    @Resource
    private UserService userService;


    private final static Gson GSON = new Gson();


    /**
     * 创建
     *
     * @param userFollowAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserFollow(@RequestBody UserFollowAddRequest userFollowAddRequest, HttpServletRequest request) {
        if (userFollowAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        LambdaQueryWrapper<UserFollow> userFollowLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userFollowLambdaQueryWrapper.eq(UserFollow::getFollowerId, userFollowAddRequest.getFollowerId());
        userFollowLambdaQueryWrapper.eq(UserFollow::getUserId, userId);
        UserFollow userFollow = userFollowService.getOne(userFollowLambdaQueryWrapper);
        if (userFollow == null) {
            userFollow = new UserFollow();
            BeanUtils.copyProperties(userFollowAddRequest, userFollow);
            userFollow.setUserId(userId);
            boolean result = userFollowService.save(userFollow);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            long newUserFollowId = userFollow.getId();
            return ResultUtils.success(newUserFollowId, "关注成功");
        }
        boolean result = userFollowService.removeById(userFollow.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(1L, "取消关注成功");
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserFollow(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserFollow oldUserFollow = userFollowService.getById(id);
        ThrowUtils.throwIf(oldUserFollow == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserFollow.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = userFollowService.removeById(id);
        return ResultUtils.success(b);
    }


    /**
     * 分页获取指定用户创建的资源列表
     *
     * @param userFollowQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserFollowVO>> listUserFollowVOByPage(@RequestBody UserFollowQueryRequest userFollowQueryRequest,
                                                                   HttpServletRequest request) {
        if (userFollowQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        userFollowQueryRequest.setUserId(loginUser.getId());
        long current = userFollowQueryRequest.getCurrent();
        long size = userFollowQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<UserFollow> userFollowPage = userFollowService.page(new Page<>(current, size),
                userFollowService.getQueryWrapper(userFollowQueryRequest));
        return ResultUtils.success(userFollowService.getUserFollowVOPage(userFollowPage, request));
    }

}
