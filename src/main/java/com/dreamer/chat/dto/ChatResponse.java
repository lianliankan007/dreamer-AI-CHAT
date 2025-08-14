package com.dreamer.chat.dto;

import java.time.LocalDateTime;

/**
 * 聊天响应DTO
 * 
 * @author panshenguo
 * @since 1.0.0
 */
public class ChatResponse {

    /**
     * 对话ID
     */
    private Long conversationId;

    /**
     * 用户消息ID
     */
    private Long userMessageId;

    /**
     * AI助手回复消息ID
     */
    private Long assistantMessageId;

    /**
     * AI助手回复内容
     */
    private String assistantMessage;

    /**
     * 使用的模型提供商
     */
    private String modelProvider;

    /**
     * 使用的具体模型
     */
    private String modelName;
    /**
     * 响应时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 使用的Token数量
     */
    private Integer tokenCount;

    /**
     * 响应耗时（毫秒）
     */
    private Long responseTime;

    /**
     * 是否成功
     */
    private boolean success = true;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    // 构造函数
    public ChatResponse() {
    }

    public ChatResponse(Long conversationId, String assistantMessage, String modelProvider) {
        this.conversationId = conversationId;
        this.assistantMessage = assistantMessage;
        this.modelProvider = modelProvider;
        this.timestamp = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getUserMessageId() {
        return userMessageId;
    }

    public void setUserMessageId(Long userMessageId) {
        this.userMessageId = userMessageId;
    }

    public Long getAssistantMessageId() {
        return assistantMessageId;
    }

    public void setAssistantMessageId(Long assistantMessageId) {
        this.assistantMessageId = assistantMessageId;
    }

    public String getAssistantMessage() {
        return assistantMessage;
    }

    public void setAssistantMessage(String assistantMessage) {
        this.assistantMessage = assistantMessage;
    }

    public String getModelProvider() {
        return modelProvider;
    }

    public void setModelProvider(String modelProvider) {
        this.modelProvider = modelProvider;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "ChatResponse{" +
                "conversationId=" + conversationId +
                ", userMessageId=" + userMessageId +
                ", assistantMessageId=" + assistantMessageId +
                ", assistantMessage='" + assistantMessage + '\'' +
                ", modelProvider='" + modelProvider + '\'' +
                ", modelName='" + modelName + '\'' +
                ", timestamp=" + timestamp +
                ", tokenCount=" + tokenCount +
                ", responseTime=" + responseTime +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}