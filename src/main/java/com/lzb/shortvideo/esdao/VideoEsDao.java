package com.lzb.shortvideo.esdao;

import com.lzb.shortvideo.model.dto.video.VideoEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 帖子 ES 操作
 */
public interface VideoEsDao extends ElasticsearchRepository<VideoEsDTO, Long> {

    List<VideoEsDTO> findByUserId(Long userId);
}