# 项目管理工具 - 系统设计文档

## 1. 项目概述

通用项目管理工具，面向 10-50 人中型团队，支持软件研发及通用项目管理场景。采用混合管理模式（看板 + 列表 + 甘特图 + 日历），包含完整的任务管理、文档版本管理、报表统计功能。

## 2. 技术选型

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.15 + JDK 25 |
| ORM | Spring Data JPA |
| 数据库 | PostgreSQL |
| 缓存 | Redis |
| 文件存储 | MinIO |
| 认证 | Spring Security + JWT |
| 前端框架 | Vue 3 + TypeScript + Vite |
| UI 库 | Ant Design Vue（淡蓝色主题定制） |
| 状态管理 | Pinia |
| 图表 | ECharts |
| HTTP 客户端 | Axios |
| 日期处理 | dayjs |

## 3. 数据模型

### 3.1 用户 (user)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| username | VARCHAR(50) UNIQUE | 用户名 |
| email | VARCHAR(100) UNIQUE | 邮箱 |
| password | VARCHAR(255) | 加密密码 |
| avatar | VARCHAR(500) | 头像 URL |
| status | SMALLINT | 状态：0-禁用 1-启用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 3.2 工作空间 (workspace)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| name | VARCHAR(100) | 空间名称 |
| description | TEXT | 描述 |
| owner_id | BIGINT FK | 创建者 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 3.3 工作空间成员 (workspace_member)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| workspace_id | BIGINT FK | 工作空间 |
| user_id | BIGINT FK | 用户 |
| role | VARCHAR(20) | 角色：OWNER/ADMIN/MEMBER |

### 3.4 项目 (project)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| workspace_id | BIGINT FK | 工作空间 |
| name | VARCHAR(100) | 项目名称 |
| description | TEXT | 描述 |
| status | VARCHAR(20) | 状态：ACTIVE/ARCHIVED/COMPLETED |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| created_by | BIGINT FK | 创建者 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 3.5 项目成员 (project_member)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| project_id | BIGINT FK | 项目 |
| user_id | BIGINT FK | 用户 |
| role | VARCHAR(20) | 角色：ADMIN/MANAGER/MEMBER/READONLY |

### 3.6 任务 (task)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| project_id | BIGINT FK | 项目 |
| sprint_id | BIGINT FK nullable | 迭代 |
| parent_id | BIGINT FK nullable | 父任务（子任务支持） |
| title | VARCHAR(200) | 标题 |
| description | TEXT | 描述 |
| status | VARCHAR(20) | 状态：TODO/IN_PROGRESS/IN_REVIEW/DONE |
| priority | VARCHAR(10) | 优先级：LOW/MEDIUM/HIGH/URGENT |
| assignee_id | BIGINT FK nullable | 指派人 |
| creator_id | BIGINT FK | 创建者 |
| start_date | DATE | 开始日期 |
| due_date | DATE | 截止日期 |
| sort_order | INT | 看板排序 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 3.7 标签 (tag)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| project_id | BIGINT FK | 项目 |
| name | VARCHAR(50) | 标签名 |
| color | VARCHAR(7) | 颜色（HEX） |

### 3.8 任务标签关联 (task_tag)

| 字段 | 类型 | 说明 |
|------|------|------|
| task_id | BIGINT FK | 任务 |
| tag_id | BIGINT FK | 标签 |

### 3.9 评论 (comment)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| task_id | BIGINT FK | 任务 |
| user_id | BIGINT FK | 评论者 |
| content | TEXT | 内容 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 3.10 迭代 (sprint)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| project_id | BIGINT FK | 项目 |
| name | VARCHAR(100) | 迭代名称 |
| goal | TEXT | 迭代目标 |
| status | VARCHAR(20) | 状态：PLANNING/ACTIVE/COMPLETED |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 3.11 文档 (document)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| project_id | BIGINT FK | 项目 |
| title | VARCHAR(200) | 文档标题 |
| type | VARCHAR(20) | 类型：DESIGN/CUSTOMER/MEETING/OTHER |
| current_version | INT | 当前版本号 |
| created_by | BIGINT FK | 创建者 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 3.12 文档版本 (document_version)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| document_id | BIGINT FK | 文档 |
| version_no | INT | 版本号 |
| file_path | VARCHAR(500) | MinIO bucket/key |
| file_name | VARCHAR(200) | 原始文件名 |
| file_size | BIGINT | 文件大小（字节） |
| uploaded_by | BIGINT FK | 上传者 |
| change_summary | VARCHAR(500) | 变更说明 |
| created_at | TIMESTAMP | 上传时间 |

### 3.13 任务附件 (attachment)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| task_id | BIGINT FK | 任务 |
| file_name | VARCHAR(200) | 文件名 |
| file_path | VARCHAR(500) | MinIO bucket/key |
| file_size | BIGINT | 文件大小（字节） |
| uploaded_by | BIGINT FK | 上传者 |
| created_at | TIMESTAMP | 上传时间 |

### 3.14 活动日志 (activity_log)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| project_id | BIGINT FK | 项目 |
| task_id | BIGINT FK nullable | 任务 |
| user_id | BIGINT FK | 操作者 |
| action | VARCHAR(50) | 操作类型 |
| detail | TEXT | 详情（JSON） |
| created_at | TIMESTAMP | 操作时间 |

## 4. API 设计

### 4.1 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

分页响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

### 4.2 API 列表

**认证模块**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 登录 |
| POST | /api/auth/register | 注册 |
| POST | /api/auth/refresh | 刷新 Token |
| POST | /api/auth/logout | 登出 |

**用户模块**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/users/profile | 获取当前用户信息 |
| PUT | /api/users/profile | 更新用户信息 |
| PUT | /api/users/password | 修改密码 |
| POST | /api/users/avatar | 上传头像 |

**工作空间模块**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/workspaces | 工作空间列表 |
| POST | /api/workspaces | 创建工作空间 |
| GET | /api/workspaces/{id} | 工作空间详情 |
| PUT | /api/workspaces/{id} | 更新工作空间 |
| DELETE | /api/workspaces/{id} | 删除工作空间 |
| GET | /api/workspaces/{id}/members | 成员列表 |
| POST | /api/workspaces/{id}/members | 添加成员 |
| PUT | /api/workspaces/{id}/members/{uid} | 更新成员角色 |
| DELETE | /api/workspaces/{id}/members/{uid} | 移除成员 |

**项目模块**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/projects | 项目列表 |
| POST | /api/projects | 创建项目 |
| GET | /api/projects/{id} | 项目详情 |
| PUT | /api/projects/{id} | 更新项目 |
| DELETE | /api/projects/{id} | 删除项目 |
| GET | /api/projects/{id}/members | 项目成员列表 |
| POST | /api/projects/{id}/members | 添加项目成员 |
| PUT | /api/projects/{id}/members/{uid} | 更新成员角色 |
| DELETE | /api/projects/{id}/members/{uid} | 移除成员 |

**任务模块**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/projects/{pid}/tasks | 任务列表（支持筛选/排序/分页） |
| POST | /api/projects/{pid}/tasks | 创建任务 |
| GET | /api/tasks/{id} | 任务详情 |
| PUT | /api/tasks/{id} | 更新任务 |
| DELETE | /api/tasks/{id} | 删除任务 |
| PUT | /api/tasks/{id}/status | 更新任务状态 |
| PUT | /api/tasks/{id}/assignee | 指派任务 |
| PUT | /api/tasks/batch | 批量操作 |
| GET | /api/tasks/{id}/comments | 评论列表 |
| POST | /api/tasks/{id}/comments | 添加评论 |
| PUT | /api/comments/{id} | 更新评论 |
| DELETE | /api/comments/{id} | 删除评论 |
| POST | /api/tasks/{id}/attachments | 上传附件 |
| GET | /api/tasks/{id}/attachments | 附件列表 |
| DELETE | /api/attachments/{id} | 删除附件 |
| GET | /api/tasks/{id}/activities | 活动日志 |

**迭代模块**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/projects/{pid}/sprints | 迭代列表 |
| POST | /api/projects/{pid}/sprints | 创建迭代 |
| GET | /api/sprints/{id} | 迭代详情 |
| PUT | /api/sprints/{id} | 更新迭代 |
| DELETE | /api/sprints/{id} | 删除迭代 |

**文档模块**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/projects/{pid}/documents | 文档列表 |
| POST | /api/projects/{pid}/documents | 创建文档 |
| GET | /api/documents/{id} | 文档详情（含当前版本） |
| PUT | /api/documents/{id} | 更新文档信息 |
| DELETE | /api/documents/{id} | 删除文档 |
| POST | /api/documents/{id}/versions | 上传新版本 |
| GET | /api/documents/{id}/versions | 版本历史列表 |
| GET | /api/document-versions/{vid}/download | 下载指定版本 |
| GET | /api/document-versions/{vid}/preview | 预览（返回预签名 URL） |

**报表模块**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/reports/project/{id}/progress | 项目进度概览 |
| GET | /api/reports/project/{id}/task-distribution | 任务分布统计 |
| GET | /api/reports/sprint/{id}/burndown | 燃尽图数据 |
| GET | /api/reports/project/{id}/member-workload | 成员工作量 |
| GET | /api/reports/dashboard | 工作台汇总数据 |

## 5. 后端架构

### 5.1 分层结构

```
com.pm/
├── config/           # 配置类（Security、MinIO、Redis、CORS、JPA）
├── common/           # 统一响应、异常处理、常量
├── entity/           # 数据库实体（对应表）
├── dto/              # 请求/响应 DTO
│   ├── request/      # 请求 DTO
│   └── response/     # 响应 DTO
├── repository/       # JPA Repository 接口
├── service/          # 业务逻辑接口
│   └── impl/         # 业务逻辑实现
├── controller/       # REST 控制器
├── security/         # JWT 过滤器、认证服务
└── util/             # 工具类（MinIO、Redis）
```

### 5.2 认证流程

1. 用户登录 → 校验账号密码 → 生成 JWT Access Token（30min）+ Refresh Token（7d）
2. Access Token 存 Redis，Refresh Token 存 Redis
3. 请求携带 Access Token → JwtAuthenticationFilter 校验 → 放行
4. Token 过期 → 前端用 Refresh Token 换新 Access Token
5. 登出 → 删除 Redis 中对应 Token

### 5.3 文件存储

- MinIO Bucket：`project-docs`（项目文档）、`task-attachments`（任务附件）、`user-avatars`（用户头像）
- 上传流程：前端 → 后端 → MinIO，返回 key
- 下载/预览：后端生成预签名 URL（有效期 1 小时），前端直接访问

## 6. 前端架构

### 6.1 目录结构

```
frontend/src/
├── api/              # 按模块封装 API 请求
│   ├── auth.ts
│   ├── user.ts
│   ├── project.ts
│   ├── task.ts
│   ├── sprint.ts
│   ├── document.ts
│   └── report.ts
├── assets/           # 静态资源（图片、样式变量）
├── components/       # 通用组件
│   ├── TaskCard.vue       # 任务卡片
│   ├── CommentList.vue    # 评论列表
│   ├── FileUpload.vue     # 文件上传
│   └── ...
├── layouts/
│   └── MainLayout.vue     # 侧边栏 + 顶栏 + 内容区
├── router/
│   └── index.ts           # 路由配置 + 权限守卫
├── stores/           # Pinia Store
│   ├── auth.ts
│   ├── project.ts
│   └── task.ts
├── views/            # 页面
│   ├── auth/
│   │   ├── Login.vue
│   │   └── Register.vue
│   ├── dashboard/
│   │   └── Index.vue       # 工作台
│   ├── project/
│   │   ├── List.vue        # 项目列表
│   │   └── Detail.vue      # 项目详情（Tab 切换视图）
│   ├── task/
│   │   └── Detail.vue      # 任务详情（抽屉）
│   ├── document/
│   │   └── Index.vue       # 文档中心
│   ├── report/
│   │   └── Index.vue       # 报表统计
│   └── settings/
│       └── Index.vue       # 系统设置
├── types/            # TypeScript 类型定义
├── utils/
│   ├── request.ts          # Axios 实例（拦截器、Token 注入）
│   └── ...
├── App.vue
└── main.ts
```

### 6.2 主题配色

```css
:root {
  --color-primary: #5B9BD5;       /* 主色 - 淡蓝 */
  --color-primary-light: #E8F1FB; /* 浅蓝背景 */
  --color-bg: #F0F5FF;            /* 页面背景 */
  --color-bg-white: #FFFFFF;      /* 卡片/侧边栏 */
  --color-accent: #1890FF;        /* 交互强调 */
  --color-success: #52C41A;
  --color-warning: #FAAD14;
  --color-error: #FF4D4F;
}
```

### 6.3 核心页面

**工作台 Dashboard**：任务概览卡片（待办/进行中/已完成数量）、我的待办列表、参与的项目进度条、近期活动流。

**项目详情**：顶部 Tab 切换四种视图——
- 看板视图：四列拖拽（待办/进行中/待审核/已完成），任务卡支持拖拽排序
- 列表视图：表格形式，支持筛选排序分页
- 甘特图视图：任务时间条，支持依赖关系展示，可拖拽调整日期
- 日历视图：按月展示任务，根据截止日期标注

**文档中心**：左侧树形目录（按类型分组），右侧文档列表，上传新版本弹出对话框填写变更说明，侧边面板查看版本历史。

**报表统计**：ECharts 图表——项目进度饼图、任务状态分布柱状图、燃尽图折线图、成员工作量横向柱状图。

## 7. 工程结构总览

```
project-management/
├── backend/                # Spring Boot 后端
│   ├── src/main/java/com/pm/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/
│   └── pom.xml
├── frontend/               # Vue 3 前端
│   ├── src/
│   ├── package.json
│   └── vite.config.ts
└── docs/                   # 项目文档
```
