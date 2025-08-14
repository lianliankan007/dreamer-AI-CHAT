package com.dreamer.chat.config;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.util.StringUtils;

import com.dreamer.chat.enums.ModelProvider;

/**
 * AI模型配置类
 * 
 * <p>
 * 支持多个模型提供商的配置和管理，包括千问、星火、豆包、DeepSeek等。
 * 为每个模型提供标准化的配置方式，确保model参数正确设置。
 * </p>
 * 
 * <p>
 * 主要功能：
 * </p>
 * <ul>
 * <li>统一管理多个AI模型提供商的配置</li>
 * <li>为每个模型创建对应的ChatClient实例</li>
 * <li>提供ChatClientManager进行统一管理</li>
 * <li>支持动态检查模型可用性</li>
 * </ul>
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Configuration
public class AiModelConfig {

    private static final Logger logger = LoggerFactory.getLogger(AiModelConfig.class);

    // ========== 千问模型配置 ==========
    @Value("${spring.ai.qianwen.api-key:}")
    private String qianwenApiKey;

    @Value("${spring.ai.qianwen.base-url:https://dashscope.aliyuncs.com/api/v1}")
    private String qianwenBaseUrl;

    // ========== 星火模型配置 ==========
    @Value("${spring.ai.xinghuo.api-key:}")
    private String xinghuoApiKey;

    @Value("${spring.ai.xinghuo.base-url:https://spark-api.xf-yun.com}")
    private String xinghuoBaseUrl;

    // ========== 豆包模型配置 ==========
    @Value("${spring.ai.doubao.api-key:}")
    private String doubaoApiKey;

    @Value("${spring.ai.doubao.base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String doubaoBaseUrl;

    // ========== DeepSeek模型配置 ==========
    @Value("${spring.ai.deepseek.api-key:#{systemEnvironment['DEEPSEEK_API_KEY'] ?: 'dummy-key'}}")
    private String deepseekApiKey;

    @Value("${spring.ai.deepseek.base-url:https://api.deepseek.com}")
    private String deepseekBaseUrl;

    /**
     * 千问模型客户端配置
     * 使用qwq-plus模型，适用于高质量对话场景
     * 
     * @return ChatClient实例，如果API密钥未配置则返回null
     */
    @Bean("qianwenChatClient")
    public ChatClient qianwenChatClient() {
        if (!StringUtils.hasText(qianwenApiKey)) {
            logger.warn("千问API密钥未配置，跳过千问模型初始化");
            return null;
        }

        try {
            return createChatClient(
                    ModelProvider.QIANWEN,
                    qianwenBaseUrl,
                    qianwenApiKey);
        } catch (Exception e) {
            logger.error("创建千问ChatClient失败", e);
            return null;
        }
    }

    /**
     * 星火模型客户端配置
     * 使用讯飞星火认知大模型，支持多轮对话
     * 
     * @return ChatClient实例，如果API密钥未配置则返回null
     */
    @Bean("xinghuoChatClient")
    public ChatClient xinghuoChatClient() {
        if (!StringUtils.hasText(xinghuoApiKey)) {
            logger.warn("星火API密钥未配置，跳过星火模型初始化");
            return null;
        }

        try {
            return createChatClient(
                    ModelProvider.XINGHUO,
                    xinghuoBaseUrl,
                    xinghuoApiKey);
        } catch (Exception e) {
            logger.error("创建星火ChatClient失败", e);
            return null;
        }
    }

    /**
     * 豆包模型客户端配置
     * 使用字节跳动豆包大模型，支持高效对话
     * 
     * @return ChatClient实例，如果API密钥未配置则返回null
     */
    @Bean("doubaoChatClient")
    public ChatClient doubaoChatClient() {
        if (!StringUtils.hasText(doubaoApiKey)) {
            logger.warn("豆包API密钥未配置，跳过豆包模型初始化");
            return null;
        }

        try {
            return createChatClient(
                    ModelProvider.DOUBAO,
                    doubaoBaseUrl,
                    doubaoApiKey);
        } catch (Exception e) {
            logger.error("创建豆包ChatClient失败", e);
            return null;
        }
    }

    /**
     * DeepSeek模型客户端配置
     * 使用DeepSeek深度推理模型，擅长复杂推理任务
     * 
     * @return ChatClient实例，设为Primary Bean
     */
    @Bean("deepseekChatClient")
    @Primary
    public ChatClient deepseekChatClient() {
        if (!StringUtils.hasText(deepseekApiKey) || "dummy-key".equals(deepseekApiKey)) {
            logger.warn("DeepSeek API密钥未配置，跳过DeepSeek模型初始化");
            return null;
        }

        try {
            return createChatClient(
                    ModelProvider.DEEPSEEK,
                    deepseekBaseUrl,
                    deepseekApiKey);
        } catch (Exception e) {
            logger.error("创建DeepSeek ChatClient失败", e);
            return null;
        }
    }

    /**
     * 创建ChatClient的通用方法
     * 
     * <p>
     * 该方法封装了ChatClient的创建逻辑，为每个模型提供统一的创建方式。
     * 自动从ModelProvider枚举中获取对应的model参数，确保配置正确。
     * </p>
     * 
     * @param provider 模型提供商枚举
     * @param baseUrl  API基础URL
     * @param apiKey   API密钥
     * @return 配置好的ChatClient实例
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    private ChatClient createChatClient(ModelProvider provider, String baseUrl, String apiKey) {
        Objects.requireNonNull(provider, "模型提供商不能为空");
        Objects.requireNonNull(baseUrl, "API基础URL不能为空");
        Objects.requireNonNull(apiKey, "API密钥不能为空");

        logger.info("正在创建{}模型ChatClient，使用模型: {}", provider.getName(), provider.getModel());

        // 创建OpenAI API实例
        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);

        // 创建默认的ChatOptions
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(provider.getModel()).build();

        // 创建ChatModel，在构造时指定默认选项
        OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi, options);

        // 构建并返回ChatClient
        return ChatClient.builder(chatModel).build();
    }

    /**
     * 聊天客户端管理器Bean配置
     * 
     * <p>
     * 负责管理所有可用的ChatClient实例，提供统一的访问接口。
     * 只注册成功创建的客户端，确保运行时稳定性。
     * </p>
     * 
     * @return ChatClientManager实例
     */
    @Bean
    public ChatClientManager chatClientManager() {
        Map<ModelProvider, ChatClient> clientMap = new HashMap<>();

        // 注册所有可用的模型客户端
        registerClientIfAvailable(clientMap, ModelProvider.QIANWEN, qianwenChatClient());
        registerClientIfAvailable(clientMap, ModelProvider.XINGHUO, xinghuoChatClient());
        registerClientIfAvailable(clientMap, ModelProvider.DOUBAO, doubaoChatClient());
        registerClientIfAvailable(clientMap, ModelProvider.DEEPSEEK, deepseekChatClient());

        logger.info("ChatClientManager初始化完成，可用模型数量: {}", clientMap.size());
        clientMap.keySet().forEach(provider -> logger.info("可用模型: {} ({})", provider.getName(), provider.getModel()));

        return new ChatClientManager(clientMap);
    }

    /**
     * 注册客户端到管理器（如果客户端可用）
     * 
     * @param clientMap 客户端映射表
     * @param provider  模型提供商
     * @param client    客户端实例
     */
    private void registerClientIfAvailable(Map<ModelProvider, ChatClient> clientMap,
            ModelProvider provider, ChatClient client) {
        if (client != null) {
            clientMap.put(provider, client);
            logger.debug("成功注册{}模型客户端", provider.getName());
        } else {
            logger.debug("{}模型客户端未配置，跳过注册", provider.getName());
        }
    }

    /**
     * 聊天客户端管理器
     * 
     * <p>
     * 负责管理和提供不同模型提供商的客户端实例。
     * 提供统一的接口来获取、检查和管理各种AI模型客户端。
     * </p>
     * 
     * <p>
     * 主要功能：
     * </p>
     * <ul>
     * <li>根据模型提供商获取对应的ChatClient</li>
     * <li>检查特定模型提供商是否可用</li>
     * <li>获取所有可用的模型客户端列表</li>
     * <li>提供统一的异常处理</li>
     * </ul>
     * 
     * @author panshenguo
     * @since 1.0.0
     */
    public static class ChatClientManager {

        private static final Logger logger = LoggerFactory.getLogger(ChatClientManager.class);

        /**
         * 模型提供商到客户端的映射表
         */
        private final Map<ModelProvider, ChatClient> clientMap;

        /**
         * 构造函数
         * 
         * @param clientMap 客户端映射表，不能为null
         */
        public ChatClientManager(Map<ModelProvider, ChatClient> clientMap) {
            this.clientMap = Objects.requireNonNull(clientMap, "客户端映射表不能为空");
        }

        /**
         * 根据模型提供商获取对应的聊天客户端
         * 
         * @param provider 模型提供商，不能为null
         * @return ChatClient实例
         * @throws IllegalArgumentException 当模型提供商不支持或未配置时抛出
         */
        public ChatClient getChatClient(ModelProvider provider) {
            Objects.requireNonNull(provider, "模型提供商不能为空");

            ChatClient client = clientMap.get(provider);
            if (client == null) {
                String errorMsg = String.format("模型提供商 %s (%s) 不可用，请检查配置",
                        provider.getName(), provider.getCode());
                logger.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            logger.debug("获取{}模型客户端成功", provider.getName());
            return client;
        }

        /**
         * 检查模型提供商是否可用
         * 
         * @param provider 模型提供商
         * @return true如果可用，false否则
         */
        public boolean isProviderAvailable(ModelProvider provider) {
            boolean available = provider != null && clientMap.containsKey(provider);
            logger.debug("检查{}模型可用性: {}",
                    provider != null ? provider.getName() : "null", available);
            return available;
        }

        /**
         * 获取所有可用的模型提供商及其客户端
         * 
         * @return 包含所有可用客户端的映射表副本
         */
        public Map<ModelProvider, ChatClient> getAllAvailableClients() {
            Map<ModelProvider, ChatClient> result = new HashMap<>(clientMap);
            logger.debug("返回所有可用客户端，数量: {}", result.size());
            return result;
        }

        /**
         * 获取可用模型提供商数量
         * 
         * @return 可用模型数量
         */
        public int getAvailableProviderCount() {
            return clientMap.size();
        }

        /**
         * 检查是否有任何可用的模型提供商
         * 
         * @return true如果至少有一个可用的模型，false否则
         */
        public boolean hasAnyAvailableProvider() {
            return !clientMap.isEmpty();
        }
    }
}