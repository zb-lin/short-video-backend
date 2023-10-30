package com.lzb.shortvideo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 自定义缓存注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cache {
    /**
     * key的前缀,默认取方法全限定名，除非我们在不同方法上对同一个资源做缓存，就自己指定
     *
     * @return key的前缀
     */
    String prefixKey() default "";

    /**
     * springEl 表达式
     *
     * @return 表达式
     */
    String spEl() default "";

    /**
     * 缓存时间范围，默认单位秒
     *
     * @return 时间范围
     */
    long time() default -1;

    /**
     * 缓存时间单位，默认秒
     *
     * @return 单位
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 结果的类型
     */
    Class<?> resultClass() default String.class;

    /**
     * 是否为集合
     *
     * @return
     */
    boolean isList() default false;
}
