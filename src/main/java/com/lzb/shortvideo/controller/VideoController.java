package com.lzb.shortvideo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.lzb.shortvideo.common.BaseResponse;
import com.lzb.shortvideo.common.DeleteRequest;
import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.common.ResultUtils;
import com.lzb.shortvideo.exception.BusinessException;
import com.lzb.shortvideo.exception.ThrowUtils;
import com.lzb.shortvideo.model.dto.video.VideoAddRequest;
import com.lzb.shortvideo.model.dto.video.VideoQueryRequest;
import com.lzb.shortvideo.model.dto.video.VideoUpdateRequest;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.model.entity.Video;
import com.lzb.shortvideo.model.vo.VideoVO;
import com.lzb.shortvideo.service.UserService;
import com.lzb.shortvideo.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 视频接口
 */
@RestController
@RequestMapping("/video")
@Slf4j
public class VideoController {

    @Resource
    private VideoService videoService;

    @Resource
    private UserService userService;

    private final static Gson GSON = new Gson();


    /**
     * 创建
     *
     * @param videoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addVideo(@RequestBody VideoAddRequest videoAddRequest, HttpServletRequest request) {
        if (videoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Video video = new Video();
        BeanUtils.copyProperties(videoAddRequest, video);
        List<String> tags = videoAddRequest.getTags();
        if (tags != null) {
            video.setTags(GSON.toJson(tags));
        }
        videoService.validVideo(video, true);
        User loginUser = userService.getLoginUser(request);
        video.setUserId(loginUser.getId());
        video.setFavourNum(0);
        video.setThumbNum(0);
        boolean result = videoService.save(video);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newVideoId = video.getId();
        return ResultUtils.success(newVideoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteVideo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Video oldVideo = videoService.getById(id);
        ThrowUtils.throwIf(oldVideo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldVideo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = videoService.removeById(id);
        // todo 删除实际视频


        return ResultUtils.success(b);
    }


    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<VideoVO> getVideoVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Video video = videoService.getById(id);
        if (video == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(videoService.getVideoVO(video, request));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param videoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<VideoVO>> listVideoVOByPage(@RequestBody VideoQueryRequest videoQueryRequest,
                                                         HttpServletRequest request) {
        long current = videoQueryRequest.getCurrent();
        long size = videoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Video> videoPage = videoService.page(new Page<>(current, size),
                videoService.getQueryWrapper(videoQueryRequest));
        return ResultUtils.success(videoService.getVideoVOPage(videoPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param videoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<VideoVO>> listMyVideoVOByPage(@RequestBody VideoQueryRequest videoQueryRequest,
                                                           HttpServletRequest request) {
        if (videoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        videoQueryRequest.setUserId(loginUser.getId());
        long current = videoQueryRequest.getCurrent();
        long size = videoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Video> videoPage = videoService.page(new Page<>(current, size),
                videoService.getQueryWrapper(videoQueryRequest));
        return ResultUtils.success(videoService.getVideoVOPage(videoPage, request));
    }

    // endregion

    /**
     * 分页搜索（从 ES 查询，封装类）
     *
     * @param videoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/search/page/vo")
    public BaseResponse<Page<VideoVO>> searchVideoVOByPage(@RequestBody VideoQueryRequest videoQueryRequest,
                                                           HttpServletRequest request) {
        long size = videoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Video> videoPage = videoService.searchFromEs(videoQueryRequest);
        return ResultUtils.success(videoService.getVideoVOPage(videoPage, request));
    }

    /**
     * 更新
     *
     * @param videoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateVideo(@RequestBody VideoUpdateRequest videoUpdateRequest, HttpServletRequest request) {
        if (videoUpdateRequest == null || videoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Video video = new Video();
        BeanUtils.copyProperties(videoUpdateRequest, video);
        List<String> tags = videoUpdateRequest.getTags();
        if (tags != null) {
            video.setTags(GSON.toJson(tags));
        }
        // 参数校验
        videoService.validVideo(video, false);
        User loginUser = userService.getLoginUser(request);
        long id = videoUpdateRequest.getId();
        // 判断是否存在
        Video oldVideo = videoService.getById(id);
        ThrowUtils.throwIf(oldVideo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldVideo.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = videoService.updateById(video);
        return ResultUtils.success(result);
    }

}
