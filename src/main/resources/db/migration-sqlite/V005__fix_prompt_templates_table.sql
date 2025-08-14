-- 修复prompt_templates表结构，使其与实体类匹配

-- 1. 删除旧表（如果存在）
DROP TABLE IF EXISTS prompt_templates;

-- 2. 重新创建表，字段与实体类完全匹配
CREATE TABLE prompt_templates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    model_provider VARCHAR(20) NOT NULL,
    prompt_type VARCHAR(30) NOT NULL,
    system_prompt TEXT,
    user_prefix VARCHAR(50),
    assistant_prefix VARCHAR(50),
    conversation_starter TEXT,
    max_context_length INTEGER,
    temperature DECIMAL(3,2),
    max_tokens INTEGER,
    enabled BOOLEAN NOT NULL DEFAULT 1,
    priority INTEGER DEFAULT 0,
    description TEXT,
    extra_config TEXT,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)
);

-- 3. 创建索引
CREATE INDEX idx_prompt_templates_provider_type ON prompt_templates(model_provider, prompt_type);
CREATE INDEX idx_prompt_templates_enabled ON prompt_templates(enabled);
CREATE INDEX idx_prompt_templates_priority ON prompt_templates(priority DESC);
CREATE INDEX idx_prompt_templates_name ON prompt_templates(name);

-- 4. 创建复合索引
CREATE INDEX idx_prompt_templates_provider_type_enabled_priority 
ON prompt_templates(model_provider, prompt_type, enabled, priority DESC);

-- 5. 创建唯一索引
CREATE UNIQUE INDEX idx_prompt_templates_name_provider ON prompt_templates(name, model_provider); 