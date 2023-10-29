package com.lzb.shortvideo.job.cycle;

import com.lzb.shortvideo.esdao.VideoEsDao;
import com.lzb.shortvideo.mapper.VideoMapper;
import com.lzb.shortvideo.model.dto.video.VideoEsDTO;
import com.lzb.shortvideo.model.entity.Video;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 增量同步帖子到 es
 */
// todo 取消注释开启任务
//@Component
@Slf4j
@Deprecated
public class IncSyncVideoToEs {

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private VideoEsDao videoEsDao;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void run() {
        // 查询近 5 分钟内的数据
        Date fiveMinutesAgoDate = new Date(new Date().getTime() - 5 * 60 * 1000L);
        List<Video> videoList = videoMapper.listVideoWithDelete(fiveMinutesAgoDate);
        if (CollectionUtils.isEmpty(videoList)) {
            log.info("no inc video");
            return;
        }
        List<VideoEsDTO> videoEsDTOList = videoList.stream()
                .map(VideoEsDTO::objToDto)
                .collect(Collectors.toList());
        final int pageSize = 500;
        int total = videoEsDTOList.size();
        log.info("IncSyncVideoToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            videoEsDao.saveAll(videoEsDTOList.subList(i, end));
        }
        log.info("IncSyncVideoToEs end, total {}", total);
    }
}
