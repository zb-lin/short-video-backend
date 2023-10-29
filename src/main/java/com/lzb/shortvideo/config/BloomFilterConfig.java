package com.lzb.shortvideo.config;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomFilterConfig {

/*    @Bean
    public BloomFilter<String> addBloomFilter() {
        return BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8), 1000000, 0.004);
    }*/

    @Bean
    public BitMapBloomFilter addBitMapBloomFilter() {
        return new BitMapBloomFilter(10);
    }

}