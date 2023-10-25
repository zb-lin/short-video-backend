package com.lzb.shortvideo.controller;

import com.lzb.shortvideo.common.BaseResponse;
import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.common.ResultUtils;
import com.lzb.shortvideo.exception.BusinessException;
import com.lzb.shortvideo.model.dto.videothumb.VideoThumbAddRequest;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.service.UserService;
import com.lzb.shortvideo.service.VideoThumbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 视频点赞接口
 */
@RestController
@RequestMapping("/video_thumb")
@Slf4j
public class VideoThumbController {

    @Resource
    private VideoThumbService videoThumbService;

    @Resource
    private UserService userService;

    /**
     * 点赞 / 取消点赞
     *
     * @param videoThumbAddRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/")
    public BaseResponse<Integer> doThumb(@RequestBody VideoThumbAddRequest videoThumbAddRequest,
                                         HttpServletRequest request) {
        if (videoThumbAddRequest == null || videoThumbAddRequest.getVideoId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long videoId = videoThumbAddRequest.getVideoId();
        int result = videoThumbService.doVideoThumb(videoId, loginUser);
        return ResultUtils.success(result);
    }

}
