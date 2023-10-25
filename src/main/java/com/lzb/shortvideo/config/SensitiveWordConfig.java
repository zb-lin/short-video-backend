package com.lzb.shortvideo.config;

import com.lzb.shortvideo.utils.sensitive.sensitive.MyWordFactory;
import com.lzb.shortvideo.utils.sensitive.sensitiveWord.DFAFilter;
import com.lzb.shortvideo.utils.sensitive.sensitiveWord.SensitiveWordBs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class SensitiveWordConfig {

    @Resource
    private MyWordFactory myWordFactory;

    /**
     * 初始化引导类
     */
    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        return SensitiveWordBs.newInstance()
                .filterStrategy(DFAFilter.getInstance())
                .sensitiveWord(myWordFactory)
                .init();
    }

}