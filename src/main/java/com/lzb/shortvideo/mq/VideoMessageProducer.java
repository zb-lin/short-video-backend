package com.lzb.shortvideo.mq;

import com.lzb.shortvideo.config.RabbitmqClientConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class VideoMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RabbitmqClientConfig rabbitmqClientConfig;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(rabbitmqClientConfig.getExchangeName(), rabbitmqClientConfig.getRoutingKey(), message);
    }

}