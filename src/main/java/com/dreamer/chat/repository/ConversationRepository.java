package com.dreamer.chat.repository;

import com.dreamer.chat.entity.Conversation;
import com.dreamer.chat.enums.ModelProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 对话数据访问层
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    /**
     * 根据用户ID查找对话列表
     */
    List<Conversation> findByUserIdOrderByUpdatedTimeDesc(String userId);
    
    /**
     * 根据用户ID分页查找对话列表
     */
    Page<Conversation> findByUserIdAndStatusOrderByUpdatedTimeDesc(String userId, String status, Pageable pageable);
    
    /**
     * 根据模型提供商查找对话
     */
    List<Conversation> findByModelProviderOrderByCreatedTimeDesc(ModelProvider modelProvider);
    
    /**
     * 根据标题模糊查询对话
     */
    @Query("SELECT c FROM Conversation c WHERE c.title LIKE %:title% AND c.status = :status ORDER BY c.updatedTime DESC")
    List<Conversation> findByTitleContainingAndStatus(@Param("title") String title, @Param("status") String status);
    
    /**
     * 查找指定时间范围内的对话
     */
    @Query("SELECT c FROM Conversation c WHERE c.createdTime BETWEEN :startTime AND :endTime ORDER BY c.createdTime DESC")
    List<Conversation> findByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据ID和用户ID查找对话（确保安全性）
     */
    Optional<Conversation> findByIdAndUserId(Long id, String userId);
    
    /**
     * 统计用户的对话数量
     */
    long countByUserIdAndStatus(String userId, String status);
    
    /**
     * 查找用户最近的对话
     */
    @Query("SELECT c FROM Conversation c WHERE c.userId = :userId AND c.status = :status ORDER BY c.updatedTime DESC")
    Page<Conversation> findRecentConversations(@Param("userId") String userId, @Param("status") String status, Pageable pageable);
}