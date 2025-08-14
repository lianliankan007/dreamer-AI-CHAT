-- 创建消息表
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    sequence_number INTEGER NOT NULL,
    token_count INTEGER,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata TEXT,
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_sequence ON messages(conversation_id, sequence_number);
CREATE INDEX idx_messages_timestamp ON messages(timestamp DESC);
CREATE INDEX idx_messages_type ON messages(message_type);
CREATE INDEX idx_messages_content_search ON messages USING gin(to_tsvector('chinese', content));

-- 添加唯一约束（同一对话中的序号唯一）
CREATE UNIQUE INDEX idx_messages_conversation_sequence ON messages(conversation_id, sequence_number);

-- 添加注释
COMMENT ON TABLE messages IS '消息表';
COMMENT ON COLUMN messages.id IS '主键ID';
COMMENT ON COLUMN messages.conversation_id IS '所属对话ID';
COMMENT ON COLUMN messages.content IS '消息内容';
COMMENT ON COLUMN messages.message_type IS '消息类型：USER-用户，ASSISTANT-助手，SYSTEM-系统';
COMMENT ON COLUMN messages.sequence_number IS '消息序号（在对话中的顺序）';
COMMENT ON COLUMN messages.token_count IS 'Token数量';
COMMENT ON COLUMN messages.timestamp IS '消息时间戳';
COMMENT ON COLUMN messages.metadata IS '额外的元数据（JSON格式）'; 