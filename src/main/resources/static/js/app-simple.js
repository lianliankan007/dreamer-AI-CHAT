/**
 * 简化版主应用程序
 * 使用SimpleChatManager，专注于核心功能
 * 
 * @author panshenguo
 * @since 1.0.0
 */

class SimpleApp {
    constructor() {
        this.chatManager = null;
        this.elements = {};
        this.isInitialized = false;
        
        console.log('SimpleApp 开始初始化...');
    }
    
    async init() {
        try {
            // 等待DOM加载完成
            if (document.readyState === 'loading') {
                await new Promise(resolve => {
                    document.addEventListener('DOMContentLoaded', resolve);
                });
            }
            
            // 初始化DOM元素
            this.initElements();
            
            // 初始化聊天管理器
            this.initChatManager();
            
            // 绑定事件
            this.bindEvents();
            
            // 初始化界面
            this.initUI();
            
            this.isInitialized = true;
            console.log('SimpleApp 初始化完成');
            
        } catch (error) {
            console.error('SimpleApp 初始化失败:', error);
            this.showError('应用初始化失败: ' + error.message);
        }
    }
    
    initElements() {
        this.elements = {
            messageInput: document.getElementById('messageInput'),
            sendButton: document.getElementById('sendButton'),
            modelSelect: document.getElementById('modelSelect'),
            clearChat: document.getElementById('clearChat'),
            chatMessages: document.getElementById('chatMessages'),
            charCount: document.getElementById('charCount'),
            welcomeTime: document.getElementById('welcomeTime'),
            loadingOverlay: document.getElementById('loadingOverlay'),
            errorToast: document.getElementById('errorToast'),
            errorMessage: document.getElementById('errorMessage'),
            closeError: document.getElementById('closeError')
        };
        
        // 验证必需元素
        const required = ['messageInput', 'sendButton', 'modelSelect', 'chatMessages'];
        for (const id of required) {
            if (!this.elements[id]) {
                throw new Error(`找不到必需的DOM元素: ${id}`);
            }
        }
        
        console.log('DOM元素初始化完成');
    }
    
    initChatManager() {
        this.chatManager = new window.SimpleChatManager();
        console.log('聊天管理器创建完成');
    }
    
    bindEvents() {
        // 发送按钮点击
        this.elements.sendButton.addEventListener('click', () => {
            this.handleSendMessage();
        });
        
        // 输入框键盘事件
        this.elements.messageInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                if (e.shiftKey) {
                    // Shift+Enter: 换行
                    return;
                } else {
                    // Enter: 发送消息
                    e.preventDefault();
                    this.handleSendMessage();
                }
            } else if (e.key === 'Escape') {
                // Esc: 清空输入框
                this.elements.messageInput.value = '';
                this.updateSendButton();
                this.updateCharCount();
            }
        });
        
        // 输入框内容变化
        this.elements.messageInput.addEventListener('input', () => {
            this.updateSendButton();
            this.updateCharCount();
            this.autoResizeInput();
        });
        
        // 模型选择
        this.elements.modelSelect.addEventListener('change', (e) => {
            if (this.chatManager && this.chatManager.setCurrentModel) {
                this.chatManager.setCurrentModel(e.target.value);
            }
        });
        
        // 清空对话
        if (this.elements.clearChat) {
            this.elements.clearChat.addEventListener('click', () => {
                if (confirm('确定要清空当前对话吗？')) {
                    this.clearConversation();
                }
            });
        }
        
        // 错误提示关闭
        if (this.elements.closeError) {
            this.elements.closeError.addEventListener('click', () => {
                this.hideError();
            });
        }
        
        console.log('事件监听器绑定完成');
    }
    
    initUI() {
        // 设置欢迎消息时间
        if (this.elements.welcomeTime) {
            this.elements.welcomeTime.textContent = new Date().toLocaleTimeString('zh-CN', {
                hour: '2-digit',
                minute: '2-digit'
            });
        }
        
        // 初始化按钮状态
        this.updateSendButton();
        this.updateCharCount();
        this.autoResizeInput();
        
        // 聚焦输入框
        this.elements.messageInput.focus();
        
        console.log('界面初始化完成');
    }
    
    // 聊天管理器就绪回调
    onChatManagerReady() {
        console.log('聊天管理器就绪，更新模型选择器');
        this.updateModelSelector();
        this.updateSendButton();
    }
    
    updateModelSelector() {
        if (!this.chatManager || !this.chatManager.models) return;
        
        this.elements.modelSelect.innerHTML = '';
        
        this.chatManager.models.forEach((name, code) => {
            const option = document.createElement('option');
            option.value = code;
            option.textContent = name;
            if (code === this.chatManager.currentModel) {
                option.selected = true;
            }
            this.elements.modelSelect.appendChild(option);
        });
        
        console.log('模型选择器更新完成');
    }
    
    async handleSendMessage() {
        const message = this.elements.messageInput.value.trim();
        
        if (!message) {
            this.elements.messageInput.focus();
            return;
        }
        
        if (!this.chatManager) {
            this.showError('聊天管理器尚未初始化');
            return;
        }
        
        if (this.chatManager.isStreaming) {
            this.showError('正在处理其他消息，请稍候...');
            return;
        }
        
        console.log('开始发送消息:', message);
        
        try {
            // 禁用输入并显示加载状态
            this.setInputEnabled(false);
            this.showLoading(true);
            
            // 清空输入框
            this.elements.messageInput.value = '';
            this.updateCharCount();
            this.updateSendButton();
            this.autoResizeInput();
            
            // 发送消息
            await this.chatManager.sendMessage(message);
            
            console.log('消息发送完成');
            
        } catch (error) {
            console.error('发送消息失败:', error);
            this.showError(error.message);
            
            // 恢复输入框内容
            this.elements.messageInput.value = message;
            this.updateCharCount();
            this.updateSendButton();
            
        } finally {
            // 恢复界面状态
            this.setInputEnabled(true);
            this.showLoading(false);
            this.elements.messageInput.focus();
        }
    }
    
    updateSendButton() {
        const hasContent = this.elements.messageInput.value.trim().length > 0;
        const canSend = this.chatManager && this.chatManager.isReady && !this.chatManager.isStreaming;
        
        const shouldEnable = hasContent && canSend;
        this.elements.sendButton.disabled = !shouldEnable;
        
        // 调试信息
        console.log('按钮状态:', {
            hasContent,
            canSend,
            shouldEnable,
            disabled: this.elements.sendButton.disabled
        });
    }
    
    updateCharCount() {
        if (this.elements.charCount) {
            const count = this.elements.messageInput.value.length;
            this.elements.charCount.textContent = count;
            
            // 颜色提示
            const charCountElement = this.elements.charCount.parentElement;
            if (count > 8000) {
                charCountElement.style.color = '#dc3545';
            } else if (count > 6000) {
                charCountElement.style.color = '#ffc107';
            } else {
                charCountElement.style.color = '#6c757d';
            }
        }
    }
    
    autoResizeInput() {
        const textarea = this.elements.messageInput;
        textarea.style.height = 'auto';
        const newHeight = Math.max(24, Math.min(200, textarea.scrollHeight));
        textarea.style.height = newHeight + 'px';
    }
    
    setInputEnabled(enabled) {
        this.elements.messageInput.disabled = !enabled;
        this.elements.modelSelect.disabled = !enabled;
        
        if (this.elements.clearChat) {
            this.elements.clearChat.disabled = !enabled;
        }
        
        if (enabled) {
            this.updateSendButton();
        } else {
            this.elements.sendButton.disabled = true;
        }
    }
    
    showLoading(show) {
        if (this.elements.loadingOverlay) {
            if (show) {
                this.elements.loadingOverlay.classList.add('show');
            } else {
                this.elements.loadingOverlay.classList.remove('show');
            }
        }
    }
    
    showError(message) {
        console.error('错误:', message);
        
        if (this.elements.errorToast && this.elements.errorMessage) {
            this.elements.errorMessage.textContent = message;
            this.elements.errorToast.classList.add('show');
            
            // 5秒后自动隐藏
            setTimeout(() => {
                this.hideError();
            }, 5000);
        } else {
            alert('错误: ' + message);
        }
    }
    
    hideError() {
        if (this.elements.errorToast) {
            this.elements.errorToast.classList.remove('show');
        }
    }
    
    clearConversation() {
        if (this.chatManager) {
            this.chatManager.clearConversation();
        }
        this.elements.messageInput.focus();
    }
    
    getStatus() {
        return {
            isInitialized: this.isInitialized,
            chatManager: this.chatManager ? this.chatManager.getStatus() : null,
            elements: Object.keys(this.elements).length
        };
    }
}

// 应用启动函数
async function startSimpleApp() {
    try {
        console.log('启动 SimpleApp...');
        
        const app = new SimpleApp();
        await app.init();
        
        // 全局暴露应用实例
        window.app = app;
        
        console.log('SimpleApp 启动成功！');
        
    } catch (error) {
        console.error('SimpleApp 启动失败:', error);
        
        // 显示启动失败信息
        document.body.innerHTML += `
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
                z-index: 9999;
            ">
                <h3 style="color: #dc3545; margin-bottom: 15px;">应用启动失败</h3>
                <p style="margin-bottom: 20px;">${error.message}</p>
                <button onclick="window.location.reload()" style="
                    background: #007bff; 
                    color: white; 
                    border: none; 
                    padding: 10px 20px; 
                    border-radius: 5px; 
                    cursor: pointer;
                ">重新加载</button>
            </div>
        `;
    }
}

// 自动启动
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', startSimpleApp);
} else {
    startSimpleApp();
} 