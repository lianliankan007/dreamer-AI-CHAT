/**
 * 聊天功能模块
 * 处理聊天消息发送、接收、流式显示等功能
 * 
 * @author panshenguo
 * @since 1.0.0
 */

const { API_CONFIG, TimeUtils, TextUtils, DomUtils, HttpUtils, EventBus } = window.Utils;

/**
 * 聊天管理器类
 */
class ChatManager {
    constructor() {
        this.conversationId = null;
        this.currentModel = 'qianwen';
        this.models = new Map();
        this.messages = [];
        this.isStreaming = false;
        this.currentEventSource = null;
        this.eventBus = new EventBus();
        
        // 初始化
        this.init();
    }
    
    /**
     * 初始化聊天管理器
     */
    async init() {
        try {
            await this.loadModels();
            console.log('聊天管理器初始化成功');
        } catch (error) {
            console.error('聊天管理器初始化失败:', error);
            this.eventBus.emit('error', '聊天管理器初始化失败: ' + error.message);
        }
    }
    
    /**
     * 加载可用模型列表
     */
    async loadModels() {
        try {
            const response = await HttpUtils.get(API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.CHAT_MODELS);
            
            if (response.success) {
                this.models.clear();
                Object.entries(response.models).forEach(([code, name]) => {
                    this.models.set(code, name);
                });
                
                // 设置默认模型
                if (response.defaultModel && this.models.has(response.defaultModel)) {
                    this.currentModel = response.defaultModel;
                }
                
                this.eventBus.emit('modelsLoaded', {
                    models: this.models,
                    defaultModel: this.currentModel
                });
                
                console.log('模型列表加载成功:', this.models);
            } else {
                throw new Error('获取模型列表失败');
            }
        } catch (error) {
            console.error('加载模型列表失败:', error);
            
            // 使用默认模型列表作为备用方案
            this.models.set('qianwen', '阿里巴巴千问');
            this.models.set('xinghuo', '讯飞星火');
            this.models.set('doubao', '豆包');
            this.models.set('deepseek', 'DeepSeek');
            
            this.eventBus.emit('modelsLoaded', {
                models: this.models,
                defaultModel: this.currentModel
            });
            
            throw error;
        }
    }
    
    /**
     * 发送聊天消息
     * @param {string} message - 消息内容
     * @param {Object} options - 选项
     */
    async sendMessage(message, options = {}) {
        if (this.isStreaming) {
            throw new Error('正在处理其他消息，请稍候...');
        }
        
        if (!message || !message.trim()) {
            throw new Error('消息内容不能为空');
        }
        
        try {
            this.isStreaming = true;
            
            // 创建用户消息
            const userMessage = this.createMessage('user', message.trim());
            this.addMessage(userMessage);
            
            // 创建AI消息占位符
            const aiMessage = this.createMessage('assistant', '');
            const messageElement = this.addMessage(aiMessage);
            
            // 准备请求数据
            const requestData = {
                message: message.trim(),
                modelProvider: this.currentModel,
                conversationId: this.conversationId,
                title: options.title || this.generateTitle(message),
                userId: 'frontend-user',
                maxTokens: options.maxTokens,
                temperature: options.temperature
            };
            
            // 开始流式接收
            await this.startStreamingChat(requestData, aiMessage, messageElement);
            
        } catch (error) {
            console.error('发送消息失败:', error);
            this.eventBus.emit('error', error.message);
            throw error;
        } finally {
            this.isStreaming = false;
        }
    }
    
    /**
     * 开始流式聊天
     * @param {Object} requestData - 请求数据
     * @param {Object} aiMessage - AI消息对象
     * @param {HTMLElement} messageElement - 消息DOM元素
     */
    async startStreamingChat(requestData, aiMessage, messageElement) {
        return new Promise((resolve, reject) => {
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.CHAT_SEND;
            
            // 创建EventSource连接
            this.currentEventSource = new EventSource(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Cache-Control': 'no-cache'
                },
                body: JSON.stringify(requestData)
            });
            
            // 由于EventSource不支持POST，我们使用fetch + stream替代
            this.streamWithFetch(url, requestData, aiMessage, messageElement)
                .then(resolve)
                .catch(reject);
        });
    }
    
    /**
     * 使用fetch进行流式请求
     * @param {string} url - 请求URL
     * @param {Object} requestData - 请求数据
     * @param {Object} aiMessage - AI消息对象
     * @param {HTMLElement} messageElement - 消息DOM元素
     */
    async streamWithFetch(url, requestData, aiMessage, messageElement) {
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'text/event-stream',
                    'Cache-Control': 'no-cache'
                },
                body: JSON.stringify(requestData)
            });
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            let buffer = '';
            
            // 显示打字指示器
            this.showTypingIndicator(messageElement);
            
            // 标记开始接收流式数据
            let hasReceivedData = false;
            
            while (true) {
                const { done, value } = await reader.read();
                
                if (done) {
                    break;
                }
                
                hasReceivedData = true;
                buffer += decoder.decode(value, { stream: true });
                const lines = buffer.split('\n');
                buffer = lines.pop(); // 保留不完整的行
                
                for (const line of lines) {
                    if (line.trim()) {
                        this.processStreamLine(line, aiMessage, messageElement);
                    }
                }
            }
            
            // 处理最后的buffer
            if (buffer.trim()) {
                this.processStreamLine(buffer, aiMessage, messageElement);
            }
            
            // 如果没有接收到数据，显示默认消息
            if (!hasReceivedData || !aiMessage.content.trim()) {
                aiMessage.content = '抱歉，我现在无法正常响应。这可能是由于网络连接问题或服务暂时不可用。请稍后重试。';
                this.updateMessageContent(messageElement, aiMessage.content);
            }
            
            // 完成流式接收
            this.onStreamComplete(aiMessage, messageElement);
            
        } catch (error) {
            console.error('流式请求失败:', error);
            this.onStreamError(error, messageElement);
            throw error;
        }
    }
    
    /**
     * 处理流式数据行
     * @param {string} line - 数据行
     * @param {Object} aiMessage - AI消息对象
     * @param {HTMLElement} messageElement - 消息DOM元素
     */
    processStreamLine(line, aiMessage, messageElement) {
        try {
            // 解析SSE格式
            if (line.startsWith('data: ')) {
                const data = line.substring(6);
                
                if (data === '[DONE]') {
                    return;
                }
                
                try {
                    const eventData = JSON.parse(data);
                    this.handleStreamEvent(eventData, aiMessage, messageElement);
                } catch (parseError) {
                    // 如果不是JSON格式，可能是纯文本数据
                    aiMessage.content += data;
                    this.updateMessageContent(messageElement, aiMessage.content);
                }
                
            } else if (line.startsWith('event: ')) {
                const eventType = line.substring(7);
                console.log('收到事件类型:', eventType);
            } else if (line.trim() && !line.startsWith(':')) {
                // 处理其他格式的数据
                aiMessage.content += line + '\n';
                this.updateMessageContent(messageElement, aiMessage.content);
            }
        } catch (error) {
            console.error('解析流式数据失败:', line, error);
            
            // 作为备用方案，将原始数据添加到消息内容
            aiMessage.content += line + '\n';
            this.updateMessageContent(messageElement, aiMessage.content);
        }
    }
    
    /**
     * 处理流式事件
     * @param {Object} eventData - 事件数据
     * @param {Object} aiMessage - AI消息对象
     * @param {HTMLElement} messageElement - 消息DOM元素
     */
    handleStreamEvent(eventData, aiMessage, messageElement) {
        switch (eventData.type) {
            case 'content':
                // 更新消息内容
                aiMessage.content += eventData.content || '';
                this.updateMessageContent(messageElement, aiMessage.content);
                break;
                
            case 'conversation':
                // 设置对话ID
                if (eventData.conversationId && !this.conversationId) {
                    this.conversationId = eventData.conversationId;
                    console.log('对话ID设置为:', this.conversationId);
                }
                break;
                
            case 'complete':
                // 消息完成
                if (eventData.totalTokens) {
                    console.log('总Token数:', eventData.totalTokens);
                }
                break;
                
            case 'error':
                // 错误处理
                this.onStreamError(new Error(eventData.message), messageElement);
                break;
                
            default:
                console.log('未知事件类型:', eventData.type, eventData);
        }
    }
    
    /**
     * 显示打字指示器
     * @param {HTMLElement} messageElement - 消息元素
     */
    showTypingIndicator(messageElement) {
        const textElement = messageElement.querySelector('.message-text');
        if (textElement) {
            textElement.innerHTML = '<span class="typing-indicator"></span>';
        }
    }
    
    /**
     * 更新消息内容
     * @param {HTMLElement} messageElement - 消息元素
     * @param {string} content - 消息内容
     */
    updateMessageContent(messageElement, content) {
        const textElement = messageElement.querySelector('.message-text');
        if (textElement) {
            // 解析Markdown并更新内容
            const htmlContent = TextUtils.parseMarkdown(content);
            textElement.innerHTML = htmlContent;
            
            // 高亮代码块
            if (typeof Prism !== 'undefined') {
                textElement.querySelectorAll('pre code').forEach(block => {
                    Prism.highlightElement(block);
                });
            }
        }
        
        // 滚动到底部
        this.eventBus.emit('scrollToBottom');
    }
    
    /**
     * 流式接收完成
     * @param {Object} aiMessage - AI消息对象
     * @param {HTMLElement} messageElement - 消息DOM元素
     */
    onStreamComplete(aiMessage, messageElement) {
        // 更新消息时间戳
        aiMessage.timestamp = TimeUtils.now();
        this.updateMessageTime(messageElement, aiMessage.timestamp);
        
        // 触发完成事件
        this.eventBus.emit('streamComplete', {
            message: aiMessage,
            conversationId: this.conversationId
        });
        
        console.log('流式接收完成');
    }
    
    /**
     * 流式接收错误
     * @param {Error} error - 错误对象
     * @param {HTMLElement} messageElement - 消息DOM元素
     */
    onStreamError(error, messageElement) {
        const textElement = messageElement.querySelector('.message-text');
        if (textElement) {
            textElement.innerHTML = `
                <div style="color: #dc3545; padding: 10px; border: 1px solid #dc3545; border-radius: 4px; background: #f8d7da;">
                    <i class="fas fa-exclamation-triangle"></i>
                    <strong>错误：</strong>${error.message}
                    <br><small>请稍后重试或检查网络连接</small>
                </div>
            `;
        }
        
        this.eventBus.emit('error', error.message);
    }
    
    /**
     * 创建消息对象
     * @param {string} role - 角色（user/assistant）
     * @param {string} content - 消息内容
     * @returns {Object} 消息对象
     */
    createMessage(role, content) {
        return {
            id: Date.now() + Math.random(),
            role,
            content,
            timestamp: TimeUtils.now(),
            model: role === 'assistant' ? this.currentModel : null
        };
    }
    
    /**
     * 添加消息到界面
     * @param {Object} message - 消息对象
     * @returns {HTMLElement} 消息DOM元素
     */
    addMessage(message) {
        this.messages.push(message);
        
        const messageElement = this.createMessageElement(message);
        this.eventBus.emit('messageAdded', {
            message,
            element: messageElement
        });
        
        return messageElement;
    }
    
    /**
     * 创建消息DOM元素
     * @param {Object} message - 消息对象
     * @returns {HTMLElement} 消息DOM元素
     */
    createMessageElement(message) {
        const messageClass = message.role === 'user' ? 'user-message' : 'assistant-message';
        const avatarIcon = message.role === 'user' ? 'fas fa-user' : 'fas fa-robot';
        
        const messageElement = DomUtils.createElement('div', {
            className: `message ${messageClass}`,
            'data-message-id': message.id
        });
        
        // 头像
        const avatar = DomUtils.createElement('div', {
            className: 'message-avatar'
        });
        avatar.innerHTML = `<i class="${avatarIcon}"></i>`;
        
        // 消息内容
        const content = DomUtils.createElement('div', {
            className: 'message-content'
        });
        
        const textElement = DomUtils.createElement('div', {
            className: 'message-text'
        });
        
        if (message.content) {
            if (message.role === 'user') {
                textElement.textContent = message.content;
            } else {
                textElement.innerHTML = TextUtils.parseMarkdown(message.content);
            }
        }
        
        const timeElement = DomUtils.createElement('div', {
            className: 'message-time'
        });
        timeElement.innerHTML = `<span>${TimeUtils.formatTime(message.timestamp)}</span>`;
        
        content.appendChild(textElement);
        content.appendChild(timeElement);
        
        messageElement.appendChild(avatar);
        messageElement.appendChild(content);
        
        return messageElement;
    }
    
    /**
     * 更新消息时间显示
     * @param {HTMLElement} messageElement - 消息DOM元素
     * @param {number} timestamp - 时间戳
     */
    updateMessageTime(messageElement, timestamp) {
        const timeElement = messageElement.querySelector('.message-time span');
        if (timeElement) {
            timeElement.textContent = TimeUtils.formatTime(timestamp);
        }
    }
    
    /**
     * 设置当前模型
     * @param {string} modelCode - 模型代码
     */
    setCurrentModel(modelCode) {
        if (this.models.has(modelCode)) {
            this.currentModel = modelCode;
            this.eventBus.emit('modelChanged', {
                oldModel: this.currentModel,
                newModel: modelCode,
                modelName: this.models.get(modelCode)
            });
            console.log('切换模型:', modelCode, this.models.get(modelCode));
        } else {
            throw new Error(`未知模型: ${modelCode}`);
        }
    }
    
    /**
     * 获取当前模型信息
     * @returns {Object} 模型信息
     */
    getCurrentModel() {
        return {
            code: this.currentModel,
            name: this.models.get(this.currentModel)
        };
    }
    
    /**
     * 清空对话
     */
    async clearConversation() {
        try {
            if (this.conversationId) {
                const url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.CHAT_CLEAR}/${this.conversationId}`;
                await HttpUtils.request(url, { method: 'DELETE' });
            }
            
            this.conversationId = null;
            this.messages = [];
            
            this.eventBus.emit('conversationCleared');
            console.log('对话已清空');
            
        } catch (error) {
            console.error('清空对话失败:', error);
            // 即使API调用失败，也清空本地数据
            this.conversationId = null;
            this.messages = [];
            this.eventBus.emit('conversationCleared');
        }
    }
    
    /**
     * 停止当前流式接收
     */
    stopStreaming() {
        if (this.currentEventSource) {
            this.currentEventSource.close();
            this.currentEventSource = null;
        }
        
        this.isStreaming = false;
        this.eventBus.emit('streamStopped');
        console.log('流式接收已停止');
    }
    
    /**
     * 生成对话标题
     * @param {string} message - 首条消息
     * @returns {string} 对话标题
     */
    generateTitle(message) {
        const cleanMessage = TextUtils.cleanText(message);
        return TextUtils.truncate(cleanMessage, 50);
    }
    
    /**
     * 监听事件
     * @param {string} event - 事件名
     * @param {Function} callback - 回调函数
     */
    on(event, callback) {
        this.eventBus.on(event, callback);
    }
    
    /**
     * 移除事件监听
     * @param {string} event - 事件名
     * @param {Function} callback - 回调函数
     */
    off(event, callback) {
        this.eventBus.off(event, callback);
    }
    
    /**
     * 获取聊天状态
     * @returns {Object} 聊天状态
     */
    getStatus() {
        return {
            conversationId: this.conversationId,
            currentModel: this.currentModel,
            messageCount: this.messages.length,
            isStreaming: this.isStreaming,
            hasModels: this.models.size > 0
        };
    }
}

// 导出聊天管理器
window.ChatManager = ChatManager; 