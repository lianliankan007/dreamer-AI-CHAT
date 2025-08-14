-- 插入默认Prompt模板数据

-- 千问聊天模板
INSERT INTO prompt_templates (
    name, model_provider, prompt_type, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, max_tokens, enabled, priority, description, created_by
) VALUES (
    '千问默认聊天模板', 'QIANWEN', 'CHAT',
    '你是通义千问，由阿里云开发的AI助手。你要以友善、专业、准确的方式回答用户的问题。请确保回答简洁明了，同时提供有价值的信息。',
    '用户：', '千问：',
    4000, 0.7, 2000, true, 100,
    '阿里巴巴千问的默认聊天模板，适用于日常对话',
    'system'
);

-- 千问代码生成模板
INSERT INTO prompt_templates (
    name, model_provider, prompt_type, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, max_tokens, enabled, priority, description, created_by
) VALUES (
    '千问代码生成模板', 'QIANWEN', 'CODE_GENERATION',
    '你是一个专业的编程助手。请根据用户需求提供高质量的代码解决方案。代码应该：
1. 遵循最佳实践和编程规范
2. 包含必要的注释
3. 考虑错误处理
4. 提供使用示例（如有必要）',
    '需求：', '代码：',
    6000, 0.3, 3000, true, 90,
    '千问专用代码生成模板，优化代码质量和可读性',
    'system'
);

-- 星火聊天模板
INSERT INTO prompt_templates (
    name, model_provider, prompt_type, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, max_tokens, enabled, priority, description, created_by
) VALUES (
    '星火默认聊天模板', 'XINGHUO', 'CHAT',
    '你是讯飞星火认知大模型，由科大讯飞开发。请以专业、友好的态度为用户提供准确、有用的回答。',
    'Human: ', 'Assistant: ',
    4000, 0.8, 2000, true, 100,
    '讯飞星火的默认聊天模板',
    'system'
);

-- 星火分析模板
INSERT INTO prompt_templates (
    name, model_provider, prompt_type, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, max_tokens, enabled, priority, description, created_by
) VALUES (
    '星火分析模板', 'XINGHUO', 'ANALYSIS',
    '你是一个专业的分析师。请对用户提出的问题进行深入分析，提供：
1. 现象描述
2. 原因分析
3. 影响评估
4. 建议措施
请确保分析客观、全面、有条理。',
    '分析需求：', '分析结果：',
    5000, 0.5, 2500, true, 90,
    '星火专用分析模板，提供结构化分析',
    'system'
);

-- 豆包聊天模板
INSERT INTO prompt_templates (
    name, model_provider, prompt_type, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, max_tokens, enabled, priority, description, created_by
) VALUES (
    '豆包默认聊天模板', 'DOUBAO', 'CHAT',
    '你是豆包，由字节跳动开发的AI助手。请以活泼、友好的方式与用户交流，提供准确、有趣的回答。',
    '👤 ', '🤖 ',
    4000, 0.9, 2000, true, 100,
    '豆包的默认聊天模板，风格活泼友好',
    'system'
);

-- 豆包创意写作模板
INSERT INTO prompt_templates (
    name, model_provider, prompt_type, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, max_tokens, enabled, priority, description, created_by
) VALUES (
    '豆包创意写作模板', 'DOUBAO', 'CREATIVE_WRITING',
    '你是一个富有创意的写作助手。请发挥想象力，创作出生动有趣、富有创意的内容。写作时请注意：
1. 语言生动形象
2. 情节合理有趣
3. 人物性格鲜明
4. 主题积极正面',
    '创作要求：', '创作内容：',
    6000, 1.2, 3000, true, 90,
    '豆包专用创意写作模板，激发创造力',
    'system'
);

-- DeepSeek聊天模板
INSERT INTO prompt_templates (
    name, model_provider, prompt_type, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, max_tokens, enabled, priority, description, created_by
) VALUES (
    'DeepSeek默认聊天模板', 'DEEPSEEK', 'CHAT',
    'You are DeepSeek, a helpful AI assistant developed by DeepSeek AI. Please provide accurate, helpful, and detailed responses to user queries.',
    'Human: ', 'Assistant: ',
    8000, 0.7, 2000, true, 100,
    'DeepSeek默认聊天模板，支持长上下文',
    'system'
);

-- DeepSeek代码生成模板
INSERT INTO prompt_templates (
    name, model_provider, prompt_type, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, max_tokens, enabled, priority, description, created_by
) VALUES (
    'DeepSeek代码生成模板', 'DEEPSEEK', 'CODE_GENERATION',
    'You are an expert programmer and coding assistant. Provide high-quality code solutions that are:
1. Well-structured and following best practices
2. Properly commented and documented
3. Efficient and maintainable
4. Include error handling where appropriate
5. Provide usage examples when helpful',
    'Requirement: ', 'Code Solution: ',
    10000, 0.2, 4000, true, 90,
    'DeepSeek专用代码生成模板，支持复杂编程任务',
    'system'
);

-- 通用问答模板（适用于所有模型）
INSERT INTO prompt_templates (
    name, model_provider, prompt_type, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, max_tokens, enabled, priority, description, created_by
) VALUES 
('千问问答模板', 'QIANWEN', 'QA',
 '请简洁准确地回答用户的问题。如果问题涉及专业知识，请提供详细解释。',
 'Q: ', 'A: ', 3000, 0.3, 1500, true, 80, '千问问答模板', 'system'),

('星火问答模板', 'XINGHUO', 'QA',
 '请基于事实准确回答用户问题，提供清晰的解释和必要的背景信息。',
 'Q: ', 'A: ', 3000, 0.3, 1500, true, 80, '星火问答模板', 'system'),

('豆包问答模板', 'DOUBAO', 'QA',
 '请用通俗易懂的语言回答用户问题，确保答案准确且易于理解。',
 'Q: ', 'A: ', 3000, 0.3, 1500, true, 80, '豆包问答模板', 'system'),

('DeepSeek问答模板', 'DEEPSEEK', 'QA',
 'Please provide accurate and comprehensive answers to user questions with clear explanations.',
 'Q: ', 'A: ', 5000, 0.3, 1500, true, 80, 'DeepSeek问答模板', 'system');

-- 翻译模板
INSERT INTO prompt_templates (
    name, model_provider, prompt_type, system_prompt, user_prefix, assistant_prefix,
    max_context_length, temperature, max_tokens, enabled, priority, description, created_by
) VALUES 
('通用翻译模板', 'QIANWEN', 'TRANSLATION',
 '你是一个专业的翻译助手。请提供准确、自然、符合目标语言习惯的翻译。',
 '原文：', '译文：', 2000, 0.3, 1000, true, 90, '通用翻译模板', 'system'),

('星火翻译模板', 'XINGHUO', 'TRANSLATION',
 '请提供准确的翻译，保持原文的语调和风格。',
 '原文：', '译文：', 2000, 0.3, 1000, true, 90, '星火翻译模板', 'system'),

('豆包翻译模板', 'DOUBAO', 'TRANSLATION',
 '请提供自然流畅的翻译，确保语言地道。',
 '原文：', '译文：', 2000, 0.3, 1000, true, 90, '豆包翻译模板', 'system'),

('DeepSeek翻译模板', 'DEEPSEEK', 'TRANSLATION',
 'Please provide accurate and natural translations that maintain the original tone and meaning.',
 'Original: ', 'Translation: ', 3000, 0.3, 1000, true, 90, 'DeepSeek翻译模板', 'system'); 