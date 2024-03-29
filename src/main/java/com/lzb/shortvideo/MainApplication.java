package com.lzb.shortvideo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 主类（项目启动入口）
 */
@SpringBootApplication()
@MapperScan({"com.lzb.**.mapper"})
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class MainApplication {

    public  static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

}
