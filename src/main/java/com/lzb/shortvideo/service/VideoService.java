package com.lzb.shortvideo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lzb.shortvideo.model.dto.video.VideoQueryRequest;
import com.lzb.shortvideo.model.entity.Video;
import com.lzb.shortvideo.model.vo.VideoVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface VideoService extends IService<Video> {
    /**
     * 校验
     *
     * @param video
     * @param add
     */
    void validVideo(Video video, boolean add);

    /**
     * 获取查询条件
     *
     * @param videoQueryRequest
     * @return
     */
    QueryWrapper<Video> getQueryWrapper(VideoQueryRequest videoQueryRequest);

    /**
     * 从 ES 查询
     *
     * @param videoQueryRequest
     * @return
     */
    Page<Video> searchFromEs(VideoQueryRequest videoQueryRequest);

    /**
     * 获取视频封装
     *
     * @param video
     * @param request
     * @return
     */
    VideoVO getVideoVO(Video video, HttpServletRequest request);

    /**
     * 分页获取视频封装
     *
     * @param videoPage
     * @param request
     * @return
     */
    Page<VideoVO> getVideoVOPage(Page<Video> videoPage, HttpServletRequest request);

    List<VideoVO> recommend(Long userId, HttpServletRequest request);
}
