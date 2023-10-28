package com.lzb.shortvideo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rabbitmq.video")
@Data
public class RabbitmqClientConfig {
    private String host;
    private String exchangeName;
    private String exchangeType;
    private String queueName;
    private String routingKey;
}
