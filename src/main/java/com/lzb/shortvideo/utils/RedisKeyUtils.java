package com.lzb.shortvideo.utils;

public class RedisKeyUtils {

    public static String getUUID() {
        return RedisUtils.getStr("redisKey");
    }

    public static String getUUID(long id) {
        return RedisUtils.getStr("redisKey:" + id);
    }

    public static void setUUID(String UUID) {
        RedisUtils.set("redisKey", UUID);
    }

    public static void setUUID(String UUID, long id) {
        RedisUtils.set("redisKey:" + id, UUID);
    }
}
