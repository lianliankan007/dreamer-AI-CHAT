package com.dreamer.chat.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JSON处理工具类
 * 提供安全的JSON序列化和反序列化功能
 * 
 * @author panshenguo
 * @since 1.0.0
 */
public final class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private JsonUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 创建并配置ObjectMapper
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册Java时间模块
        mapper.registerModule(new JavaTimeModule());

        // 禁用将日期写为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 设置属性命名策略为驼峰命名
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);

        return mapper;
    }

    /**
     * 对象转JSON字符串
     * 
     * @param object 要转换的对象
     * @return JSON字符串
     */
    public static String toJson(Object object) {
        if (object == null) {
            return "null";
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("对象转JSON失败: {}", object.getClass().getName(), e);
            return "{}";
        }
    }

    /**
     * JSON字符串转对象
     * 
     * @param json  JSON字符串
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON转对象失败: {}", clazz.getName(), e);
            return null;
        }
    }

    /**
     * 创建SSE事件数据的JSON字符串
     * 
     * @param keyValues 键值对参数
     * @return 格式化的JSON字符串
     */
    public static String createSseEventData(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("参数必须是成对的键值对");
        }

        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = String.valueOf(keyValues[i]);
            Object value = keyValues[i + 1];
            data.put(key, value);
        }

        return toJson(data);
    }

    /**
     * 转义JSON特殊字符
     * 用于手动构建JSON字符串时的安全处理
     * 
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    public static String escapeJsonString(String input) {
        if (input == null) {
            return "";
        }

        return input
                .replace("\\", "\\\\") // 反斜杠
                .replace("\"", "\\\"") // 双引号
                .replace("\n", "\\n") // 换行符
                .replace("\r", "\\r") // 回车符
                .replace("\t", "\\t") // 制表符
                .replace("\b", "\\b") // 退格符
                .replace("\f", "\\f"); // 换页符
    }

    /**
     * 创建错误响应的JSON数据
     * 
     * @param errorMessage 错误消息
     * @param timestamp    时间戳
     * @return 错误响应JSON
     */
    public static String createErrorResponse(String errorMessage, long timestamp) {
        return createSseEventData(
                "error", errorMessage,
                "timestamp", timestamp,
                "status", "error");
    }

    /**
     * 创建成功响应的JSON数据
     * 
     * @param message 成功消息
     * @param data    附加数据
     * @return 成功响应JSON
     */
    public static String createSuccessResponse(String message, Object data) {
        return createSseEventData(
                "message", message,
                "data", data,
                "status", "success");
    }

    /**
     * 验证JSON字符串格式
     * 
     * @param json JSON字符串
     * @return 是否为有效的JSON格式
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }

        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}