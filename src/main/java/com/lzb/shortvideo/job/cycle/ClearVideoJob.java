package com.lzb.shortvideo.job.cycle;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.annotations.Beta;
import com.lzb.shortvideo.config.CosClientConfig;
import com.lzb.shortvideo.model.entity.Video;
import com.lzb.shortvideo.service.VideoService;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lzb.shortvideo.constant.RedisConstant.VIDEO_RECOMMEND_LOCK;

/**
 * 定期删除无用文件
 */
//@Component
@Slf4j
@Deprecated
public class ClearVideoJob {


    @Resource
    private RedissonClient redissonClient;
    @Resource
    private VideoService videoService;
    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 每一小时执行任务
     */
    @Scheduled(cron = "0 0 */1 * * ?")
    public void doClearVideo() {
        RLock lock = redissonClient.getLock(VIDEO_RECOMMEND_LOCK);
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
                queryWrapper.select("url");
                List<String> urls = videoService.list(queryWrapper)
                        .stream().map(Video::getUrl).collect(Collectors.toList());

                // 构造一个带指定 Region 对象的配置类
                Configuration cfg = new Configuration(Region.region2());
                //...其他参数参考类注释
                String accessKey = cosClientConfig.getAccessKey();
                String secretKey = cosClientConfig.getSecretKey();
                String bucketName = cosClientConfig.getBucketName();

                Auth auth = Auth.create(accessKey, secretKey);
                BucketManager bucketManager = new BucketManager(auth, cfg);
                // 文件名前缀
                String prefix = "";
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
                        System.out.println(item.hash);
                        System.out.println(item.fsize);
                        System.out.println(item.mimeType);
                        System.out.println(item.putTime);
                        System.out.println(item.endUser);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
