package com.lzb.shortvideo.config;

import cn.hutool.core.thread.NamedThreadFactory;
import com.lzb.shortvideo.exception.GlobalUncaughtExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@Configuration
@Slf4j
public class ThreadPoolExecutorConfig {

    private ThreadPoolExecutor threadPoolExecutor;

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        threadPoolExecutor = new ThreadPoolExecutor(5, 10, 3000L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(20),
                new NamedThreadFactory("video-thread", null, false,
                        GlobalUncaughtExceptionHandler.getInstance()));
        return threadPoolExecutor;
    }

    @PreDestroy
    public void destroy() {
        log.info("开始销毁线程池");
        try {
            threadPoolExecutor.shutdown();
            if (!threadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPoolExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // 如果当前线程被中断，重新取消所有任务
            threadPoolExecutor.shutdownNow();
            // 保持中断状态
            LockSupport.unpark(Thread.currentThread());
        }
    }
}