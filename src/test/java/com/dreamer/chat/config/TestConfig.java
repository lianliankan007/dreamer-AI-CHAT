package com.dreamer.chat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.dreamer.chat.repository.*;
import com.dreamer.chat.service.*;

/**
 * 测试配置类
 * 提供测试环境所需的Bean配置和Mock对象
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@TestConfiguration
public class TestConfig {

    @MockBean
    private ConversationRepository conversationRepository;

    @MockBean
    private MessageRepository messageRepository;

    @MockBean
    private PromptTemplateRepository promptTemplateRepository;

    @MockBean
    private PromptTemplateService promptTemplateService;

    @MockBean
    private ConversationService conversationService;

    @MockBean
    private PromptBuilderService promptBuilderService;

    @MockBean
    private ChatService chatService;

    /**
     * 创建Mock的ChatClient用于测试
     */
    @MockBean
    public ChatClient chatClient;
}