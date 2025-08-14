-- 创建对话表 (SQLite版本)
CREATE TABLE conversations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(200) NOT NULL,
    model_provider VARCHAR(20) NOT NULL,
    model_name VARCHAR(50),
    user_id VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_conversations_user_id ON conversations(user_id);
CREATE INDEX idx_conversations_status ON conversations(status);
CREATE INDEX idx_conversations_updated_time ON conversations(updated_time DESC);
CREATE INDEX idx_conversations_user_status ON conversations(user_id, status); 