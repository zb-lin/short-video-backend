package com.lzb.shortvideo.manager;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lzb.shortvideo.esdao.VideoEsDao;
import com.lzb.shortvideo.model.dto.video.VideoEsDTO;
import com.lzb.shortvideo.model.entity.Video;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 通过 canal 同步 mysql 数据到 es
 */
@Slf4j
// todo 开启同步
//@Component
public class CanalClient implements CommandLineRunner {

    @Resource
    private VideoEsDao videoEsDao;

    private static final String SUBSCRIBE = "short_video.video";

    @Override
    public void run(String... args) throws Exception {
        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(
                "127.0.0.1", 11111), "example", "", "");
        int batchSize = 1000;
        int emptyCount = 0;
        try {
            connector.connect();
            connector.subscribe(SUBSCRIBE);
            connector.rollback();
            while (true) {
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                try {
                    if (batchId == -1 || size == 0) {
                        emptyCount++;
                        if (emptyCount > 1800) {
                            try {
                                TimeUnit.SECONDS.sleep(1800);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        emptyCount = 0;
                        printEntry(message.getEntries());
                    }
                    connector.ack(batchId); // 提交确认
                } catch (Exception e) {
                    connector.rollback(batchId); // 处理失败, 回滚数据
                    log.error("error={}", e.getMessage());
                }
            }
        } finally {
            connector.disconnect();
        }
    }

    private void printEntry(List<CanalEntry.Entry> entries) throws InvalidProtocolBufferException {
        for (CanalEntry.Entry entry : entries) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }
            // 反序列化数据
            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            // 获取当前事件操作类型
            CanalEntry.EventType eventType = rowChange.getEventType();
            // 获取数据集
            List<CanalEntry.RowData> rowDataList = rowChange.getRowDatasList();

            if (eventType == CanalEntry.EventType.INSERT || eventType == CanalEntry.EventType.UPDATE) {
                List<VideoEsDTO> videoEsDTOList = rowDataList.stream().map(rowData -> {
                    Map<String, String> map = new HashMap<>();
                    rowData.getAfterColumnsList().forEach(column -> {
                        map.put(column.getName(), column.getValue());
                    });
                    Video video = BeanUtil.toBean(map, Video.class);
                    System.out.println(video);
                    return video;
                }).map(VideoEsDTO::objToDto).collect(Collectors.toList());
                videoEsDao.saveAll(videoEsDTOList);
            } else if (eventType == CanalEntry.EventType.DELETE) {
                List<Long> idList = rowDataList.stream()
                        .map(rowData -> Long.valueOf(rowData.getBeforeColumns(0).getValue()))
                        .collect(Collectors.toList());
                // es批量删除文档
                videoEsDao.deleteAllById(idList);
            }
        }
    }
}