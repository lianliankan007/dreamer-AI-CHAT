-- 创建消息表 (SQLite版本)
CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    conversation_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    sequence_number INTEGER NOT NULL,
    token_count INTEGER,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    metadata TEXT,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_sequence ON messages(conversation_id, sequence_number);
CREATE INDEX idx_messages_timestamp ON messages(timestamp DESC);
CREATE INDEX idx_messages_type ON messages(message_type);

-- 添加唯一约束（同一对话中的序号唯一）
CREATE UNIQUE INDEX idx_messages_conversation_sequence ON messages(conversation_id, sequence_number); 