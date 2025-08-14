-- 插入默认Prompt模板数据 (SQLite版本)

-- 千问聊天模板
INSERT INTO prompt_templates (
    name, type, model_provider, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, is_active, description
) VALUES (
    '千问默认聊天模板', 'CHAT', 'QIANWEN',
    '你是通义千问，由阿里云开发的AI助手。你要以友善、专业、准确的方式回答用户的问题。请确保回答简洁明了，同时提供有价值的信息。',
    '用户：', '千问：',
    4000, 0.7, true,
    '阿里巴巴千问的默认聊天模板，适用于日常对话'
);

-- 千问代码生成模板
INSERT INTO prompt_templates (
    name, type, model_provider, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, is_active, description
) VALUES (
    '千问代码生成模板', 'CODE_GENERATION', 'QIANWEN',
    '你是一个专业的编程助手。请根据用户需求提供高质量的代码解决方案。代码应该：
1. 遵循最佳实践和编程规范
2. 包含必要的注释
3. 考虑错误处理
4. 提供使用示例（如有必要）',
    '需求：', '代码：',
    6000, 0.3, true,
    '千问专用代码生成模板，优化代码质量和可读性'
);

-- 星火聊天模板
INSERT INTO prompt_templates (
    name, type, model_provider, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, is_active, description
) VALUES (
    '星火默认聊天模板', 'CHAT', 'XINGHUO',
    '你是讯飞星火认知大模型，由科大讯飞开发。请以专业、友好的态度为用户提供准确、有用的回答。',
    'Human: ', 'Assistant: ',
    4000, 0.8, true,
    '讯飞星火的默认聊天模板'
);

-- 豆包聊天模板
INSERT INTO prompt_templates (
    name, type, model_provider, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, is_active, description
) VALUES (
    '豆包默认聊天模板', 'CHAT', 'DOUBAO',
    '你是豆包，由字节跳动开发的AI助手。请为用户提供准确、有帮助的回答。',
    '用户：', '豆包：',
    4000, 0.7, true,
    '豆包的默认聊天模板'
);

-- DeepSeek聊天模板
INSERT INTO prompt_templates (
    name, type, model_provider, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, is_active, description
) VALUES (
    'DeepSeek默认聊天模板', 'CHAT', 'DEEPSEEK',
    '你是DeepSeek，一个专业的AI助手。请为用户提供准确、深入的回答。',
    'User: ', 'Assistant: ',
    4000, 0.7, true,
    'DeepSeek的默认聊天模板'
); 