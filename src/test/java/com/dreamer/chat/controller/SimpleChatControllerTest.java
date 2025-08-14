package com.dreamer.chat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dreamer.chat.service.*;

/**
 * 简化的聊天控制器测试
 * 用于调试Spring上下文配置问题
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@WebMvcTest(ChatController.class)
@ActiveProfiles("test")
public class SimpleChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private ConversationService conversationService;

    @MockBean
    private PromptBuilderService promptBuilderService;

    @MockBean
    private PromptTemplateService promptTemplateService;

    @MockBean
    private StreamChatService streamChatService;

    @BeforeEach
    public void setUp() {
        // Mock StreamChatService behavior
        CompletableFuture<Void> mockFuture = CompletableFuture.completedFuture(null);
        when(streamChatService.processStreamChatAsync(any(), any(SseEmitter.class)))
                .thenReturn(mockFuture);
    }

    @Test
    public void testBasicChatEndpoint() throws Exception {
        mockMvc.perform(post("/chat/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"test\",\"modelProvider\":\"qianwen\"}"))
                .andExpect(status().isOk());
    }
}