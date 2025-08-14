package com.dreamer.chat.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dreamer.chat.config.AiModelConfig;
import com.dreamer.chat.config.TestConfig;
import com.dreamer.chat.dto.ChatRequest;
import com.dreamer.chat.entity.Conversation;
import com.dreamer.chat.entity.Message;
import com.dreamer.chat.factory.ChatTestDataFactory;
import com.dreamer.chat.service.StreamChatService;
import com.dreamer.chat.util.JsonUtils;
import com.dreamer.chat.util.SseTestUtils;
import com.dreamer.chat.util.SseTestUtils.SseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ChatController 流式接口测试类
 * 专门测试千问大模型的流式聊天功能
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@WebMvcTest(ChatController.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("千问大模型流式聊天接口测试")
public class ChatControllerStreamTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StreamChatService streamChatService;

    @MockBean
    private AiModelConfig.ChatClientManager chatClientManager;

    private static final String STREAM_CHAT_URL = "/chat/send";
    private static final String TEST_STREAM_URL = "/chat/test-stream";

    @BeforeEach
    void setUp() {
        reset(streamChatService, chatClientManager);
    }

    @Test
    @Order(1)
    @DisplayName("测试千问模型流式聊天 - 成功场景")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testQianwenStreamChat_Success() throws Exception {
        // 准备测试数据
        ChatRequest request = ChatTestDataFactory.createQianwenChatRequest();
        Conversation conversation = ChatTestDataFactory.createTestConversation();
        Message userMessage = ChatTestDataFactory.createUserMessage(conversation.getId(), request.getMessage());
        Message assistantMessage = ChatTestDataFactory.createAssistantMessage(conversation.getId(),
                ChatTestDataFactory.getCompleteQianwenResponse());

        // Mock 异步处理方法
        when(streamChatService.processStreamChatAsync(any(ChatRequest.class), any(SseEmitter.class)))
                .thenAnswer(invocation -> {
                    SseEmitter emitter = invocation.getArgument(1);
                    return CompletableFuture.runAsync(() -> {
                        try {
                            // 模拟流式事件序列
                            simulateQianwenStreamEvents(emitter, conversation, userMessage, assistantMessage);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                });

        // 执行请求
        MvcResult result = mockMvc.perform(post(STREAM_CHAT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andReturn();

        // 等待异步处理完成
        Thread.sleep(2000);

        // 验证Mock调用
        verify(streamChatService, times(1)).processStreamChatAsync(any(ChatRequest.class), any(SseEmitter.class));

        // 验证响应头
        assertTrue(SseTestUtils.isSseResponse(result));
    }

    @Test
    @Order(2)
    @DisplayName("测试千问模型流式聊天 - 继续对话")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testQianwenStreamChat_ContinueConversation() throws Exception {
        // 准备测试数据
        Long conversationId = 1L;
        ChatRequest request = ChatTestDataFactory.createContinueChatRequest(conversationId);
        Conversation conversation = ChatTestDataFactory.createTestConversation();
        Message userMessage = ChatTestDataFactory.createUserMessage(conversationId, request.getMessage());
        Message assistantMessage = ChatTestDataFactory.createAssistantMessage(conversationId,
                "Spring Boot的自动配置机制是通过条件注解和配置类实现的...");

        // Mock 异步处理
        when(streamChatService.processStreamChatAsync(any(ChatRequest.class), any(SseEmitter.class)))
                .thenAnswer(invocation -> {
                    SseEmitter emitter = invocation.getArgument(1);
                    return CompletableFuture.runAsync(() -> {
                        try {
                            simulateQianwenStreamEvents(emitter, conversation, userMessage, assistantMessage);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                });

        // 执行请求
        mockMvc.perform(post(STREAM_CHAT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));

        // 验证对话ID被正确传递
        verify(streamChatService).processStreamChatAsync(argThat(req -> req.getConversationId().equals(conversationId)),
                any(SseEmitter.class));
    }

    @Test
    @Order(3)
    @DisplayName("测试流式聊天 - 无效模型提供商")
    void testStreamChat_InvalidProvider() throws Exception {
        // 准备测试数据
        ChatRequest request = ChatTestDataFactory.createInvalidProviderRequest();

        // Mock 异步处理抛出异常
        when(streamChatService.processStreamChatAsync(any(ChatRequest.class), any(SseEmitter.class)))
                .thenAnswer(invocation -> {
                    SseEmitter emitter = invocation.getArgument(1);
                    return CompletableFuture.runAsync(() -> {
                        try {
                            // 模拟错误事件
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data("{\"error\":\"不支持的模型提供商: " + request.getModelProvider() + "\"}"));
                            emitter.complete();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                });

        // 执行请求
        mockMvc.perform(post(STREAM_CHAT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));

        // 验证错误处理
        verify(streamChatService).processStreamChatAsync(any(ChatRequest.class), any(SseEmitter.class));
    }

    @Test
    @Order(4)
    @DisplayName("测试流式聊天 - 空消息验证")
    void testStreamChat_EmptyMessage() throws Exception {
        // 准备测试数据
        ChatRequest request = ChatTestDataFactory.createEmptyMessageRequest();

        // 执行请求 - 应该返回400错误
        mockMvc.perform(post(STREAM_CHAT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // 验证服务没有被调用
        verifyNoInteractions(streamChatService);
    }

    @Test
    @Order(5)
    @DisplayName("测试简单流式聊天接口")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testSimpleStreamChat() throws Exception {
        // 执行GET请求到测试接口
        mockMvc.perform(get(TEST_STREAM_URL)
                .param("message", "测试千问模型")
                .param("modelProvider", "qianwen"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));
    }

    @Test
    @Order(6)
    @DisplayName("测试流式聊天 - 超时处理")
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testStreamChat_Timeout() throws Exception {
        // 准备测试数据
        ChatRequest request = ChatTestDataFactory.createQianwenChatRequest();

        // Mock 长时间处理
        when(streamChatService.processStreamChatAsync(any(ChatRequest.class), any(SseEmitter.class)))
                .thenAnswer(invocation -> {
                    SseEmitter emitter = invocation.getArgument(1);
                    return CompletableFuture.runAsync(() -> {
                        try {
                            // 模拟超时
                            Thread.sleep(130000); // 超过120秒超时
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                });

        // 执行请求
        mockMvc.perform(post(STREAM_CHAT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));

        // 验证处理被调用
        verify(streamChatService).processStreamChatAsync(any(ChatRequest.class), any(SseEmitter.class));
    }

    @Test
    @Order(7)
    @DisplayName("测试流式聊天 - 边界值测试")
    void testStreamChat_BoundaryValues() throws Exception {
        // 测试各种边界值场景
        String[] testCases = { "min_message", "max_message", "max_title", "special_chars", "unicode" };

        for (String testCase : testCases) {
            ChatRequest request = ChatTestDataFactory.createBoundaryTestRequest(testCase);

            if ("max_message".equals(testCase)) {
                // 超长消息应该返回400错误
                mockMvc.perform(post(STREAM_CHAT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());
            } else {
                // 其他情况应该正常处理
                when(streamChatService.processStreamChatAsync(any(ChatRequest.class), any(SseEmitter.class)))
                        .thenReturn(CompletableFuture.completedFuture(null));

                mockMvc.perform(post(STREAM_CHAT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));
            }
        }
    }

    @Test
    @Order(8)
    @DisplayName("测试流式聊天 - 参数验证")
    void testStreamChat_ParameterValidation() throws Exception {
        // 测试缺少必要参数
        ChatRequest invalidRequest = new ChatRequest();
        // 不设置任何必要字段

        mockMvc.perform(post(STREAM_CHAT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // 验证服务没有被调用
        verifyNoInteractions(streamChatService);
    }

    @Test
    @Order(9)
    @DisplayName("测试流式聊天 - JSON格式错误")
    void testStreamChat_InvalidJson() throws Exception {
        // 发送无效的JSON
        mockMvc.perform(post(STREAM_CHAT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        // 验证服务没有被调用
        verifyNoInteractions(streamChatService);
    }

    @Test
    @Order(10)
    @DisplayName("测试流式聊天 - 不支持的Content-Type")
    void testStreamChat_UnsupportedContentType() throws Exception {
        ChatRequest request = ChatTestDataFactory.createQianwenChatRequest();

        mockMvc.perform(post(STREAM_CHAT_URL)
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());

        // 验证服务没有被调用
        verifyNoInteractions(streamChatService);
    }

    /**
     * 模拟千问模型的流式事件序列
     */
    private void simulateQianwenStreamEvents(SseEmitter emitter, Conversation conversation,
            Message userMessage, Message assistantMessage) throws Exception {

        // 1. 发送开始事件
        emitter.send(SseEmitter.event()
                .name("start")
                .data(JsonUtils.createSseEventData("status", "processing", "message", "开始处理请求")));

        // 2. 发送对话事件
        emitter.send(SseEmitter.event()
                .name("conversation")
                .data(JsonUtils.createSseEventData("conversationId", conversation.getId(), "title",
                        conversation.getTitle())));

        // 3. 发送用户消息确认事件
        emitter.send(SseEmitter.event()
                .name("user_message")
                .data(JsonUtils.createSseEventData("messageId", userMessage.getId(), "content",
                        userMessage.getContent())));

        // 4. 发送AI开始生成事件
        emitter.send(SseEmitter.event()
                .name("ai_start")
                .data(JsonUtils.createSseEventData("provider", "qianwen", "model", "阿里巴巴千问", "promptType", "GENERAL")));

        // 5. 发送流式数据块
        List<String> chunks = ChatTestDataFactory.createQianwenStreamChunks();
        for (int i = 0; i < chunks.size(); i++) {
            emitter.send(SseEmitter.event()
                    .name("ai_chunk")
                    .data(JsonUtils.createSseEventData("chunk", chunks.get(i), "index", i + 1)));

            // 模拟流式传输的延迟
            Thread.sleep(50);
        }

        // 6. 发送完成事件
        emitter.send(SseEmitter.event()
                .name("complete")
                .data(JsonUtils.createSseEventData(
                        "messageId", assistantMessage.getId(),
                        "responseTime", 1500L,
                        "status", "success")));

        // 7. 完成流式传输
        emitter.complete();
    }

    /**
     * 验证SSE事件的工具方法
     */
    private void verifyQianwenStreamEvents(List<SseEvent> events) {
        // 验证事件序列
        assertTrue(SseTestUtils.validateQianwenStreamResponse(events), "千问流式响应格式验证失败");

        // 验证开始事件
        List<SseEvent> startEvents = SseTestUtils.findEventsByName(events, "start");
        assertEquals(1, startEvents.size(), "应该有一个开始事件");
        assertTrue(SseTestUtils.eventDataContains(startEvents.get(0), "status"), "开始事件应包含状态字段");

        // 验证对话事件
        List<SseEvent> conversationEvents = SseTestUtils.findEventsByName(events, "conversation");
        assertEquals(1, conversationEvents.size(), "应该有一个对话事件");
        assertTrue(SseTestUtils.eventDataContains(conversationEvents.get(0), "conversationId"), "对话事件应包含对话ID");

        // 验证AI块事件
        List<SseEvent> chunkEvents = SseTestUtils.findEventsByName(events, "ai_chunk");
        assertFalse(chunkEvents.isEmpty(), "应该有AI数据块事件");

        // 验证完成事件
        List<SseEvent> completeEvents = SseTestUtils.findEventsByName(events, "complete");
        assertEquals(1, completeEvents.size(), "应该有一个完成事件");
        assertTrue(SseTestUtils.eventDataContains(completeEvents.get(0), "messageId"), "完成事件应包含消息ID");

        // 验证完整响应内容
        String completeContent = SseTestUtils.getCompleteStreamContent(events);
        assertFalse(completeContent.isEmpty(), "流式响应内容不应为空");
    }
}