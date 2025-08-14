package com.dreamer.chat.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * SSE 测试工具类
 * 提供Server-Sent Events相关的测试辅助方法
 * 
 * @author panshenguo
 * @since 1.0.0
 */
public class SseTestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * SSE事件数据类
     */
    public static class SseEvent {
        private String name;
        private String data;
        private String id;

        public SseEvent(String name, String data) {
            this.name = name;
            this.data = data;
        }

        public SseEvent(String name, String data, String id) {
            this.name = name;
            this.data = data;
            this.id = id;
        }

        // Getters
        public String getName() {
            return name;
        }

        public String getData() {
            return data;
        }

        public String getId() {
            return id;
        }
    }

    /**
     * SSE事件收集器
     */
    public static class SseEventCollector {
        private final List<SseEvent> events = new ArrayList<>();
        private final CountDownLatch completionLatch = new CountDownLatch(1);
        private boolean completed = false;
        private Exception error;

        public void addEvent(String name, String data) {
            events.add(new SseEvent(name, data));
        }

        public void addEvent(String name, String data, String id) {
            events.add(new SseEvent(name, data, id));
        }

        public void markCompleted() {
            completed = true;
            completionLatch.countDown();
        }

        public void markError(Exception e) {
            error = e;
            completionLatch.countDown();
        }

        public boolean waitForCompletion(long timeout, TimeUnit unit) throws InterruptedException {
            return completionLatch.await(timeout, unit);
        }

        public List<SseEvent> getEvents() {
            return new ArrayList<>(events);
        }

        public boolean isCompleted() {
            return completed;
        }

        public Exception getError() {
            return error;
        }
    }

    /**
     * 解析SSE响应内容
     */
    public static List<SseEvent> parseSseResponse(String responseContent) {
        List<SseEvent> events = new ArrayList<>();
        String[] lines = responseContent.split("\n");

        String currentEvent = null;
        StringBuilder currentData = new StringBuilder();
        String currentId = null;

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty()) {
                // 空行表示事件结束
                if (currentEvent != null) {
                    events.add(new SseEvent(currentEvent, currentData.toString(), currentId));
                    currentEvent = null;
                    currentData.setLength(0);
                    currentId = null;
                }
            } else if (line.startsWith("event:")) {
                currentEvent = line.substring(6).trim();
            } else if (line.startsWith("data:")) {
                if (currentData.length() > 0) {
                    currentData.append("\n");
                }
                currentData.append(line.substring(5).trim());
            } else if (line.startsWith("id:")) {
                currentId = line.substring(3).trim();
            }
        }

        // 处理最后一个事件（如果没有以空行结尾）
        if (currentEvent != null) {
            events.add(new SseEvent(currentEvent, currentData.toString(), currentId));
        }

        return events;
    }

    /**
     * 验证SSE事件序列是否符合预期
     */
    public static boolean validateEventSequence(List<SseEvent> events, String... expectedEventNames) {
        if (events.size() < expectedEventNames.length) {
            return false;
        }

        for (int i = 0; i < expectedEventNames.length; i++) {
            if (!expectedEventNames[i].equals(events.get(i).getName())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 查找指定名称的事件
     */
    public static List<SseEvent> findEventsByName(List<SseEvent> events, String eventName) {
        return events.stream()
                .filter(event -> eventName.equals(event.getName()))
                .toList();
    }

    /**
     * 获取事件数据的JSON节点
     */
    public static JsonNode getEventDataAsJson(SseEvent event) throws IOException {
        return objectMapper.readTree(event.getData());
    }

    /**
     * 验证事件数据是否包含指定字段
     */
    public static boolean eventDataContains(SseEvent event, String fieldName) {
        try {
            JsonNode jsonNode = getEventDataAsJson(event);
            return jsonNode.has(fieldName);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 获取事件数据中的字段值
     */
    public static String getEventDataField(SseEvent event, String fieldName) {
        try {
            JsonNode jsonNode = getEventDataAsJson(event);
            JsonNode fieldNode = jsonNode.get(fieldName);
            return fieldNode != null ? fieldNode.asText() : null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 创建测试用的SseEmitter响应收集器
     */
    public static SseEventCollector createEventCollector() {
        return new SseEventCollector();
    }

    /**
     * 验证响应是否为SSE格式
     */
    public static boolean isSseResponse(MvcResult result) {
        MockHttpServletResponse response = result.getResponse();
        String contentType = response.getContentType();
        return MediaType.TEXT_EVENT_STREAM_VALUE.equals(contentType);
    }

    /**
     * 从MvcResult中提取SSE事件
     */
    public static List<SseEvent> extractSseEvents(MvcResult result) throws Exception {
        MockHttpServletResponse response = result.getResponse();
        String content = response.getContentAsString(StandardCharsets.UTF_8);
        return parseSseResponse(content);
    }

    /**
     * 验证千问模型的流式响应格式
     */
    public static boolean validateQianwenStreamResponse(List<SseEvent> events) {
        // 验证基本的事件序列：start -> conversation -> user_message -> ai_start -> ai_chunk(s) ->
        // complete
        String[] expectedSequence = { "start", "conversation", "user_message", "ai_start" };

        if (!validateEventSequence(events, expectedSequence)) {
            return false;
        }

        // 验证必须包含AI块事件和完成事件
        boolean hasAiChunk = events.stream().anyMatch(e -> "ai_chunk".equals(e.getName()));
        boolean hasComplete = events.stream().anyMatch(e -> "complete".equals(e.getName()));

        return hasAiChunk && hasComplete;
    }

    /**
     * 获取流式响应的完整内容（合并所有ai_chunk）
     */
    public static String getCompleteStreamContent(List<SseEvent> events) {
        StringBuilder content = new StringBuilder();

        events.stream()
                .filter(event -> "ai_chunk".equals(event.getName()))
                .forEach(event -> {
                    String chunk = getEventDataField(event, "chunk");
                    if (chunk != null) {
                        content.append(chunk);
                    }
                });

        return content.toString();
    }
}