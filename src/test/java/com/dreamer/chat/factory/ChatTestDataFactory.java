package com.dreamer.chat.factory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.dreamer.chat.dto.ChatRequest;
import com.dreamer.chat.dto.ChatResponse;
import com.dreamer.chat.entity.Conversation;
import com.dreamer.chat.entity.Message;
import com.dreamer.chat.enums.MessageType;
import com.dreamer.chat.enums.ModelProvider;

/**
 * 聊天测试数据工厂
 * 提供各种测试场景下的数据构建方法
 * 
 * @author panshenguo
 * @since 1.0.0
 */
public class ChatTestDataFactory {

    /**
     * 创建千问模型的标准聊天请求
     */
    public static ChatRequest createQianwenChatRequest() {
        ChatRequest request = new ChatRequest();
        request.setMessage("你好，请介绍一下Spring Boot的核心特性");
        request.setModelProvider("qianwen");
        request.setTitle("Spring Boot咨询");
        request.setUserId("test-user-001");
        request.setMaxTokens(1000);
        request.setTemperature(0.7);
        return request;
    }

    /**
     * 创建继续对话的聊天请求
     */
    public static ChatRequest createContinueChatRequest(Long conversationId) {
        ChatRequest request = new ChatRequest();
        request.setConversationId(conversationId);
        request.setMessage("能详细说明一下Spring Boot的自动配置机制吗？");
        request.setModelProvider("qianwen");
        request.setUserId("test-user-001");
        return request;
    }

    /**
     * 创建无效模型提供商的请求
     */
    public static ChatRequest createInvalidProviderRequest() {
        ChatRequest request = new ChatRequest();
        request.setMessage("测试消息");
        request.setModelProvider("invalid-provider");
        request.setTitle("测试对话");
        request.setUserId("test-user-001");
        return request;
    }

    /**
     * 创建空消息的请求
     */
    public static ChatRequest createEmptyMessageRequest() {
        ChatRequest request = new ChatRequest();
        request.setMessage("");
        request.setModelProvider("qianwen");
        request.setTitle("测试对话");
        request.setUserId("test-user-001");
        return request;
    }

    /**
     * 创建超长消息的请求
     */
    public static ChatRequest createLongMessageRequest() {
        ChatRequest request = new ChatRequest();
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("这是一个很长的测试消息，用于测试系统处理超长文本的能力。");
        }
        request.setMessage(longMessage.toString());
        request.setModelProvider("qianwen");
        request.setTitle("长消息测试");
        request.setUserId("test-user-001");
        return request;
    }

    /**
     * 创建测试对话实体
     */
    public static Conversation createTestConversation() {
        Conversation conversation = new Conversation();
        conversation.setId(1L);
        conversation.setTitle("Spring Boot咨询");
        conversation.setUserId("test-user-001");
        conversation.setModelProvider(ModelProvider.QIANWEN);
        conversation.setCreatedTime(LocalDateTime.now());
        conversation.setUpdatedTime(LocalDateTime.now());
        return conversation;
    }

    /**
     * 创建用户消息实体
     */
    public static Message createUserMessage(Long conversationId, String content) {
        Message message = new Message();
        message.setId(1L);
        message.setContent(content);
        message.setMessageType(MessageType.USER);
        message.setTimestamp(LocalDateTime.now());
        // 创建并关联对话对象
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        message.setConversation(conversation);
        return message;
    }

    /**
     * 创建助手消息实体
     */
    public static Message createAssistantMessage(Long conversationId, String content) {
        Message message = new Message();
        message.setId(2L);
        message.setContent(content);
        message.setMessageType(MessageType.ASSISTANT);
        message.setTimestamp(LocalDateTime.now());
        // 创建并关联对话对象
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        message.setConversation(conversation);
        return message;
    }

    /**
     * 创建对话历史消息列表
     */
    public static List<Message> createConversationHistory(Long conversationId) {
        return Arrays.asList(
                createUserMessage(conversationId, "你好，请介绍一下Spring Boot"),
                createAssistantMessage(conversationId, "Spring Boot是一个用于简化Spring应用程序开发的框架..."),
                createUserMessage(conversationId, "它有哪些核心特性？"),
                createAssistantMessage(conversationId, "Spring Boot的核心特性包括：1. 自动配置 2. 起步依赖 3. 内嵌服务器..."));
    }

    /**
     * 创建成功的聊天响应
     */
    public static ChatResponse createSuccessfulChatResponse(Long conversationId) {
        ChatResponse response = new ChatResponse();
        response.setConversationId(conversationId);
        response.setAssistantMessage("Spring Boot是一个基于Spring框架的快速开发工具，它提供了自动配置、起步依赖等特性，大大简化了Spring应用的开发过程。");
        response.setSuccess(true);
        response.setResponseTime(1500L);
        response.setModelProvider("qianwen");
        return response;
    }

    /**
     * 创建失败的聊天响应
     */
    public static ChatResponse createFailedChatResponse(String errorMessage) {
        ChatResponse response = new ChatResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        response.setResponseTime(100L);
        return response;
    }

    /**
     * 模拟千问模型的流式响应数据
     */
    public static List<String> createQianwenStreamChunks() {
        return Arrays.asList(
                "Spring",
                " Boot",
                "是一个",
                "基于",
                "Spring",
                "框架的",
                "快速",
                "开发",
                "工具，",
                "它提供了",
                "自动配置、",
                "起步依赖",
                "等特性，",
                "大大简化了",
                "Spring应用的",
                "开发过程。");
    }

    /**
     * 获取完整的模拟响应内容
     */
    public static String getCompleteQianwenResponse() {
        return String.join("", createQianwenStreamChunks());
    }

    /**
     * 创建不同模型提供商的请求列表
     */
    public static List<ChatRequest> createMultiProviderRequests() {
        return Arrays.asList(
                createRequestForProvider("qianwen", "阿里巴巴千问测试"),
                createRequestForProvider("xinghuo", "讯飞星火测试"),
                createRequestForProvider("doubao", "豆包测试"),
                createRequestForProvider("deepseek", "DeepSeek测试"));
    }

    /**
     * 为指定提供商创建请求
     */
    private static ChatRequest createRequestForProvider(String provider, String message) {
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        request.setModelProvider(provider);
        request.setTitle("多模型测试");
        request.setUserId("test-user-001");
        return request;
    }

    /**
     * 创建性能测试用的请求列表
     */
    public static List<ChatRequest> createPerformanceTestRequests(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> {
                    ChatRequest request = new ChatRequest();
                    request.setMessage("性能测试消息 #" + i);
                    request.setModelProvider("qianwen");
                    request.setTitle("性能测试 #" + i);
                    request.setUserId("perf-test-user");
                    return request;
                })
                .toList();
    }

    /**
     * 创建并发测试场景的请求
     */
    public static ChatRequest createConcurrentTestRequest(int threadId) {
        ChatRequest request = new ChatRequest();
        request.setMessage("并发测试消息，线程ID: " + threadId);
        request.setModelProvider("qianwen");
        request.setTitle("并发测试-线程" + threadId);
        request.setUserId("concurrent-test-user-" + threadId);
        return request;
    }

    /**
     * 创建边界值测试请求
     */
    public static ChatRequest createBoundaryTestRequest(String testCase) {
        ChatRequest request = new ChatRequest();
        request.setModelProvider("qianwen");
        request.setUserId("boundary-test-user");

        switch (testCase) {
            case "min_message":
                request.setMessage("a");
                request.setTitle("最小消息测试");
                break;
            case "max_message":
                request.setMessage("a".repeat(9999)); // 接近最大长度
                request.setTitle("最大消息测试");
                break;
            case "max_title":
                request.setMessage("测试标题长度边界");
                request.setTitle("a".repeat(199)); // 接近最大标题长度
                break;
            case "special_chars":
                request.setMessage("测试特殊字符：@#$%^&*(){}[]|\\:;\"'<>?/.,~`");
                request.setTitle("特殊字符测试");
                break;
            case "unicode":
                request.setMessage("测试Unicode字符：🚀🌟💫⭐️🎯🔥💡🎨🌈🎪");
                request.setTitle("Unicode测试");
                break;
            default:
                request.setMessage("默认边界测试消息");
                request.setTitle("默认边界测试");
        }

        return request;
    }
}