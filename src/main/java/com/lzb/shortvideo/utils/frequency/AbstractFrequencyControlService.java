package com.lzb.shortvideo.utils.frequency;


import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

/**
 * 抽象类频控服务 其他类如果要实现限流服务 直接注入使用通用限流类
 * 后期会通过继承此类实现令牌桶等算法
 *
 * @see TotalCountWithInFixTimeFrequencyController 通用限流类
 */
@Slf4j
public abstract class AbstractFrequencyControlService<K extends FrequencyControlDTO> {

    @PostConstruct
    protected void registerMyselfToFactory() {
        FrequencyControlStrategyFactory.registerFrequencyController(getStrategyName(), this);
    }

    /**
     * 单限流策略的调用方法-编程式调用
     *
     * @param frequencyControl 单个频控对象
     * @param supplier         服务提供着
     * @return 业务方法执行结果
     * @throws Throwable
     */
    public <T> T executeWithFrequencyControl(K frequencyControl, SupplierThrowWithoutParam<T> supplier) throws Throwable {
        if (reachRateLimit(frequencyControl)) {
            throw new BusinessException(ErrorCode.FREQUENCY_LIMIT);
        }
        try {
            return supplier.get();
        } finally {
            //不管成功还是失败，都增加次数
            addFrequencyControlStatisticsCount(frequencyControl);
        }
    }


    @FunctionalInterface
    public interface SupplierThrowWithoutParam<T> {

        /**
         * Gets a result.
         *
         * @return a result
         */
        T get() throws Throwable;
    }


    /**
     * 是否达到限流阈值 子类实现 每个子类都可以自定义自己的限流逻辑判断
     *
     * @param frequencyControl 定义的注解频控 Key-对应redis的单个频控的Key Value-对应redis的单个频控的Key限制的Value
     * @return true-方法被限流 false-方法没有被限流
     */
    protected abstract boolean reachRateLimit(K frequencyControl);

    /**
     * 增加限流统计次数 子类实现 每个子类都可以自定义自己的限流统计信息增加的逻辑
     *
     * @param frequencyControl 定义的注解频控 Key-对应redis的单个频控的Key Value-对应redis的单个频控的Key限制的Value
     */
    protected abstract void addFrequencyControlStatisticsCount(K frequencyControl);

    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    protected abstract String getStrategyName();

}
