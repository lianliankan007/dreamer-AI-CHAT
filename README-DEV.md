# 开发环境配置说明

## 数据库切换：PostgreSQL → SQLite

为了方便本地开发和测试，项目现在支持使用SQLite数据库，无需安装PostgreSQL。

### 环境配置

#### 1. 使用SQLite（开发环境）

```bash
# 启动应用，使用SQLite数据库
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

或者设置环境变量：
```bash
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

#### 2. 使用PostgreSQL（生产环境）

```bash
# 启动应用，使用PostgreSQL数据库（默认）
mvn spring-boot:run
```

### 数据库文件位置

SQLite数据库文件将保存在：
```
~/.dreamer-ai-chat/dev-database.db
```

### 配置差异

| 配置项 | SQLite (dev) | PostgreSQL (prod) |
|--------|-------------|-------------------|
| 数据库驱动 | org.sqlite.JDBC | org.postgresql.Driver |
| Hibernate方言 | SQLiteDialect | PostgreSQLDialect |
| 迁移脚本 | db/migration-sqlite | db/migration |
| 数据文件 | 本地文件 | 服务器数据库 |

### 开发环境特性

- 更详细的日志输出
- SQL语句打印
- 增加的超时时间
- 自动创建数据库文件

### 数据迁移

- **SQLite版本**：使用 `db/migration-sqlite/` 目录下的脚本
- **PostgreSQL版本**：使用 `db/migration/` 目录下的脚本

### 注意事项

1. SQLite迁移脚本针对SQLite语法进行了优化
2. 去除了PostgreSQL特有的功能（如全文搜索索引）
3. 开发环境数据不会影响生产环境

### 清理开发数据

```bash
# 删除SQLite数据库文件
rm ~/.dreamer-ai-chat/dev-database.db
```

### 验证安装

查看数据库表：
```bash
sqlite3 ~/.dreamer-ai-chat/dev-database.db "SELECT name FROM sqlite_master WHERE type='table';"
```

查看默认模板：
```bash
sqlite3 ~/.dreamer-ai-chat/dev-database.db "SELECT name, model_provider FROM prompt_templates;"
```

### 故障排除

如果遇到启动问题：

1. 确认SQLite驱动已正确加载
2. 检查数据库文件目录权限
3. 查看日志中的Flyway迁移信息  
4. 验证配置文件中的数据源设置
5. 确保已创建 `~/.dreamer-ai-chat` 目录

#### 常见问题

**问题**: 目录不存在错误
```bash
mkdir -p ~/.dreamer-ai-chat
```

**问题**: API key错误
- 开发环境使用占位符 API key，无需真实的API密钥
- 生产环境需要配置真实的API密钥

### 🎉 成功状态

✅ SQLite数据库文件已创建: `~/.dreamer-ai-chat/dev-database.db`  
✅ 数据表迁移完成: `conversations`, `messages`, `prompt_templates`  
✅ 默认数据已插入: 5个预置的AI模型模板  
✅ 开发环境配置就绪 