package com.lzb.shortvideo.model.dto.video;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.lzb.shortvideo.model.entity.Video;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子 ES 包装类
 **/
@Document(indexName = "video")
@Data
public class VideoEsDTO implements Serializable {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * id
     */
    @Id
    private Long id;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 视频url
     */
    private String url;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 评论数
     */
    private Integer commentNum;

    /**
     * 创建时间
     */
    @Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date createTime;

    /**
     * 更新时间
     */
    @Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;

    private static final Gson GSON = new Gson();

    /**
     * 对象转包装类
     *
     * @param video
     * @return
     */
    public static VideoEsDTO objToDto(Video video) {
        if (video == null) {
            return null;
        }
        VideoEsDTO videoEsDTO = new VideoEsDTO();
        BeanUtils.copyProperties(video, videoEsDTO);
        String tagsStr = video.getTags();
        if (StringUtils.isNotBlank(tagsStr)) {
            videoEsDTO.setTags(GSON.fromJson(tagsStr, new TypeToken<List<String>>() {
            }.getType()));
        }
        return videoEsDTO;
    }


    /**
     * 包装类转对象
     *
     * @param videoEsDTO
     * @return
     */
    public static Video dtoToObj(VideoEsDTO videoEsDTO) {
        if (videoEsDTO == null) {
            return null;
        }
        Video video = new Video();
        BeanUtils.copyProperties(videoEsDTO, video);
        List<String> tagList = videoEsDTO.getTags();
        if (CollectionUtils.isNotEmpty(tagList)) {
            video.setTags(GSON.toJson(tagList));
        }
        return video;
    }
}
