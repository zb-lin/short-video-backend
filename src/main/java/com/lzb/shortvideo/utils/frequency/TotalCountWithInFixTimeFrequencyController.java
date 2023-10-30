package com.lzb.shortvideo.utils.frequency;

import com.lzb.shortvideo.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.lzb.shortvideo.utils.frequency.FrequencyControlStrategyFactory.TOTAL_COUNT_WITH_IN_FIX_TIME_FREQUENCY_CONTROLLER;


/**
 * 抽象类频控服务 -使用redis实现 固定时间内不超过固定次数的限流类
 */
@Slf4j
@Service
public class TotalCountWithInFixTimeFrequencyController extends AbstractFrequencyControlService<FrequencyControlDTO> {


    /**
     * 是否达到限流阈值 子类实现 每个子类都可以自定义自己的限流逻辑判断
     *
     * @param frequencyControl 定义的注解频控 Key-对应redis的单个频控的Key Value-对应redis的单个频控的Key限制的Value
     * @return true-方法被限流 false-方法没有被限流
     */
    @Override
    protected boolean reachRateLimit(FrequencyControlDTO frequencyControl) {
        //批量获取redis统计的值
        String key = frequencyControl.getKey();
        Integer frequencyControlCount = frequencyControl.getCount();
        Integer count = RedisUtils.get(key, Integer.class);
        if (Objects.nonNull(count) && count >= frequencyControlCount) {
            //频率超过了
            log.warn("frequencyControl limit key:{},count:{}", key, count);
            return true;
        }
        return false;
    }

    /**
     * 增加限流统计次数 子类实现 每个子类都可以自定义自己的限流统计信息增加的逻辑
     *
     * @param frequencyControl 定义的注解频控 Key-对应redis的单个频控的Key Value-对应redis的单个频控的Key限制的Value
     */
    @Override
    protected void addFrequencyControlStatisticsCount(FrequencyControlDTO frequencyControl) {
        RedisUtils.inc(frequencyControl.getKey(), frequencyControl.getTime(), frequencyControl.getUnit());
    }

    @Override
    protected String getStrategyName() {
        return TOTAL_COUNT_WITH_IN_FIX_TIME_FREQUENCY_CONTROLLER;
    }
}
