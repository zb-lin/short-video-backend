package com.lzb.shortvideo.aop;

import cn.hutool.core.util.StrUtil;
import com.lzb.shortvideo.annotation.Cache;
import com.lzb.shortvideo.utils.RedisUtils;
import com.lzb.shortvideo.utils.SpElUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;


/**
 * 缓存实现
 */
@Slf4j
@Aspect
@Component
public class CacheAspect {

    @Around("@annotation(com.lzb.shortvideo.annotation.Cache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Cache cache = method.getAnnotation(Cache.class);
        String prefix = StrUtil.isBlank(cache.prefixKey()) ? SpElUtils.getMethodKey(method) : cache.prefixKey();
        String key = "";
        if (StrUtil.isNotBlank(cache.spEl())) {
            key = prefix + ":" + SpElUtils.parseSpEl(method, joinPoint.getArgs(), cache.spEl());
        } else {
            key = prefix;
        }
        long time = cache.time();
        TimeUnit unit = cache.unit();
        Class<?> resultClass = cache.resultClass();
        boolean b = cache.isList();
        Object result;
        if (!b) {
            result = RedisUtils.get(key, resultClass);
        } else {
            result = RedisUtils.getList(key, resultClass);
        }
        if (result == null) {
            result = joinPoint.proceed();
            RedisUtils.set(key, result, time, unit);
        }
        return result;
    }
}
