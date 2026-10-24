CREATE TABLE app_users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(80) NOT NULL
);

CREATE TABLE auth_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES app_users(id)
);
CREATE INDEX idx_auth_tokens_expiry ON auth_tokens(expires_at);

CREATE TABLE content_items (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    category VARCHAR(40) NOT NULL,
    status VARCHAR(24) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);
CREATE INDEX idx_content_updated ON content_items(updated_at);

INSERT INTO content_items (title, summary, category, status, updated_at) VALUES
('欢迎使用启程工作台', '从这里开始，了解工作台的基本布局与使用方式。', '入门指南', 'PUBLISHED', '2026-10-24 09:30:00'),
('让想法有序发生', '把零散的想法整理成清晰的内容，让每一次记录都有迹可循。', '工作方法', 'PUBLISHED', '2026-10-23 14:20:00'),
('前后端协作指南', '了解独立部署、接口约定与本地代理，让团队协作更顺畅。', '开发文档', 'PUBLISHED', '2026-10-22 11:00:00'),
('新项目启动清单', '从环境配置到首次交付，为下一个项目做好准备。', '项目管理', 'DRAFT', '2026-10-21 16:45:00'),
('构建你的内容空间', '通过搜索与状态筛选，快速找到你需要的内容。', '入门指南', 'PUBLISHED', '2026-10-20 10:15:00'),
('团队知识库整理计划', '沉淀常见问题、实践经验与技术决策，建立可持续的知识积累。', '团队协作', 'DRAFT', '2026-10-19 15:00:00'),
('接口与数据结构说明', '账户鉴权、内容列表与错误响应的接口约定。', '开发文档', 'PUBLISHED', '2026-10-18 09:00:00'),
('本周工作回顾', '留出一点时间，回顾进展，记录收获，规划下一步。', '工作方法', 'DRAFT', '2026-10-17 17:30:00');
