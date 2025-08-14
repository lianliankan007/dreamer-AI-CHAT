-- 创建提示词模板表 (SQLite版本)
CREATE TABLE prompt_templates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    model_provider VARCHAR(20),
    system_prompt TEXT,
    user_prefix VARCHAR(20) DEFAULT '用户：',
    assistant_prefix VARCHAR(20) DEFAULT '助手：',
    conversation_starter TEXT,
    max_context_length INTEGER DEFAULT 4000,
    temperature REAL DEFAULT 0.7,
    is_active BOOLEAN DEFAULT true,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_prompt_templates_type ON prompt_templates(type);
CREATE INDEX idx_prompt_templates_provider ON prompt_templates(model_provider);
CREATE INDEX idx_prompt_templates_active ON prompt_templates(is_active);
CREATE INDEX idx_prompt_templates_type_provider ON prompt_templates(type, model_provider);

-- 添加唯一约束
CREATE UNIQUE INDEX idx_prompt_templates_name_provider ON prompt_templates(name, model_provider); 