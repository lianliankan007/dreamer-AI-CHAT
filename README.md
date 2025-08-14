# 梦想家AI聊天系统

## 项目简介

梦想家AI聊天系统是一个基于Spring Boot + Spring AI的多模型聊天应用，支持接入多个AI大模型提供商，为用户提供完整的多轮对话体验。

## 主要功能

✅ **多模型支持** - 支持千问、星火、豆包、DeepSeek等多个AI模型  
✅ **多轮对话** - 完整的上下文管理和对话历史记录  
✅ **数据持久化** - 基于PostgreSQL的完整数据存储方案  
✅ **对话管理** - 对话创建、查询、删除、归档等完整功能  
✅ **用户隔离** - 支持多用户独立的聊天记录管理  
✅ **REST API** - 完整的RESTful接口设计

## 技术架构

- **框架**: Spring Boot 3.2.2
- **AI集成**: Spring AI 1.0.0-M3
- **数据库**: PostgreSQL + Flyway
- **持久化**: Spring Data JPA + Hibernate
- **构建工具**: Maven
- **Java版本**: 17

## 快速开始

### 1. 环境准备

- Java 17+
- PostgreSQL 12+
- Maven 3.6+

### 2. 数据库配置

创建PostgreSQL数据库：
```sql
CREATE DATABASE dreamer_ai_chat;
```

### 3. 配置文件

在 `application.yml` 中配置数据库和AI模型参数：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dreamer_ai_chat
    username: your_db_username
    password: your_db_password
  
  ai:
    qianwen:
      api-key: your-qianwen-api-key
    xinghuo:
      api-key: your-xinghuo-api-key
    doubao:
      api-key: your-doubao-api-key
```

### 4. 启动应用

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

## API接口

### 聊天接口

#### 发送消息（流式输出）
```http
POST /api/chat/send
Content-Type: application/json
Accept: text/event-stream

{
  "message": "你好",
  "modelProvider": "qianwen", 
  "title": "测试对话",
  "userId": "user001"
}
```

**流式响应事件类型:**
- `start`: 开始处理请求
- `conversation`: 对话信息
- `user_message`: 用户消息确认
- `ai_start`: AI开始生成回复
- `ai_chunk`: AI回复片段
- `complete`: 处理完成
- `error`: 错误信息

#### 发送消息（同步版本）
```http
POST /api/chat/send-sync
Content-Type: application/json

{
  "message": "你好",
  "modelProvider": "qianwen",
  "title": "测试对话", 
  "userId": "user001"
}
```

#### 获取对话历史
```http
GET /api/chat/history/{conversationId}
```

#### 测试接口（同步）
```http
POST /api/chat/test?message=你好&modelProvider=qianwen
```

#### 测试接口（流式）
```http
GET /api/chat/test-stream?message=你好&modelProvider=qianwen
Accept: text/event-stream
```

### 对话管理接口

#### 获取用户对话列表
```http
GET /api/conversations/user/{userId}
```

#### 分页获取活跃对话
```http
GET /api/conversations/user/{userId}/active?page=0&size=20
```

#### 更新对话标题
```http
PUT /api/conversations/{conversationId}/title?userId={userId}&newTitle=新标题
```

#### 删除对话
```http
DELETE /api/conversations/{conversationId}?userId={userId}
```

## 支持的AI模型

| 模型提供商 | 代码标识 | 状态 | 说明 |
|-----------|---------|------|------|
| 阿里巴巴千问 | `qianwen` | ✅ | 支持 |
| 讯飞星火 | `xinghuo` | ✅ | 支持 |
| 豆包 | `doubao` | ✅ | 支持 |
| DeepSeek | `deepseek` | ✅ | 支持 |

## 数据库表结构

### conversations（对话表）
- `id` - 对话ID
- `title` - 对话标题
- `model_provider` - 模型提供商
- `model_name` - 具体模型名称
- `user_id` - 用户ID
- `status` - 对话状态（ACTIVE/ARCHIVED/DELETED）
- `created_time` - 创建时间
- `updated_time` - 更新时间

### messages（消息表）
- `id` - 消息ID
- `conversation_id` - 所属对话ID
- `content` - 消息内容
- `message_type` - 消息类型（USER/ASSISTANT/SYSTEM）
- `sequence_number` - 消息序号
- `timestamp` - 消息时间戳

## 配置说明

### 聊天配置
```yaml
app:
  chat:
    max-history-size: 50      # 最大历史消息数量
    default-max-tokens: 2000  # 默认最大Token数
    timeout-seconds: 30       # 超时时间（秒）
```

### 模型配置
每个模型提供商都需要配置相应的API密钥和基础URL。

## 开发规范

- 遵循阿里巴巴Java开发规范
- 所有新增类都包含作者信息（panshenguo）
- 完整的JavaDoc注释
- 统一的异常处理机制
- 完善的日志记录

## 扩展开发

### 添加新的AI模型提供商

1. 在 `ModelProvider` 枚举中添加新模型
2. 在 `AiModelConfig` 中添加对应的客户端配置
3. 更新配置文件添加相关参数

### 自定义业务逻辑

系统采用分层架构设计，可以轻松扩展：
- **Controller层** - 处理HTTP请求
- **Service层** - 业务逻辑处理
- **Repository层** - 数据访问
- **Entity层** - 数据模型

## 前端集成示例

### JavaScript流式聊天集成

```javascript
// 发送流式聊天请求
function sendStreamMessage(message, modelProvider = 'qianwen') {
    const eventSource = new EventSource(`/api/chat/test-stream?message=${encodeURIComponent(message)}&modelProvider=${modelProvider}`);
    
    let assistantMessage = '';
    
    eventSource.addEventListener('start', function(event) {
        const data = JSON.parse(event.data);
        console.log('开始处理:', data);
    });
    
    eventSource.addEventListener('conversation', function(event) {
        const data = JSON.parse(event.data);
        console.log('对话信息:', data);
    });
    
    eventSource.addEventListener('user_message', function(event) {
        const data = JSON.parse(event.data);
        console.log('用户消息:', data);
    });
    
    eventSource.addEventListener('ai_start', function(event) {
        const data = JSON.parse(event.data);
        console.log('AI开始生成:', data);
        assistantMessage = ''; // 重置消息内容
    });
    
    eventSource.addEventListener('ai_chunk', function(event) {
        const data = JSON.parse(event.data);
        assistantMessage += data.chunk;
        // 实时更新UI显示
        document.getElementById('ai-response').textContent = assistantMessage;
    });
    
    eventSource.addEventListener('complete', function(event) {
        const data = JSON.parse(event.data);
        console.log('处理完成:', data);
        eventSource.close();
    });
    
    eventSource.addEventListener('error', function(event) {
        const data = JSON.parse(event.data);
        console.error('错误:', data);
        eventSource.close();
    });
    
    eventSource.onerror = function(event) {
        console.error('连接错误:', event);
        eventSource.close();
    };
}

// 使用示例
sendStreamMessage('你好，请介绍一下你自己');
```

### POST方式流式聊天

```javascript
// 使用fetch发送POST请求接收流式响应
async function sendStreamChatPost(chatRequest) {
    try {
        const response = await fetch('/api/chat/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'text/event-stream'
            },
            body: JSON.stringify(chatRequest)
        });
        
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        
        while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            
            const chunk = decoder.decode(value);
            const lines = chunk.split('\n');
            
            for (const line of lines) {
                if (line.startsWith('data: ')) {
                    const data = line.slice(6);
                    if (data.trim()) {
                        try {
                            const eventData = JSON.parse(data);
                            handleStreamEvent(eventData);
                        } catch (e) {
                            console.error('解析事件数据失败:', e);
                        }
                    }
                }
            }
        }
    } catch (error) {
        console.error('流式请求失败:', error);
    }
}

function handleStreamEvent(data) {
    // 根据事件类型处理数据
    console.log('收到流式数据:', data);
}
```

## 许可证

本项目采用 MIT 许可证。

## 联系方式

如有问题或建议，请联系项目维护者。 # dreamer-AI-CHAT
