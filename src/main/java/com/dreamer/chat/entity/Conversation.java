package com.dreamer.chat.entity;

import com.dreamer.chat.enums.ModelProvider;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 对话实体类
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Entity
@Table(name = "conversations")
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 对话标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    /**
     * 使用的模型提供商
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "model_provider", nullable = false, length = 20)
    private ModelProvider modelProvider;    
    /**
     * 具体模型名称
     */
    @Column(name = "model_name", length = 50)
    private String modelName;
    
    /**
     * 用户ID（预留）
     */
    @Column(name = "user_id", length = 50)
    private String userId;
    
    /**
     * 对话状态：ACTIVE-活跃，ARCHIVED-归档，DELETED-已删除
     */
    @Column(name = "status", length = 20)
    private String status = "ACTIVE";
    
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
     * 对话中的消息列表
     */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages = new ArrayList<>();    
    // 构造函数
    public Conversation() {}
    
    public Conversation(String title, ModelProvider modelProvider, String modelName) {
        this.title = title;
        this.modelProvider = modelProvider;
        this.modelName = modelName;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public ModelProvider getModelProvider() {
        return modelProvider;
    }
    
    public void setModelProvider(ModelProvider modelProvider) {
        this.modelProvider = modelProvider;
    }    
    public String getModelName() {
        return modelName;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    /**
     * 添加消息到对话中
     */
    public void addMessage(Message message) {
        this.messages.add(message);
        message.setConversation(this);
    }
    
    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", modelProvider=" + modelProvider +
                ", modelName='" + modelName + '\'' +
                ", userId='" + userId + '\'' +
                ", status='" + status + '\'' +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }
}