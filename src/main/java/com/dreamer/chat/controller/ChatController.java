package com.dreamer.chat.controller;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dreamer.chat.dto.ChatRequest;
import com.dreamer.chat.dto.ChatResponse;
import com.dreamer.chat.entity.Message;
import com.dreamer.chat.service.ChatService;
import com.dreamer.chat.service.StreamChatService;

import jakarta.validation.Valid;

/**
 * 聊天控制器
 * 提供聊天相关的REST API接口
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private StreamChatService streamChatService;

    /**
     * 发送聊天消息（流式输出）
     * 支持新建对话和继续现有对话
     * 
     * @param request 聊天请求
     * @return 流式响应
     */
    @PostMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(@Valid @RequestBody ChatRequest request) {
        log.info("收到流式聊天请求: conversationId={}, modelProvider={}, messageLength={}",
                request.getConversationId(), request.getModelProvider(),
                request.getMessage() != null ? request.getMessage().length() : 0);

        SseEmitter emitter = new SseEmitter(120000L); // 120秒超时，适应AI生成时间

        // 设置SSE连接事件处理器
        emitter.onCompletion(() -> log.debug("SSE连接正常完成: conversationId={}", request.getConversationId()));
        emitter.onTimeout(() -> log.warn("SSE连接超时: conversationId={}", request.getConversationId()));
        emitter.onError(throwable -> log.error("SSE连接发生错误: conversationId={}", request.getConversationId(), throwable));

        // 使用新的流式聊天服务异步处理
        streamChatService.processStreamChatAsync(request, emitter)
                .exceptionally(throwable -> {
                    log.error("异步流式处理失败", throwable);
                    try {
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data("{\"error\":\"处理请求失败: " + throwable.getMessage() + "\"}"));
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("发送错误事件失败", e);
                        emitter.completeWithError(e);
                    }
                    return null;
                });

        return emitter;
    }

    /**
     * 发送聊天消息（同步版本，保留兼容性）
     * 支持新建对话和继续现有对话
     * 
     * @param request 聊天请求
     * @return 聊天响应
     */
    @PostMapping("/send-sync")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        log.info("收到同步聊天请求: conversationId={}, modelProvider={}, messageLength={}",
                request.getConversationId(), request.getModelProvider(),
                request.getMessage() != null ? request.getMessage().length() : 0);

        ChatResponse response = chatService.chat(request);

        log.info("聊天响应: conversationId={}, success={}, responseTime={}ms",
                response.getConversationId(), response.isSuccess(), response.getResponseTime());

        return ResponseEntity.ok(response);
    }

    /**
     * 获取对话历史消息
     * 
     * @param conversationId 对话ID
     * @return 消息列表
     */
    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<Message>> getConversationHistory(@PathVariable Long conversationId) {
        log.info("获取对话历史: conversationId={}", conversationId);

        List<Message> messages = chatService.getConversationHistory(conversationId);

        log.info("获取对话历史完成: conversationId={}, messageCount={}",
                conversationId, messages.size());

        return ResponseEntity.ok(messages);
    }

    /**
     * 清空对话历史
     * 
     * @param conversationId 对话ID
     * @return 操作结果
     */
    @DeleteMapping("/history/{conversationId}")
    public ResponseEntity<Map<String, Object>> clearConversationHistory(@PathVariable Long conversationId) {
        log.info("清空对话历史: conversationId={}", conversationId);

        chatService.clearConversationHistory(conversationId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "对话历史已清空");
        result.put("conversationId", conversationId);

        log.info("清空对话历史完成: conversationId={}", conversationId);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取支持的模型列表
     * 
     * @return 模型列表
     */
    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> getSupportedModels() {
        log.info("获取支持的模型列表");

        Map<String, Object> result = new HashMap<>();
        Map<String, String> models = new HashMap<>();

        // 基于配置返回支持的模型
        models.put("qianwen", "阿里巴巴千问");
        models.put("xinghuo", "讯飞星火");
        models.put("doubao", "豆包");
        models.put("deepseek", "DeepSeek");

        result.put("success", true);
        result.put("models", models);
        result.put("defaultModel", "qianwen");

        return ResponseEntity.ok(result);
    }

    /**
     * 健康检查接口
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "chat-service");
        result.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(result);
    }

    /**
     * 测试聊天接口（同步版本）
     * 用于快速测试聊天功能
     * 
     * @param message       测试消息
     * @param modelProvider 模型提供商（可选，默认qianwen）
     * @return 聊天响应
     */
    @PostMapping("/test")
    public ResponseEntity<ChatResponse> testChat(
            @RequestParam String message,
            @RequestParam(defaultValue = "qianwen") String modelProvider) {

        log.info("测试聊天: message={}, modelProvider={}", message, modelProvider);

        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        request.setModelProvider(modelProvider);
        request.setTitle("测试对话");
        request.setUserId("test-user");

        ChatResponse response = chatService.chat(request);

        log.info("测试聊天完成: success={}, responseTime={}ms",
                response.isSuccess(), response.getResponseTime());

        return ResponseEntity.ok(response);
    }

    /**
     * 测试流式聊天接口
     * 用于快速测试流式聊天功能
     * 
     * @param message       测试消息
     * @param modelProvider 模型提供商（可选，默认qianwen）
     * @return 流式响应
     */
    @GetMapping(value = "/test-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testChatStream(
            @RequestParam String message,
            @RequestParam(defaultValue = "qianwen") String modelProvider) {

        log.info("测试流式聊天: message={}, modelProvider={}", message, modelProvider);

        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        request.setModelProvider(modelProvider);
        request.setTitle("测试流式对话");
        request.setUserId("test-user");

        SseEmitter emitter = new SseEmitter(30000L);
        chatService.chatStream(request, emitter);

        return emitter;
    }
}