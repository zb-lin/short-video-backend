package com.lzb.shortvideo.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 用于创建测试程序用到的交换机和队列（只用在程序启动前执行一次）
 */
public class InitMq {

    public static void main(String[] args) {
        InitMq initMq = new InitMq();
        initMq.initDelete();
    }

    public void initDelete(){
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String EXCHANGE_NAME = "video_delete_exchange";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 创建队列，随机分配一个队列名称
            String queueName = "video_delete_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, EXCHANGE_NAME, "video_delete_routingKey");
        } catch (Exception e) {

        }
    }

}
