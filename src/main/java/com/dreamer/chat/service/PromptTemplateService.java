package com.dreamer.chat.service;

import com.dreamer.chat.entity.PromptTemplate;
import com.dreamer.chat.enums.ModelProvider;
import com.dreamer.chat.enums.PromptType;
import com.dreamer.chat.repository.PromptTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Prompt模板管理服务
 * 负责模板的CRUD操作和智能选择
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Service
@Transactional
public class PromptTemplateService {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateService.class);

    @Autowired
    private PromptTemplateRepository promptTemplateRepository;

    /**
     * 获取最佳Prompt模板
     * 优先选择指定模型和类型的模板，如果没有则使用默认聊天模板
     * 
     * @param modelProvider 模型提供商
     * @param promptType    Prompt类型
     * @return Prompt模板
     */
    @Cacheable(value = "promptTemplates", key = "#modelProvider.code + '_' + #promptType.code")
    public Optional<PromptTemplate> getBestTemplate(ModelProvider modelProvider, PromptType promptType) {
        log.debug("查找最佳模板: modelProvider={}, promptType={}", modelProvider, promptType);

        // 1. 首先尝试获取指定模型和类型的模板
        Optional<PromptTemplate> specificTemplate = promptTemplateRepository
                .findTopByModelProviderAndPromptType(modelProvider, promptType);

        if (specificTemplate.isPresent()) {
            log.debug("找到专用模板: {}", specificTemplate.get().getName());
            return specificTemplate;
        }

        // 2. 如果没有专用模板，使用默认聊天模板
        Optional<PromptTemplate> defaultTemplate = promptTemplateRepository
                .findDefaultChatTemplate(modelProvider);

        if (defaultTemplate.isPresent()) {
            log.debug("使用默认聊天模板: {}", defaultTemplate.get().getName());
            return defaultTemplate;
        }

        // 3. 如果连默认模板都没有，记录警告
        log.warn("未找到适用的模板: modelProvider={}, promptType={}", modelProvider, promptType);
        return Optional.empty();
    }

    /**
     * 获取默认聊天模板
     * 
     * @param modelProvider 模型提供商
     * @return 默认聊天模板
     */
    @Cacheable(value = "defaultChatTemplates", key = "#modelProvider.code")
    public Optional<PromptTemplate> getDefaultChatTemplate(ModelProvider modelProvider) {
        return promptTemplateRepository.findDefaultChatTemplate(modelProvider);
    }

    /**
     * 根据模型提供商获取所有模板
     * 
     * @param modelProvider 模型提供商
     * @return 模板列表
     */
    public List<PromptTemplate> getTemplatesByProvider(ModelProvider modelProvider) {
        return promptTemplateRepository.findByModelProviderAndEnabledTrueOrderByPriorityDesc(modelProvider);
    }

    /**
     * 根据Prompt类型获取所有模板
     * 
     * @param promptType Prompt类型
     * @return 模板列表
     */
    public List<PromptTemplate> getTemplatesByType(PromptType promptType) {
        return promptTemplateRepository.findByPromptTypeAndEnabledTrueOrderByPriorityDesc(promptType);
    }

    /**
     * 根据名称获取模板
     * 
     * @param name 模板名称
     * @return 模板
     */
    public Optional<PromptTemplate> getTemplateByName(String name) {
        if (!StringUtils.hasText(name)) {
            return Optional.empty();
        }
        return promptTemplateRepository.findByNameAndEnabledTrue(name);
    }

    /**
     * 创建新模板
     * 
     * @param template 模板实体
     * @return 创建的模板
     */
    @CacheEvict(value = { "promptTemplates", "defaultChatTemplates" }, allEntries = true)
    public PromptTemplate createTemplate(PromptTemplate template) {
        log.info("创建新模板: name={}, provider={}, type={}",
                template.getName(), template.getModelProvider(), template.getPromptType());

        // 验证模板
        validateTemplate(template);

        // 设置默认值
        if (template.getEnabled() == null) {
            template.setEnabled(true);
        }
        if (template.getPriority() == null) {
            template.setPriority(0);
        }
        if (template.getMaxContextLength() == null) {
            template.setMaxContextLength(4000);
        }

        return promptTemplateRepository.save(template);
    }

    /**
     * 更新模板
     * 
     * @param id       模板ID
     * @param template 更新的模板数据
     * @return 更新后的模板
     */
    @CacheEvict(value = { "promptTemplates", "defaultChatTemplates" }, allEntries = true)
    public Optional<PromptTemplate> updateTemplate(Long id, PromptTemplate template) {
        log.info("更新模板: id={}", id);

        return promptTemplateRepository.findById(id)
                .map(existingTemplate -> {
                    // 更新字段
                    if (StringUtils.hasText(template.getName())) {
                        existingTemplate.setName(template.getName());
                    }
                    if (template.getModelProvider() != null) {
                        existingTemplate.setModelProvider(template.getModelProvider());
                    }
                    if (template.getPromptType() != null) {
                        existingTemplate.setPromptType(template.getPromptType());
                    }
                    if (template.getSystemPrompt() != null) {
                        existingTemplate.setSystemPrompt(template.getSystemPrompt());
                    }
                    if (template.getUserPrefix() != null) {
                        existingTemplate.setUserPrefix(template.getUserPrefix());
                    }
                    if (template.getAssistantPrefix() != null) {
                        existingTemplate.setAssistantPrefix(template.getAssistantPrefix());
                    }
                    if (template.getConversationStarter() != null) {
                        existingTemplate.setConversationStarter(template.getConversationStarter());
                    }
                    if (template.getMaxContextLength() != null) {
                        existingTemplate.setMaxContextLength(template.getMaxContextLength());
                    }
                    if (template.getTemperature() != null) {
                        existingTemplate.setTemperature(template.getTemperature());
                    }
                    if (template.getMaxTokens() != null) {
                        existingTemplate.setMaxTokens(template.getMaxTokens());
                    }
                    if (template.getEnabled() != null) {
                        existingTemplate.setEnabled(template.getEnabled());
                    }
                    if (template.getPriority() != null) {
                        existingTemplate.setPriority(template.getPriority());
                    }
                    if (template.getDescription() != null) {
                        existingTemplate.setDescription(template.getDescription());
                    }
                    if (template.getExtraConfig() != null) {
                        existingTemplate.setExtraConfig(template.getExtraConfig());
                    }

                    // 验证更新后的模板
                    validateTemplate(existingTemplate);

                    return promptTemplateRepository.save(existingTemplate);
                });
    }

    /**
     * 删除模板（软删除，设置为禁用）
     * 
     * @param id 模板ID
     * @return 是否删除成功
     */
    @CacheEvict(value = { "promptTemplates", "defaultChatTemplates" }, allEntries = true)
    public boolean deleteTemplate(Long id) {
        log.info("删除模板: id={}", id);

        return promptTemplateRepository.findById(id)
                .map(template -> {
                    template.setEnabled(false);
                    promptTemplateRepository.save(template);
                    return true;
                })
                .orElse(false);
    }

    /**
     * 获取所有启用的模板
     * 
     * @return 模板列表
     */
    public List<PromptTemplate> getAllEnabledTemplates() {
        return promptTemplateRepository.findByEnabledTrueOrderByModelProviderAscPromptTypeAscPriorityDesc();
    }

    /**
     * 根据创建者获取模板
     * 
     * @param createdBy 创建者
     * @return 模板列表
     */
    public List<PromptTemplate> getTemplatesByCreator(String createdBy) {
        if (!StringUtils.hasText(createdBy)) {
            return List.of();
        }
        return promptTemplateRepository.findByCreatedByAndEnabledTrueOrderByCreatedTimeDesc(createdBy);
    }

    /**
     * 检查模板是否存在
     * 
     * @param modelProvider 模型提供商
     * @param promptType    Prompt类型
     * @param name          模板名称
     * @return 是否存在
     */
    public boolean templateExists(ModelProvider modelProvider, PromptType promptType, String name) {
        return promptTemplateRepository.existsByModelProviderAndPromptTypeAndNameAndEnabledTrue(
                modelProvider, promptType, name);
    }

    /**
     * 清除模板缓存
     */
    @CacheEvict(value = { "promptTemplates", "defaultChatTemplates" }, allEntries = true)
    public void clearCache() {
        log.info("清除Prompt模板缓存");
    }

    /**
     * 验证模板数据
     * 
     * @param template 待验证的模板
     */
    private void validateTemplate(PromptTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("模板不能为空");
        }

        if (!StringUtils.hasText(template.getName())) {
            throw new IllegalArgumentException("模板名称不能为空");
        }

        if (template.getModelProvider() == null) {
            throw new IllegalArgumentException("模型提供商不能为空");
        }

        if (template.getPromptType() == null) {
            throw new IllegalArgumentException("Prompt类型不能为空");
        }

        // 验证数值范围
        if (template.getMaxContextLength() != null && template.getMaxContextLength() <= 0) {
            throw new IllegalArgumentException("最大上下文长度必须大于0");
        }

        if (template.getTemperature() != null &&
                (template.getTemperature() < 0 || template.getTemperature() > 2)) {
            throw new IllegalArgumentException("温度参数必须在0-2之间");
        }

        if (template.getMaxTokens() != null && template.getMaxTokens() <= 0) {
            throw new IllegalArgumentException("最大Token数必须大于0");
        }

        if (template.getPriority() != null && template.getPriority() < 0) {
            throw new IllegalArgumentException("优先级不能为负数");
        }
    }
}