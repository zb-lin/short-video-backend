package com.lzb.shortvideo.utils;

import com.lzb.shortvideo.config.CosClientConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.processing.OperationManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

/**
 * EasyExcel 测试
 */
@SpringBootTest
public class EasyExcelTest {

    @Test
    public void doImport() throws FileNotFoundException {
//        File file = ResourceUtils.getFile("classpath:test_excel.xlsx");
//        List<Map<Integer, String>> list = EasyExcel.read(file)
//                .excelType(ExcelTypeEnum.XLSX)
//                .sheet()
//                .headRowNumber(0)
//                .doReadSync();
//        System.out.println(list);
    }

    @Resource
    private CosClientConfig cosClientConfig;

    @Test
    void vframe() throws QiniuException {
        //设置账号的AK,SK
        Auth auth = Auth.create(cosClientConfig.getAccessKey(), cosClientConfig.getSecretKey());
        Configuration cfg = new Configuration(Region.region2());
        //新建一个OperationManager对象
        String upToken = cosClientConfig.getUpToken();
        OperationManager operater = new OperationManager(auth, cfg);
        //设置要转码的空间和key，并且这个key在你空间中存在
        String bucket = "shortvideobucket";
        String key = "http://s32x7tly6.hn-bkt.clouddn.com/pexels-eberhard-grossgasteiger-10079386%20%281080p%29.mp4";
        //设置转码操作参数
        String fops = "vframe/jpg/offset/1/w/480/h/360/rotate/90";
        //设置转码的队列
        String pipeline = "pipeline";
        //可以对转码后的文件进行使用saveas参数自定义命名，当然也可以不指定文件会默认命名并保存在当前空间。
        String urlbase64 = UrlSafeBase64.encodeToString("photo");
        String pfops = fops + "|saveas/" + urlbase64;
        //设置pipeline参数
        StringMap params = new StringMap().putWhen("force", 1, true).putNotEmpty("pipeline", pipeline);
        try {
            String persistid = operater.pfop(bucket, key, pfops, params);
            //打印返回的persistid
            System.out.println(persistid);
        } catch (QiniuException e) {
            //捕获异常信息
            Response r = e.response;
            // 请求失败时简单状态信息
            System.out.println(r.toString());
            try {
                // 响应的文本信息
                System.out.println(r.bodyString());
            } catch (QiniuException e1) {
                //ignore
            }
        }
    }

}