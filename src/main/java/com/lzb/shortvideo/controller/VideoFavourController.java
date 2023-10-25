package com.lzb.shortvideo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzb.shortvideo.common.BaseResponse;
import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.common.ResultUtils;
import com.lzb.shortvideo.exception.BusinessException;
import com.lzb.shortvideo.exception.ThrowUtils;
import com.lzb.shortvideo.model.dto.video.VideoQueryRequest;
import com.lzb.shortvideo.model.dto.videofavour.VideoFavourAddRequest;
import com.lzb.shortvideo.model.dto.videofavour.VideoFavourQueryRequest;
import com.lzb.shortvideo.model.entity.Video;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.model.vo.VideoVO;
import com.lzb.shortvideo.service.VideoFavourService;
import com.lzb.shortvideo.service.VideoService;
import com.lzb.shortvideo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 视频收藏接口
 */
@RestController
@RequestMapping("/video_favour")
@Slf4j
public class VideoFavourController {

    @Resource
    private VideoFavourService videoFavourService;

    @Resource
    private VideoService videoService;

    @Resource
    private UserService userService;

    /**
     * 收藏 / 取消收藏
     *
     * @param videoFavourAddRequest
     * @param request
     * @return resultNum 收藏变化数
     */
    @PostMapping("/")
    public BaseResponse<Integer> doVideoFavour(@RequestBody VideoFavourAddRequest videoFavourAddRequest,
                                              HttpServletRequest request) {
        if (videoFavourAddRequest == null || videoFavourAddRequest.getVideoId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能操作
        final User loginUser = userService.getLoginUser(request);
        long videoId = videoFavourAddRequest.getVideoId();
        int result = videoFavourService.doVideoFavour(videoId, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取我收藏的视频列表
     *
     * @param videoQueryRequest
     * @param request
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<VideoVO>> listMyFavourVideoByPage(@RequestBody VideoQueryRequest videoQueryRequest,
                                                             HttpServletRequest request) {
        if (videoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long current = videoQueryRequest.getCurrent();
        long size = videoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Video> videoPage = videoFavourService.listFavourVideoByPage(new Page<>(current, size),
                videoService.getQueryWrapper(videoQueryRequest), loginUser.getId());
        return ResultUtils.success(videoService.getVideoVOPage(videoPage, request));
    }

    /**
     * 获取用户收藏的视频列表
     *
     * @param videoFavourQueryRequest
     * @param request
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<VideoVO>> listFavourVideoByPage(@RequestBody VideoFavourQueryRequest videoFavourQueryRequest,
                                                           HttpServletRequest request) {
        if (videoFavourQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = videoFavourQueryRequest.getCurrent();
        long size = videoFavourQueryRequest.getPageSize();
        Long userId = videoFavourQueryRequest.getUserId();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20 || userId == null, ErrorCode.PARAMS_ERROR);
        Page<Video> videoPage = videoFavourService.listFavourVideoByPage(new Page<>(current, size),
                videoService.getQueryWrapper(videoFavourQueryRequest.getVideoQueryRequest()), userId);
        return ResultUtils.success(videoService.getVideoVOPage(videoPage, request));
    }
}
