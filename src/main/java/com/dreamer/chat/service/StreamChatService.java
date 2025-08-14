package com.dreamer.chat.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dreamer.chat.config.AiModelConfig;
import com.dreamer.chat.config.GlobalExceptionHandler;
import com.dreamer.chat.dto.ChatRequest;
import com.dreamer.chat.dto.PromptContext;
import com.dreamer.chat.entity.Conversation;
import com.dreamer.chat.entity.Message;
import com.dreamer.chat.enums.ModelProvider;
import com.dreamer.chat.enums.PromptType;
import com.dreamer.chat.util.JsonUtils;

import reactor.core.publisher.Flux;

/**
 * 流式聊天处理服务
 * 专门负责流式AI调用和SSE事件管理
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Service
public class StreamChatService {

    private static final Logger log = LoggerFactory.getLogger(StreamChatService.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private AiModelConfig.ChatClientManager chatClientManager;

    @Autowired
    private PromptBuilderService promptBuilderService;

    @Value("${app.chat.timeout-seconds:30}")
    private int timeoutSeconds;

    /**
     * 异步处理流式聊天请求
     * 
     * @param request 聊天请求
     * @param emitter SSE发射器
     * @return 异步处理结果
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> processStreamChatAsync(ChatRequest request, SseEmitter emitter) {
        return CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();

            try {
                // 发送开始事件
                sendSseEvent(emitter, "start", JsonUtils.createSseEventData(
                        "status", "processing",
                        "message", "开始处理请求"));

                // 1. 验证和准备数据（同步部分）
                ModelProvider modelProvider = chatService.validateAndGetModelProvider(request.getModelProvider());
                Conversation conversation = chatService.getOrCreateConversation(request, modelProvider);
                Message userMessage = chatService.saveUserMessage(conversation, request.getMessage());

                // 发送对话和用户消息确认事件
                sendConversationEvents(emitter, conversation, userMessage);

                // 2. 获取上下文消息
                List<Message> contextMessages = chatService.getConversationContext(conversation.getId());

                // 3. 检测Prompt类型
                PromptType promptType = promptBuilderService.detectPromptType(request.getMessage());

                // 4. 流式生成AI回复
                String aiResponse = generateStreamingResponse(
                        modelProvider, promptType, contextMessages, request, conversation, emitter);

                // 5. 保存AI回复（异步事务）
                Message assistantMessage = chatService.saveAssistantMessage(conversation, aiResponse);

                // 6. 发送完成事件
                sendCompletionEvent(emitter, assistantMessage, startTime);

            } catch (Exception e) {
                handleStreamError(emitter, e, startTime);
            }
        });
    }

    /**
     * 生成流式AI响应
     */
    private String generateStreamingResponse(ModelProvider provider, PromptType promptType,
            List<Message> contextMessages, ChatRequest request, Conversation conversation, SseEmitter emitter)
            throws IOException {

        try {
            // 发送AI开始生成事件
            sendSseEvent(emitter, "ai_start", JsonUtils.createSseEventData(
                    "provider", provider.getCode(),
                    "model", provider.getModel(),
                    "promptType", promptType.getCode()));

            // 创建ChatClient实例
            ChatClient chatClient = chatClientManager.getChatClient(provider);
            // 构建提示词上下文
            PromptContext promptContext = chatService.buildPromptContext(request, conversation);
            Map<String, String> variables = chatService.buildVariablesFromContext(promptContext);
            String promptText = promptBuilderService.buildPrompt(
                    provider, promptType, contextMessages, request.getMessage(), variables);

            // 执行流式调用
            return executeStreamingCall(chatClient, promptText, emitter);

        } catch (Exception e) {
            log.error("AI流式生成失败: provider={}, promptType={}", provider.getCode(), promptType.getCode(), e);
            throw new GlobalExceptionHandler.BusinessException("AI_STREAM_FAILED",
                    "AI流式生成失败: " + e.getMessage());
        }
    }

    /**
     * 执行实际的流式调用
     */
    private String executeStreamingCall(ChatClient chatClient, String promptText, SseEmitter emitter)
            throws IOException {

        StringBuilder fullResponse = new StringBuilder();
        AtomicInteger chunkIndex = new AtomicInteger(0);

        try {
            // 使用Spring AI的流式API
            Flux<String> responseFlux = chatClient.prompt()
                    .user(promptText)
                    .stream()
                    .content();

            // 处理流式响应
            responseFlux
                    .doOnNext(chunk -> processChunk(chunk, fullResponse, chunkIndex, emitter))
                    .doOnError(error -> handleStreamingError(error, emitter))
                    .doOnComplete(() -> log.debug("流式响应完成，总长度: {}", fullResponse.length()))
                    .blockLast(); // 等待流式处理完成

            String response = fullResponse.toString();

            if (!StringUtils.hasText(response)) {
                throw new GlobalExceptionHandler.BusinessException("AI_RESPONSE_EMPTY", "AI模型返回空响应");
            }

            log.debug("AI流式回复生成成功，响应长度: {}", response.length());
            return response.trim();

        } catch (Exception e) {
            log.error("执行流式调用失败", e);
            throw e;
        }
    }

    /**
     * 处理单个数据块
     */
    private void processChunk(String chunk, StringBuilder fullResponse, AtomicInteger chunkIndex, SseEmitter emitter) {
        try {
            fullResponse.append(chunk);

            // 发送流式数据块
            sendSseEvent(emitter, "ai_chunk", JsonUtils.createSseEventData(
                    "chunk", chunk,
                    "index", chunkIndex.incrementAndGet()));

        } catch (Exception e) {
            log.error("发送流式数据块失败", e);
            throw new RuntimeException("发送流式数据块失败", e);
        }
    }

    /**
     * 处理流式过程中的错误
     */
    private void handleStreamingError(Throwable error, SseEmitter emitter) {
        log.error("流式处理过程中发生错误", error);
        try {
            sendSseEvent(emitter, "error", JsonUtils.createSseEventData(
                    "error", "流式处理失败: " + error.getMessage(),
                    "timestamp", System.currentTimeMillis()));
        } catch (IOException e) {
            log.error("发送流式错误事件失败", e);
        }
    }

    /**
     * 发送对话相关事件
     */
    private void sendConversationEvents(SseEmitter emitter, Conversation conversation, Message userMessage)
            throws IOException {

        // 发送对话信息
        sendSseEvent(emitter, "conversation", JsonUtils.createSseEventData(
                "conversationId", conversation.getId(),
                "title", conversation.getTitle()));

        // 发送用户消息确认
        sendSseEvent(emitter, "user_message", JsonUtils.createSseEventData(
                "messageId", userMessage.getId(),
                "content", userMessage.getContent()));
    }

    /**
     * 发送完成事件
     */
    private void sendCompletionEvent(SseEmitter emitter, Message assistantMessage, long startTime)
            throws IOException {

        long responseTime = System.currentTimeMillis() - startTime;
        sendSseEvent(emitter, "complete", JsonUtils.createSseEventData(
                "messageId", assistantMessage.getId(),
                "responseTime", responseTime,
                "status", "success"));
        emitter.complete();

        log.info("流式聊天请求处理完成，响应时间: {}ms", responseTime);
    }

    /**
     * 处理流式聊天错误
     */
    private void handleStreamError(SseEmitter emitter, Exception e, long startTime) {
        log.error("流式聊天请求处理失败", e);

        try {
            long errorTime = System.currentTimeMillis() - startTime;
            sendSseEvent(emitter, "error", JsonUtils.createSseEventData(
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis(),
                    "responseTime", errorTime));
            emitter.complete();

        } catch (IOException ioException) {
            log.error("发送错误事件失败", ioException);
            emitter.completeWithError(ioException);
        }
    }

    /**
     * 发送SSE事件的统一方法
     */
    private void sendSseEvent(SseEmitter emitter, String eventName, String data) throws IOException {
        emitter.send(SseEmitter.event().name(eventName).data(data));
    }

    // 已移除escapeJson方法，改用JsonUtils.escapeJsonString()进行JSON字符串转义
}