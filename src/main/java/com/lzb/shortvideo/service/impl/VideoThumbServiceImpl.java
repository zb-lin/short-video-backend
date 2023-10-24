package com.lzb.shortvideo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzb.shortvideo.model.entity.VideoThumb;
import com.lzb.shortvideo.service.VideoThumbService;
import com.lzb.shortvideo.mapper.VideoThumbMapper;
import org.springframework.stereotype.Service;

/**
* @author 86177
* @description 针对表【video_thumb(帖子点赞)】的数据库操作Service实现
* @createDate 2023-10-24 22:15:27
*/
@Service
public class VideoThumbServiceImpl extends ServiceImpl<VideoThumbMapper, VideoThumb>
    implements VideoThumbService{

}




