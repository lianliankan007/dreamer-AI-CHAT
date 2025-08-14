package com.dreamer.chat.entity;

import com.dreamer.chat.enums.ModelProvider;
import com.dreamer.chat.enums.PromptType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Prompt模板实体类
 * 定义不同模型和场景的prompt模板
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Entity
@Table(name = "prompt_templates")
public class PromptTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 模板名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 模型提供商
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "model_provider", nullable = false, length = 20)
    private ModelProvider modelProvider;

    /**
     * Prompt类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "prompt_type", nullable = false, length = 30)
    private PromptType promptType;

    /**
     * 系统提示词
     */
    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    /**
     * 用户消息前缀
     */
    @Column(name = "user_prefix", length = 50)
    private String userPrefix;

    /**
     * 助手消息前缀
     */
    @Column(name = "assistant_prefix", length = 50)
    private String assistantPrefix;

    /**
     * 对话开始模板
     */
    @Column(name = "conversation_starter", columnDefinition = "TEXT")
    private String conversationStarter;

    /**
     * 最大上下文长度
     */
    @Column(name = "max_context_length")
    private Integer maxContextLength;

    /**
     * 温度参数（创造性）
     */
    @Column(name = "temperature")
    private Double temperature;

    /**
     * 最大输出Token数
     */
    @Column(name = "max_tokens")
    private Integer maxTokens;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 优先级（数字越大优先级越高）
     */
    @Column(name = "priority")
    private Integer priority = 0;

    /**
     * 模板描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 额外配置（JSON格式）
     */
    @Column(name = "extra_config", columnDefinition = "TEXT")
    private String extraConfig;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    /**
     * 创建者
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    // 构造函数
    public PromptTemplate() {
    }

    public PromptTemplate(String name, ModelProvider modelProvider, PromptType promptType) {
        this.name = name;
        this.modelProvider = modelProvider;
        this.promptType = promptType;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModelProvider getModelProvider() {
        return modelProvider;
    }

    public void setModelProvider(ModelProvider modelProvider) {
        this.modelProvider = modelProvider;
    }

    public PromptType getPromptType() {
        return promptType;
    }

    public void setPromptType(PromptType promptType) {
        this.promptType = promptType;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getUserPrefix() {
        return userPrefix;
    }

    public void setUserPrefix(String userPrefix) {
        this.userPrefix = userPrefix;
    }

    public String getAssistantPrefix() {
        return assistantPrefix;
    }

    public void setAssistantPrefix(String assistantPrefix) {
        this.assistantPrefix = assistantPrefix;
    }

    public String getConversationStarter() {
        return conversationStarter;
    }

    public void setConversationStarter(String conversationStarter) {
        this.conversationStarter = conversationStarter;
    }

    public Integer getMaxContextLength() {
        return maxContextLength;
    }

    public void setMaxContextLength(Integer maxContextLength) {
        this.maxContextLength = maxContextLength;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtraConfig() {
        return extraConfig;
    }

    public void setExtraConfig(String extraConfig) {
        this.extraConfig = extraConfig;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "PromptTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", modelProvider=" + modelProvider +
                ", promptType=" + promptType +
                ", enabled=" + enabled +
                ", priority=" + priority +
                '}';
    }
}