package com.lzb.shortvideo.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzb.shortvideo.model.entity.Video;
import com.lzb.shortvideo.model.entity.VideoFavour;
import org.apache.ibatis.annotations.Param;

public interface VideoFavourMapper extends BaseMapper<VideoFavour> {
    /**
     * 分页查询收藏视频列表
     *
     * @param page
     * @param queryWrapper
     * @param favourUserId
     * @return
     */
    Page<Video> listFavourVideoByPage(IPage<Video> page, @Param(Constants.WRAPPER) Wrapper<Video> queryWrapper,
                                     long favourUserId);
}




