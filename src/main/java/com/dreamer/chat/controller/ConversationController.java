package com.dreamer.chat.controller;

import com.dreamer.chat.entity.Conversation;
import com.dreamer.chat.service.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 对话管理控制器
 * 提供对话历史查询和管理的REST API接口
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/conversations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConversationController {

    private static final Logger log = LoggerFactory.getLogger(ConversationController.class);

    @Autowired
    private ConversationService conversationService;

    /**
     * 获取用户的对话列表
     * 
     * @param userId 用户ID
     * @return 对话列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Conversation>> getUserConversations(@PathVariable String userId) {
        log.info("获取用户对话列表: userId={}", userId);

        List<Conversation> conversations = conversationService.getUserConversations(userId);

        log.info("获取用户对话列表完成: userId={}, count={}", userId, conversations.size());

        return ResponseEntity.ok(conversations);
    }

    /**
     * 分页获取用户的活跃对话列表
     * 
     * @param userId 用户ID
     * @param page   页码（从0开始）
     * @param size   每页大小
     * @return 分页对话列表
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<Page<Conversation>> getUserActiveConversations(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("分页获取用户活跃对话: userId={}, page={}, size={}", userId, page, size);

        Page<Conversation> conversationPage = conversationService.getUserActiveConversations(userId, page, size);

        log.info("分页获取用户活跃对话完成: userId={}, totalElements={}, totalPages={}",
                userId, conversationPage.getTotalElements(), conversationPage.getTotalPages());

        return ResponseEntity.ok(conversationPage);
    }

    /**
     * 获取最近的对话列表
     * 
     * @param userId 用户ID
     * @param limit  限制数量（默认10）
     * @return 最近对话列表
     */
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<List<Conversation>> getRecentConversations(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("获取最近对话列表: userId={}, limit={}", userId, limit);

        List<Conversation> conversations = conversationService.getRecentConversations(userId, limit);

        log.info("获取最近对话列表完成: userId={}, count={}", userId, conversations.size());

        return ResponseEntity.ok(conversations);
    }

    /**
     * 获取对话详情
     * 
     * @param conversationId 对话ID
     * @param userId         用户ID（用于权限校验）
     * @return 对话详情
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationService.ConversationDetail> getConversationDetail(
            @PathVariable Long conversationId,
            @RequestParam String userId) {

        log.info("获取对话详情: conversationId={}, userId={}", conversationId, userId);

        ConversationService.ConversationDetail detail = conversationService.getConversationDetail(conversationId,
                userId);

        log.info("获取对话详情完成: conversationId={}, messageCount={}",
                conversationId, detail.getMessageCount());

        return ResponseEntity.ok(detail);
    }

    /**
     * 更新对话标题
     * 
     * @param conversationId 对话ID
     * @param userId         用户ID（用于权限校验）
     * @param newTitle       新标题
     * @return 更新后的对话
     */
    @PutMapping("/{conversationId}/title")
    public ResponseEntity<Conversation> updateConversationTitle(
            @PathVariable Long conversationId,
            @RequestParam String userId,
            @RequestParam String newTitle) {

        log.info("更新对话标题: conversationId={}, userId={}, newTitle={}",
                conversationId, userId, newTitle);

        Conversation updatedConversation = conversationService.updateConversationTitle(conversationId, newTitle,
                userId);

        log.info("更新对话标题完成: conversationId={}", conversationId);

        return ResponseEntity.ok(updatedConversation);
    }

    /**
     * 归档对话
     * 
     * @param conversationId 对话ID
     * @param userId         用户ID（用于权限校验）
     * @return 操作结果
     */
    @PutMapping("/{conversationId}/archive")
    public ResponseEntity<Map<String, Object>> archiveConversation(
            @PathVariable Long conversationId,
            @RequestParam String userId) {

        log.info("归档对话: conversationId={}, userId={}", conversationId, userId);

        conversationService.archiveConversation(conversationId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "对话已归档");
        result.put("conversationId", conversationId);

        log.info("归档对话完成: conversationId={}", conversationId);

        return ResponseEntity.ok(result);
    }

    /**
     * 删除对话（软删除）
     * 
     * @param conversationId 对话ID
     * @param userId         用户ID（用于权限校验）
     * @return 操作结果
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Map<String, Object>> deleteConversation(
            @PathVariable Long conversationId,
            @RequestParam String userId) {

        log.info("删除对话: conversationId={}, userId={}", conversationId, userId);

        conversationService.deleteConversation(conversationId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "对话已删除");
        result.put("conversationId", conversationId);

        log.info("删除对话完成: conversationId={}", conversationId);

        return ResponseEntity.ok(result);
    }

    /**
     * 彻底删除对话及其消息
     * 
     * @param conversationId 对话ID
     * @param userId         用户ID（用于权限校验）
     * @return 操作结果
     */
    @DeleteMapping("/{conversationId}/permanent")
    public ResponseEntity<Map<String, Object>> permanentlyDeleteConversation(
            @PathVariable Long conversationId,
            @RequestParam String userId) {

        log.info("彻底删除对话: conversationId={}, userId={}", conversationId, userId);

        conversationService.permanentlyDeleteConversation(conversationId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "对话已彻底删除");
        result.put("conversationId", conversationId);

        log.info("彻底删除对话完成: conversationId={}", conversationId);

        return ResponseEntity.ok(result);
    }

    /**
     * 恢复已删除的对话
     * 
     * @param conversationId 对话ID
     * @param userId         用户ID（用于权限校验）
     * @return 操作结果
     */
    @PutMapping("/{conversationId}/restore")
    public ResponseEntity<Map<String, Object>> restoreConversation(
            @PathVariable Long conversationId,
            @RequestParam String userId) {

        log.info("恢复对话: conversationId={}, userId={}", conversationId, userId);

        conversationService.restoreConversation(conversationId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "对话已恢复");
        result.put("conversationId", conversationId);

        log.info("恢复对话完成: conversationId={}", conversationId);

        return ResponseEntity.ok(result);
    }

    /**
     * 搜索对话
     * 
     * @param userId  用户ID
     * @param keyword 搜索关键词
     * @return 匹配的对话列表
     */
    @GetMapping("/search")
    public ResponseEntity<List<Conversation>> searchConversations(
            @RequestParam String userId,
            @RequestParam String keyword) {

        log.info("搜索对话: userId={}, keyword={}", userId, keyword);

        List<Conversation> conversations = conversationService.searchConversationsByTitle(userId, keyword);

        log.info("搜索对话完成: userId={}, keyword={}, count={}",
                userId, keyword, conversations.size());

        return ResponseEntity.ok(conversations);
    }

    /**
     * 统计用户对话数量
     * 
     * @param userId 用户ID
     * @param status 对话状态（可选）
     * @return 统计结果
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> getUserConversationStats(
            @PathVariable String userId,
            @RequestParam(defaultValue = "ACTIVE") String status) {

        log.info("统计用户对话: userId={}, status={}", userId, status);

        long count = conversationService.countUserConversations(userId, status);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("status", status);
        result.put("count", count);

        log.info("统计用户对话完成: userId={}, status={}, count={}", userId, status, count);

        return ResponseEntity.ok(result);
    }

    /**
     * 检查对话是否存在
     * 
     * @param conversationId 对话ID
     * @return 检查结果
     */
    @GetMapping("/{conversationId}/exists")
    public ResponseEntity<Map<String, Object>> checkConversationExists(@PathVariable Long conversationId) {
        log.info("检查对话是否存在: conversationId={}", conversationId);

        Optional<Conversation> conversation = conversationService.getConversationById(conversationId);
        boolean exists = conversation.isPresent();

        Map<String, Object> result = new HashMap<>();
        result.put("conversationId", conversationId);
        result.put("exists", exists);
        if (exists) {
            result.put("status", conversation.get().getStatus());
        }

        log.info("检查对话是否存在完成: conversationId={}, exists={}", conversationId, exists);

        return ResponseEntity.ok(result);
    }
}