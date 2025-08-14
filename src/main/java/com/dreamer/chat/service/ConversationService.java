package com.dreamer.chat.service;

import com.dreamer.chat.config.GlobalExceptionHandler;
import com.dreamer.chat.entity.Conversation;
import com.dreamer.chat.entity.Message;
import com.dreamer.chat.enums.ModelProvider;
import com.dreamer.chat.repository.ConversationRepository;
import com.dreamer.chat.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 对话管理服务类
 * 负责对话的创建、查询、更新和删除等操作
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Service
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    /**
     * 创建新对话
     * 
     * @param title         对话标题
     * @param modelProvider 模型提供商
     * @param modelName     模型名称
     * @param userId        用户ID
     * @return 创建的对话实体
     */
    @Transactional
    public Conversation createConversation(String title, ModelProvider modelProvider,
            String modelName, String userId) {
        if (!StringUtils.hasText(title)) {
            throw new GlobalExceptionHandler.BusinessException("INVALID_TITLE", "对话标题不能为空");
        }

        if (modelProvider == null) {
            throw new GlobalExceptionHandler.BusinessException("INVALID_MODEL_PROVIDER", "模型提供商不能为空");
        }

        Conversation conversation = new Conversation(title, modelProvider, modelName);
        conversation.setUserId(userId);
        conversation.setStatus("ACTIVE");
        conversation.setCreatedTime(LocalDateTime.now());
        conversation.setUpdatedTime(LocalDateTime.now());

        Conversation savedConversation = conversationRepository.save(conversation);

        log.info("创建新对话成功: id={}, title={}, modelProvider={}, userId={}",
                savedConversation.getId(), title, modelProvider.getCode(), userId);

        return savedConversation;
    }

    /**
     * 根据ID获取对话
     * 
     * @param conversationId 对话ID
     * @return 对话实体（可能为空）
     */
    public Optional<Conversation> getConversationById(Long conversationId) {
        if (conversationId == null) {
            return Optional.empty();
        }

        return conversationRepository.findById(conversationId);
    }

    /**
     * 根据ID和用户ID获取对话（安全检查）
     * 
     * @param conversationId 对话ID
     * @param userId         用户ID
     * @return 对话实体（可能为空）
     */
    public Optional<Conversation> getConversationByIdAndUserId(Long conversationId, String userId) {
        if (conversationId == null) {
            return Optional.empty();
        }

        return conversationRepository.findByIdAndUserId(conversationId, userId);
    }

    /**
     * 获取用户的对话列表
     * 
     * @param userId 用户ID
     * @return 对话列表
     */
    public List<Conversation> getUserConversations(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new GlobalExceptionHandler.BusinessException("INVALID_USER_ID", "用户ID不能为空");
        }

        return conversationRepository.findByUserIdOrderByUpdatedTimeDesc(userId);
    }

    /**
     * 分页获取用户的活跃对话列表
     * 
     * @param userId 用户ID
     * @param page   页码（从0开始）
     * @param size   每页大小
     * @return 分页对话列表
     */
    public Page<Conversation> getUserActiveConversations(String userId, int page, int size) {
        if (!StringUtils.hasText(userId)) {
            throw new GlobalExceptionHandler.BusinessException("INVALID_USER_ID", "用户ID不能为空");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedTime"));
        return conversationRepository.findByUserIdAndStatusOrderByUpdatedTimeDesc(userId, "ACTIVE", pageable);
    }

    /**
     * 获取最近的对话列表
     * 
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 最近对话列表
     */
    public List<Conversation> getRecentConversations(String userId, int limit) {
        if (!StringUtils.hasText(userId)) {
            throw new GlobalExceptionHandler.BusinessException("INVALID_USER_ID", "用户ID不能为空");
        }

        Pageable pageable = PageRequest.of(0, limit);
        Page<Conversation> page = conversationRepository.findRecentConversations(userId, "ACTIVE", pageable);
        return page.getContent();
    }

    /**
     * 更新对话标题
     * 
     * @param conversationId 对话ID
     * @param newTitle       新标题
     * @param userId         用户ID（用于安全检查）
     * @return 更新后的对话
     */
    @Transactional
    public Conversation updateConversationTitle(Long conversationId, String newTitle, String userId) {
        if (!StringUtils.hasText(newTitle)) {
            throw new GlobalExceptionHandler.BusinessException("INVALID_TITLE", "对话标题不能为空");
        }

        Conversation conversation = getConversationByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new GlobalExceptionHandler.BusinessException("CONVERSATION_NOT_FOUND",
                        "对话不存在或无权限访问"));

        conversation.setTitle(newTitle);
        conversation.setUpdatedTime(LocalDateTime.now());

        Conversation updatedConversation = conversationRepository.save(conversation);

        log.info("更新对话标题成功: id={}, newTitle={}, userId={}",
                conversationId, newTitle, userId);

        return updatedConversation;
    }

    /**
     * 归档对话
     * 
     * @param conversationId 对话ID
     * @param userId         用户ID（用于安全检查）
     */
    @Transactional
    public void archiveConversation(Long conversationId, String userId) {
        Conversation conversation = getConversationByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new GlobalExceptionHandler.BusinessException("CONVERSATION_NOT_FOUND",
                        "对话不存在或无权限访问"));

        conversation.setStatus("ARCHIVED");
        conversation.setUpdatedTime(LocalDateTime.now());

        conversationRepository.save(conversation);

        log.info("归档对话成功: id={}, userId={}", conversationId, userId);
    }

    /**
     * 删除对话（软删除）
     * 
     * @param conversationId 对话ID
     * @param userId         用户ID（用于安全检查）
     */
    @Transactional
    public void deleteConversation(Long conversationId, String userId) {
        Conversation conversation = getConversationByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new GlobalExceptionHandler.BusinessException("CONVERSATION_NOT_FOUND",
                        "对话不存在或无权限访问"));

        conversation.setStatus("DELETED");
        conversation.setUpdatedTime(LocalDateTime.now());

        conversationRepository.save(conversation);

        log.info("删除对话成功: id={}, userId={}", conversationId, userId);
    }

    /**
     * 彻底删除对话及其消息
     * 
     * @param conversationId 对话ID
     * @param userId         用户ID（用于安全检查）
     */
    @Transactional
    public void permanentlyDeleteConversation(Long conversationId, String userId) {
        Conversation conversation = getConversationByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new GlobalExceptionHandler.BusinessException("CONVERSATION_NOT_FOUND",
                        "对话不存在或无权限访问"));

        // 先删除所有相关消息
        messageRepository.deleteByConversationId(conversationId);

        // 再删除对话
        conversationRepository.delete(conversation);

        log.info("彻底删除对话成功: id={}, userId={}", conversationId, userId);
    }

    /**
     * 恢复已删除的对话
     * 
     * @param conversationId 对话ID
     * @param userId         用户ID（用于安全检查）
     */
    @Transactional
    public void restoreConversation(Long conversationId, String userId) {
        Optional<Conversation> optConversation = conversationRepository.findByIdAndUserId(conversationId, userId);

        if (optConversation.isEmpty()) {
            throw new GlobalExceptionHandler.BusinessException("CONVERSATION_NOT_FOUND",
                    "对话不存在或无权限访问");
        }

        Conversation conversation = optConversation.get();

        if (!"DELETED".equals(conversation.getStatus())) {
            throw new GlobalExceptionHandler.BusinessException("CONVERSATION_NOT_DELETED",
                    "对话未处于删除状态，无法恢复");
        }

        conversation.setStatus("ACTIVE");
        conversation.setUpdatedTime(LocalDateTime.now());

        conversationRepository.save(conversation);

        log.info("恢复对话成功: id={}, userId={}", conversationId, userId);
    }

    /**
     * 根据标题搜索对话
     * 
     * @param userId       用户ID
     * @param titleKeyword 标题关键词
     * @return 匹配的对话列表
     */
    public List<Conversation> searchConversationsByTitle(String userId, String titleKeyword) {
        if (!StringUtils.hasText(userId)) {
            throw new GlobalExceptionHandler.BusinessException("INVALID_USER_ID", "用户ID不能为空");
        }

        if (!StringUtils.hasText(titleKeyword)) {
            return getUserConversations(userId);
        }

        return conversationRepository.findByTitleContainingAndStatus(titleKeyword, "ACTIVE");
    }

    /**
     * 统计用户的对话数量
     * 
     * @param userId 用户ID
     * @param status 对话状态
     * @return 对话数量
     */
    public long countUserConversations(String userId, String status) {
        if (!StringUtils.hasText(userId)) {
            return 0;
        }

        return conversationRepository.countByUserIdAndStatus(userId, status);
    }

    /**
     * 获取对话的详细信息（包含消息）
     * 
     * @param conversationId 对话ID
     * @param userId         用户ID（用于安全检查）
     * @return 对话详情
     */
    public ConversationDetail getConversationDetail(Long conversationId, String userId) {
        Conversation conversation = getConversationByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new GlobalExceptionHandler.BusinessException("CONVERSATION_NOT_FOUND",
                        "对话不存在或无权限访问"));

        List<Message> messages = messageRepository.findByConversationIdOrderBySequenceNumberAsc(conversationId);

        return new ConversationDetail(conversation, messages);
    }

    /**
     * 对话详情类
     */
    public static class ConversationDetail {
        private final Conversation conversation;
        private final List<Message> messages;

        public ConversationDetail(Conversation conversation, List<Message> messages) {
            this.conversation = conversation;
            this.messages = messages;
        }

        public Conversation getConversation() {
            return conversation;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public int getMessageCount() {
            return messages.size();
        }

        public LocalDateTime getLastMessageTime() {
            if (messages.isEmpty()) {
                return conversation.getCreatedTime();
            }
            return messages.get(messages.size() - 1).getTimestamp();
        }
    }
}