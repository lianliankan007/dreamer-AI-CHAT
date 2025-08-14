package com.dreamer.chat.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dreamer.chat.config.AiModelConfig;
import com.dreamer.chat.config.GlobalExceptionHandler;
import com.dreamer.chat.dto.*;
import com.dreamer.chat.entity.Conversation;
import com.dreamer.chat.entity.Message;
import com.dreamer.chat.enums.*;
import com.dreamer.chat.repository.MessageRepository;

import reactor.core.publisher.Flux;

/**
 * 聊天服务类
 * 负责处理用户与AI模型的对话交互逻辑
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Service
@EnableAsync
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private AiModelConfig.ChatClientManager chatClientManager;

    @Autowired
    private PromptBuilderService promptBuilderService;

    @Value("${app.chat.max-history-size:50}")
    private int maxHistorySize;

    @Value("${app.chat.default-max-tokens:2000}")
    private int defaultMaxTokens;

    @Value("${app.chat.timeout-seconds:30}")
    private int timeoutSeconds;

    /**
     * 处理聊天请求
     * 支持新对话创建和现有对话继续
     * 
     * @param request 聊天请求
     * @return 聊天响应
     */
    @Transactional
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证模型提供商
            ModelProvider modelProvider = validateAndGetModelProvider(request.getModelProvider());

            // 2. 获取或创建对话
            Conversation conversation = getOrCreateConversation(request, modelProvider);

            // 3. 保存用户消息
            Message userMessage = saveUserMessage(conversation, request.getMessage());

            // 4. 获取聊天历史上下文
            List<Message> contextMessages = getConversationContext(conversation.getId());

            // 5. 智能检测Prompt类型
            PromptType promptType = promptBuilderService.detectPromptType(request.getMessage());

            // 6. 调用AI模型生成回复
            String aiResponse = generateAiResponse(modelProvider, promptType, contextMessages, request);

            // 6. 保存AI回复消息
            Message assistantMessage = saveAssistantMessage(conversation, aiResponse);

            // 7. 构建响应
            ChatResponse response = buildChatResponse(
                    conversation, userMessage, assistantMessage,
                    modelProvider, request.getModelName(), aiResponse);

            response.setResponseTime(System.currentTimeMillis() - startTime);

            log.info("聊天请求处理完成: conversationId={}, responseTime={}ms",
                    conversation.getId(), response.getResponseTime());

            return response;

        } catch (Exception e) {
            log.error("聊天请求处理失败", e);
            return buildErrorResponse(e, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 验证并获取模型提供商
     */
    public ModelProvider validateAndGetModelProvider(String providerCode) {
        if (!StringUtils.hasText(providerCode)) {
            throw new GlobalExceptionHandler.BusinessException("INVALID_MODEL_PROVIDER", "模型提供商不能为空");
        }

        try {
            ModelProvider provider = ModelProvider.fromCode(providerCode);
            if (!chatClientManager.isProviderAvailable(provider)) {
                throw new GlobalExceptionHandler.BusinessException("MODEL_UNAVAILABLE",
                        "模型提供商 " + provider.getName() + " 当前不可用");
            }
            return provider;
        } catch (IllegalArgumentException e) {
            throw new GlobalExceptionHandler.BusinessException("INVALID_MODEL_PROVIDER",
                    "不支持的模型提供商: " + providerCode);
        }
    }

    /**
     * 获取或创建对话
     */
    public Conversation getOrCreateConversation(ChatRequest request, ModelProvider modelProvider) {
        if (request.getConversationId() != null) {
            // 继续现有对话
            return conversationService.getConversationById(request.getConversationId())
                    .orElseThrow(() -> new GlobalExceptionHandler.BusinessException("CONVERSATION_NOT_FOUND",
                            "对话不存在: " + request.getConversationId()));
        } else {
            // 创建新对话
            String title = StringUtils.hasText(request.getTitle()) ? request.getTitle()
                    : generateConversationTitle(request.getMessage());

            return conversationService.createConversation(
                    title, modelProvider, request.getModelName(), request.getUserId());
        }
    }

    /**
     * 生成对话标题
     */
    private String generateConversationTitle(String message) {
        if (message.length() <= 30) {
            return message;
        }
        return message.substring(0, 27) + "...";
    }

    /**
     * 保存用户消息
     */
    public Message saveUserMessage(Conversation conversation, String content) {
        int nextSequence = getNextSequenceNumber(conversation.getId());

        Message message = new Message(content, MessageType.USER);
        message.setConversation(conversation);
        message.setSequenceNumber(nextSequence);
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);
    }

    /**
     * 保存AI助手消息
     */
    public Message saveAssistantMessage(Conversation conversation, String content) {
        int nextSequence = getNextSequenceNumber(conversation.getId());

        Message message = new Message(content, MessageType.ASSISTANT);
        message.setConversation(conversation);
        message.setSequenceNumber(nextSequence);
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);
    }

    /**
     * 获取下一个序号
     */
    private int getNextSequenceNumber(Long conversationId) {
        Integer maxSequence = messageRepository.findMaxSequenceNumberByConversationId(conversationId);
        return (maxSequence == null ? 0 : maxSequence) + 1;
    }

    /**
     * 获取对话上下文
     */
    public List<Message> getConversationContext(Long conversationId) {
        List<Message> allMessages = messageRepository.findByConversationIdOrderBySequenceNumberAsc(conversationId);

        // 限制历史消息数量，保留最近的消息
        if (allMessages.size() > maxHistorySize) {
            return allMessages.subList(allMessages.size() - maxHistorySize, allMessages.size());
        }

        return allMessages;
    }

    /**
     * 调用AI模型生成回复
     */
    private String generateAiResponse(ModelProvider provider, PromptType promptType,
            List<Message> contextMessages, ChatRequest request) {
        try {
            ChatClient chatClient = chatClientManager.getChatClient(provider);

            // 构建增强的用户上下文
            PromptContext promptContext = buildPromptContext(request,
                    conversationService.getConversationById(request.getConversationId()).orElse(null));

            // 使用智能Prompt构建器构建提示词
            Map<String, String> variables = buildVariablesFromContext(promptContext);
            String promptText = promptBuilderService.buildPrompt(
                    provider, promptType, contextMessages, request.getMessage(), variables);

            // 创建提示模板
            PromptTemplate promptTemplate = new PromptTemplate(promptText);
            Prompt prompt = promptTemplate.create();

            // 调用模型生成回复
            String response = chatClient.prompt(prompt).call().content();

            if (!StringUtils.hasText(response)) {
                throw new GlobalExceptionHandler.BusinessException("AI_RESPONSE_EMPTY", "AI模型返回空响应");
            }

            log.debug("AI回复生成成功: provider={}, promptType={}, responseLength={}",
                    provider.getCode(), promptType.getCode(), response.length());

            return response.trim();

        } catch (Exception e) {
            log.error("AI模型调用失败: provider={}, promptType={}", provider.getCode(), promptType.getCode(), e);
            throw new GlobalExceptionHandler.BusinessException("AI_CALL_FAILED",
                    "AI模型调用失败: " + e.getMessage());
        }
    }

    /**
     * 从PromptContext构建变量映射
     * 
     * @param promptContext 提示词上下文
     * @return 变量映射
     */
    public Map<String, String> buildVariablesFromContext(PromptContext promptContext) {
        Map<String, String> variables = new HashMap<>();

        if (promptContext != null) {
            variables.put("user_id", promptContext.getUserId() != null ? promptContext.getUserId() : "");
            variables.put("conversation_id",
                    promptContext.getConversationId() != null ? promptContext.getConversationId().toString() : "");
            variables.put("conversation_title",
                    promptContext.getConversationTitle() != null ? promptContext.getConversationTitle() : "");

            // 添加额外的上下文信息
            if (promptContext.getAdditionalContext() != null) {
                promptContext.getAdditionalContext().forEach((key, value) -> {
                    if (value != null) {
                        variables.put(key, value.toString());
                    }
                });
            }
        }

        return variables;
    }

    /**
     * 构建增强的Prompt上下文
     * 
     * @param request      聊天请求
     * @param conversation 对话信息
     * @return 增强的Prompt上下文
     */
    public PromptContext buildPromptContext(ChatRequest request, Conversation conversation) {
        PromptContext context = new PromptContext(request.getMessage(), request.getUserId());

        // 设置对话信息
        if (conversation != null) {
            context.setConversationId(conversation.getId());
        }

        // 设置消息类型信息
        context.setMessageType(detectMessageCategory(request.getMessage()));

        // 设置用户偏好（可以从数据库加载）
        loadUserPreferences(context, request.getUserId());

        // 设置会话元数据
        context.addSessionMetadata("modelProvider", request.getModelProvider());
        context.addSessionMetadata("modelName", request.getModelName());

        // 分析消息特征
        analyzeMessageFeatures(context, request.getMessage());

        return context;
    }

    /**
     * 检测消息分类
     */
    private String detectMessageCategory(String message) {
        if (message == null)
            return "unknown";

        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("紧急") || lowerMessage.contains("急") || lowerMessage.contains("urgent")) {
            return "urgent";
        } else if (lowerMessage.contains("代码") || lowerMessage.contains("编程") || lowerMessage.contains("code")) {
            return "technical";
        } else if (lowerMessage.contains("翻译") || lowerMessage.contains("translate")) {
            return "translation";
        } else if (lowerMessage.contains("创作") || lowerMessage.contains("写作") || lowerMessage.contains("creative")) {
            return "creative";
        } else if (lowerMessage.contains("分析") || lowerMessage.contains("analyze")) {
            return "analytical";
        }

        return "general";
    }

    /**
     * 加载用户偏好设置
     */
    private void loadUserPreferences(PromptContext context, String userId) {
        if (userId == null)
            return;

        // 这里可以从数据库或缓存中加载用户偏好
        // 暂时添加一些默认偏好
        context.addUserPreference("language", "中文");
        context.addUserPreference("style", "professional");
        context.addUserPreference("detail_level", "medium");
    }

    /**
     * 分析消息特征
     */
    private void analyzeMessageFeatures(PromptContext context, String message) {
        if (message == null)
            return;

        // 分析消息长度并设置期望回复长度
        if (message.length() < 50) {
            context.setExpectedLength("short");
        } else if (message.length() < 200) {
            context.setExpectedLength("medium");
        } else {
            context.setExpectedLength("detailed");
        }

        // 检测紧急程度
        if (message.toLowerCase().contains("紧急") || message.toLowerCase().contains("急")) {
            context.setUrgency("high");
        } else if (message.contains("尽快") || message.contains("快")) {
            context.setUrgency("medium");
        } else {
            context.setUrgency("normal");
        }
    }

    /**
     * 构建聊天响应
     */
    private ChatResponse buildChatResponse(Conversation conversation, Message userMessage,
            Message assistantMessage, ModelProvider provider,
            String modelName, String aiResponse) {
        ChatResponse response = new ChatResponse();
        response.setConversationId(conversation.getId());
        response.setUserMessageId(userMessage.getId());
        response.setAssistantMessageId(assistantMessage.getId());
        response.setAssistantMessage(aiResponse);
        response.setModelProvider(provider.getCode());
        response.setModelName(StringUtils.hasText(modelName) ? modelName : provider.getName());
        response.setTimestamp(LocalDateTime.now());
        response.setSuccess(true);

        return response;
    }

    /**
     * 构建错误响应
     */
    private ChatResponse buildErrorResponse(Exception e, long responseTime) {
        ChatResponse response = new ChatResponse();
        response.setSuccess(false);
        response.setErrorMessage(e.getMessage());
        response.setTimestamp(LocalDateTime.now());
        response.setResponseTime(responseTime);

        return response;
    }

    /**
     * 获取对话历史
     * 
     * @param conversationId 对话ID
     * @return 消息列表
     */
    public List<Message> getConversationHistory(Long conversationId) {
        return messageRepository.findByConversationIdOrderBySequenceNumberAsc(conversationId);
    }

    /**
     * 流式聊天处理
     * 
     * @param request 聊天请求
     * @param emitter SSE发射器
     */
    @Async("taskExecutor")
    public void chatStream(ChatRequest request, SseEmitter emitter) {
        long startTime = System.currentTimeMillis();

        try {
            // 发送开始事件
            emitter.send(SseEmitter.event()
                    .name("start")
                    .data("{\"status\":\"processing\",\"message\":\"开始处理请求\"}"));

            // 1. 验证模型提供商
            ModelProvider modelProvider = validateAndGetModelProvider(request.getModelProvider());

            // 2. 获取或创建对话
            Conversation conversation = getOrCreateConversation(request, modelProvider);

            // 发送对话信息
            emitter.send(SseEmitter.event()
                    .name("conversation")
                    .data("{\"conversationId\":" + conversation.getId() +
                            ",\"title\":\"" + conversation.getTitle() + "\"}"));

            // 3. 保存用户消息
            Message userMessage = saveUserMessage(conversation, request.getMessage());

            // 发送用户消息确认
            emitter.send(SseEmitter.event()
                    .name("user_message")
                    .data("{\"messageId\":" + userMessage.getId() +
                            ",\"content\":\"" + escapeJson(userMessage.getContent()) + "\"}"));

            // 4. 获取聊天历史上下文
            List<Message> contextMessages = getConversationContext(conversation.getId());

            // 5. 智能检测Prompt类型
            PromptType promptType = promptBuilderService.detectPromptType(request.getMessage());

            // 6. 流式生成AI回复
            String aiResponse = generateAiResponseStream(modelProvider, promptType, contextMessages, request, emitter);

            // 6. 保存AI回复消息
            Message assistantMessage = saveAssistantMessage(conversation, aiResponse);

            // 7. 发送完成事件
            long responseTime = System.currentTimeMillis() - startTime;
            emitter.send(SseEmitter.event()
                    .name("complete")
                    .data("{\"messageId\":" + assistantMessage.getId() +
                            ",\"responseTime\":" + responseTime +
                            ",\"status\":\"success\"}"));

            emitter.complete();

            log.info("流式聊天请求处理完成: conversationId={}, responseTime={}ms",
                    conversation.getId(), responseTime);

        } catch (Exception e) {
            log.error("流式聊天请求处理失败", e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"error\":\"" + escapeJson(e.getMessage()) +
                                "\",\"timestamp\":" + System.currentTimeMillis() + "}"));
                emitter.complete();
            } catch (IOException ioException) {
                log.error("发送错误事件失败", ioException);
                emitter.completeWithError(ioException);
            }
        }
    }

    /**
     * 流式生成AI回复
     */
    private String generateAiResponseStream(ModelProvider provider, PromptType promptType,
            List<Message> contextMessages, ChatRequest request, SseEmitter emitter) throws IOException {
        try {
            // 发送AI开始生成事件
            emitter.send(SseEmitter.event()
                    .name("ai_start")
                    .data("{\"provider\":\"" + provider.getCode() + "\",\"model\":\"" + provider.getModel() +
                            "\",\"promptType\":\"" + promptType.getCode() + "\"}"));

            ChatClient chatClient = chatClientManager.getChatClient(provider);

            // 从当前方法上下文获取conversation对象
            Conversation conversation = null;
            if (request.getConversationId() != null) {
                conversation = conversationService.getConversationById(request.getConversationId()).orElse(null);
            }

            // 构建增强的用户上下文
            PromptContext promptContext = buildPromptContext(request, conversation);

            // 使用智能Prompt构建器构建提示词
            Map<String, String> variables = buildVariablesFromContext(promptContext);
            String promptText = promptBuilderService.buildPrompt(
                    provider, promptType, contextMessages, request.getMessage(), variables);

            // 使用Spring AI的流式API进行真正的流式调用
            StringBuilder fullResponse = new StringBuilder();
            AtomicInteger chunkIndex = new AtomicInteger(0);

            Flux<String> responseFlux = chatClient.prompt()
                    .user(promptText)
                    .stream()
                    .content();

            // 阻塞式处理流式响应
            responseFlux
                    .doOnNext(chunk -> {
                        try {
                            fullResponse.append(chunk);

                            // 发送流式数据块
                            emitter.send(SseEmitter.event()
                                    .name("ai_chunk")
                                    .data("{\"chunk\":\"" + escapeJson(chunk) +
                                            "\",\"index\":" + chunkIndex.incrementAndGet() + "}"));
                        } catch (IOException e) {
                            log.error("发送流式数据块失败", e);
                            throw new RuntimeException(e);
                        }
                    })
                    .doOnError(error -> {
                        log.error("流式处理过程中发生错误", error);
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data("{\"error\":\"流式处理失败: " + escapeJson(error.getMessage()) + "\"}"));
                        } catch (IOException e) {
                            log.error("发送错误事件失败", e);
                        }
                    })
                    .doOnComplete(() -> {
                        log.debug("流式响应完成，总长度: {}", fullResponse.length());
                    })
                    .blockLast(); // 等待流式处理完成

            String response = fullResponse.toString();

            if (!StringUtils.hasText(response)) {
                throw new GlobalExceptionHandler.BusinessException("AI_RESPONSE_EMPTY", "AI模型返回空响应");
            }

            log.debug("AI流式回复生成成功: provider={}, promptType={}, responseLength={}",
                    provider.getCode(), promptType.getCode(), response.length());

            return response.trim();

        } catch (Exception e) {
            log.error("AI流式生成失败: provider={}, promptType={}", provider.getCode(), promptType.getCode(), e);
            throw new GlobalExceptionHandler.BusinessException("AI_STREAM_FAILED",
                    "AI流式生成失败: " + e.getMessage());
        }
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 清空对话历史
     * 
     * @param conversationId 对话ID
     */
    @Transactional
    public void clearConversationHistory(Long conversationId) {
        messageRepository.deleteByConversationId(conversationId);
        log.info("已清空对话历史: conversationId={}", conversationId);
    }
}