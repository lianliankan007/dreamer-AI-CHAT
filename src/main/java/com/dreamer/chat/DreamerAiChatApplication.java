package com.dreamer.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 梦想家AI聊天应用主启动类
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@SpringBootApplication(exclude = {
        org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class
})
@EnableAsync
public class DreamerAiChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(DreamerAiChatApplication.class, args);
    }
}