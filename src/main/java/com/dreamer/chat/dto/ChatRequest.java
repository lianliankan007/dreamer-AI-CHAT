package com.dreamer.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 聊天请求DTO
 * 
 * @author panshenguo
 * @since 1.0.0
 */
public class ChatRequest {

    /**
     * 对话ID（如果是新对话则为空）
     */
    private Long conversationId;

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 10000, message = "消息内容长度不能超过10000字符")
    private String message;

    /**
     * 模型提供商代码
     */
    @NotBlank(message = "模型提供商不能为空")
    private String modelProvider;

    /**
     * 具体模型名称（可选）
     */
    private String modelName;

    /**
     * 对话标题（新对话时使用）
     */
    @Size(max = 200, message = "对话标题长度不能超过200字符")
    private String title;

    /**
     * 最大生成Token数（可选）
     */
    private Integer maxTokens;

    /**
     * 温度参数（可选，0.0-2.0）
     */
    private Double temperature;

    /**
     * 用户ID（预留）
     */
    private String userId;

    // 构造函数
    public ChatRequest() {
    }

    public ChatRequest(String message, String modelProvider) {
        this.message = message;
        this.modelProvider = modelProvider;
    }

    // Getter和Setter方法
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "ChatRequest{" +
                "conversationId=" + conversationId +
                ", message='" + message + '\'' +
                ", modelProvider='" + modelProvider + '\'' +
                ", modelName='" + modelName + '\'' +
                ", title='" + title + '\'' +
                ", maxTokens=" + maxTokens +
                ", temperature=" + temperature +
                ", userId='" + userId + '\'' +
                '}';
    }
}