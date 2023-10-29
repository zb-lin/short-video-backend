package com.lzb.shortvideo.utils;

import com.lzb.shortvideo.config.CosClientConfig;
import com.lzb.shortvideo.job.cycle.ClearVideoJob;
import com.qiniu.common.QiniuException;
import com.qiniu.processing.OperationManager;
import com.qiniu.processing.OperationStatus;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.qiniu.util.UrlSafeBase64;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class VframeTest {
    @Resource
    private CosClientConfig cosClientConfig;
    public static void main(String[] args) {
        String url = "http://s32x7tly6.hn-bkt.clouddn.com/video/1717188659762479105/cWQKfqun-pexels-eberhard-grossgasteiger-10079386 (1080p).mp4";
        int index = url.lastIndexOf('/');
        String key = url.substring(index + 1);
        System.out.println(key);
    }
    @Test
    void test(){
        // 构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region2());
        //...其他参数参考类注释
        String accessKey = cosClientConfig.getAccessKey();
        String secretKey = cosClientConfig.getSecretKey();
        String bucketName = cosClientConfig.getBucketName();

        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);
        // 文件名前缀
        String prefix = "c";
        // 每次迭代的长度限制，最大1000，推荐值 1000
        int limit = 1000;
        // 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
        String delimiter = "";
        // 列举空间文件列表
        BucketManager.FileListIterator fileListIterator =
                bucketManager.createFileListIterator(bucketName, prefix, limit, delimiter);
        while (fileListIterator.hasNext()) {
            //处理获取的file list结果
            FileInfo[] items = fileListIterator.next();
            for (FileInfo item : items) {
                System.out.println(item.key);
            }
        }
    }

    @Test
    void vframe() throws QiniuException {
//待处理文件名
        String key = "pexels-eberhard-grossgasteiger-10079386 (1080p).mp4";
        String bucket = cosClientConfig.getBucketName();;
        Auth auth = Auth.create(cosClientConfig.getAccessKey(), cosClientConfig.getSecretKey());
//数据处理指令，支持多个指令
        String saveMp4Entry = String.format("%s:avthumb_test_target.mp4", bucket);
        String saveJpgEntry = String.format("%s:vframe_test_target.jpg", bucket);
        String avthumbMp4Fop = String.format("avthumb/mp4|saveas/%s", UrlSafeBase64.encodeToString(saveMp4Entry));
        String vframeJpgFop = String.format("vframe/jpg/offset/1|saveas/%s", UrlSafeBase64.encodeToString(saveJpgEntry));
//将多个数据处理指令拼接起来
        String persistentOpfs = StringUtils.join(new String[]{
                avthumbMp4Fop, vframeJpgFop
        }, ";");
//数据处理队列名称，必须
        String persistentPipeline = "default.sys";
//数据处理完成结果通知地址
        String persistentNotifyUrl = "http://localhost:8081/qiniu/pfop/notify";
//构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region0());
//...其他参数参考类注释
//构建持久化数据处理对象
        OperationManager operationManager = new OperationManager(auth, cfg);
        try {
            String persistentId = operationManager.pfop(bucket, key, persistentOpfs, persistentPipeline, persistentNotifyUrl, true);
            //可以根据该 persistentId 查询任务处理进度
            System.out.println(persistentId);
            try {
                 TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                 throw new RuntimeException(e);
            }
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
//        //设置账号的AK,SK
//        Auth auth = Auth.create(cosClientConfig.getAccessKey(), cosClientConfig.getSecretKey());
//        Configuration cfg = new Configuration(Region.region2());
//        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;  // 指定分片上传版本
//        cfg.resumableUploadMaxConcurrentTaskCount = 2;  // 设置分片上传并发，1：采用同步上传；大于1：采用并发上传
//        //新建一个OperationManager对象
//        String uploadToken = auth.uploadToken(cosClientConfig.getBucketName());
//        OperationManager operater = new OperationManager(auth, cfg);
//        //设置要转码的空间和key，并且这个key在你空间中存在
//        String bucket = "shortvideobucket";
//        String key = "pexels-eberhard-grossgasteiger-10079386 (1080p).mp4";
//        //设置转码操作参数
//        String fops = "vframe/jpg/offset/1/w/480/h/360/rotate/90";
//        //设置转码的队列
//        String pipeline = "default.sys";
//        //可以对转码后的文件进行使用saveas参数自定义命名，当然也可以不指定文件会默认命名并保存在当前空间。
//        String urlbase64 = UrlSafeBase64.encodeToString("目标Bucket_Name:自定义文件key");
//        String pfops = fops + "|saveas/" + urlbase64;
//        //设置pipeline参数
//        StringMap params = new StringMap().putWhen("force", 1, true).putNotEmpty("pipeline", pipeline);
//        try {
//            String persistid = operater.pfop(bucket, key, pfops, params);
//            //打印返回的persistid
//            System.out.println(persistid);
//        } catch (QiniuException e) {
//            //捕获异常信息
//            Response r = e.response;
//            // 请求失败时简单状态信息
//            System.out.println(r.toString());
//            try {
//                // 响应的文本信息
//                System.out.println(r.bodyString());
//            } catch (QiniuException e1) {
//                //ignore
//            }
//        }
    }
}
