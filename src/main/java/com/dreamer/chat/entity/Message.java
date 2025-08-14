package com.dreamer.chat.entity;

import com.dreamer.chat.enums.MessageType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 消息实体类
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Entity
@Table(name = "messages")
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 所属对话
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;
    
    /**
     * 消息内容
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;    
    /**
     * 消息类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;
    
    /**
     * 消息序号（在对话中的顺序）
     */
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;
    
    /**
     * Token数量（预留）
     */
    @Column(name = "token_count")
    private Integer tokenCount;
    
    /**
     * 消息时间戳
     */
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    /**
     * 额外的元数据（JSON格式）
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    // 构造函数
    public Message() {}
    
    public Message(String content, MessageType messageType) {
        this.content = content;
        this.messageType = messageType;
    }    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Conversation getConversation() {
        return conversation;
    }
    
    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public MessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }    
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public Integer getTokenCount() {
        return tokenCount;
    }
    
    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", messageType=" + messageType +
                ", sequenceNumber=" + sequenceNumber +
                ", tokenCount=" + tokenCount +
                ", timestamp=" + timestamp +
                '}';
    }
}