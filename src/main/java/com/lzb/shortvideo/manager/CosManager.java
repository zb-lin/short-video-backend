package com.lzb.shortvideo.manager;

import com.google.gson.Gson;
import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.config.CosClientConfig;
import com.lzb.shortvideo.exception.BusinessException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.persistent.FileRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Cos 对象存储操作
 */
@Component
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;


    /**
     * 上传对象
     *
     * @return
     */
    public void putObject(String filepath, File file) {
        // 构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region2());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;  // 指定分片上传版本
        cfg.resumableUploadMaxConcurrentTaskCount = 2;  // 设置分片上传并发，1：采用同步上传；大于1：采用并发上传
        //...其他参数参考类注释
        //...生成上传凭证，然后准备上传
        String upToken = cosClientConfig.getUpToken();
        String localTempDir = Paths.get(System.getenv("java.io.tmpdir"), cosClientConfig.getBucket()).toString();
        try {
            //设置断点续传文件进度保存目录
            FileRecorder fileRecorder = new FileRecorder(localTempDir);
            UploadManager uploadManager = new UploadManager(cfg, fileRecorder);
            // 上传文件
            Response response = uploadManager.put(file, filepath, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }
}
