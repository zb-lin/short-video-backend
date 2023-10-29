package com.lzb.shortvideo.mq;

import com.lzb.shortvideo.config.RabbitmqClientConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class VideoDeleteMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend("video_delete_exchange", "video_delete_routingKey", message);
    }

}