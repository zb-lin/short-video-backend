package com.lzb.shortvideo.aop;

import cn.hutool.core.util.StrUtil;
import com.lzb.shortvideo.annotation.FrequencyControl;
import com.lzb.shortvideo.utils.RequestHolder;
import com.lzb.shortvideo.utils.SpElUtils;
import com.lzb.shortvideo.utils.frequency.AbstractFrequencyControlService;
import com.lzb.shortvideo.utils.frequency.FrequencyControlDTO;
import com.lzb.shortvideo.utils.frequency.FrequencyControlStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import static com.lzb.shortvideo.utils.frequency.FrequencyControlStrategyFactory.TOTAL_COUNT_WITH_IN_FIX_TIME_FREQUENCY_CONTROLLER;


/**
 * 频控实现
 */
@Slf4j
@Aspect
@Component
public class FrequencyControlAspect {

    @Around("@annotation(com.lzb.shortvideo.annotation.FrequencyControl)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        FrequencyControl frequencyControl = method.getAnnotation(FrequencyControl.class);
        String prefix = StrUtil.isBlank(frequencyControl.prefixKey()) ? SpElUtils.getMethodKey(method) : frequencyControl.prefixKey();
        String key = "";
        switch (frequencyControl.target()) {
            case EL:
                key = SpElUtils.parseSpEl(method, joinPoint.getArgs(), frequencyControl.spEl());
                break;
            case IP:
                key = RequestHolder.get().getIp();
                break;
            case UID:
                key = RequestHolder.get().getUid().toString();
        }
        FrequencyControlDTO frequencyControlDTO = FrequencyControlDTO.builder()
                .key(prefix + ":" + key)
                .count(frequencyControl.count())
                .unit(frequencyControl.unit())
                .time(frequencyControl.time())
                .build();
        return executeWithFrequencyControl(TOTAL_COUNT_WITH_IN_FIX_TIME_FREQUENCY_CONTROLLER, frequencyControlDTO, joinPoint::proceed);
    }

    /**
     * 单限流策略的调用方法-编程式调用
     *
     * @param strategyName        策略名称
     * @param frequencyControlDTO 单个频控对象
     * @param supplier            服务提供着
     * @return 业务方法执行结果
     */
    public static <T, K extends FrequencyControlDTO> T executeWithFrequencyControl(String strategyName, K frequencyControlDTO, AbstractFrequencyControlService.SupplierThrowWithoutParam<T> supplier) throws Throwable {
        AbstractFrequencyControlService<K> frequencyController = FrequencyControlStrategyFactory.getFrequencyControllerByName(strategyName);
        return frequencyController.executeWithFrequencyControl(frequencyControlDTO, supplier);
    }


}
