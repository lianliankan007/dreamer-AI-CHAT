-- 创建prompt_templates表
CREATE TABLE prompt_templates (
    id BIGSERIAL PRIMARY KEY,
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
    enabled BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER DEFAULT 0,
    description TEXT,
    extra_config TEXT,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)
);

-- 创建索引
CREATE INDEX idx_prompt_templates_provider_type ON prompt_templates(model_provider, prompt_type);
CREATE INDEX idx_prompt_templates_enabled ON prompt_templates(enabled);
CREATE INDEX idx_prompt_templates_priority ON prompt_templates(priority DESC);
CREATE INDEX idx_prompt_templates_name ON prompt_templates(name);

-- 创建复合索引
CREATE INDEX idx_prompt_templates_provider_type_enabled_priority 
ON prompt_templates(model_provider, prompt_type, enabled, priority DESC);

-- 添加注释
COMMENT ON TABLE prompt_templates IS 'AI模型Prompt模板配置表';
COMMENT ON COLUMN prompt_templates.id IS '主键ID';
COMMENT ON COLUMN prompt_templates.name IS '模板名称';
COMMENT ON COLUMN prompt_templates.model_provider IS '模型提供商';
COMMENT ON COLUMN prompt_templates.prompt_type IS 'Prompt类型';
COMMENT ON COLUMN prompt_templates.system_prompt IS '系统提示词';
COMMENT ON COLUMN prompt_templates.user_prefix IS '用户消息前缀';
COMMENT ON COLUMN prompt_templates.assistant_prefix IS '助手消息前缀';
COMMENT ON COLUMN prompt_templates.conversation_starter IS '对话开始模板';
COMMENT ON COLUMN prompt_templates.max_context_length IS '最大上下文长度';
COMMENT ON COLUMN prompt_templates.temperature IS '温度参数';
COMMENT ON COLUMN prompt_templates.max_tokens IS '最大输出Token数';
COMMENT ON COLUMN prompt_templates.enabled IS '是否启用';
COMMENT ON COLUMN prompt_templates.priority IS '优先级';
COMMENT ON COLUMN prompt_templates.description IS '模板描述';
COMMENT ON COLUMN prompt_templates.extra_config IS '额外配置JSON';
COMMENT ON COLUMN prompt_templates.created_time IS '创建时间';
COMMENT ON COLUMN prompt_templates.updated_time IS '更新时间';
COMMENT ON COLUMN prompt_templates.created_by IS '创建者'; 