package com.lzb.shortvideo.aop;

import cn.hutool.core.util.StrUtil;
import com.lzb.shortvideo.annotation.RedissonLock;
import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.exception.BusinessException;
import com.lzb.shortvideo.utils.SpElUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁切面
 */
@Slf4j
@Aspect
@Component
@Order(0)//确保比事务注解先执行，分布式锁在事务外
public class RedissonLockAspect {

    @Resource
    private RedissonClient redissonClient;

    @Around("@annotation(com.lzb.shortvideo.annotation.RedissonLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("come in");
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);
        // 默认方法限定名
        String prefix = StrUtil.isBlank(redissonLock.prefixKey()) ? SpElUtils.getMethodKey(method) : redissonLock.prefixKey();
        String key;
        if (StrUtil.isNotBlank(redissonLock.key())) {
            key = prefix + ":" + SpElUtils.parseSpEl(method, joinPoint.getArgs(), redissonLock.key());
        } else {
            key = prefix;
        }
        if (redissonLock.throwIfNot()) {
            return executeWithLockThrows(key, redissonLock, joinPoint::proceed);
        } else {
            return executeWithLock(key, redissonLock, joinPoint::proceed);
        }
    }

    public <T> T executeWithLockThrows(String key, RedissonLock redissonLock, SupplierThrow<T> supplier) throws Throwable {
        RLock lock = redissonClient.getLock(key);
        RedissonLock.TYPE type = redissonLock.type();
        int waitTime = redissonLock.waitTime();
        TimeUnit unit = redissonLock.unit();
        if (RedissonLock.TYPE.ABANDON == type) {
            try {
                boolean lockSuccess = lock.tryLock(waitTime, unit);
                if (!lockSuccess) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                return supplier.get();//执行锁内的代码逻辑
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } else {
            return competitionLock(lock, waitTime, unit, supplier);
        }
    }

    @SneakyThrows
    public <T> T executeWithLock(String key, RedissonLock redissonLock, SupplierThrow<T> supplier) {
        RLock lock = redissonClient.getLock(key);
        RedissonLock.TYPE type = redissonLock.type();
        int waitTime = redissonLock.waitTime();
        TimeUnit unit = redissonLock.unit();
        if (RedissonLock.TYPE.ABANDON == type) {
            try {
                boolean lockSuccess = lock.tryLock(waitTime, unit);
                if (!lockSuccess) {
                    return null;
                }
                return supplier.get();//执行锁内的代码逻辑
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } else {
            return competitionLock(lock, waitTime, unit, supplier);
        }
    }

    public <T> T competitionLock(RLock lock, int waitTime, TimeUnit unit, SupplierThrow<T> supplier) throws Throwable {
        boolean lockSuccess = lock.tryLock(waitTime, unit);
        while (!lockSuccess) {
            lockSuccess = lock.tryLock(waitTime, unit);
            log.debug(Thread.currentThread().getName() + ": 抢锁失败");
        }
        try {
            return supplier.get();//执行锁内的代码逻辑
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    @FunctionalInterface
    public interface SupplierThrow<T> {

        /**
         * Gets a result.
         *
         * @return a result
         */
        T get() throws Throwable;
    }
}
