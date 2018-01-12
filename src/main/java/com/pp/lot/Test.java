package com.pp.lot;


import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;

import java.util.concurrent.TimeUnit;

/**
 * Created by admin on 2018/1/12.
 */
public class Test {

    public static void main(String[] args) {
        // 构建一个缓存管理器
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
        // cacheManager.init();
        // cacheConfiguration -100个换成最大，缓存过期时间4秒
        CacheConfiguration<String, String> cacheConfiguration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, String.class, ResourcePoolsBuilder.heap(100))
                .withExpiry(Expirations.timeToLiveExpiration(Duration.of(4, TimeUnit.SECONDS))).build();
        // 根据配置创建一个缓存容器
        Cache<String, String> myCache = cacheManager.createCache("myCache", cacheConfiguration);
        // 设置一个值
        myCache.put("testKey", "testValue");
        // 循环直到数据过期 否包含该key
        while (myCache.containsKey("testKey")) {
            try {
                System.out.println("值:" + myCache.get("testKey"));
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("缓存已过期");
        cacheManager.close();

    }
}
