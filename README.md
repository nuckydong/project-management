# 项目管理系统（ProjectHub）

面向中小型团队的通用项目管理工具，支持看板、列表、甘特图、日历多视图切换，包含任务管理、文档版本管理、报表统计等完整功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.5 + JDK 25 + Spring Security + JWT |
| 数据库 | PostgreSQL + Spring Data JPA |
| 缓存 | Redis |
| 文件存储 | MinIO |
| 前端 | Vue 3 + TypeScript + Vite |
| UI 组件库 | Ant Design Vue |
| 状态管理 | Pinia |
| 图表 | ECharts |

## 功能概览

- **工作台**：任务统计卡片、我的待办、项目进度、近期活动
- **项目管理**：创建/编辑项目，看板/列表/甘特图/日历四种视图
- **任务管理**：创建任务、指派负责人、状态流转、优先级、评论、附件
- **文档中心**：按项目归档文档，支持多版本上传、下载、预览
- **报表统计**：项目进度、任务分布、燃尽图、成员工作量
- **工作空间**：超级管理员管理工作空间及成员
- **用户管理**：超级管理员管理系统用户（增删改查、重置密码）
- **权限控制**：超级管理员（admin）独占用户管理/工作空间管理，工作空间管理员可管理项目成员及角色

## 项目结构

```
project-management/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/pm/
│   │   ├── config/             # 配置（Security、MinIO、Redis、CORS、全局异常、日志拦截）
│   │   ├── common/             # 统一响应 Result、全局异常处理、常量
│   │   ├── entity/             # JPA 实体
│   │   ├── dto/request/        # 请求 DTO
│   │   ├── dto/response/       # 响应 DTO
│   │   ├── repository/         # JPA Repository
│   │   ├── service/impl/       # 业务逻辑
│   │   ├── controller/         # REST 控制器
│   │   ├── security/           # JWT 过滤器、Token 生成
│   │   └── util/               # MinIO 工具类
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── api/                # 按模块封装的 API 请求
│   │   ├── assets/styles/      # 主题样式
│   │   ├── components/         # 通用组件（看板、任务列表、甘特图、日历、任务抽屉等）
│   │   ├── layouts/            # 主布局（侧边栏 + 顶栏）
│   │   ├── router/             # 路由 + 权限守卫
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── types/              # TypeScript 类型定义
│   │   └── views/              # 页面组件
│   ├── nginx.conf              # Nginx 配置（SPA 路由 + API 代理）
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml          # 一键部署编排
└── README.md
```

## 快速开始

### 环境依赖

- JDK 25+
- Node.js 22+
- PostgreSQL 16+
- Redis 7+
- MinIO

### 本地开发

**1. 启动基础设施**

确保 PostgreSQL、Redis、MinIO 已启动，创建数据库：

```sql
CREATE DATABASE project_management;
```

**2. 启动后端**

```bash
cd backend
# 修改 src/main/resources/application.yml 中的数据库、Redis、MinIO 连接信息
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`，首次启动会自动：
- 初始化 MinIO 存储桶（project-docs、task-attachments、user-avatars）
- 创建超级管理员账户（admin / admin149162536）

**3. 启动前端**

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:3000`，开发模式自动代理 `/api` 到后端。

### Docker 部署

修改 `docker-compose.yml` 中的密码等配置，然后：

```bash
docker-compose up --build -d
```

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 80 | Nginx 提供静态文件 + API 代理 |
| 后端 | 8080 | Spring Boot |
| PostgreSQL | 5432 | 数据库 |
| Redis | 6379 | 缓存 |
| MinIO | 9000 / 9001 | 对象存储 / 管理控制台 |

访问 `http://localhost` 即可使用。

## 权限体系

```
超级管理员（admin）
  ├── 用户管理：创建/编辑/删除/禁用 系统用户，重置密码
  ├── 工作空间管理：创建工作空间，添加系统用户到工作空间并分配角色
  │
  └── 工作空间管理员
        ├── 创建项目
        ├── 从工作空间成员中选择人员加入项目，分配项目角色
        │     ├── 管理员：管理项目成员和设置
        │     ├── 成员：创建和管理任务
        │     └── 查看者：只读访问
        └── 管理项目内任务、文档、报表
```

## API 概览

### 统一响应格式

```json
// 成功
{ "code": 200, "message": "success", "data": {} }

// 失败
{ "code": -1, "message": "错误描述", "data": null }
```

### 主要接口

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | `/api/auth/login` | 登录 |
| | POST | `/api/auth/register` | 注册 |
| | POST | `/api/auth/refresh` | 刷新 Token |
| | POST | `/api/auth/logout` | 登出 |
| 用户 | GET | `/api/users/profile` | 当前用户信息 |
| | PUT | `/api/users/profile` | 更新个人信息 |
| | POST | `/api/users/avatar` | 上传头像 |
| | PUT | `/api/users/password` | 修改密码 |
| 用户管理 | GET | `/api/users/list` | 用户列表（管理员） |
| | POST | `/api/users/create` | 创建用户 |
| | PUT | `/api/users/{id}/edit` | 编辑用户 |
| | DELETE | `/api/users/{id}` | 删除用户 |
| | PUT | `/api/users/{id}/status` | 修改状态 |
| | PUT | `/api/users/{id}/reset-password` | 重置密码 |
| 用户搜索 | GET | `/api/users/search?keyword=` | 搜索用户 |
| 工作空间 | GET | `/api/workspaces` | 工作空间列表 |
| | POST | `/api/workspaces` | 创建工作空间 |
| | GET/PUT/DELETE | `/api/workspaces/{id}` | 工作空间 CRUD |
| | GET | `/api/workspaces/{id}/members` | 成员列表 |
| | POST | `/api/workspaces/{id}/members` | 添加成员 |
| | PUT | `/api/workspaces/{id}/members/{uid}` | 修改角色 |
| | DELETE | `/api/workspaces/{id}/members/{uid}` | 移除成员 |
| 项目 | GET | `/api/projects` | 项目列表 |
| | POST | `/api/projects` | 创建项目 |
| | GET/PUT/DELETE | `/api/projects/{id}` | 项目 CRUD |
| | GET/POST | `/api/projects/{id}/members` | 项目成员管理 |
| 任务 | GET | `/api/projects/{pid}/tasks` | 任务列表 |
| | POST | `/api/projects/{pid}/tasks` | 创建任务 |
| | GET/PUT/DELETE | `/api/tasks/{id}` | 任务 CRUD |
| | PUT | `/api/tasks/{id}/status` | 更新状态 |
| | PUT | `/api/tasks/{id}/assignee` | 指派任务 |
| | GET | `/api/my/tasks` | 我的任务 |
| 评论 | GET/POST | `/api/tasks/{id}/comments` | 评论 CRUD |
| 附件 | GET/POST | `/api/tasks/{id}/attachments` | 附件管理 |
| 迭代 | GET/POST | `/api/projects/{pid}/sprints` | 迭代管理 |
| 文档 | GET/POST | `/api/projects/{pid}/documents` | 文档管理 |
| | POST | `/api/documents/{id}/versions` | 上传新版本 |
| | GET | `/api/documents/{id}/versions` | 版本历史 |
| 报表 | GET | `/api/reports/dashboard` | 工作台数据 |
| | GET | `/api/reports/project/{id}/progress` | 项目进度 |
| | GET | `/api/reports/project/{id}/task-distribution` | 任务分布 |
| | GET | `/api/reports/sprint/{id}/burndown` | 燃尽图 |
| | GET | `/api/reports/project/{id}/member-workload` | 成员工作量 |

## 配置说明

后端配置文件 `backend/src/main/resources/application.yml`：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/project_management
    username: postgres
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password

minio:
  endpoint: http://localhost:9000
  access-key: minio
  secret-key: your_minio_secret
  buckets-docs: project-docs
  buckets-attachments: task-attachments
  buckets-avatars: user-avatars

jwt:
  secret: your-jwt-secret-key-must-be-long-enough
  access-token-expiration: 1800000    # 30 分钟
  refresh-token-expiration: 604800000 # 7 天
```