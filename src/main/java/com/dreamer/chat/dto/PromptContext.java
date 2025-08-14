package com.dreamer.chat.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 提示词上下文数据传输对象
 * 用于封装构建提示词所需的上下文信息
 * 
 * @author panshenguo
 * @since 1.0.0
 */
public class PromptContext {

    /**
     * 用户输入的原始消息
     */
    private String userMessage;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 对话ID
     */
    private Long conversationId;

    /**
     * 对话标题
     */
    private String conversationTitle;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 额外的上下文参数
     */
    private Map<String, Object> additionalContext;

    /**
     * 默认构造函数
     */
    public PromptContext() {
        this.additionalContext = new HashMap<>();
        this.createTime = LocalDateTime.now();
    }

    /**
     * 构造函数
     * 
     * @param userMessage 用户消息
     * @param userId      用户ID
     */
    public PromptContext(String userMessage, String userId) {
        this();
        this.userMessage = userMessage;
        this.userId = userId;
    }

    /**
     * 构造函数
     * 
     * @param userMessage    用户消息
     * @param userId         用户ID
     * @param conversationId 对话ID
     */
    public PromptContext(String userMessage, String userId, Long conversationId) {
        this(userMessage, userId);
        this.conversationId = conversationId;
    }

    /**
     * 添加额外的上下文参数
     * 
     * @param key   参数键
     * @param value 参数值
     * @return 当前对象，支持链式调用
     */
    public PromptContext addContext(String key, Object value) {
        this.additionalContext.put(key, value);
        return this;
    }

    /**
     * 设置消息类型
     * 
     * @param messageType 消息类型
     */
    public void setMessageType(String messageType) {
        addContext("messageType", messageType);
    }

    /**
     * 添加会话元数据
     * 
     * @param key   元数据键
     * @param value 元数据值
     */
    public void addSessionMetadata(String key, String value) {
        addContext("session_" + key, value);
    }

    /**
     * 添加用户偏好
     * 
     * @param key   偏好键
     * @param value 偏好值
     */
    public void addUserPreference(String key, String value) {
        addContext("preference_" + key, value);
    }

    /**
     * 设置期望的回复长度
     * 
     * @param expectedLength 期望长度
     */
    public void setExpectedLength(String expectedLength) {
        addContext("expectedLength", expectedLength);
    }

    /**
     * 设置紧急程度
     * 
     * @param urgency 紧急程度
     */
    public void setUrgency(String urgency) {
        addContext("urgency", urgency);
    }

    /**
     * 获取额外的上下文参数
     * 
     * @param key 参数键
     * @return 参数值
     */
    public Object getContext(String key) {
        return this.additionalContext.get(key);
    }

    /**
     * 获取指定类型的上下文参数
     * 
     * @param key   参数键
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 类型化的参数值
     */
    @SuppressWarnings("unchecked")
    public <T> T getContext(String key, Class<T> clazz) {
        Object value = this.additionalContext.get(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    // Getter和Setter方法

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationTitle() {
        return conversationTitle;
    }

    public void setConversationTitle(String conversationTitle) {
        this.conversationTitle = conversationTitle;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Map<String, Object> getAdditionalContext() {
        return additionalContext;
    }

    public void setAdditionalContext(Map<String, Object> additionalContext) {
        this.additionalContext = additionalContext != null ? additionalContext : new HashMap<>();
    }

    @Override
    public String toString() {
        return "PromptContext{" +
                "userMessage='" + userMessage + '\'' +
                ", userId='" + userId + '\'' +
                ", conversationId=" + conversationId +
                ", conversationTitle='" + conversationTitle + '\'' +
                ", createTime=" + createTime +
                ", additionalContext=" + additionalContext +
                '}';
    }
}