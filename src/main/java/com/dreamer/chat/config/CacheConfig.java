package com.dreamer.chat.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存配置类
 * 为Prompt模板系统提供缓存支持
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置缓存管理器
     * 
     * @return 缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();

        // 设置缓存名称
        cacheManager.setCacheNames(
                java.util.List.of(
                        "promptTemplates", // Prompt模板缓存
                        "defaultChatTemplates" // 默认聊天模板缓存
                ));

        // 允许null值缓存
        cacheManager.setAllowNullValues(true);

        return cacheManager;
    }
}