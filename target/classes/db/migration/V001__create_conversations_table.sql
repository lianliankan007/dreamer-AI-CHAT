-- 创建对话表
CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    model_provider VARCHAR(20) NOT NULL,
    model_name VARCHAR(50),
    user_id VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_conversations_user_id ON conversations(user_id);
CREATE INDEX idx_conversations_status ON conversations(status);
CREATE INDEX idx_conversations_updated_time ON conversations(updated_time DESC);
CREATE INDEX idx_conversations_user_status ON conversations(user_id, status);

-- 添加注释
COMMENT ON TABLE conversations IS '对话表';
COMMENT ON COLUMN conversations.id IS '主键ID';
COMMENT ON COLUMN conversations.title IS '对话标题';
COMMENT ON COLUMN conversations.model_provider IS '模型提供商';
COMMENT ON COLUMN conversations.model_name IS '具体模型名称';
COMMENT ON COLUMN conversations.user_id IS '用户ID';
COMMENT ON COLUMN conversations.status IS '对话状态：ACTIVE-活跃，ARCHIVED-归档，DELETED-已删除';
COMMENT ON COLUMN conversations.created_time IS '创建时间';
COMMENT ON COLUMN conversations.updated_time IS '更新时间'; 