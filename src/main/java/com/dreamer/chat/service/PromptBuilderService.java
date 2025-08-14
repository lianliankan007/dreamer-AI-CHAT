package com.dreamer.chat.service;

import com.dreamer.chat.entity.Message;
import com.dreamer.chat.entity.PromptTemplate;
import com.dreamer.chat.enums.MessageType;
import com.dreamer.chat.enums.ModelProvider;
import com.dreamer.chat.enums.PromptType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 智能Prompt构建服务
 * 根据模板和上下文构建最优化的prompt
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Service
public class PromptBuilderService {

    private static final Logger log = LoggerFactory.getLogger(PromptBuilderService.class);

    // 变量替换的正则表达式
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    @Autowired
    private PromptTemplateService promptTemplateService;

    /**
     * 构建完整的Prompt
     * 
     * @param modelProvider   模型提供商
     * @param promptType      Prompt类型
     * @param contextMessages 上下文消息
     * @param currentMessage  当前用户消息
     * @param variables       自定义变量
     * @return 构建的Prompt字符串
     */
    public String buildPrompt(ModelProvider modelProvider, PromptType promptType,
            List<Message> contextMessages, String currentMessage,
            Map<String, String> variables) {

        log.debug("构建Prompt: provider={}, type={}, contextSize={}",
                modelProvider, promptType, contextMessages.size());

        // 1. 获取最佳模板
        Optional<PromptTemplate> templateOpt = promptTemplateService.getBestTemplate(modelProvider, promptType);

        if (templateOpt.isEmpty()) {
            log.warn("未找到适用模板，使用基础格式: provider={}, type={}", modelProvider, promptType);
            return buildBasicPrompt(contextMessages, currentMessage);
        }

        PromptTemplate template = templateOpt.get();

        // 2. 处理上下文长度限制
        List<Message> processedMessages = limitContextLength(contextMessages, template.getMaxContextLength());

        // 3. 构建完整Prompt
        StringBuilder promptBuilder = new StringBuilder();

        // 3.1 添加系统提示词
        if (StringUtils.hasText(template.getSystemPrompt())) {
            String systemPrompt = replaceVariables(template.getSystemPrompt(), variables);
            promptBuilder.append(systemPrompt).append("\n\n");
        }

        // 3.2 添加对话开始模板
        if (StringUtils.hasText(template.getConversationStarter()) && processedMessages.isEmpty()) {
            String starter = replaceVariables(template.getConversationStarter(), variables);
            promptBuilder.append(starter).append("\n\n");
        }

        // 3.3 添加历史对话
        if (!processedMessages.isEmpty()) {
            String historyPrompt = formatContextMessages(processedMessages, template);
            promptBuilder.append(historyPrompt).append("\n");
        }

        // 3.4 添加当前用户消息
        if (StringUtils.hasText(currentMessage)) {
            String userPrefix = StringUtils.hasText(template.getUserPrefix()) ? template.getUserPrefix() : "用户：";
            promptBuilder.append(userPrefix).append(" ").append(currentMessage).append("\n");
        }

        // 3.5 添加助手前缀
        if (StringUtils.hasText(template.getAssistantPrefix())) {
            promptBuilder.append(template.getAssistantPrefix()).append(" ");
        }

        String finalPrompt = promptBuilder.toString().trim();
        log.debug("构建完成，Prompt长度: {}", finalPrompt.length());

        return finalPrompt;
    }

    /**
     * 构建基础Prompt（当没有模板时使用）
     * 
     * @param contextMessages 上下文消息
     * @param currentMessage  当前消息
     * @return 基础Prompt
     */
    public String buildBasicPrompt(List<Message> contextMessages, String currentMessage) {
        StringBuilder promptBuilder = new StringBuilder();

        // 添加历史对话
        for (Message message : contextMessages) {
            String role = message.getMessageType() == MessageType.USER ? "用户" : "助手";
            promptBuilder.append(role).append("：").append(message.getContent()).append("\n");
        }

        // 添加当前消息
        if (StringUtils.hasText(currentMessage)) {
            promptBuilder.append("用户：").append(currentMessage).append("\n");
            promptBuilder.append("助手：");
        }

        return promptBuilder.toString();
    }

    /**
     * 智能检测Prompt类型
     * 根据用户消息内容自动判断最适合的Prompt类型
     * 
     * @param userMessage 用户消息
     * @return 推荐的Prompt类型
     */
    public PromptType detectPromptType(String userMessage) {
        if (!StringUtils.hasText(userMessage)) {
            return PromptType.CHAT;
        }

        String message = userMessage.toLowerCase();

        // 代码相关关键词
        if (message.contains("代码") || message.contains("编程") || message.contains("函数") ||
                message.contains("算法") || message.contains("bug") || message.contains("调试") ||
                message.contains("code") || message.contains("function") || message.contains("class")) {
            return PromptType.CODE_GENERATION;
        }

        // 翻译相关关键词
        if (message.contains("翻译") || message.contains("translate") ||
                message.contains("英文") || message.contains("中文") || message.contains("日文")) {
            return PromptType.TRANSLATION;
        }

        // 总结相关关键词
        if (message.contains("总结") || message.contains("摘要") || message.contains("概括") ||
                message.contains("summarize") || message.contains("summary")) {
            return PromptType.SUMMARIZATION;
        }

        // 分析相关关键词
        if (message.contains("分析") || message.contains("解释") || message.contains("为什么") ||
                message.contains("原因") || message.contains("analyze") || message.contains("explain")) {
            return PromptType.ANALYSIS;
        }

        // 创意写作关键词
        if (message.contains("写作") || message.contains("故事") || message.contains("诗歌") ||
                message.contains("创作") || message.contains("小说") || message.contains("write")) {
            return PromptType.CREATIVE_WRITING;
        }

        // 角色扮演关键词
        if (message.contains("扮演") || message.contains("角色") || message.contains("假设你是") ||
                message.contains("你是一个") || message.contains("role play") || message.contains("pretend")) {
            return PromptType.ROLE_PLAY;
        }

        // 问答关键词
        if (message.contains("什么是") || message.contains("如何") || message.contains("怎么") ||
                message.contains("what is") || message.contains("how to") || message.contains("?")) {
            return PromptType.QA;
        }

        // 默认为聊天
        return PromptType.CHAT;
    }

    /**
     * 限制上下文长度
     * 根据模板配置智能截断历史消息
     * 
     * @param messages  原始消息列表
     * @param maxLength 最大长度
     * @return 截断后的消息列表
     */
    private List<Message> limitContextLength(List<Message> messages, Integer maxLength) {
        if (maxLength == null || maxLength <= 0 || messages.isEmpty()) {
            return messages;
        }

        // 计算总长度
        int totalLength = messages.stream()
                .mapToInt(msg -> msg.getContent().length())
                .sum();

        if (totalLength <= maxLength) {
            return messages;
        }

        // 从最新消息开始保留
        List<Message> result = messages.stream()
                .sorted((m1, m2) -> m2.getSequenceNumber().compareTo(m1.getSequenceNumber()))
                .collect(Collectors.toList());

        int currentLength = 0;
        int keepCount = 0;

        for (Message message : result) {
            int messageLength = message.getContent().length();
            if (currentLength + messageLength <= maxLength) {
                currentLength += messageLength;
                keepCount++;
            } else {
                break;
            }
        }

        // 恢复原始顺序
        return result.subList(0, keepCount).stream()
                .sorted((m1, m2) -> m1.getSequenceNumber().compareTo(m2.getSequenceNumber()))
                .collect(Collectors.toList());
    }

    /**
     * 格式化上下文消息
     * 
     * @param messages 消息列表
     * @param template 模板
     * @return 格式化后的字符串
     */
    private String formatContextMessages(List<Message> messages, PromptTemplate template) {
        return messages.stream()
                .map(message -> formatSingleMessage(message, template))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 格式化单个消息
     * 
     * @param message  消息
     * @param template 模板
     * @return 格式化后的字符串
     */
    private String formatSingleMessage(Message message, PromptTemplate template) {
        String prefix;

        if (message.getMessageType() == MessageType.USER) {
            prefix = StringUtils.hasText(template.getUserPrefix()) ? template.getUserPrefix() : "用户：";
        } else {
            prefix = StringUtils.hasText(template.getAssistantPrefix()) ? template.getAssistantPrefix() : "助手：";
        }

        return prefix + " " + message.getContent();
    }

    /**
     * 替换模板中的变量
     * 
     * @param template  模板字符串
     * @param variables 变量映射
     * @return 替换后的字符串
     */
    private String replaceVariables(String template, Map<String, String> variables) {
        if (!StringUtils.hasText(template)) {
            return template;
        }

        String result = template;

        // 内置变量
        result = result.replace("{{current_time}}", getCurrentTime());
        result = result.replace("{{current_date}}", getCurrentDate());

        // 自定义变量
        if (variables != null && !variables.isEmpty()) {
            Matcher matcher = VARIABLE_PATTERN.matcher(result);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String variableName = matcher.group(1);
                String replacement = variables.getOrDefault(variableName, matcher.group(0));
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }

        return result;
    }

    /**
     * 获取当前时间
     * 
     * @return 格式化的时间字符串
     */
    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * 获取当前日期
     * 
     * @return 格式化的日期字符串
     */
    private String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}