/**
 * 主应用程序
 * 负责初始化界面、绑定事件、协调各模块交互
 * 
 * @author panshenguo
 * @since 1.0.0
 */

const { TimeUtils, TextUtils, DomUtils, debounce } = window.Utils;

/**
 * 应用程序类
 */
class App {
    constructor() {
        this.chatManager = null;
        this.elements = {};
        this.isInitialized = false;
        
        // 绑定方法上下文
        this.handleSendMessage = this.handleSendMessage.bind(this);
        this.handleKeyPress = this.handleKeyPress.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleModelChange = this.handleModelChange.bind(this);
        this.handleClearChat = this.handleClearChat.bind(this);
        this.handleCloseError = this.handleCloseError.bind(this);
        
        // 防抖函数
        this.debouncedInputChange = debounce(this.handleInputChange, 300);
        this.debouncedAutoResize = debounce(this.autoResizeInput, 100);
    }
    
    /**
     * 初始化应用程序
     */
    async init() {
        try {
            console.log('应用程序初始化开始...');
            
            // 等待DOM加载完成
            if (document.readyState === 'loading') {
                await new Promise(resolve => {
                    document.addEventListener('DOMContentLoaded', resolve);
                });
            }
            
            // 初始化元素引用
            this.initElements();
            
            // 初始化聊天管理器
            await this.initChatManager();
            
            // 绑定事件监听器
            this.bindEvents();
            
            // 初始化界面状态
            this.initUI();
            
            this.isInitialized = true;
            console.log('应用程序初始化完成');
            
        } catch (error) {
            console.error('应用程序初始化失败:', error);
            this.showError('应用程序初始化失败: ' + error.message);
        }
    }
    
    /**
     * 初始化DOM元素引用
     */
    initElements() {
        this.elements = {
            // 主要容器
            chatMessages: document.getElementById('chatMessages'),
            messageInput: document.getElementById('messageInput'),
            sendButton: document.getElementById('sendButton'),
            modelSelect: document.getElementById('modelSelect'),
            
            // 功能按钮
            clearChat: document.getElementById('clearChat'),
            
            // 状态显示
            charCount: document.getElementById('charCount'),
            welcomeTime: document.getElementById('welcomeTime'),
            loadingOverlay: document.getElementById('loadingOverlay'),
            errorToast: document.getElementById('errorToast'),
            errorMessage: document.getElementById('errorMessage'),
            closeError: document.getElementById('closeError')
        };
        
        // 验证必需元素
        const requiredElements = ['chatMessages', 'messageInput', 'sendButton', 'modelSelect'];
        for (const elementId of requiredElements) {
            if (!this.elements[elementId]) {
                throw new Error(`找不到必需的DOM元素: ${elementId}`);
            }
        }
        
        console.log('DOM元素初始化完成');
    }
    
    /**
     * 初始化聊天管理器
     */
    async initChatManager() {
        this.chatManager = new window.ChatManager();
        
        // 监听聊天管理器事件
        this.chatManager.on('modelsLoaded', this.onModelsLoaded.bind(this));
        this.chatManager.on('modelChanged', this.onModelChanged.bind(this));
        this.chatManager.on('messageAdded', this.onMessageAdded.bind(this));
        this.chatManager.on('streamComplete', this.onStreamComplete.bind(this));
        this.chatManager.on('streamStopped', this.onStreamStopped.bind(this));
        this.chatManager.on('conversationCleared', this.onConversationCleared.bind(this));
        this.chatManager.on('scrollToBottom', this.scrollToBottom.bind(this));
        this.chatManager.on('error', this.onChatError.bind(this));
        
        console.log('聊天管理器初始化完成');
    }
    
    /**
     * 绑定事件监听器
     */
    bindEvents() {
        // 发送消息
        this.elements.sendButton.addEventListener('click', this.handleSendMessage);
        
        // 键盘事件
        this.elements.messageInput.addEventListener('keydown', this.handleKeyPress);
        this.elements.messageInput.addEventListener('input', this.debouncedInputChange);
        this.elements.messageInput.addEventListener('input', this.debouncedAutoResize);
        
        // 模型选择
        this.elements.modelSelect.addEventListener('change', this.handleModelChange);
        
        // 清空对话
        if (this.elements.clearChat) {
            this.elements.clearChat.addEventListener('click', this.handleClearChat);
        }
        
        // 错误提示关闭
        if (this.elements.closeError) {
            this.elements.closeError.addEventListener('click', this.handleCloseError);
        }
        
        // 全局键盘快捷键
        document.addEventListener('keydown', this.handleGlobalKeyPress.bind(this));
        
        // 窗口大小变化
        window.addEventListener('resize', debounce(() => {
            this.scrollToBottom();
        }, 250));
        
        console.log('事件监听器绑定完成');
    }
    
    /**
     * 初始化界面状态
     */
    initUI() {
        // 设置欢迎消息时间
        if (this.elements.welcomeTime) {
            this.elements.welcomeTime.textContent = TimeUtils.formatTime(TimeUtils.now());
        }
        
        // 初始化输入框状态
        this.updateSendButton();
        this.updateCharCount();
        this.autoResizeInput();
        
        // 聚焦输入框
        this.elements.messageInput.focus();
        
        console.log('界面状态初始化完成');
    }
    
    /**
     * 处理发送消息
     */
    async handleSendMessage() {
        const message = this.elements.messageInput.value.trim();
        
        if (!message) {
            this.elements.messageInput.focus();
            return;
        }
        
        if (this.chatManager.isStreaming) {
            this.showError('正在处理其他消息，请稍候...');
            return;
        }
        
        try {
            // 显示加载状态
            this.showLoading(false);
            this.setInputEnabled(false);
            
            // 清空输入框
            this.elements.messageInput.value = '';
            this.updateCharCount();
            this.updateSendButton();
            this.autoResizeInput();
            
            // 发送消息
            await this.chatManager.sendMessage(message);
            
        } catch (error) {
            console.error('发送消息失败:', error);
            this.showError(error.message);
            
            // 恢复输入框内容
            this.elements.messageInput.value = message;
            this.updateCharCount();
            this.updateSendButton();
            
        } finally {
            this.showLoading(false);
            this.setInputEnabled(true);
            this.elements.messageInput.focus();
        }
    }
    
    /**
     * 处理键盘按键
     * @param {KeyboardEvent} event - 键盘事件
     */
    handleKeyPress(event) {
        if (event.key === 'Enter') {
            if (event.shiftKey) {
                // Shift+Enter: 换行（默认行为）
                return;
            } else {
                // Enter: 发送消息
                event.preventDefault();
                this.handleSendMessage();
            }
        } else if (event.key === 'Escape') {
            // Esc: 清空输入框
            this.elements.messageInput.value = '';
            this.updateCharCount();
            this.updateSendButton();
            this.autoResizeInput();
        }
    }
    
    /**
     * 处理全局键盘快捷键
     * @param {KeyboardEvent} event - 键盘事件
     */
    handleGlobalKeyPress(event) {
        // Ctrl/Cmd + K: 清空对话
        if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
            event.preventDefault();
            this.handleClearChat();
        }
        
        // Ctrl/Cmd + /: 聚焦输入框
        if ((event.ctrlKey || event.metaKey) && event.key === '/') {
            event.preventDefault();
            this.elements.messageInput.focus();
        }
    }
    
    /**
     * 处理输入内容变化
     */
    handleInputChange() {
        this.updateCharCount();
        this.updateSendButton();
        
        // 调试信息
        console.log('输入变化 - 内容长度:', this.elements.messageInput.value.trim().length, 
                   '按钮状态:', this.elements.sendButton.disabled ? '禁用' : '启用');
    }
    
    /**
     * 处理模型切换
     * @param {Event} event - 选择事件
     */
    handleModelChange(event) {
        const newModel = event.target.value;
        
        try {
            this.chatManager.setCurrentModel(newModel);
            console.log('模型切换成功:', newModel);
        } catch (error) {
            console.error('模型切换失败:', error);
            this.showError(error.message);
            
            // 恢复之前的选择
            event.target.value = this.chatManager.currentModel;
        }
    }
    
    /**
     * 处理清空对话
     */
    async handleClearChat() {
        if (!confirm('确定要清空当前对话吗？此操作不可撤销。')) {
            return;
        }
        
        try {
            await this.chatManager.clearConversation();
        } catch (error) {
            console.error('清空对话失败:', error);
            this.showError('清空对话失败: ' + error.message);
        }
    }
    
    /**
     * 处理错误提示关闭
     */
    handleCloseError() {
        this.hideError();
    }
    
    /**
     * 更新发送按钮状态
     */
    updateSendButton() {
        const hasContent = this.elements.messageInput.value.trim().length > 0;
        // const chatManagerReady = this.chatManager && this.chatManager.getStatus && this.chatManager.getStatus().hasModels;
        // const isNotStreaming = !this.chatManager || !this.chatManager.isStreaming;
        
        // 只有当有内容、聊天管理器就绪、且不在流式处理时，按钮才可用
        // const shouldEnable = hasContent && chatManagerReady && isNotStreaming;
        this.elements.sendButton.disabled = !hasContent;
        
        // 添加调试信息
        console.log('按钮状态更新:', {
            hasContent,
            chatManagerReady,
            isNotStreaming,
            shouldEnable,
            disabled: this.elements.sendButton.disabled
        });
    }
    
    /**
     * 更新字符计数
     */
    updateCharCount() {
        if (this.elements.charCount) {
            const count = this.elements.messageInput.value.length;
            this.elements.charCount.textContent = count;
            
            // 颜色提示
            const charCountElement = this.elements.charCount.parentElement;
            if (count > 8000) {
                charCountElement.style.color = '#dc3545'; // 红色
            } else if (count > 6000) {
                charCountElement.style.color = '#ffc107'; // 黄色
            } else {
                charCountElement.style.color = '#6c757d'; // 默认灰色
            }
        }
    }
    
    /**
     * 自动调整输入框高度
     */
    autoResizeInput() {
        DomUtils.autoResizeTextarea(this.elements.messageInput, 24, 200);
    }
    
    /**
     * 设置输入区域启用状态
     * @param {boolean} enabled - 是否启用
     */
    setInputEnabled(enabled) {
        this.elements.messageInput.disabled = !enabled;
        this.elements.modelSelect.disabled = !enabled;
        
        if (this.elements.clearChat) {
            this.elements.clearChat.disabled = !enabled;
        }
        
        // 发送按钮状态由updateSendButton统一管理
        if (enabled) {
            this.updateSendButton();
        } else {
            this.elements.sendButton.disabled = true;
        }
    }
    
    /**
     * 显示/隐藏加载状态
     * @param {boolean} show - 是否显示
     */
    showLoading(show) {
        if (this.elements.loadingOverlay) {
            if (show) {
                DomUtils.addClass(this.elements.loadingOverlay, 'show');
            } else {
                DomUtils.removeClass(this.elements.loadingOverlay, 'show');
            }
        }
    }
    
    /**
     * 显示错误提示
     * @param {string} message - 错误消息
     */
    showError(message) {
        if (this.elements.errorToast && this.elements.errorMessage) {
            this.elements.errorMessage.textContent = message;
            DomUtils.addClass(this.elements.errorToast, 'show');
            
            // 5秒后自动隐藏
            setTimeout(() => {
                this.hideError();
            }, 5000);
        } else {
            // 备用方案：使用alert
            alert('错误: ' + message);
        }
    }
    
    /**
     * 隐藏错误提示
     */
    hideError() {
        if (this.elements.errorToast) {
            DomUtils.removeClass(this.elements.errorToast, 'show');
        }
    }
    
    /**
     * 滚动到聊天区域底部
     */
    scrollToBottom() {
        if (this.elements.chatMessages) {
            DomUtils.scrollToBottom(this.elements.chatMessages, true);
        }
    }
    
    /**
     * 模型列表加载完成事件
     * @param {Object} data - 事件数据
     */
    onModelsLoaded(data) {
        // 更新模型选择器
        this.elements.modelSelect.innerHTML = '';
        
        data.models.forEach((name, code) => {
            const option = document.createElement('option');
            option.value = code;
            option.textContent = name;
            if (code === data.defaultModel) {
                option.selected = true;
            }
            this.elements.modelSelect.appendChild(option);
        });
        
        // 初始化完成后更新按钮状态
        this.updateSendButton();
        
        console.log('模型选择器更新完成，聊天管理器已就绪');
    }
    
    /**
     * 模型切换事件
     * @param {Object} data - 事件数据
     */
    onModelChanged(data) {
        // 更新欢迎消息中的模型信息
        const welcomeMessage = this.elements.chatMessages.querySelector('.assistant-message .message-text');
        if (welcomeMessage) {
            const modelInfo = welcomeMessage.querySelector('strong');
            if (modelInfo && modelInfo.textContent === '当前模型：') {
                const nextElement = modelInfo.nextSibling;
                if (nextElement) {
                    nextElement.textContent = data.modelName;
                }
            }
        }
        
        console.log('界面模型信息已更新');
    }
    
    /**
     * 消息添加事件
     * @param {Object} data - 事件数据
     */
    onMessageAdded(data) {
        this.elements.chatMessages.appendChild(data.element);
        this.scrollToBottom();
        
        // 如果是第一条用户消息，移除欢迎消息
        if (data.message.role === 'user' && this.chatManager.messages.length === 2) {
            const welcomeMessage = this.elements.chatMessages.querySelector('.assistant-message:first-child');
            if (welcomeMessage) {
                welcomeMessage.style.opacity = '0.6';
            }
        }
    }
    
    /**
     * 流式接收完成事件
     * @param {Object} data - 事件数据
     */
    onStreamComplete(data) {
        this.showLoading(false);
        this.setInputEnabled(true);
        this.elements.messageInput.focus();
        this.scrollToBottom();
        
        console.log('消息接收完成:', data.message.content.length, '字符');
    }
    
    /**
     * 流式接收停止事件
     */
    onStreamStopped() {
        this.showLoading(false);
        this.setInputEnabled(true);
        this.elements.messageInput.focus();
    }
    
    /**
     * 对话清空事件
     */
    onConversationCleared() {
        // 清空消息显示区域（保留欢迎消息）
        const messages = this.elements.chatMessages.querySelectorAll('.message:not(:first-child)');
        messages.forEach(message => message.remove());
        
        // 恢复欢迎消息样式
        const welcomeMessage = this.elements.chatMessages.querySelector('.assistant-message:first-child');
        if (welcomeMessage) {
            welcomeMessage.style.opacity = '1';
        }
        
        this.elements.messageInput.focus();
        console.log('界面对话已清空');
    }
    
    /**
     * 聊天错误事件
     * @param {string} message - 错误消息
     */
    onChatError(message) {
        this.showError(message);
        this.showLoading(false);
        this.setInputEnabled(true);
    }
    
    /**
     * 获取应用状态
     * @returns {Object} 应用状态
     */
    getStatus() {
        return {
            isInitialized: this.isInitialized,
            chatManager: this.chatManager ? this.chatManager.getStatus() : null,
            hasElements: Object.keys(this.elements).length > 0
        };
    }
}

// 全局应用实例
let app = null;

/**
 * 应用程序入口点
 */
async function startApp() {
    try {
        console.log('启动 Dreamer AI Chat 应用程序...');
        
        app = new App();
        await app.init();
        
        // 将app实例暴露到全局，便于调试
        window.app = app;
        
        console.log('应用程序启动成功！');
        
    } catch (error) {
        console.error('应用程序启动失败:', error);
        
        // 显示错误信息
        const errorHtml = `
            <div style="
                position: fixed; 
                top: 50%; 
                left: 50%; 
                transform: translate(-50%, -50%);
                background: white; 
                padding: 30px; 
                border-radius: 10px; 
                box-shadow: 0 4px 20px rgba(0,0,0,0.3);
                text-align: center;
                max-width: 400px;
                z-index: 9999;
            ">
                <h3 style="color: #dc3545; margin-bottom: 15px;">
                    <i class="fas fa-exclamation-triangle"></i>
                    应用启动失败
                </h3>
                <p style="margin-bottom: 20px; color: #6c757d;">
                    ${error.message}
                </p>
                <button onclick="window.location.reload()" style="
                    background: #007bff; 
                    color: white; 
                    border: none; 
                    padding: 10px 20px; 
                    border-radius: 5px; 
                    cursor: pointer;
                ">
                    重新加载
                </button>
            </div>
        `;
        
        document.body.innerHTML += errorHtml;
    }
}

// 自动启动应用程序
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', startApp);
} else {
    startApp();
} 