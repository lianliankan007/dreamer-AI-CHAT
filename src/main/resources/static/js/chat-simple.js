/**
 * 简化版聊天管理器
 * 专注于核心的发送接收功能，减少复杂性
 * 
 * @author panshenguo
 * @since 1.0.0
 */

class SimpleChatManager {
    constructor() {
        this.currentModel = 'qianwen';
        this.conversationId = null;
        this.isStreaming = false;
        this.models = new Map();
        this.isReady = false;
        
        console.log('SimpleChatManager 初始化');
        this.init();
    }
    
    async init() {
        try {
            await this.loadModels();
            this.isReady = true;
            console.log('SimpleChatManager 初始化完成');
            
            // 触发就绪事件
            if (window.app && window.app.onChatManagerReady) {
                window.app.onChatManagerReady();
            }
        } catch (error) {
            console.error('SimpleChatManager 初始化失败:', error);
        }
    }
    
    async loadModels() {
        try {
            const response = await fetch('/chat/models');
            const data = await response.json();
            
            if (data.success) {
                this.models.clear();
                Object.entries(data.models).forEach(([code, name]) => {
                    this.models.set(code, name);
                });
                
                if (data.defaultModel) {
                    this.currentModel = data.defaultModel;
                }
                
                console.log('模型列表加载成功:', this.models);
                return true;
            } else {
                throw new Error('获取模型列表失败');
            }
        } catch (error) {
            console.error('加载模型失败:', error);
            
            // 使用默认模型作为备用
            this.models.set('qianwen', '阿里巴巴千问');
            this.currentModel = 'qianwen';
            return false;
        }
    }
    
    async sendMessage(message) {
        if (this.isStreaming) {
            throw new Error('正在处理其他消息，请稍候...');
        }
        
        if (!message || !message.trim()) {
            throw new Error('消息内容不能为空');
        }
        
        if (!this.isReady) {
            throw new Error('聊天管理器尚未就绪');
        }
        
        console.log('开始发送消息:', message);
        this.isStreaming = true;
        
        try {
            // 添加用户消息到界面
            this.addUserMessage(message);
            
            // 创建AI消息占位符
            const aiMessageElement = this.addAIMessage('');
            
            // 发送请求并处理流式响应
            await this.sendStreamRequest(message, aiMessageElement);
            
        } catch (error) {
            console.error('发送消息失败:', error);
            this.showError('发送失败: ' + error.message);
            throw error;
        } finally {
            this.isStreaming = false;
        }
    }
    
    async sendStreamRequest(message, aiMessageElement) {
        const requestData = {
            message: message.trim(),
            modelProvider: this.currentModel,
            conversationId: this.conversationId,
            title: this.generateTitle(message),
            userId: 'web-user'
        };
        
        console.log('发送流式请求:', requestData);
        
        try {
            const response = await fetch('/chat/send', {
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
            
            // 处理流式响应
            await this.handleStreamResponse(response, aiMessageElement);
            
        } catch (error) {
            console.error('流式请求失败:', error);
            this.updateAIMessage(aiMessageElement, '抱歉，消息发送失败。请稍后重试。');
            throw error;
        }
    }
    
    async handleStreamResponse(response, aiMessageElement) {
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';
        let content = '';
        
        console.log('开始处理流式响应');
        
        try {
            while (true) {
                const { done, value } = await reader.read();
                
                if (done) {
                    console.log('流式响应完成');
                    break;
                }
                
                buffer += decoder.decode(value, { stream: true });
                const lines = buffer.split('\n');
                buffer = lines.pop() || '';
                
                // 只处理 event:ai_chunk 类型的数据
                const chunkContent = this.extractAiChunkEvents(lines);
                if (chunkContent) {
                    content += chunkContent;
                    this.updateAIMessage(aiMessageElement, content);
                }
            }
            
            // 如果没有接收到内容，显示默认消息
            if (!content.trim()) {
                content = '我现在暂时无法响应，请稍后重试。';
                this.updateAIMessage(aiMessageElement, content);
            }
            
        } catch (error) {
            console.error('处理流式响应失败:', error);
            this.updateAIMessage(aiMessageElement, '接收响应时出现错误，请重试。');
        }
    }
    
    /**
     * 只处理 event:ai_chunk 类型的SSE事件
     */
    extractAiChunkEvents(lines) {
        let content = '';
        let isAiChunkEvent = false;
        
        for (let i = 0; i < lines.length; i++) {
            const line = lines[i].trim();
            
            if (line.startsWith('event:')) {
                // 检查事件类型
                const eventType = line.substring(6).trim();
                isAiChunkEvent = (eventType === 'ai_chunk');
                console.log('事件类型:', eventType, isAiChunkEvent ? '(处理)' : '(忽略)');
            } else if (line.startsWith('data:') && isAiChunkEvent) {
                // 只有在 ai_chunk 事件下才处理数据
                try {
                    const dataStr = line.substring(5).trim();
                    const eventData = JSON.parse(dataStr);
                    
                    if (eventData.chunk !== undefined && eventData.chunk !== null) {
                        console.log('提取ai_chunk内容:', eventData.chunk);
                        content += eventData.chunk;
                    }
                } catch (error) {
                    console.error('解析ai_chunk数据失败:', line, error);
                }
                
                // 处理完数据后重置标志
                isAiChunkEvent = false;
            }
        }
        
        return content;
    }
    
    addUserMessage(message) {
        const messagesContainer = document.getElementById('chatMessages');
        if (!messagesContainer) return;
        
        const messageElement = this.createMessageElement('user', message);
        messagesContainer.appendChild(messageElement);
        this.scrollToBottom();
        
        console.log('添加用户消息:', message);
        return messageElement;
    }
    
    addAIMessage(content) {
        const messagesContainer = document.getElementById('chatMessages');
        if (!messagesContainer) return null;
        
        const messageElement = this.createMessageElement('assistant', content);
        messagesContainer.appendChild(messageElement);
        this.scrollToBottom();
        
        console.log('添加AI消息占位符');
        return messageElement;
    }
    
    updateAIMessage(messageElement, content) {
        if (!messageElement) return;
        
        const textElement = messageElement.querySelector('.message-text');
        if (textElement) {
            // 使用Markdown渲染
            textElement.innerHTML = this.formatContent(content);
            
            // 触发代码块高亮
            this.highlightCodeBlocks(textElement);
            
            this.scrollToBottom();
        }
    }
    
    /**
     * 高亮代码块
     */
    highlightCodeBlocks(element) {
        if (window.Prism) {
            // 查找所有代码块并高亮
            const codeBlocks = element.querySelectorAll('pre code, code');
            codeBlocks.forEach(block => {
                try {
                    if (block.parentNode.tagName === 'PRE') {
                        // 代码块
                        Prism.highlightElement(block);
                    }
                } catch (error) {
                    console.warn('代码高亮失败:', error);
                }
            });
        }
    }
    
    createMessageElement(role, content) {
        const messageClass = role === 'user' ? 'user-message' : 'assistant-message';
        const avatarIcon = role === 'user' ? 'fas fa-user' : 'fas fa-robot';
        
        const messageElement = document.createElement('div');
        messageElement.className = `message ${messageClass}`;
        
        messageElement.innerHTML = `
            <div class="message-avatar">
                <i class="${avatarIcon}"></i>
            </div>
            <div class="message-content">
                <div class="message-text">${this.formatContent(content)}</div>
                <div class="message-time">
                    <span>${this.formatTime(Date.now())}</span>
                </div>
            </div>
        `;
        
        return messageElement;
    }
    
    formatContent(content) {
        if (!content) return '';
        
        try {
            // 使用marked.js解析Markdown
            if (window.marked) {
                // 配置marked选项
                window.marked.setOptions({
                    breaks: true,        // 支持换行符转换为<br>
                    gfm: true,          // 启用GitHub风格的Markdown
                    sanitize: false,    // 不使用marked的sanitize，我们自己处理
                    highlight: function(code, lang) {
                        // 使用Prism.js进行代码高亮
                        if (window.Prism && lang && Prism.languages[lang]) {
                            try {
                                return Prism.highlight(code, Prism.languages[lang], lang);
                            } catch (err) {
                                console.warn('代码高亮失败:', err);
                                return this.escapeHtml(code);
                            }
                        }
                        return this.escapeHtml(code);
                    }.bind(this)
                });
                
                // 解析Markdown
                let html = window.marked.parse(content);
                
                // 基本的XSS防护
                html = this.sanitizeHtml(html);
                
                return html;
            } else {
                console.warn('marked.js未加载，使用简单格式化');
                return this.simpleFormat(content);
            }
        } catch (error) {
            console.error('Markdown解析失败:', error);
            return this.simpleFormat(content);
        }
    }
    
    /**
     * 简单的文本格式化（降级方案）
     */
    simpleFormat(content) {
        return content
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/\n/g, '<br>')
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            .replace(/\*(.*?)\*/g, '<em>$1</em>')
            .replace(/`(.*?)`/g, '<code>$1</code>');
    }
    
    /**
     * HTML转义
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    /**
     * 基本的HTML净化，移除危险标签
     */
    sanitizeHtml(html) {
        // 允许的标签和属性
        const allowedTags = ['p', 'br', 'strong', 'em', 'b', 'i', 'u', 'code', 'pre', 
                           'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'ul', 'ol', 'li', 
                           'blockquote', 'a', 'img', 'table', 'thead', 'tbody', 'tr', 'th', 'td'];
        const allowedAttributes = ['href', 'src', 'alt', 'title', 'class'];
        
        // 创建临时DOM来解析HTML
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;
        
        // 递归净化元素
        this.sanitizeElement(tempDiv, allowedTags, allowedAttributes);
        
        return tempDiv.innerHTML;
    }
    
    /**
     * 递归净化DOM元素
     */
    sanitizeElement(element, allowedTags, allowedAttributes) {
        const children = Array.from(element.children);
        
        for (const child of children) {
            const tagName = child.tagName.toLowerCase();
            
            // 检查标签是否允许
            if (!allowedTags.includes(tagName)) {
                // 不允许的标签，保留文本内容
                const textNode = document.createTextNode(child.textContent);
                child.parentNode.replaceChild(textNode, child);
                continue;
            }
            
            // 净化属性
            const attributes = Array.from(child.attributes);
            for (const attr of attributes) {
                if (!allowedAttributes.includes(attr.name.toLowerCase())) {
                    child.removeAttribute(attr.name);
                } else if (attr.name.toLowerCase() === 'href') {
                    // 检查链接安全性
                    const href = attr.value.toLowerCase();
                    if (!href.startsWith('http://') && !href.startsWith('https://') && !href.startsWith('#')) {
                        child.removeAttribute('href');
                    }
                }
            }
            
            // 递归处理子元素
            this.sanitizeElement(child, allowedTags, allowedAttributes);
        }
    }
    
    formatTime(timestamp) {
        const date = new Date(timestamp);
        return date.toLocaleTimeString('zh-CN', { 
            hour: '2-digit', 
            minute: '2-digit' 
        });
    }
    
    scrollToBottom() {
        const messagesContainer = document.getElementById('chatMessages');
        if (messagesContainer) {
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }
    }
    
    showError(message) {
        console.error('聊天错误:', message);
        if (window.app && window.app.showError) {
            window.app.showError(message);
        } else {
            alert('错误: ' + message);
        }
    }
    
    setCurrentModel(modelCode) {
        if (this.models.has(modelCode)) {
            this.currentModel = modelCode;
            console.log('切换模型:', modelCode, this.models.get(modelCode));
            return true;
        }
        return false;
    }
    
    clearConversation() {
        this.conversationId = null;
        
        const messagesContainer = document.getElementById('chatMessages');
        if (messagesContainer) {
            // 清空除了欢迎消息外的所有消息
            const messages = messagesContainer.querySelectorAll('.message:not(:first-child)');
            messages.forEach(message => message.remove());
            
            // 恢复欢迎消息样式
            const welcomeMessage = messagesContainer.querySelector('.message:first-child');
            if (welcomeMessage) {
                welcomeMessage.style.opacity = '1';
            }
        }
        
        console.log('对话已清空');
    }
    
    generateTitle(message) {
        return message.substring(0, 50);
    }
    
    getStatus() {
        return {
            isReady: this.isReady,
            hasModels: this.models.size > 0,
            isStreaming: this.isStreaming,
            currentModel: this.currentModel,
            conversationId: this.conversationId
        };
    }
}

// 导出简化聊天管理器
window.SimpleChatManager = SimpleChatManager; 