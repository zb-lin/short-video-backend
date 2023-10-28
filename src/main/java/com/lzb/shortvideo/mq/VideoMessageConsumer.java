package com.lzb.shortvideo.mq;

import com.lzb.shortvideo.config.CosClientConfig;
import com.lzb.shortvideo.config.RabbitmqClientConfig;
import com.lzb.shortvideo.model.enums.FileUploadBizEnum;
import com.qiniu.common.QiniuException;
import com.qiniu.processing.OperationManager;
import com.qiniu.processing.OperationStatus;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.Auth;
import com.qiniu.util.UrlSafeBase64;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class VideoMessageConsumer {

    @Resource
    private RabbitmqClientConfig rabbitmqClientConfig;
    @Resource
    private CosClientConfig cosClientConfig;
    private static final String FOPS = "vframe/jpg/offset/1|saveas/%s";

    private static final String PIPELINE = "default.sys";
    /**
     * 数据处理完成结果通知地址
     */
    private static final String persistentNotifyUrl = "http://localhost:8081/api/file/vframe";

    /**
     * 指定程序监听的消息队列和确认机制
     */
    @SneakyThrows
    @RabbitListener(queues = {"video_queue"}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        channel.basicAck(deliveryTag, false);
    }

}