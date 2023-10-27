package com.lzb.shortvideo.manager;

import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.config.CosClientConfig;
import com.lzb.shortvideo.exception.BusinessException;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.processing.OperationManager;
import com.qiniu.processing.OperationStatus;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Auth;
import com.qiniu.util.UrlSafeBase64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    private static final String POPS = "vframe/jpg/offset/1|saveas/%s";

    private static final String PIPELINE = "default.sys";

    /**
     * 上传对象
     *
     * @return
     */
    public void putObject(String filepath, File file, String thumbnailPath) {
        String bucketName = cosClientConfig.getBucketName();
        String accessKey = cosClientConfig.getAccessKey();
        String secretKey = cosClientConfig.getSecretKey();
        // 构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region2());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;  // 指定分片上传版本
        cfg.resumableUploadMaxConcurrentTaskCount = 2;  // 设置分片上传并发，1：采用同步上传；大于1：采用并发上传
        //...其他参数参考类注释
        //...生成上传凭证，然后准备上传
        String upToken = cosClientConfig.getUpToken();
        String localTempDir = Paths.get(System.getenv("java.io.tmpdir"), bucketName).toString();
        try {
            //设置断点续传文件进度保存目录
            FileRecorder fileRecorder = new FileRecorder(localTempDir);
            UploadManager uploadManager = new UploadManager(cfg, fileRecorder);
            // 上传文件
            Response response = uploadManager.put(file, filepath, upToken);

            Auth auth = Auth.create(accessKey, secretKey);
            // 数据处理指令，支持多个指令
            String saveJpgEntry = String.format("%s:%s", bucketName, thumbnailPath);
            String vframeJpgFop = String.format(POPS, UrlSafeBase64.encodeToString(saveJpgEntry));
            // 将多个数据处理指令拼接起来
            String persistentOpfs = StringUtils.join(new String[]{
                    vframeJpgFop
            }, ";");
            // 数据处理队列名称，必须
            // 数据处理完成结果通知地址
            String persistentNotifyUrl = "http://localhost:8081/api/vframe";
            // 构造一个带指定 Region 对象的配置类
            // 构建持久化数据处理对象
            OperationManager operationManager = new OperationManager(auth, cfg);
            try {
                String persistentId = operationManager.pfop(bucketName, filepath, persistentOpfs, PIPELINE, persistentNotifyUrl, true);
                //可以根据该 persistentId 查询任务处理进度
                System.out.println(persistentId);
                OperationStatus operationStatus = operationManager.prefop(persistentId);
                //解析 operationStatus 的结果
                System.out.println(operationStatus.code);
                System.out.println(operationStatus.desc);
                System.out.println(operationStatus.inputKey);
                for (OperationStatus.OperationResult item : operationStatus.items) {
                    System.out.println(item.key);
                }
            } catch (QiniuException e) {
                System.err.println(e.response.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }


}
