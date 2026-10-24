# 启程 · 前后端分离脚手架

Vue 3 + TypeScript + Element Plus / Java 21 + Spring Boot 3.5 + Spring Security + MySQL 8.4。

仅包含两个路由页面：**登录页** `/login` 和 **内容页** `/content`。前后端独立构建、独立运行，通过 `/api` REST 接口通信；开发时由 Vite 代理，容器部署时由 Nginx 代理。

## 已实现

- 登录表单、输入校验、密码显隐、错误提示。
- BCrypt 密码存储、8 小时 Bearer 令牌、过期校验、退出时服务端撤销令牌。
- 刷新后恢复当前用户、前端路由守卫、后端接口鉴权、401 自动返回登录。
- MySQL 内容列表、标题搜索、状态筛选、服务端分页、统计卡片、摘要弹窗。
- 加载、空结果、请求失败及重试状态，桌面与手机布局。
- Flyway 自动建表及 8 条示例内容；开发环境首次启动创建体验账号。
- Maven Wrapper、前端锁文件、Docker Compose、后端集成测试与浏览器端到端测试。

内容页目前提供查询与摘要查看；新增、编辑、删除、注册和找回密码留作业务扩展。

## 一键启动

需要 Docker Engine 和 Docker Compose v2。首次启动会拉取镜像、下载依赖并构建应用。

```bash
cp .env.example .env
docker compose up -d --build --wait
```

在本机浏览器访问端口 **8088**，默认进入登录页。开发体验账号：`admin` / `Admin@123456`。

```bash
docker compose logs -f backend    # 查看后端日志
docker compose down              # 停止；保留 MySQL 数据卷
```

`.env.example` 中的密码仅供本地体验。对外部署前修改数据库与管理员密码，配置 HTTPS，并将 `APP_BOOTSTRAP_ENABLED` 关闭。管理员只在账号不存在时创建；修改环境变量不会重置已有账号密码。已有用户需通过后续账户管理功能或经过验证的维护流程修改密码。

## 本地开发（热更新）

需要 **JDK 21（含 javac）**、**Node.js 22.12+**、Docker Compose v2。Maven Wrapper 会自动安装经校验的 Maven 3.9.11，无需全局 Maven。

先在仓库根目录准备 MySQL：

```bash
cp .env.example .env  # 已存在时不要覆盖
docker compose up -d --wait mysql
```

终端一：

```bash
cd backend
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run
```

终端二：

```bash
cd frontend
npm ci
npm run dev
```

前端开发端口 **5173**，后端端口 **8080**。后端默认 `dev` profile，与示例 `.env` 使用相同的本地数据库配置。

`docker compose` 会读取根目录 `.env`；直接执行 Java/Maven 不会自动加载该文件。如果修改了数据库密码或用户名，需要在启动后端的终端中显式设置同名环境变量。

如果使用现有 MySQL，请先创建 `workspace` 数据库及有建表权限的专用用户，再设置 `DB_URL`、`DB_USERNAME` 和 `DB_PASSWORD`。不需要手动导入 SQL，Flyway 会管理版本迁移。

## 配置

| 变量 | 默认值 / 用途 |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/workspace?useUnicode=true&characterEncoding=UTF-8&connectionTimeZone=UTC` |
| `DB_USERNAME` / `DB_PASSWORD` | `workspace` / `workspace_local`，仅用于本地开发 |
| `SERVER_PORT` | `8080` |
| `SPRING_PROFILES_ACTIVE` | 本地默认 `dev`；部署设置 `prod` |
| `APP_CORS_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173`；独立域名部署时填写前端来源，逗号分隔 |
| `APP_TOKEN_HOURS` | `8`，允许范围 1–168 小时 |
| `APP_BOOTSTRAP_ENABLED` | `dev` 默认 `true`，其他 profile 默认 `false` |
| `APP_ADMIN_USERNAME` / `APP_ADMIN_PASSWORD` | 首次创建账号；启用初始化时密码至少 12 字符、最多 72 UTF-8 字节 |
| `API_PROXY_TARGET` | Vite 代理目标，默认 `http://127.0.0.1:8080` |

前端只把令牌保存在 `sessionStorage`，不持久保存密码；关闭该标签页后需重新登录。数据库只保存令牌的 SHA-256 哈希，不保存令牌原文。接口采用显式 Bearer 鉴权，不使用 Cookie 登录状态。令牌过期后不能访问接口；过期记录会在下一次成功登录时清理。

生产环境需要 HTTPS。此脚手架尚未实现多租户、角色权限、登录限流和密码重置；接入公网前根据业务补齐。所有已登录账号共享示例内容空间。

## API

请求体和响应体均为 JSON。除健康检查、登录外，接口需要 `Authorization: Bearer <token>`。

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| GET | `/api/health` | 检查应用与数据库连接，成功返回 `{"status":"UP"}` |
| POST | `/api/auth/login` | 请求 `{"username":"admin","password":"…"}`；返回 `token`、`expiresAt`、`user` |
| GET | `/api/auth/me` | 返回 `id`、`username`、`displayName` |
| POST | `/api/auth/logout` | 撤销当前令牌，返回 204 |
| GET | `/api/contents` | `q` 标题关键词、`status`（`PUBLISHED` / `DRAFT`）、`page`（从 0 开始）、`size`（1–100，默认 6） |

内容响应为 `{items, total, page, size, stats: {total, published, draft}}`，统计值为整个内容空间的数量。常规错误格式为 `{code, message}`，使用 HTTP 400 / 401 / 403 状态码。

## 验证

后端测试独立使用 H2 MySQL 兼容模式，执行真实的 Flyway 迁移、Spring Security 和数据库查询，无需先启动 MySQL：

```bash
cd backend
./mvnw verify
```

前端构建及浏览器测试：

```bash
cd frontend
npm ci
npm run build
npx playwright install chromium
# 先启动 MySQL 和后端；Playwright 会自动启动或复用 Vite
npm run test:e2e
```

浏览器测试覆盖桌面和手机：错误密码、登录、查询、筛选、分页、摘要弹窗、刷新恢复、退出撤销令牌、未登录和失效令牌重定向。默认使用体验账号，可通过 `E2E_USERNAME`、`E2E_PASSWORD` 覆盖。已有系统 Chromium 可设置 `PLAYWRIGHT_CHROMIUM_EXECUTABLE` 跳过浏览器下载。

## 目录

```text
backend/
  src/main/java/com/qicheng/workspace/
    auth/       # 登录、令牌生成/撤销、鉴权过滤器
    config/     # Spring Security、CORS
    content/    # 内容实体、查询接口
    user/       # 用户、初始账号
    web/        # 健康检查、错误处理
  src/main/resources/db/migration/  # Flyway 数据库迁移
  src/test/     # 后端集成测试
frontend/
  src/views/    # LoginView / ContentView
  src/stores/   # 轻量用户状态
  src/api.ts    # 请求与错误处理
  src/router.ts # 路由与鉴权守卫
  tests/        # Playwright 测试
compose.yaml    # MySQL / Spring Boot / Nginx 独立容器
```

原有 `.agents/` 技能资料保留，与应用运行无依赖关系。
