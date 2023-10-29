package com.lzb.shortvideo.kafka;

import com.alibaba.otter.canal.client.kafka.KafkaCanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Kafka consumer获取Message的测试例子
 *
 * @author machengyuan @ 2018-6-12
 * @version 1.0.0
 */
@Slf4j
public class KafkaClientRunningTest extends AbstractKafkaTest {


    private boolean running = true;

    public void testKafkaConsumer() {
        final ExecutorService executor = Executors.newFixedThreadPool(1);
        final KafkaCanalConnector connector = new KafkaCanalConnector(servers, topic, partition, groupId, null, false);
        executor.submit(() -> {
            connector.connect();
            connector.subscribe();
            while (running) {
                List<Message> messages = connector.getList(3L, TimeUnit.SECONDS);
                if (messages != null) {
                    System.out.println(messages);
                }
                connector.ack();
            }
            connector.unsubscribe();
            connector.disconnect();
        });

        sleep(60000);
        running = false;
        executor.shutdown();
        log.info("shutdown completed");
    }

}