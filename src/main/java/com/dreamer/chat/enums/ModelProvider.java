package com.dreamer.chat.enums;

/**
 * AI模型提供商枚举
 * 
 * @author panshenguo
 * @since 1.0.0
 */
public enum ModelProvider {

    /**
     * 阿里巴巴千问
     */
    QIANWEN("qianwen", "阿里巴巴千问", "qwen-plus"),

    /**
     * 讯飞星火
     */
    XINGHUO("xinghuo", "讯飞星火", "spark"),

    /**
     * 豆包
     */
    DOUBAO("doubao", "豆包", "ark"),

    /**
     * DeepSeek
     */
    DEEPSEEK("deepseek", "DeepSeek", "deepseek-chat");

    private final String code;
    private final String name;
    private final String model;

    ModelProvider(String code, String name, String model) {
        this.code = code;
        this.name = name;
        this.model = model;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    /**
     * 根据代码获取模型提供商
     */
    public static ModelProvider fromCode(String code) {
        for (ModelProvider provider : values()) {
            if (provider.code.equals(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown model provider code: " + code);
    }
}