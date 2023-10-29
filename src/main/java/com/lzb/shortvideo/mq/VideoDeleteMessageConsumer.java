package com.lzb.shortvideo.mq;

import com.lzb.shortvideo.manager.CosManager;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class VideoDeleteMessageConsumer {

    @Resource
    private CosManager cosManager;

    /**
     * 指定程序监听的消息队列和确认机制
     */
    @SneakyThrows
    @RabbitListener(queues = {"video_delete_queue"}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        long id = Long.parseLong(message);
        cosManager.deleteObject(id);
        channel.basicAck(deliveryTag, false);
    }

}