package com.lzb.shortvideo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzb.shortvideo.model.dto.recommend.UserPreference;
import com.lzb.shortvideo.model.entity.Video;

import java.util.Date;
import java.util.List;


public interface VideoMapper extends BaseMapper<Video> {

    List<Video> listVideoWithDelete(Date fiveMinutesAgoDate);
}




