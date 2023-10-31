package com.lzb.shortvideo.model.vo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzb.shortvideo.model.entity.Video;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子视图
 */
@Data
public class VideoVO implements Serializable {

    private final static Gson GSON = new Gson();

    /**
     * id
     */
    private Long id;

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
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 创建人信息
     */
    private UserVO user;

    /**
     * 是否已点赞
     */
    private Boolean hasThumb;

    /**
     * 是否已收藏
     */
    private Boolean hasFavour;

    /**
     * 包装类转对象
     *
     * @param videoVO
     * @return
     */
    public static Video voToObj(VideoVO videoVO) {
        if (videoVO == null) {
            return null;
        }
        Video video = new Video();
        BeanUtils.copyProperties(videoVO, video);
        List<String> tagList = videoVO.getTagList();
        if (tagList != null) {
            video.setTags(GSON.toJson(tagList));
        }
        return video;
    }

    /**
     * 对象转包装类
     *
     * @param video
     * @return
     */
    public static VideoVO objToVo(Video video) {
        if (video == null) {
            return null;
        }
        VideoVO videoVO = new VideoVO();
        BeanUtils.copyProperties(video, videoVO);
        videoVO.setTagList(GSON.fromJson(video.getTags(), new TypeToken<List<String>>() {
        }.getType()));
        return videoVO;
    }
}
