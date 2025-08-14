# Dreamer AI Chat - 前端页面说明

## 🌟 功能特性

### 🎯 核心功能
- **智能对话**: 支持多种AI模型（千问、星火、豆包、DeepSeek）
- **流式显示**: 实时显示AI回复，打字机效果
- **模型切换**: 支持实时切换不同AI模型
- **对话管理**: 支持清空对话历史
- **响应式设计**: 适配桌面和移动端

### 🎨 界面特色
- **现代化设计**: 渐变背景，卡片式布局
- **流畅动画**: 消息滑入动效，按钮悬停效果
- **代码高亮**: 支持多种编程语言代码高亮
- **Markdown支持**: 支持Markdown格式解析和显示
- **用户体验**: 智能输入框高度调整，字符计数提示

## 🚀 快速开始

### 启动应用
```bash
# 使用开发环境配置启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或使用生产环境配置启动
mvn spring-boot:run
```

### 访问地址
- **前端页面**: http://localhost:8080/
- **API文档**: http://localhost:8080/chat/health

## 📱 使用指南

### 基本操作
1. **发送消息**: 在输入框中输入文本，按Enter或点击发送按钮
2. **换行输入**: 按Shift+Enter可以在输入框中换行
3. **切换模型**: 在顶部选择器中选择不同的AI模型
4. **清空对话**: 点击垃圾桶图标清空当前对话

### 快捷键
- `Enter`: 发送消息
- `Shift + Enter`: 换行
- `Esc`: 清空输入框
- `Ctrl/Cmd + K`: 清空对话
- `Ctrl/Cmd + /`: 聚焦输入框

### 支持格式
- **纯文本**: 普通文本消息
- **Markdown**: 支持粗体、斜体、代码块等
- **代码块**: 自动语法高亮显示

## 🔧 技术架构

### 前端技术栈
- **HTML5**: 语义化标签结构
- **CSS3**: 现代化样式，响应式设计
- **Vanilla JavaScript**: 原生JS实现，无框架依赖
- **EventSource/Fetch**: 处理流式数据传输

### 核心模块
```
static/
├── index.html          # 主页面
├── css/
│   └── style.css      # 样式文件
├── js/
│   ├── utils.js       # 工具函数模块
│   ├── chat.js        # 聊天功能模块
│   └── app.js         # 主应用逻辑
└── favicon.ico        # 网站图标
```

### 模块说明

#### Utils模块 (utils.js)
- **TimeUtils**: 时间格式化工具
- **TextUtils**: 文本处理和Markdown解析
- **DomUtils**: DOM操作工具
- **HttpUtils**: HTTP请求封装
- **EventBus**: 事件总线

#### Chat模块 (chat.js)
- **ChatManager**: 聊天管理器
- **流式处理**: SSE数据流解析
- **消息管理**: 消息创建、显示、更新
- **模型管理**: AI模型切换和配置

#### App模块 (app.js)
- **应用初始化**: 界面初始化和事件绑定
- **用户交互**: 输入处理、按钮响应
- **状态管理**: 界面状态更新
- **错误处理**: 友好的错误提示

## 🎛️ 配置说明

### API配置
```javascript
const API_CONFIG = {
    BASE_URL: '',
    ENDPOINTS: {
        CHAT_SEND: '/chat/send',
        CHAT_MODELS: '/chat/models',
        CHAT_HISTORY: '/chat/history',
        CHAT_CLEAR: '/chat/history'
    },
    TIMEOUT: 120000
};
```

### 支持的AI模型
- **千问 (qianwen)**: 阿里巴巴千问模型（默认）
- **星火 (xinghuo)**: 讯飞星火认知大模型
- **豆包 (doubao)**: 字节跳动豆包模型
- **DeepSeek (deepseek)**: DeepSeek深度求索模型

## 🔍 API接口

### 获取模型列表
```http
GET /chat/models
```

### 发送聊天消息（流式）
```http
POST /chat/send
Content-Type: application/json

{
    "message": "你好",
    "modelProvider": "qianwen",
    "conversationId": null,
    "title": "测试对话",
    "userId": "frontend-user"
}
```

### 健康检查
```http
GET /chat/health
```

## 🐛 故障排除

### 常见问题
1. **页面无法访问**: 检查应用是否正常启动，端口是否被占用
2. **模型列表为空**: 检查后端API是否正常响应
3. **消息发送失败**: 检查网络连接和API密钥配置
4. **流式显示异常**: 检查浏览器是否支持EventSource

### 调试方法
- 打开浏览器开发者工具查看控制台日志
- 检查网络面板中的API请求响应
- 使用`window.app.getStatus()`查看应用状态

## 📞 技术支持

如有问题或建议，请联系开发团队：
- 项目地址: [GitHub仓库地址]
- 问题反馈: [Issues页面]
- 技术文档: [Wiki页面]

---

> 🎉 **感谢使用 Dreamer AI Chat！** 
> 
> 这是一个现代化的AI聊天应用，致力于提供最佳的对话体验。 