package com.dreamer.chat.enums;

/**
 * Prompt模板类型枚举
 * 定义不同的对话场景和用途
 * 
 * @author panshenguo
 * @since 1.0.0
 */
public enum PromptType {

    /**
     * 常规聊天对话
     */
    CHAT("chat", "常规聊天"),

    /**
     * 问答场景
     */
    QA("qa", "问答"),

    /**
     * 代码生成
     */
    CODE_GENERATION("code_generation", "代码生成"),

    /**
     * 创意写作
     */
    CREATIVE_WRITING("creative_writing", "创意写作"),

    /**
     * 翻译
     */
    TRANSLATION("translation", "翻译"),

    /**
     * 总结摘要
     */
    SUMMARIZATION("summarization", "总结摘要"),

    /**
     * 分析解释
     */
    ANALYSIS("analysis", "分析解释"),

    /**
     * 角色扮演
     */
    ROLE_PLAY("role_play", "角色扮演");

    private final String code;
    private final String name;

    PromptType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据代码获取Prompt类型
     */
    public static PromptType fromCode(String code) {
        for (PromptType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown prompt type code: " + code);
    }
}