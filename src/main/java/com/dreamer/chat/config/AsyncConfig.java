package com.dreamer.chat.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步配置类
 * 配置异步任务执行器
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 配置异步任务执行器
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数 - 适应流式处理需求
        executor.setCorePoolSize(10);

        // 最大线程数 - 支持更多并发流式连接
        executor.setMaxPoolSize(50);

        // 队列容量 - 增加队列容量应对突发请求
        executor.setQueueCapacity(200);

        // 线程名前缀
        executor.setThreadNamePrefix("stream-chat-");

        // 拒绝策略：由调用线程处理该任务
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间 - 给流式处理更多时间完成
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}