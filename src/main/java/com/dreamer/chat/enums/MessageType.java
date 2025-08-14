package com.dreamer.chat.enums;

/**
 * 消息类型枚举
 * 
 * @author panshenguo
 * @since 1.0.0
 */
public enum MessageType {
    
    /**
     * 用户消息
     */
    USER("user", "用户"),
    
    /**
     * AI助手消息
     */
    ASSISTANT("assistant", "助手"),
    
    /**
     * 系统消息
     */
    SYSTEM("system", "系统");
    
    private final String code;
    private final String name;
    
    MessageType(String code, String name) {
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
     * 根据代码获取消息类型
     */
    public static MessageType fromCode(String code) {
        for (MessageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type code: " + code);
    }
}