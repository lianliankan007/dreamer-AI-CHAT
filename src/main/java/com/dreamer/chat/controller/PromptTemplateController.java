package com.dreamer.chat.controller;

import com.dreamer.chat.entity.PromptTemplate;
import com.dreamer.chat.enums.ModelProvider;
import com.dreamer.chat.enums.PromptType;
import com.dreamer.chat.service.PromptTemplateService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Prompt模板管理控制器
 * 提供模板的CRUD操作API
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/prompt-templates")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PromptTemplateController {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateController.class);

    @Autowired
    private PromptTemplateService promptTemplateService;

    /**
     * 获取所有启用的模板
     * 
     * @return 模板列表
     */
    @GetMapping
    public ResponseEntity<List<PromptTemplate>> getAllTemplates() {
        log.info("获取所有启用的Prompt模板");

        List<PromptTemplate> templates = promptTemplateService.getAllEnabledTemplates();

        log.info("获取模板完成，数量: {}", templates.size());
        return ResponseEntity.ok(templates);
    }

    /**
     * 根据模型提供商获取模板
     * 
     * @param provider 模型提供商代码
     * @return 模板列表
     */
    @GetMapping("/provider/{provider}")
    public ResponseEntity<List<PromptTemplate>> getTemplatesByProvider(@PathVariable String provider) {
        log.info("根据模型提供商获取模板: provider={}", provider);

        try {
            ModelProvider modelProvider = ModelProvider.fromCode(provider);
            List<PromptTemplate> templates = promptTemplateService.getTemplatesByProvider(modelProvider);

            log.info("获取模板完成: provider={}, count={}", provider, templates.size());
            return ResponseEntity.ok(templates);

        } catch (IllegalArgumentException e) {
            log.warn("无效的模型提供商: {}", provider);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 根据Prompt类型获取模板
     * 
     * @param type Prompt类型代码
     * @return 模板列表
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<PromptTemplate>> getTemplatesByType(@PathVariable String type) {
        log.info("根据Prompt类型获取模板: type={}", type);

        try {
            PromptType promptType = PromptType.fromCode(type);
            List<PromptTemplate> templates = promptTemplateService.getTemplatesByType(promptType);

            log.info("获取模板完成: type={}, count={}", type, templates.size());
            return ResponseEntity.ok(templates);

        } catch (IllegalArgumentException e) {
            log.warn("无效的Prompt类型: {}", type);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取最佳模板
     * 
     * @param provider 模型提供商代码
     * @param type     Prompt类型代码
     * @return 最佳模板
     */
    @GetMapping("/best")
    public ResponseEntity<PromptTemplate> getBestTemplate(
            @RequestParam String provider,
            @RequestParam(defaultValue = "chat") String type) {

        log.info("获取最佳模板: provider={}, type={}", provider, type);

        try {
            ModelProvider modelProvider = ModelProvider.fromCode(provider);
            PromptType promptType = PromptType.fromCode(type);

            Optional<PromptTemplate> template = promptTemplateService.getBestTemplate(modelProvider, promptType);

            if (template.isPresent()) {
                log.info("找到最佳模板: {}", template.get().getName());
                return ResponseEntity.ok(template.get());
            } else {
                log.warn("未找到适用模板: provider={}, type={}", provider, type);
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            log.warn("无效参数: provider={}, type={}", provider, type);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 根据ID获取模板
     * 
     * @param id 模板ID
     * @return 模板详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<PromptTemplate> getTemplateById(@PathVariable Long id) {
        log.info("根据ID获取模板: id={}", id);

        // 这里需要扩展PromptTemplateService添加findById方法
        // 暂时返回404
        return ResponseEntity.notFound().build();
    }

    /**
     * 创建新模板
     * 
     * @param template 模板数据
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTemplate(@Valid @RequestBody PromptTemplate template) {
        log.info("创建新模板: name={}, provider={}, type={}",
                template.getName(), template.getModelProvider(), template.getPromptType());

        try {
            PromptTemplate savedTemplate = promptTemplateService.createTemplate(template);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "模板创建成功");
            result.put("template", savedTemplate);

            log.info("模板创建成功: id={}, name={}", savedTemplate.getId(), savedTemplate.getName());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("模板创建失败", e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "模板创建失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 更新模板
     * 
     * @param id       模板ID
     * @param template 更新的模板数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTemplate(
            @PathVariable Long id, @Valid @RequestBody PromptTemplate template) {

        log.info("更新模板: id={}", id);

        try {
            Optional<PromptTemplate> updatedTemplate = promptTemplateService.updateTemplate(id, template);

            Map<String, Object> result = new HashMap<>();

            if (updatedTemplate.isPresent()) {
                result.put("success", true);
                result.put("message", "模板更新成功");
                result.put("template", updatedTemplate.get());

                log.info("模板更新成功: id={}, name={}", id, updatedTemplate.get().getName());
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "模板不存在");

                log.warn("模板不存在: id={}", id);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("模板更新失败: id={}", id, e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "模板更新失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 删除模板（软删除）
     * 
     * @param id 模板ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(@PathVariable Long id) {
        log.info("删除模板: id={}", id);

        boolean deleted = promptTemplateService.deleteTemplate(id);

        Map<String, Object> result = new HashMap<>();

        if (deleted) {
            result.put("success", true);
            result.put("message", "模板删除成功");

            log.info("模板删除成功: id={}", id);
            return ResponseEntity.ok(result);
        } else {
            result.put("success", false);
            result.put("message", "模板删除失败，可能模板不存在");

            log.warn("模板删除失败: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 清除模板缓存
     * 
     * @return 操作结果
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        log.info("清除Prompt模板缓存");

        promptTemplateService.clearCache();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "缓存清除成功");

        log.info("缓存清除完成");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取支持的模型提供商列表
     * 
     * @return 模型提供商列表
     */
    @GetMapping("/providers")
    public ResponseEntity<List<Map<String, String>>> getSupportedProviders() {
        log.info("获取支持的模型提供商列表");

        List<Map<String, String>> providers = List.of(
                Map.of("code", "qianwen", "name", "阿里巴巴千问"),
                Map.of("code", "xinghuo", "name", "讯飞星火"),
                Map.of("code", "doubao", "name", "豆包"),
                Map.of("code", "deepseek", "name", "DeepSeek"));

        return ResponseEntity.ok(providers);
    }

    /**
     * 获取支持的Prompt类型列表
     * 
     * @return Prompt类型列表
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, String>>> getSupportedTypes() {
        log.info("获取支持的Prompt类型列表");

        List<Map<String, String>> types = List.of(
                Map.of("code", "chat", "name", "常规聊天"),
                Map.of("code", "qa", "name", "问答"),
                Map.of("code", "code_generation", "name", "代码生成"),
                Map.of("code", "creative_writing", "name", "创意写作"),
                Map.of("code", "translation", "name", "翻译"),
                Map.of("code", "summarization", "name", "总结摘要"),
                Map.of("code", "analysis", "name", "分析解释"),
                Map.of("code", "role_play", "name", "角色扮演"));

        return ResponseEntity.ok(types);
    }
}