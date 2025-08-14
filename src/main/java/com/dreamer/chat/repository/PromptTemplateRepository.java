package com.dreamer.chat.repository;

import com.dreamer.chat.entity.PromptTemplate;
import com.dreamer.chat.enums.ModelProvider;
import com.dreamer.chat.enums.PromptType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Prompt模板数据访问层
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

    /**
     * 根据模型提供商和Prompt类型查找模板
     */
    List<PromptTemplate> findByModelProviderAndPromptTypeAndEnabledTrueOrderByPriorityDesc(
            ModelProvider modelProvider, PromptType promptType);

    /**
     * 根据模型提供商查找所有启用的模板
     */
    List<PromptTemplate> findByModelProviderAndEnabledTrueOrderByPriorityDesc(
            ModelProvider modelProvider);

    /**
     * 根据Prompt类型查找所有启用的模板
     */
    List<PromptTemplate> findByPromptTypeAndEnabledTrueOrderByPriorityDesc(
            PromptType promptType);

    /**
     * 根据名称查找模板
     */
    Optional<PromptTemplate> findByNameAndEnabledTrue(String name);

    /**
     * 获取指定模型提供商和类型的最高优先级模板
     */
    @Query("SELECT pt FROM PromptTemplate pt WHERE pt.modelProvider = :modelProvider " +
            "AND pt.promptType = :promptType AND pt.enabled = true " +
            "ORDER BY pt.priority DESC LIMIT 1")
    Optional<PromptTemplate> findTopByModelProviderAndPromptType(
            @Param("modelProvider") ModelProvider modelProvider,
            @Param("promptType") PromptType promptType);

    /**
     * 获取默认聊天模板（最高优先级的聊天模板）
     */
    @Query("SELECT pt FROM PromptTemplate pt WHERE pt.modelProvider = :modelProvider " +
            "AND pt.promptType = 'CHAT' AND pt.enabled = true " +
            "ORDER BY pt.priority DESC LIMIT 1")
    Optional<PromptTemplate> findDefaultChatTemplate(@Param("modelProvider") ModelProvider modelProvider);

    /**
     * 根据创建者查找模板
     */
    List<PromptTemplate> findByCreatedByAndEnabledTrueOrderByCreatedTimeDesc(String createdBy);

    /**
     * 查找所有启用的模板
     */
    List<PromptTemplate> findByEnabledTrueOrderByModelProviderAscPromptTypeAscPriorityDesc();

    /**
     * 检查是否存在相同配置的模板
     */
    boolean existsByModelProviderAndPromptTypeAndNameAndEnabledTrue(
            ModelProvider modelProvider, PromptType promptType, String name);
}