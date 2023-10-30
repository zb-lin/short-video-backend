package com.lzb.shortvideo.config;

import com.lzb.shortvideo.intecepter.CollectorInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * 配置所有拦截器
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Resource
    private CollectorInterceptor collectorInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(collectorInterceptor)
                .addPathPatterns("/file/**", "/comment/**", "/video/**");
    }
}
