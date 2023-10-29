package com.lzb.shortvideo.job.once;

import com.lzb.shortvideo.esdao.VideoEsDao;
import com.lzb.shortvideo.model.dto.video.VideoEsDTO;
import com.lzb.shortvideo.model.entity.Video;
import com.lzb.shortvideo.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全量同步帖子到 es
 */
//@Component
@Slf4j
@Deprecated
public class FullSyncVideoToEs implements CommandLineRunner {

    @Resource
    private VideoService videoService;

    @Resource
    private VideoEsDao videoEsDao;

    @Override
    public void run(String... args) {
        List<Video> videoList = videoService.list();
        if (CollectionUtils.isEmpty(videoList)) {
            return;
        }
        List<VideoEsDTO> videoEsDTOList = videoList.stream().map(VideoEsDTO::objToDto).collect(Collectors.toList());
        final int pageSize = 500;
        int total = videoEsDTOList.size();
        log.info("FullSyncVideoToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            videoEsDao.saveAll(videoEsDTOList.subList(i, end));
        }
        log.info("FullSyncVideoToEs end, total {}", total);
    }
}
