/**
 * 工具函数模块
 * 提供时间格式化、文本处理、API请求等通用功能
 * 
 * @author panshenguo
 * @since 1.0.0
 */

// API配置
const API_CONFIG = {
    BASE_URL: '',
    ENDPOINTS: {
        CHAT_SEND: '/chat/send',
        CHAT_MODELS: '/chat/models',
        CHAT_HISTORY: '/chat/history',
        CHAT_CLEAR: '/chat/history'
    },
    TIMEOUT: 120000 // 120秒超时
};

/**
 * 时间工具类
 */
class TimeUtils {
    /**
     * 格式化时间戳为可读字符串
     * @param {number} timestamp - 时间戳（毫秒）
     * @param {boolean} includeSeconds - 是否包含秒数
     * @returns {string} 格式化后的时间字符串
     */
    static formatTime(timestamp, includeSeconds = false) {
        const date = new Date(timestamp);
        const now = new Date();
        const diff = now - date;
        
        // 小于1分钟
        if (diff < 60000) {
            return '刚刚';
        }
        
        // 小于1小时
        if (diff < 3600000) {
            const minutes = Math.floor(diff / 60000);
            return `${minutes}分钟前`;
        }
        
        // 小于24小时
        if (diff < 86400000) {
            const hours = Math.floor(diff / 3600000);
            return `${hours}小时前`;
        }
        
        // 同一年
        if (date.getFullYear() === now.getFullYear()) {
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hour = String(date.getHours()).padStart(2, '0');
            const minute = String(date.getMinutes()).padStart(2, '0');
            
            if (includeSeconds) {
                const second = String(date.getSeconds()).padStart(2, '0');
                return `${month}-${day} ${hour}:${minute}:${second}`;
            }
            return `${month}-${day} ${hour}:${minute}`;
        }
        
        // 不同年份
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hour = String(date.getHours()).padStart(2, '0');
        const minute = String(date.getMinutes()).padStart(2, '0');
        
        return `${year}-${month}-${day} ${hour}:${minute}`;
    }
    
    /**
     * 获取当前时间戳
     * @returns {number} 当前时间戳（毫秒）
     */
    static now() {
        return Date.now();
    }
}

/**
 * 文本处理工具类
 */
class TextUtils {
    /**
     * 转义HTML特殊字符
     * @param {string} text - 原始文本
     * @returns {string} 转义后的文本
     */
    static escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    /**
     * 解析Markdown文本为HTML
     * @param {string} markdown - Markdown文本
     * @returns {string} HTML字符串
     */
    static parseMarkdown(markdown) {
        if (typeof marked !== 'undefined') {
            // 配置marked选项
            marked.setOptions({
                highlight: function(code, lang) {
                    if (typeof Prism !== 'undefined' && lang && Prism.languages[lang]) {
                        return Prism.highlight(code, Prism.languages[lang], lang);
                    }
                    return code;
                },
                breaks: true,
                gfm: true
            });
            
            return marked.parse(markdown);
        } else {
            // 简单的Markdown解析（备用方案）
            return this.simpleMarkdownParse(markdown);
        }
    }
    
    /**
     * 简单的Markdown解析（备用方案）
     * @param {string} text - Markdown文本
     * @returns {string} HTML字符串
     */
    static simpleMarkdownParse(text) {
        return text
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            .replace(/\*(.*?)\*/g, '<em>$1</em>')
            .replace(/`(.*?)`/g, '<code>$1</code>')
            .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
            .replace(/\n/g, '<br>');
    }
    
    /**
     * 截取文本到指定长度
     * @param {string} text - 原始文本
     * @param {number} maxLength - 最大长度
     * @param {string} suffix - 后缀（默认为...）
     * @returns {string} 截取后的文本
     */
    static truncate(text, maxLength, suffix = '...') {
        if (text.length <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - suffix.length) + suffix;
    }
    
    /**
     * 清理文本中的特殊字符
     * @param {string} text - 原始文本
     * @returns {string} 清理后的文本
     */
    static cleanText(text) {
        return text.replace(/[\r\n\t]/g, ' ').replace(/\s+/g, ' ').trim();
    }
    
    /**
     * 计算文本字符数（考虑中文字符）
     * @param {string} text - 文本
     * @returns {number} 字符数
     */
    static getCharCount(text) {
        return text.length;
    }
}

/**
 * DOM操作工具类
 */
class DomUtils {
    /**
     * 创建DOM元素
     * @param {string} tag - 标签名
     * @param {Object} attributes - 属性对象
     * @param {string|Node|Array} children - 子元素
     * @returns {HTMLElement} 创建的元素
     */
    static createElement(tag, attributes = {}, children = null) {
        const element = document.createElement(tag);
        
        // 设置属性
        Object.entries(attributes).forEach(([key, value]) => {
            if (key === 'className') {
                element.className = value;
            } else if (key === 'textContent') {
                element.textContent = value;
            } else if (key === 'innerHTML') {
                element.innerHTML = value;
            } else {
                element.setAttribute(key, value);
            }
        });
        
        // 添加子元素
        if (children) {
            if (typeof children === 'string') {
                element.textContent = children;
            } else if (children instanceof Node) {
                element.appendChild(children);
            } else if (Array.isArray(children)) {
                children.forEach(child => {
                    if (typeof child === 'string') {
                        element.appendChild(document.createTextNode(child));
                    } else if (child instanceof Node) {
                        element.appendChild(child);
                    }
                });
            }
        }
        
        return element;
    }
    
    /**
     * 滚动到元素底部
     * @param {HTMLElement} element - 目标元素
     * @param {boolean} smooth - 是否平滑滚动
     */
    static scrollToBottom(element, smooth = true) {
        if (element) {
            element.scrollTo({
                top: element.scrollHeight,
                behavior: smooth ? 'smooth' : 'auto'
            });
        }
    }
    
    /**
     * 自动调整textarea高度
     * @param {HTMLTextAreaElement} textarea - textarea元素
     * @param {number} minHeight - 最小高度（像素）
     * @param {number} maxHeight - 最大高度（像素）
     */
    static autoResizeTextarea(textarea, minHeight = 24, maxHeight = 200) {
        textarea.style.height = 'auto';
        const scrollHeight = textarea.scrollHeight;
        const newHeight = Math.max(minHeight, Math.min(maxHeight, scrollHeight));
        textarea.style.height = newHeight + 'px';
    }
    
    /**
     * 添加CSS类（支持动画）
     * @param {HTMLElement} element - 目标元素
     * @param {string} className - CSS类名
     * @param {number} duration - 动画持续时间（毫秒）
     */
    static addClass(element, className, duration = 0) {
        element.classList.add(className);
        if (duration > 0) {
            setTimeout(() => {
                element.classList.remove(className);
            }, duration);
        }
    }
    
    /**
     * 移除CSS类
     * @param {HTMLElement} element - 目标元素
     * @param {string} className - CSS类名
     */
    static removeClass(element, className) {
        element.classList.remove(className);
    }
    
    /**
     * 切换CSS类
     * @param {HTMLElement} element - 目标元素
     * @param {string} className - CSS类名
     * @returns {boolean} 是否添加了类
     */
    static toggleClass(element, className) {
        return element.classList.toggle(className);
    }
}

/**
 * HTTP请求工具类
 */
class HttpUtils {
    /**
     * 发送GET请求
     * @param {string} url - 请求URL
     * @param {Object} options - 请求选项
     * @returns {Promise} 响应Promise
     */
    static async get(url, options = {}) {
        return this.request(url, {
            method: 'GET',
            ...options
        });
    }
    
    /**
     * 发送POST请求
     * @param {string} url - 请求URL
     * @param {Object} data - 请求数据
     * @param {Object} options - 请求选项
     * @returns {Promise} 响应Promise
     */
    static async post(url, data, options = {}) {
        return this.request(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            body: JSON.stringify(data),
            ...options
        });
    }
    
    /**
     * 发送HTTP请求
     * @param {string} url - 请求URL
     * @param {Object} options - 请求选项
     * @returns {Promise} 响应Promise
     */
    static async request(url, options = {}) {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), API_CONFIG.TIMEOUT);
        
        try {
            const response = await fetch(url, {
                signal: controller.signal,
                ...options
            });
            
            clearTimeout(timeoutId);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            } else {
                return await response.text();
            }
        } catch (error) {
            clearTimeout(timeoutId);
            
            if (error.name === 'AbortError') {
                throw new Error('请求超时，请检查网络连接');
            }
            throw error;
        }
    }
}

/**
 * 事件总线工具类
 */
class EventBus {
    constructor() {
        this.events = {};
    }
    
    /**
     * 监听事件
     * @param {string} event - 事件名
     * @param {Function} callback - 回调函数
     */
    on(event, callback) {
        if (!this.events[event]) {
            this.events[event] = [];
        }
        this.events[event].push(callback);
    }
    
    /**
     * 移除事件监听
     * @param {string} event - 事件名
     * @param {Function} callback - 回调函数
     */
    off(event, callback) {
        if (this.events[event]) {
            this.events[event] = this.events[event].filter(cb => cb !== callback);
        }
    }
    
    /**
     * 触发事件
     * @param {string} event - 事件名
     * @param {*} data - 事件数据
     */
    emit(event, data) {
        if (this.events[event]) {
            this.events[event].forEach(callback => callback(data));
        }
    }
    
    /**
     * 一次性监听事件
     * @param {string} event - 事件名
     * @param {Function} callback - 回调函数
     */
    once(event, callback) {
        const wrapper = (data) => {
            callback(data);
            this.off(event, wrapper);
        };
        this.on(event, wrapper);
    }
}

/**
 * 防抖函数
 * @param {Function} func - 要防抖的函数
 * @param {number} wait - 等待时间（毫秒）
 * @param {boolean} immediate - 是否立即执行
 * @returns {Function} 防抖函数
 */
function debounce(func, wait, immediate = false) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            timeout = null;
            if (!immediate) func.apply(this, args);
        };
        const callNow = immediate && !timeout;
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
        if (callNow) func.apply(this, args);
    };
}

/**
 * 节流函数
 * @param {Function} func - 要节流的函数
 * @param {number} limit - 限制时间（毫秒）
 * @returns {Function} 节流函数
 */
function throttle(func, limit) {
    let inThrottle;
    return function executedFunction(...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// 导出工具类和函数
window.Utils = {
    API_CONFIG,
    TimeUtils,
    TextUtils,
    DomUtils,
    HttpUtils,
    EventBus,
    debounce,
    throttle
}; 