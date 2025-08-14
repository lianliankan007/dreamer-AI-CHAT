package com.dreamer.chat.repository;

import com.dreamer.chat.entity.Message;
import com.dreamer.chat.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息数据访问层
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * 根据对话ID查找消息列表，按序号排序
     */
    List<Message> findByConversationIdOrderBySequenceNumberAsc(Long conversationId);
    
    /**
     * 根据对话ID分页查找消息
     */
    Page<Message> findByConversationIdOrderBySequenceNumberAsc(Long conversationId, Pageable pageable);
    
    /**
     * 根据对话ID和消息类型查找消息
     */
    List<Message> findByConversationIdAndMessageTypeOrderBySequenceNumberAsc(Long conversationId, MessageType messageType);    
    /**
     * 获取对话中的最后N条消息
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.sequenceNumber DESC")
    List<Message> findTopNByConversationId(@Param("conversationId") Long conversationId, Pageable pageable);
    
    /**
     * 获取对话中消息的最大序号
     */
    @Query("SELECT COALESCE(MAX(m.sequenceNumber), 0) FROM Message m WHERE m.conversation.id = :conversationId")
    Integer findMaxSequenceNumberByConversationId(@Param("conversationId") Long conversationId);
    
    /**
     * 根据时间范围查找消息
     */
    @Query("SELECT m FROM Message m WHERE m.timestamp BETWEEN :startTime AND :endTime ORDER BY m.timestamp DESC")
    List<Message> findByTimestampBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计对话中的消息数量
     */
    long countByConversationId(Long conversationId);
    
    /**
     * 根据内容模糊查询消息
     */
    @Query("SELECT m FROM Message m WHERE m.content LIKE %:content% ORDER BY m.timestamp DESC")
    List<Message> findByContentContaining(@Param("content") String content);
    
    /**
     * 删除对话中的所有消息
     */
    void deleteByConversationId(Long conversationId);
    
    /**
     * 获取对话的消息历史（用于上下文）
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.sequenceNumber <= :maxSequence ORDER BY m.sequenceNumber ASC")
    List<Message> findConversationHistory(@Param("conversationId") Long conversationId, @Param("maxSequence") Integer maxSequence);
}