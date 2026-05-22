# 项目管理工具 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个全栈通用项目管理工具，支持看板/列表/甘特图/日历四种视图，包含任务管理、文档版本管理、报表统计等完整功能。

**Architecture:** 单体架构，Spring Boot 3.15 后端提供 REST API，Vue 3 SPA 前端通过 Axios 调用。JWT 认证，Redis 缓存 Token，MinIO 存储文件，PostgreSQL 持久化数据。

**Tech Stack:** JDK 25, Spring Boot 3.15, Spring Data JPA, PostgreSQL, Redis, MinIO, Spring Security + JWT, Vue 3 + TypeScript + Vite, Ant Design Vue, Pinia, ECharts, Axios, dayjs

---

## File Structure

### Backend Files

```
backend/
├── pom.xml
├── src/main/java/com/pm/
│   ├── PmApplication.java
│   ├── common/
│   │   ├── Result.java                    # 统一响应封装
│   │   ├── PageResult.java                # 分页响应封装
│   │   ├── GlobalExceptionHandler.java    # 全局异常处理
│   │   └── Constants.java                 # 常量定义
│   ├── config/
│   │   ├── SecurityConfig.java            # Spring Security 配置
│   │   ├── CorsConfig.java                # CORS 跨域配置
│   │   ├── RedisConfig.java               # Redis 序列化配置
│   │   └── JpaConfig.java                 # JPA 审计配置
│   ├── entity/
│   │   ├── BaseEntity.java                # 基础实体（id, createdAt, updatedAt）
│   │   ├── User.java
│   │   ├── Workspace.java
│   │   ├── WorkspaceMember.java
│   │   ├── Project.java
│   │   ├── ProjectMember.java
│   │   ├── Task.java
│   │   ├── Tag.java
│   │   ├── TaskTag.java
│   │   ├── Comment.java
│   │   ├── Sprint.java
│   │   ├── Document.java
│   │   ├── DocumentVersion.java
│   │   ├── Attachment.java
│   │   └── ActivityLog.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── WorkspaceRepository.java
│   │   ├── WorkspaceMemberRepository.java
│   │   ├── ProjectRepository.java
│   │   ├── ProjectMemberRepository.java
│   │   ├── TaskRepository.java
│   │   ├── TagRepository.java
│   │   ├── TaskTagRepository.java
│   │   ├── CommentRepository.java
│   │   ├── SprintRepository.java
│   │   ├── DocumentRepository.java
│   │   ├── DocumentVersionRepository.java
│   │   ├── AttachmentRepository.java
│   │   └── ActivityLogRepository.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── RefreshTokenRequest.java
│   │   │   ├── UserUpdateRequest.java
│   │   │   ├── PasswordChangeRequest.java
│   │   │   ├── WorkspaceCreateRequest.java
│   │   │   ├── WorkspaceUpdateRequest.java
│   │   │   ├── MemberAddRequest.java
│   │   │   ├── MemberRoleUpdateRequest.java
│   │   │   ├── ProjectCreateRequest.java
│   │   │   ├── ProjectUpdateRequest.java
│   │   │   ├── TaskCreateRequest.java
│   │   │   ├── TaskUpdateRequest.java
│   │   │   ├── TaskStatusUpdateRequest.java
│   │   │   ├── TaskAssignRequest.java
│   │   │   ├── TaskBatchRequest.java
│   │   │   ├── CommentCreateRequest.java
│   │   │   ├── CommentUpdateRequest.java
│   │   │   ├── SprintCreateRequest.java
│   │   │   ├── SprintUpdateRequest.java
│   │   │   ├── DocumentCreateRequest.java
│   │   │   ├── DocumentUpdateRequest.java
│   │   │   ├── DocumentVersionUploadRequest.java
│   │   │   └── TaskQueryRequest.java
│   │   └── response/
│   │       ├── AuthResponse.java
│   │       ├── UserResponse.java
│   │       ├── WorkspaceResponse.java
│   │       ├── WorkspaceMemberResponse.java
│   │       ├── ProjectResponse.java
│   │       ├── ProjectMemberResponse.java
│   │       ├── TaskResponse.java
│   │       ├── CommentResponse.java
│   │       ├── SprintResponse.java
│   │       ├── DocumentResponse.java
│   │       ├── DocumentVersionResponse.java
│   │       ├── AttachmentResponse.java
│   │       ├── ActivityLogResponse.java
│   │       ├── DashboardResponse.java
│   │       ├── ProjectProgressResponse.java
│   │       ├── TaskDistributionResponse.java
│   │       ├── BurndownResponse.java
│   │       └── MemberWorkloadResponse.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── WorkspaceService.java
│   │   ├── ProjectService.java
│   │   ├── TaskService.java
│   │   ├── CommentService.java
│   │   ├── SprintService.java
│   │   ├── DocumentService.java
│   │   ├── AttachmentService.java
│   │   ├── ActivityLogService.java
│   │   ├── MinioService.java
│   │   └── ReportService.java
│   ├── service/impl/
│   │   ├── AuthServiceImpl.java
│   │   ├── UserServiceImpl.java
│   │   ├── WorkspaceServiceImpl.java
│   │   ├── ProjectServiceImpl.java
│   │   ├── TaskServiceImpl.java
│   │   ├── CommentServiceImpl.java
│   │   ├── SprintServiceImpl.java
│   │   ├── DocumentServiceImpl.java
│   │   ├── AttachmentServiceImpl.java
│   │   ├── ActivityLogServiceImpl.java
│   │   ├── MinioServiceImpl.java
│   │   └── ReportServiceImpl.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── WorkspaceController.java
│   │   ├── ProjectController.java
│   │   ├── TaskController.java
│   │   ├── CommentController.java
│   │   ├── SprintController.java
│   │   ├── DocumentController.java
│   │   ├── AttachmentController.java
│   │   ├── ActivityLogController.java
│   │   └── ReportController.java
│   ├── security/
│   │   ├── JwtTokenProvider.java          # JWT 生成/解析
│   │   ├── JwtAuthenticationFilter.java   # JWT 过滤器
│   │   └── CustomUserDetailsService.java  # 用户加载
│   └── util/
│       └── MinioUtil.java                 # MinIO 操作封装
├── src/main/resources/
│   ├── application.yml
│   └── db/
│       └── init.sql                       # 数据库初始化脚本
```

### Frontend Files

```
frontend/
├── package.json
├── tsconfig.json
├── vite.config.ts
├── index.html
├── src/
│   ├── main.ts
│   ├── App.vue
│   ├── api/
│   │   ├── request.ts               # Axios 实例和拦截器
│   │   ├── auth.ts
│   │   ├── user.ts
│   │   ├── workspace.ts
│   │   ├── project.ts
│   │   ├── task.ts
│   │   ├── sprint.ts
│   │   ├── document.ts
│   │   └── report.ts
│   ├── types/
│   │   └── index.ts                  # 全局 TypeScript 类型
│   ├── stores/
│   │   ├── auth.ts
│   │   ├── project.ts
│   │   └── task.ts
│   ├── router/
│   │   └── index.ts
│   ├── layouts/
│   │   └── MainLayout.vue
│   ├── components/
│   │   ├── TaskCard.vue
│   │   ├── TaskDrawer.vue            # 任务详情抽屉
│   │   ├── CommentList.vue
│   │   ├── FileUpload.vue
│   │   ├── KanbanBoard.vue
│   │   ├── TaskList.vue
│   │   ├── GanttChart.vue
│   │   ├── CalendarView.vue
│   │   └── DocumentVersionPanel.vue
│   ├── views/
│   │   ├── auth/
│   │   │   ├── Login.vue
│   │   │   └── Register.vue
│   │   ├── dashboard/
│   │   │   └── Index.vue
│   │   ├── project/
│   │   │   ├── List.vue
│   │   │   └── Detail.vue
│   │   ├── document/
│   │   │   └── Index.vue
│   │   ├── report/
│   │   │   └── Index.vue
│   │   └── settings/
│   │       └── Index.vue
│   └── assets/
│       └── styles/
│           └── theme.less            # 淡蓝色主题变量
```

---

## Task 1: 后端项目初始化

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/pm/PmApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/db/init.sql`

- [ ] **Step 1: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.0</version>
        <relativePath/>
    </parent>
    <groupId>com.pm</groupId>
    <artifactId>project-management</artifactId>
    <version>1.0.0</version>
    <name>project-management</name>

    <properties>
        <java.version>25</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>io.minio</groupId>
            <artifactId>minio</artifactId>
            <version>8.5.14</version>
        </dependency>

        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.6</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建启动类**

```java
package com.pm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PmApplication {
    public static void main(String[] args) {
        SpringApplication.run(PmApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建 application.yml**

```yaml
server:
  port: 8080

spring:
  application:
    name: project-management
  datasource:
    url: jdbc:postgresql://localhost:5432/pm_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  data:
    redis:
      host: localhost
      port: 6379
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  buckets:
    docs: project-docs
    attachments: task-attachments
    avatars: user-avatars

jwt:
  secret: your-256-bit-secret-key-for-project-management-tool-must-be-long-enough
  access-token-expiration: 1800000
  refresh-token-expiration: 604800000

logging:
  level:
    com.pm: DEBUG
```

- [ ] **Step 4: 创建数据库初始化脚本**

```sql
-- init.sql 用于手动初始化（JPA ddl-auto: update 会自动建表，此脚本作为备用参考）

-- 用户表
CREATE TABLE IF NOT EXISTS user_table (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(500),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 工作空间表
CREATE TABLE IF NOT EXISTS workspace (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id BIGINT NOT NULL REFERENCES user_table(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 工作空间成员表
CREATE TABLE IF NOT EXISTS workspace_member (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspace(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES user_table(id),
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    UNIQUE(workspace_id, user_id)
);

-- 项目表
CREATE TABLE IF NOT EXISTS project (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspace(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    start_date DATE,
    end_date DATE,
    created_by BIGINT NOT NULL REFERENCES user_table(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 项目成员表
CREATE TABLE IF NOT EXISTS project_member (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES user_table(id),
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    UNIQUE(project_id, user_id)
);

-- 标签表
CREATE TABLE IF NOT EXISTS tag (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) NOT NULL
);

-- 迭代表
CREATE TABLE IF NOT EXISTS sprint (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    goal TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 任务表
CREATE TABLE IF NOT EXISTS task (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    sprint_id BIGINT REFERENCES sprint(id),
    parent_id BIGINT REFERENCES task(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    assignee_id BIGINT REFERENCES user_table(id),
    creator_id BIGINT NOT NULL REFERENCES user_table(id),
    start_date DATE,
    due_date DATE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 任务标签关联表
CREATE TABLE IF NOT EXISTS task_tag (
    task_id BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, tag_id)
);

-- 评论表
CREATE TABLE IF NOT EXISTS comment (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES user_table(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 文档表
CREATE TABLE IF NOT EXISTS document (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    current_version INT NOT NULL DEFAULT 0,
    created_by BIGINT NOT NULL REFERENCES user_table(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 文档版本表
CREATE TABLE IF NOT EXISTS document_version (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES document(id) ON DELETE CASCADE,
    version_no INT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL REFERENCES user_table(id),
    change_summary VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 任务附件表
CREATE TABLE IF NOT EXISTS attachment (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
    file_name VARCHAR(200) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL REFERENCES user_table(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 活动日志表
CREATE TABLE IF NOT EXISTS activity_log (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    task_id BIGINT REFERENCES task(id) ON DELETE SET NULL,
    user_id BIGINT NOT NULL REFERENCES user_table(id),
    action VARCHAR(50) NOT NULL,
    detail TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_task_project ON task(project_id);
CREATE INDEX IF NOT EXISTS idx_task_assignee ON task(assignee_id);
CREATE INDEX IF NOT EXISTS idx_task_status ON task(status);
CREATE INDEX IF NOT EXISTS idx_task_sprint ON task(sprint_id);
CREATE INDEX IF NOT EXISTS idx_comment_task ON comment(task_id);
CREATE INDEX IF NOT EXISTS idx_document_project ON document(project_id);
CREATE INDEX IF NOT EXISTS idx_activity_project ON activity_log(project_id);
CREATE INDEX IF NOT EXISTS idx_activity_task ON activity_log(task_id);
```

- [ ] **Step 5: 验证项目能编译**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add backend/
git commit -m "feat: initialize Spring Boot 3.15 backend project with JPA, Redis, MinIO, JWT dependencies"
```

---

## Task 2: 后端公共模块（统一响应、异常处理、JPA 审计）

**Files:**
- Create: `backend/src/main/java/com/pm/common/Result.java`
- Create: `backend/src/main/java/com/pm/common/PageResult.java`
- Create: `backend/src/main/java/com/pm/common/GlobalExceptionHandler.java`
- Create: `backend/src/main/java/com/pm/common/Constants.java`
- Create: `backend/src/main/java/com/pm/config/JpaConfig.java`
- Create: `backend/src/main/java/com/pm/config/CorsConfig.java`
- Create: `backend/src/main/java/com/pm/config/RedisConfig.java`
- Create: `backend/src/main/java/com/pm/entity/BaseEntity.java`

- [ ] **Step 1: 创建统一响应类 Result.java**

```java
package com.pm.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
```

- [ ] **Step 2: 创建分页响应类 PageResult.java**

```java
package com.pm.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int page;
    private int pageSize;

    public PageResult(List<T> list, long total, int page, int pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }
}
```

- [ ] **Step 3: 创建全局异常处理器**

```java
package com.pm.common;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return Result.error(400, message);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleBadCredentials(BadCredentialsException e) {
        return Result.error(401, "用户名或密码错误");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgument(IllegalArgumentException e) {
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleRuntime(RuntimeException e) {
        return Result.error(e.getMessage());
    }
}
```

- [ ] **Step 4: 创建常量类**

```java
package com.pm.common;

public class Constants {
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String REDIS_ACCESS_TOKEN_PREFIX = "auth:access:";
    public static final String REDIS_REFRESH_TOKEN_PREFIX = "auth:refresh:";

    public enum UserStatus {
        DISABLED(0), ENABLED(1);
        public final int value;
        UserStatus(int value) { this.value = value; }
    }

    public enum TaskStatus {
        TODO, IN_PROGRESS, IN_REVIEW, DONE
    }

    public enum TaskPriority {
        LOW, MEDIUM, HIGH, URGENT
    }

    public enum ProjectStatus {
        ACTIVE, ARCHIVED, COMPLETED
    }

    public enum SprintStatus {
        PLANNING, ACTIVE, COMPLETED
    }

    public enum DocumentType {
        DESIGN, CUSTOMER, MEETING, OTHER
    }

    public enum WorkspaceRole {
        OWNER, ADMIN, MEMBER
    }

    public enum ProjectRole {
        ADMIN, MANAGER, MEMBER, READONLY
    }
}
```

- [ ] **Step 5: 创建 BaseEntity**

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 6: 创建配置类**

**JpaConfig.java:**
```java
package com.pm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
```

**CorsConfig.java:**
```java
package com.pm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

**RedisConfig.java:**
```java
package com.pm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
```

- [ ] **Step 7: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**

```bash
git add backend/src/main/java/com/pm/common/ backend/src/main/java/com/pm/config/ backend/src/main/java/com/pm/entity/BaseEntity.java
git commit -m "feat: add common module - unified response, exception handler, JPA audit, CORS, Redis config"
```

---

## Task 3: JPA 实体类

**Files:**
- Create: `backend/src/main/java/com/pm/entity/User.java`
- Create: `backend/src/main/java/com/pm/entity/Workspace.java`
- Create: `backend/src/main/java/com/pm/entity/WorkspaceMember.java`
- Create: `backend/src/main/java/com/pm/entity/Project.java`
- Create: `backend/src/main/java/com/pm/entity/ProjectMember.java`
- Create: `backend/src/main/java/com/pm/entity/Task.java`
- Create: `backend/src/main/java/com/pm/entity/Tag.java`
- Create: `backend/src/main/java/com/pm/entity/TaskTag.java`
- Create: `backend/src/main/java/com/pm/entity/Comment.java`
- Create: `backend/src/main/java/com/pm/entity/Sprint.java`
- Create: `backend/src/main/java/com/pm/entity/Document.java`
- Create: `backend/src/main/java/com/pm/entity/DocumentVersion.java`
- Create: `backend/src/main/java/com/pm/entity/Attachment.java`
- Create: `backend/src/main/java/com/pm/entity/ActivityLog.java`

- [ ] **Step 1: 创建 User.java**

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_table")
public class User extends BaseEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 500)
    private String avatar;

    @Column(nullable = false)
    private Short status = 1;
}
```

- [ ] **Step 2: 创建 Workspace.java**

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Workspace extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
```

- [ ] **Step 3: 创建 WorkspaceMember.java**

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"workspace_id", "user_id"}))
public class WorkspaceMember extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String role = "MEMBER";
}
```

- [ ] **Step 4: 创建 Project.java**

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Project extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}
```

- [ ] **Step 5: 创建 ProjectMember.java**

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
public class ProjectMember extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String role = "MEMBER";
}
```

- [ ] **Step 6: 创建 Task.java**

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"parent"})
@Entity
@Table(name = "task")
public class Task extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Task parent;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String status = "TODO";

    @Column(nullable = false, length = 10)
    private String priority = "MEDIUM";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
```

- [ ] **Step 7: 创建 Tag.java**

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Tag extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 7)
    private String color;
}
```

- [ ] **Step 8: 创建 TaskTag.java**

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "task_tag")
public class TaskTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
```

- [ ] **Step 9: 创建 Comment.java**

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Comment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
```

- [ ] **Step 10: 创建 Sprint.java**

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Sprint extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String goal;

    @Column(nullable = false, length = 20)
    private String status = "PLANNING";

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
```

- [ ] **Step 11: 创建 Document.java 和 DocumentVersion.java**

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Document extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 20)
    private String type = "OTHER";

    @Column(name = "current_version")
    private Integer currentVersion = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}
```

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Entity
public class DocumentVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_name", nullable = false, length = 200)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "change_summary", length = 500)
    private String changeSummary;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

- [ ] **Step 12: 创建 Attachment.java**

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Entity
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "file_name", nullable = false, length = 200)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

- [ ] **Step 13: 创建 ActivityLog.java**

```java
package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Entity
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

- [ ] **Step 14: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 15: Commit**

```bash
git add backend/src/main/java/com/pm/entity/
git commit -m "feat: add all JPA entities - User, Workspace, Project, Task, Sprint, Document, Comment, etc."
```

---

## Task 4: JPA Repository 层

**Files:**
- Create: `backend/src/main/java/com/pm/repository/` 下所有 Repository 接口

- [ ] **Step 1: 创建所有 Repository 接口**

```java
package com.pm.repository;

import com.pm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

```java
package com.pm.repository;

import com.pm.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
}
```

```java
package com.pm.repository;

import com.pm.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    List<WorkspaceMember> findByWorkspaceId(Long workspaceId);
    List<WorkspaceMember> findByUserId(Long userId);
    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);
}
```

```java
package com.pm.repository;

import com.pm.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByWorkspaceId(Long workspaceId);
    List<Project> findByWorkspaceIdAndStatus(Long workspaceId, String status);
}
```

```java
package com.pm.repository;

import com.pm.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectId(Long projectId);
    List<ProjectMember> findByUserId(Long userId);
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}
```

```java
package com.pm.repository;

import com.pm.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByProjectIdAndStatus(Long projectId, String status);
    List<Task> findByProjectIdAndSprintId(Long projectId, Long sprintId);
    List<Task> findByAssigneeId(Long assigneeId);
    List<Task> findByParentId(Long parentId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) " +
           "AND (:sprintId IS NULL OR t.sprint.id = :sprintId) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:keyword IS NULL OR t.title LIKE %:keyword%)")
    List<Task> findByFilters(@Param("projectId") Long projectId,
                             @Param("status") String status,
                             @Param("assigneeId") Long assigneeId,
                             @Param("sprintId") Long sprintId,
                             @Param("priority") String priority,
                             @Param("keyword") String keyword);

    long countByProjectIdAndStatus(Long projectId, String status);
    long countByProjectId(Long projectId);
    long countBySprintId(Long sprintId);
    long countBySprintIdAndStatus(Long sprintId, String status);
    long countByAssigneeIdAndStatus(Long assigneeId, String status);

    @Query("SELECT t.assignee.id, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.assignee.id")
    List<Object[]> countByAssigneeGrouped(@Param("projectId") Long projectId);
}
```

```java
package com.pm.repository;

import com.pm.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByProjectId(Long projectId);
}
```

```java
package com.pm.repository;

import com.pm.entity.TaskTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskTagRepository extends JpaRepository<TaskTag, Long> {
    List<TaskTag> findByTaskId(Long taskId);
    void deleteByTaskId(Long taskId);
}
```

```java
package com.pm.repository;

import com.pm.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
```

```java
package com.pm.repository;

import com.pm.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProjectId(Long projectId);
    List<Sprint> findByProjectIdAndStatus(Long projectId, String status);
}
```

```java
package com.pm.repository;

import com.pm.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByProjectId(Long projectId);
    List<Document> findByProjectIdAndType(Long projectId, String type);
}
```

```java
package com.pm.repository;

import com.pm.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
    List<DocumentVersion> findByDocumentIdOrderByVersionNoDesc(Long documentId);
}
```

```java
package com.pm.repository;

import com.pm.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTaskId(Long taskId);
}
```

```java
package com.pm.repository;

import com.pm.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<ActivityLog> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    List<ActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
```

- [ ] **Step 2: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/pm/repository/
git commit -m "feat: add all JPA repository interfaces with custom query methods"
```

---

## Task 5: JWT 认证与 Security 配置

**Files:**
- Create: `backend/src/main/java/com/pm/security/JwtTokenProvider.java`
- Create: `backend/src/main/java/com/pm/security/JwtAuthenticationFilter.java`
- Create: `backend/src/main/java/com/pm/security/CustomUserDetailsService.java`
- Create: `backend/src/main/java/com/pm/config/SecurityConfig.java`

- [ ] **Step 1: 创建 JwtTokenProvider**

```java
package com.pm.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(Long userId) {
        return generateToken(userId, accessTokenExpiration);
    }

    public String generateRefreshToken(Long userId) {
        return generateToken(userId, refreshTokenExpiration);
    }

    private String generateToken(Long userId, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

- [ ] **Step 2: 创建 CustomUserDetailsService**

```java
package com.pm.security;

import com.pm.entity.User;
import com.pm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), new ArrayList<>());
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + id));
    }
}
```

- [ ] **Step 3: 创建 JwtAuthenticationFilter**

```java
package com.pm.security;

import com.pm.common.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String redisKey = Constants.REDIS_ACCESS_TOKEN_PREFIX + userId;
            Object storedToken = redisTemplate.opsForValue().get(redisKey);

            if (storedToken != null && storedToken.equals(token)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(
                        userDetailsService.getUserById(userId).getUsername());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            return bearerToken.substring(Constants.TOKEN_PREFIX.length());
        }
        return null;
    }
}
```

- [ ] **Step 4: 创建 SecurityConfig**

```java
package com.pm.config;

import com.pm.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

- [ ] **Step 5: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/pm/security/ backend/src/main/java/com/pm/config/SecurityConfig.java
git commit -m "feat: add JWT authentication - token provider, filter, security config"
```

---

## Task 6: MinIO 工具类与配置

**Files:**
- Create: `backend/src/main/java/com/pm/util/MinioUtil.java`
- Create: `backend/src/main/java/com/pm/service/MinioService.java`
- Create: `backend/src/main/java/com/pm/service/impl/MinioServiceImpl.java`

- [ ] **Step 1: 创建 MinioUtil**

```java
package com.pm.util;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtil {

    private final MinioClient minioClient;

    @Value("${minio.buckets.docs}")
    private String docsBucket;

    @Value("${minio.buckets.attachments}")
    private String attachmentsBucket;

    @Value("${minio.buckets.avatars}")
    private String avatarsBucket;

    public void createBucket(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName).build());
            }
        } catch (Exception e) {
            log.error("创建桶失败: {}", bucketName, e);
            throw new RuntimeException("创建桶失败: " + bucketName, e);
        }
    }

    public String uploadFile(String bucketName, String objectName, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return objectName;
        } catch (Exception e) {
            log.error("上传文件失败: {}/{}", bucketName, objectName, e);
            throw new RuntimeException("上传文件失败", e);
        }
    }

    public String getPresignedUrl(String bucketName, String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(1, TimeUnit.HOURS)
                    .build());
        } catch (Exception e) {
            log.error("获取预签名URL失败: {}/{}", bucketName, objectName, e);
            throw new RuntimeException("获取预签名URL失败", e);
        }
    }

    public void deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("删除文件失败: {}/{}", bucketName, objectName, e);
            throw new RuntimeException("删除文件失败", e);
        }
    }

    public String getDocsBucket() { return docsBucket; }
    public String getAttachmentsBucket() { return attachmentsBucket; }
    public String getAvatarsBucket() { return avatarsBucket; }
}
```

- [ ] **Step 2: 在 application.yml 同级或用 @Bean 创建 MinioClient**

在 `backend/src/main/java/com/pm/config/` 下创建 MinioConfig.java：

```java
package com.pm.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(@Value("${minio.endpoint}") String endpoint,
                                    @Value("${minio.access-key}") String accessKey,
                                    @Value("${minio.secret-key}") String secretKey) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
```

- [ ] **Step 3: 创建 MinioService 接口和实现**

```java
package com.pm.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {
    String uploadDocument(Long projectId, MultipartFile file);
    String uploadAttachment(Long taskId, MultipartFile file);
    String uploadAvatar(Long userId, MultipartFile file);
    String getPresignedUrl(String bucket, String objectName);
    void deleteFile(String bucket, String objectName);
    void initBuckets();
}
```

```java
package com.pm.service.impl;

import com.pm.service.MinioService;
import com.pm.util.MinioUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioUtil minioUtil;

    @Override
    public String uploadDocument(Long projectId, MultipartFile file) {
        String objectName = "projects/" + projectId + "/docs/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        minioUtil.uploadFile(minioUtil.getDocsBucket(), objectName, file);
        return objectName;
    }

    @Override
    public String uploadAttachment(Long taskId, MultipartFile file) {
        String objectName = "tasks/" + taskId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        minioUtil.uploadFile(minioUtil.getAttachmentsBucket(), objectName, file);
        return objectName;
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        String objectName = "users/" + userId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        minioUtil.uploadFile(minioUtil.getAvatarsBucket(), objectName, file);
        return objectName;
    }

    @Override
    public String getPresignedUrl(String bucket, String objectName) {
        return minioUtil.getPresignedUrl(bucket, objectName);
    }

    @Override
    public void deleteFile(String bucket, String objectName) {
        minioUtil.deleteFile(bucket, objectName);
    }

    @Override
    public void initBuckets() {
        minioUtil.createBucket(minioUtil.getDocsBucket());
        minioUtil.createBucket(minioUtil.getAttachmentsBucket());
        minioUtil.createBucket(minioUtil.getAvatarsBucket());
    }
}
```

- [ ] **Step 4: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/pm/util/ backend/src/main/java/com/pm/service/MinioService.java backend/src/main/java/com/pm/service/impl/MinioServiceImpl.java backend/src/main/java/com/pm/config/MinioConfig.java
git commit -m "feat: add MinIO integration - upload, presigned URL, bucket management"
```

---

## Task 7: DTO 类（请求与响应）

**Files:**
- Create: `backend/src/main/java/com/pm/dto/request/` 下所有请求 DTO
- Create: `backend/src/main/java/com/pm/dto/response/` 下所有响应 DTO

此步骤内容较多，每个 DTO 遵循相同模式。以下列出核心 DTO 完整代码，其余按相同模式创建。

- [ ] **Step 1: 认证相关 DTO**

**LoginRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
}
```

**RegisterRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50)
    private String username;
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100)
    private String password;
}
```

**RefreshTokenRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
```

**AuthResponse.java:**
```java
package com.pm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserResponse user;
}
```

- [ ] **Step 2: 用户相关 DTO**

**UserUpdateRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Email(message = "邮箱格式不正确")
    private String email;
    private String avatar;
}
```

**PasswordChangeRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordChangeRequest {
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
```

**UserResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private Short status;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 3: 工作空间相关 DTO**

**WorkspaceCreateRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkspaceCreateRequest {
    @NotBlank(message = "空间名称不能为空")
    private String name;
    private String description;
}
```

**WorkspaceUpdateRequest.java:**
```java
package com.pm.dto.request;

import lombok.Data;

@Data
public class WorkspaceUpdateRequest {
    private String name;
    private String description;
}
```

**MemberAddRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberAddRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    @NotBlank(message = "角色不能为空")
    private String role;
}
```

**MemberRoleUpdateRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberRoleUpdateRequest {
    @NotBlank(message = "角色不能为空")
    private String role;
}
```

**WorkspaceResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class WorkspaceResponse {
    private Long id;
    private String name;
    private String description;
    private UserResponse owner;
    private LocalDateTime createdAt;
}
```

**WorkspaceMemberResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkspaceMemberResponse {
    private Long id;
    private UserResponse user;
    private String role;
}
```

- [ ] **Step 4: 项目相关 DTO**

**ProjectCreateRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ProjectCreateRequest {
    @NotNull(message = "工作空间ID不能为空")
    private Long workspaceId;
    @NotBlank(message = "项目名称不能为空")
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
```

**ProjectUpdateRequest.java:**
```java
package com.pm.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProjectUpdateRequest {
    private String name;
    private String description;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}
```

**ProjectResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ProjectResponse {
    private Long id;
    private Long workspaceId;
    private String name;
    private String description;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private UserResponse createdBy;
    private LocalDateTime createdAt;
}
```

**ProjectMemberResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectMemberResponse {
    private Long id;
    private UserResponse user;
    private String role;
}
```

- [ ] **Step 5: 任务相关 DTO**

**TaskCreateRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskCreateRequest {
    @NotBlank(message = "任务标题不能为空")
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long assigneeId;
    private Long sprintId;
    private Long parentId;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Integer sortOrder;
}
```

**TaskUpdateRequest.java:**
```java
package com.pm.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskUpdateRequest {
    private String title;
    private String description;
    private String priority;
    private Long assigneeId;
    private Long sprintId;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Integer sortOrder;
}
```

**TaskStatusUpdateRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskStatusUpdateRequest {
    @NotBlank(message = "状态不能为空")
    private String status;
}
```

**TaskAssignRequest.java:**
```java
package com.pm.dto.request;

import lombok.Data;

@Data
public class TaskAssignRequest {
    private Long assigneeId;
}
```

**TaskBatchRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class TaskBatchRequest {
    @NotEmpty(message = "任务ID列表不能为空")
    private List<Long> taskIds;
    private String status;
    private String priority;
    private Long assigneeId;
    private Long sprintId;
}
```

**TaskQueryRequest.java:**
```java
package com.pm.dto.request;

import lombok.Data;

@Data
public class TaskQueryRequest {
    private String status;
    private Long assigneeId;
    private Long sprintId;
    private String priority;
    private String keyword;
    private Integer page = 1;
    private Integer pageSize = 20;
}
```

**TaskResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private Long projectId;
    private Long sprintId;
    private Long parentId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private UserResponse assignee;
    private UserResponse creator;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Integer sortOrder;
    private List<TagResponse> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class TagResponse {
        private Long id;
        private String name;
        private String color;
    }
}
```

- [ ] **Step 6: 评论、迭代、文档、附件、活动日志 DTO**

**CommentCreateRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateRequest {
    @NotBlank(message = "评论内容不能为空")
    private String content;
}
```

**CommentUpdateRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentUpdateRequest {
    @NotBlank(message = "评论内容不能为空")
    private String content;
}
```

**CommentResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private Long taskId;
    private UserResponse user;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**SprintCreateRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class SprintCreateRequest {
    @NotBlank(message = "迭代名称不能为空")
    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
}
```

**SprintUpdateRequest.java:**
```java
package com.pm.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SprintUpdateRequest {
    private String name;
    private String goal;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}
```

**SprintResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SprintResponse {
    private Long id;
    private Long projectId;
    private String name;
    private String goal;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
}
```

**DocumentCreateRequest.java:**
```java
package com.pm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocumentCreateRequest {
    @NotBlank(message = "文档标题不能为空")
    private String title;
    private String type;
}
```

**DocumentUpdateRequest.java:**
```java
package com.pm.dto.request;

import lombok.Data;

@Data
public class DocumentUpdateRequest {
    private String title;
    private String type;
}
```

**DocumentResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {
    private Long id;
    private Long projectId;
    private String title;
    private String type;
    private Integer currentVersion;
    private UserResponse createdBy;
    private DocumentVersionResponse currentVersionInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**DocumentVersionResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DocumentVersionResponse {
    private Long id;
    private Long documentId;
    private Integer versionNo;
    private String fileName;
    private Long fileSize;
    private UserResponse uploadedBy;
    private String changeSummary;
    private String downloadUrl;
    private LocalDateTime createdAt;
}
```

**AttachmentResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AttachmentResponse {
    private Long id;
    private Long taskId;
    private String fileName;
    private Long fileSize;
    private UserResponse uploadedBy;
    private String downloadUrl;
    private LocalDateTime createdAt;
}
```

**ActivityLogResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ActivityLogResponse {
    private Long id;
    private Long projectId;
    private Long taskId;
    private UserResponse user;
    private String action;
    private String detail;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 7: 报表响应 DTO**

**DashboardResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private long todoCount;
    private long inProgressCount;
    private long inReviewCount;
    private long doneCount;
    private List<ProjectProgressResponse> projects;
    private List<TaskResponse> myTodoTasks;
    private List<ActivityLogResponse> recentActivities;
}
```

**ProjectProgressResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectProgressResponse {
    private Long projectId;
    private String projectName;
    private long totalTasks;
    private long doneTasks;
    private double progress;
}
```

**TaskDistributionResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskDistributionResponse {
    private long todoCount;
    private long inProgressCount;
    private long inReviewCount;
    private long doneCount;
}
```

**BurndownResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BurndownResponse {
    private List<String> dates;
    private List<Long> idealRemaining;
    private List<Long> actualRemaining;
}
```

**MemberWorkloadResponse.java:**
```java
package com.pm.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberWorkloadResponse {
    private Long userId;
    private String username;
    private long totalTasks;
    private long todoCount;
    private long inProgressCount;
    private long doneCount;
}
```

- [ ] **Step 8: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 9: Commit**

```bash
git add backend/src/main/java/com/pm/dto/
git commit -m "feat: add all request and response DTOs for all API modules"
```

---

## Task 8: Service 层（认证 + 用户 + 工作空间）

**Files:**
- Create: `backend/src/main/java/com/pm/service/AuthService.java`
- Create: `backend/src/main/java/com/pm/service/impl/AuthServiceImpl.java`
- Create: `backend/src/main/java/com/pm/service/UserService.java`
- Create: `backend/src/main/java/com/pm/service/impl/UserServiceImpl.java`
- Create: `backend/src/main/java/com/pm/service/WorkspaceService.java`
- Create: `backend/src/main/java/com/pm/service/impl/WorkspaceServiceImpl.java`
- Create: `backend/src/main/java/com/pm/service/ActivityLogService.java`
- Create: `backend/src/main/java/com/pm/service/impl/ActivityLogServiceImpl.java`

- [ ] **Step 1: 创建 AuthService**

```java
package com.pm.service;

import com.pm.dto.request.LoginRequest;
import com.pm.dto.request.RegisterRequest;
import com.pm.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(Long userId);
}
```

```java
package com.pm.service.impl;

import com.pm.common.Constants;
import com.pm.dto.request.LoginRequest;
import com.pm.dto.request.RegisterRequest;
import com.pm.dto.response.AuthResponse;
import com.pm.dto.response.UserResponse;
import com.pm.entity.User;
import com.pm.repository.UserRepository;
import com.pm.security.JwtTokenProvider;
import com.pm.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));

        if (user.getStatus() == 0) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        redisTemplate.opsForValue().set(Constants.REDIS_ACCESS_TOKEN_PREFIX + user.getId(),
                accessToken, 30, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(Constants.REDIS_REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken, 7, TimeUnit.DAYS);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserResponse(user))
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        redisTemplate.opsForValue().set(Constants.REDIS_ACCESS_TOKEN_PREFIX + user.getId(),
                accessToken, 30, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(Constants.REDIS_REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken, 7, TimeUnit.DAYS);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserResponse(user))
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("刷新令牌无效");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String redisKey = Constants.REDIS_REFRESH_TOKEN_PREFIX + userId;
        Object stored = redisTemplate.opsForValue().get(redisKey);

        if (stored == null || !stored.equals(refreshToken)) {
            throw new IllegalArgumentException("刷新令牌已过期");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);
        redisTemplate.opsForValue().set(Constants.REDIS_ACCESS_TOKEN_PREFIX + userId,
                newAccessToken, 30, TimeUnit.MINUTES);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .user(toUserResponse(user))
                .build();
    }

    @Override
    public void logout(Long userId) {
        redisTemplate.delete(Constants.REDIS_ACCESS_TOKEN_PREFIX + userId);
        redisTemplate.delete(Constants.REDIS_REFRESH_TOKEN_PREFIX + userId);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
```

- [ ] **Step 2: 创建 UserService**

```java
package com.pm.service;

import com.pm.dto.request.PasswordChangeRequest;
import com.pm.dto.request.UserUpdateRequest;
import com.pm.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse getProfile(Long userId);
    UserResponse updateProfile(Long userId, UserUpdateRequest request);
    void changePassword(Long userId, PasswordChangeRequest request);
    UserResponse updateAvatar(Long userId, MultipartFile file);
    UserResponse getUserById(Long userId);
}
```

```java
package com.pm.service.impl;

import com.pm.dto.request.PasswordChangeRequest;
import com.pm.dto.request.UserUpdateRequest;
import com.pm.dto.response.UserResponse;
import com.pm.entity.User;
import com.pm.repository.UserRepository;
import com.pm.service.MinioService;
import com.pm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MinioService minioService;

    @Override
    public UserResponse getProfile(Long userId) {
        return toResponse(getUserEntity(userId));
    }

    @Override
    public UserResponse updateProfile(Long userId, UserUpdateRequest request) {
        User user = getUserEntity(userId);
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        userRepository.save(user);
        return toResponse(user);
    }

    @Override
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = getUserEntity(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public UserResponse updateAvatar(Long userId, MultipartFile file) {
        User user = getUserEntity(userId);
        String path = minioService.uploadAvatar(userId, file);
        user.setAvatar(path);
        userRepository.save(user);
        return toResponse(user);
    }

    @Override
    public UserResponse getUserById(Long userId) {
        return toResponse(getUserEntity(userId));
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
```

- [ ] **Step 3: 创建 ActivityLogService**

```java
package com.pm.service;

import com.pm.dto.response.ActivityLogResponse;
import java.util.List;

public interface ActivityLogService {
    void log(Long projectId, Long taskId, Long userId, String action, String detail);
    List<ActivityLogResponse> getByProject(Long projectId);
    List<ActivityLogResponse> getByTask(Long taskId);
    List<ActivityLogResponse> getByUser(Long userId);
}
```

```java
package com.pm.service.impl;

import com.pm.dto.response.ActivityLogResponse;
import com.pm.entity.ActivityLog;
import com.pm.entity.User;
import com.pm.repository.ActivityLogRepository;
import com.pm.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    public void log(Long projectId, Long taskId, Long userId, String action, String detail) {
        ActivityLog log = new ActivityLog();
        // 使用代理引用避免加载完整实体
        ActivityLog.ProjectRef projectRef = new ActivityLog.ProjectRef(projectId);
        log.setProject(projectRef);

        if (taskId != null) {
            log.setTask(new ActivityLog.TaskRef(taskId));
        }
        log.setUser(new ActivityLog.UserRef(userId));
        log.setAction(action);
        log.setDetail(detail);
        activityLogRepository.save(log);
    }

    // 简化：直接设置 id 的辅助方法
    // 注意：JPA 中需要使用 getReferenceById 来创建代理引用
    // 重写 log 方法使用 EntityManager 或 Repository 的 getReferenceById
    // 为简洁起见，此处通过在 ActivityLog 实体上直接设置方式

    @Override
    public List<ActivityLogResponse> getByProject(Long projectId) {
        return activityLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ActivityLogResponse> getByTask(Long taskId) {
        return activityLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ActivityLogResponse> getByUser(Long userId) {
        return activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        User user = log.getUser();
        return ActivityLogResponse.builder()
                .id(log.getId())
                .projectId(log.getProject().getId())
                .taskId(log.getTask() != null ? log.getTask().getId() : null)
                .user(com.pm.dto.response.UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .avatar(user.getAvatar())
                        .status(user.getStatus())
                        .build())
                .action(log.getAction())
                .detail(log.getDetail())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
```

> 注意：ActivityLog 中的 `log` 方法需要使用 JPA 的 `getReferenceById` 来创建代理引用。实际实现中注入对应的 Repository 并调用 `getReferenceById`。简化实现如下替换上面的 log 方法：

```java
// 注入额外依赖
private final ProjectRepository projectRepository;
private final TaskRepository taskRepository;
private final UserRepository userRepository;

@Override
public void log(Long projectId, Long taskId, Long userId, String action, String detail) {
    ActivityLog activityLog = new ActivityLog();
    activityLog.setProject(projectRepository.getReferenceById(projectId));
    if (taskId != null) {
        activityLog.setTask(taskRepository.getReferenceById(taskId));
    }
    activityLog.setUser(userRepository.getReferenceById(userId));
    activityLog.setAction(action);
    activityLog.setDetail(detail);
    activityLogRepository.save(activityLog);
}
```

- [ ] **Step 4: 创建 WorkspaceService**

```java
package com.pm.service;

import com.pm.dto.request.MemberAddRequest;
import com.pm.dto.request.MemberRoleUpdateRequest;
import com.pm.dto.request.WorkspaceCreateRequest;
import com.pm.dto.request.WorkspaceUpdateRequest;
import com.pm.dto.response.WorkspaceMemberResponse;
import com.pm.dto.response.WorkspaceResponse;
import java.util.List;

public interface WorkspaceService {
    List<WorkspaceResponse> listByUser(Long userId);
    WorkspaceResponse create(Long userId, WorkspaceCreateRequest request);
    WorkspaceResponse update(Long id, WorkspaceUpdateRequest request);
    void delete(Long id);
    WorkspaceResponse getById(Long id);
    List<WorkspaceMemberResponse> getMembers(Long workspaceId);
    void addMember(Long workspaceId, MemberAddRequest request);
    void updateMemberRole(Long workspaceId, Long userId, MemberRoleUpdateRequest request);
    void removeMember(Long workspaceId, Long userId);
}
```

```java
package com.pm.service.impl;

import com.pm.dto.request.MemberAddRequest;
import com.pm.dto.request.MemberRoleUpdateRequest;
import com.pm.dto.request.WorkspaceCreateRequest;
import com.pm.dto.request.WorkspaceUpdateRequest;
import com.pm.dto.response.UserResponse;
import com.pm.dto.response.WorkspaceMemberResponse;
import com.pm.dto.response.WorkspaceResponse;
import com.pm.entity.User;
import com.pm.entity.Workspace;
import com.pm.entity.WorkspaceMember;
import com.pm.repository.UserRepository;
import com.pm.repository.WorkspaceMemberRepository;
import com.pm.repository.WorkspaceRepository;
import com.pm.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    @Override
    public List<WorkspaceResponse> listByUser(Long userId) {
        List<WorkspaceMember> memberships = workspaceMemberRepository.findByUserId(userId);
        return memberships.stream()
                .map(m -> toResponse(m.getWorkspace()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkspaceResponse create(Long userId, WorkspaceCreateRequest request) {
        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        workspace.setOwner(userRepository.getReferenceById(userId));
        workspace = workspaceRepository.save(workspace);

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(workspace);
        member.setUser(userRepository.getReferenceById(userId));
        member.setRole("OWNER");
        workspaceMemberRepository.save(member);

        return toResponse(workspace);
    }

    @Override
    public WorkspaceResponse update(Long id, WorkspaceUpdateRequest request) {
        Workspace workspace = getWorkspaceEntity(id);
        if (request.getName() != null) workspace.setName(request.getName());
        if (request.getDescription() != null) workspace.setDescription(request.getDescription());
        workspaceRepository.save(workspace);
        return toResponse(workspace);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        workspaceRepository.deleteById(id);
    }

    @Override
    public WorkspaceResponse getById(Long id) {
        return toResponse(getWorkspaceEntity(id));
    }

    @Override
    public List<WorkspaceMemberResponse> getMembers(Long workspaceId) {
        return workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::toMemberResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addMember(Long workspaceId, MemberAddRequest request) {
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, request.getUserId())) {
            throw new IllegalArgumentException("用户已是工作空间成员");
        }
        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(workspaceRepository.getReferenceById(workspaceId));
        member.setUser(userRepository.getReferenceById(request.getUserId()));
        member.setRole(request.getRole());
        workspaceMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void updateMemberRole(Long workspaceId, Long userId, MemberRoleUpdateRequest request) {
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("成员不存在"));
        member.setRole(request.getRole());
        workspaceMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void removeMember(Long workspaceId, Long userId) {
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("成员不存在"));
        workspaceMemberRepository.delete(member);
    }

    private Workspace getWorkspaceEntity(Long id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("工作空间不存在"));
    }

    private WorkspaceResponse toResponse(Workspace ws) {
        User owner = ws.getOwner();
        return WorkspaceResponse.builder()
                .id(ws.getId())
                .name(ws.getName())
                .description(ws.getDescription())
                .owner(UserResponse.builder()
                        .id(owner.getId())
                        .username(owner.getUsername())
                        .email(owner.getEmail())
                        .avatar(owner.getAvatar())
                        .status(owner.getStatus())
                        .createdAt(owner.getCreatedAt())
                        .build())
                .createdAt(ws.getCreatedAt())
                .build();
    }

    private WorkspaceMemberResponse toMemberResponse(WorkspaceMember m) {
        User user = m.getUser();
        return WorkspaceMemberResponse.builder()
                .id(m.getId())
                .user(UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .avatar(user.getAvatar())
                        .status(user.getStatus())
                        .createdAt(user.getCreatedAt())
                        .build())
                .role(m.getRole())
                .build();
    }
}
```

> 注意：`WorkspaceMemberRepository` 需要添加 `findByWorkspaceIdAndUserId` 方法：
```java
Optional<WorkspaceMember> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);
```

- [ ] **Step 5: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/pm/service/ backend/src/main/java/com/pm/repository/WorkspaceMemberRepository.java
git commit -m "feat: add auth, user, workspace, activity log services"
```

---

## Task 9: Service 层（项目 + 任务 + 评论 + 迭代 + 文档 + 附件）

**Files:**
- Create: `backend/src/main/java/com/pm/service/ProjectService.java` + impl
- Create: `backend/src/main/java/com/pm/service/TaskService.java` + impl
- Create: `backend/src/main/java/com/pm/service/CommentService.java` + impl
- Create: `backend/src/main/java/com/pm/service/SprintService.java` + impl
- Create: `backend/src/main/java/com/pm/service/DocumentService.java` + impl
- Create: `backend/src/main/java/com/pm/service/AttachmentService.java` + impl
- Create: `backend/src/main/java/com/pm/service/ReportService.java` + impl

- [ ] **Step 1: 创建 ProjectService**

接口：
```java
package com.pm.service;

import com.pm.dto.request.MemberAddRequest;
import com.pm.dto.request.MemberRoleUpdateRequest;
import com.pm.dto.request.ProjectCreateRequest;
import com.pm.dto.request.ProjectUpdateRequest;
import com.pm.dto.response.ProjectMemberResponse;
import com.pm.dto.response.ProjectResponse;
import java.util.List;

public interface ProjectService {
    List<ProjectResponse> listByWorkspace(Long workspaceId);
    List<ProjectResponse> listByUser(Long userId);
    ProjectResponse create(Long userId, ProjectCreateRequest request);
    ProjectResponse update(Long id, ProjectUpdateRequest request);
    void delete(Long id);
    ProjectResponse getById(Long id);
    List<ProjectMemberResponse> getMembers(Long projectId);
    void addMember(Long projectId, MemberAddRequest request);
    void updateMemberRole(Long projectId, Long userId, MemberRoleUpdateRequest request);
    void removeMember(Long projectId, Long userId);
}
```

实现模式与 WorkspaceService 类似，使用 ProjectRepository、ProjectMemberRepository 进行 CRUD，使用 ActivityLogService 记录日志。

- [ ] **Step 2: 创建 TaskService**

接口：
```java
package com.pm.service;

import com.pm.common.PageResult;
import com.pm.dto.request.*;
import com.pm.dto.response.TaskResponse;
import java.util.List;

public interface TaskService {
    PageResult<TaskResponse> list(Long projectId, TaskQueryRequest query);
    List<TaskResponse> listByStatus(Long projectId, String status);
    TaskResponse create(Long projectId, Long userId, TaskCreateRequest request);
    TaskResponse update(Long id, TaskUpdateRequest request);
    void delete(Long id);
    TaskResponse getById(Long id);
    TaskResponse updateStatus(Long id, String status, Long userId);
    TaskResponse assign(Long id, Long assigneeId, Long userId);
    void batch(TaskBatchRequest request, Long userId);
}
```

实现要点：
- `listByStatus` 按 status 分组返回，用于看板视图
- `create` 时记录活动日志，设置 creator
- `updateStatus` 时记录状态变更日志
- `batch` 支持批量更新状态/优先级/指派人

- [ ] **Step 3: 创建 CommentService, SprintService, DocumentService, AttachmentService**

遵循相同模式：接口 + 实现类，使用对应 Repository，调用 ActivityLogService 记录日志。

DocumentService 关键方法：
- `uploadVersion` - 创建新 DocumentVersion，更新 Document.currentVersion，上传到 MinIO
- `getVersions` - 返回版本历史列表
- `downloadVersion` - 生成预签名 URL
- `previewVersion` - 生成预签名 URL

AttachmentService 关键方法：
- `upload` - 上传到 MinIO，创建 Attachment 记录
- `delete` - 删除 MinIO 文件和数据库记录

- [ ] **Step 4: 创建 ReportService**

```java
package com.pm.service;

import com.pm.dto.response.*;

public interface ReportService {
    DashboardResponse getDashboard(Long userId);
    ProjectProgressResponse getProjectProgress(Long projectId);
    TaskDistributionResponse getTaskDistribution(Long projectId);
    BurndownResponse getBurndown(Long sprintId);
    MemberWorkloadResponse[] getMemberWorkload(Long projectId);
}
```

实现使用 TaskRepository 的统计方法（countByProjectIdAndStatus, countByAssigneeGrouped 等）计算报表数据。

- [ ] **Step 5: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/pm/service/
git commit -m "feat: add all business services - project, task, sprint, document, report"
```

---

## Task 10: Controller 层（全部 REST API）

**Files:**
- Create: `backend/src/main/java/com/pm/controller/` 下所有 Controller

- [ ] **Step 1: 创建 AuthController**

```java
package com.pm.controller;

import com.pm.common.Result;
import com.pm.dto.request.LoginRequest;
import com.pm.dto.request.RefreshTokenRequest;
import com.pm.dto.request.RegisterRequest;
import com.pm.dto.response.AuthResponse;
import com.pm.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/refresh")
    public Result<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 通过 token 获取 userId，从 Redis 获取
        authService.logout(getCurrentUserId());
        return Result.success();
    }

    private Long getCurrentUserId() {
        // 从 JWT 中获取，通过 JwtTokenProvider 解析当前请求的 token
        return 1L; // 简化，实际从 SecurityContext 或 request header 解析
    }
}
```

> 注意：获取当前用户 ID 的辅助方法。建议创建一个 `SecurityUtil` 工具类统一处理，从 SecurityContext 获取当前认证用户后查询 UserRepository 得到 userId。

- [ ] **Step 2: 创建其余 Controller**

每个 Controller 遵循相同模式：
- `@RestController` + `@RequestMapping`
- 注入对应 Service
- 调用 Service 方法，包装为 `Result.success()` 返回
- 分页接口返回 `Result<PageResult<T>>`

Controller 列表（代码模式一致，此处列出结构）：

| Controller | 路径前缀 | 方法 |
|---|---|---|
| UserController | /api/users | GET/PUT profile, PUT password, POST avatar |
| WorkspaceController | /api/workspaces | CRUD + 成员管理 |
| ProjectController | /api/projects | CRUD + 成员管理 |
| TaskController | /api/projects/{pid}/tasks, /api/tasks/{id} | CRUD + 状态/指派/批量 |
| CommentController | /api/tasks/{id}/comments, /api/comments/{id} | CRUD |
| SprintController | /api/projects/{pid}/sprints, /api/sprints/{id} | CRUD |
| DocumentController | /api/projects/{pid}/documents, /api/documents/{id} | CRUD + 版本 |
| AttachmentController | /api/tasks/{id}/attachments, /api/attachments/{id} | 上传/列表/删除 |
| ActivityLogController | /api/tasks/{id}/activities | GET |
| ReportController | /api/reports/* | GET 各类报表 |

- [ ] **Step 3: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/pm/controller/
git commit -m "feat: add all REST controllers for all API modules"
```

---

## Task 11: 前端项目初始化

**Files:**
- Create: `frontend/package.json`, `frontend/vite.config.ts`, `frontend/tsconfig.json`, `frontend/index.html`
- Create: `frontend/src/main.ts`, `frontend/src/App.vue`

- [ ] **Step 1: 初始化 Vue 3 项目**

Run:
```bash
cd D:/personal/project/project-management
npm create vite@latest frontend -- --template vue-ts
cd frontend
npm install
npm install ant-design-vue@4 @ant-design/icons-vue pinia vue-router@4 axios dayjs echarts vue-echarts
npm install -D @types/node less
```

- [ ] **Step 2: 配置 vite.config.ts**

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **Step 3: 配置 tsconfig.json 确认 paths 别名**

在 `compilerOptions` 中添加：
```json
"paths": {
  "@/*": ["./src/*"]
}
```

- [ ] **Step 4: 验证前端启动**

Run: `cd frontend && npm run dev`
Expected: Vite dev server running at http://localhost:3000

- [ ] **Step 5: Commit**

```bash
git add frontend/
git commit -m "feat: initialize Vue 3 + TypeScript + Vite frontend project"
```

---

## Task 12: 前端主题与布局

**Files:**
- Create: `frontend/src/assets/styles/theme.less`
- Create: `frontend/src/layouts/MainLayout.vue`
- Modify: `frontend/src/main.ts` (注册 Ant Design Vue、Pinia、Router)
- Modify: `frontend/src/App.vue`

- [ ] **Step 1: 创建淡蓝色主题**

```less
// theme.less
@primary-color: #5B9BD5;
@primary-color-hover: #4A8BC4;
@primary-color-active: #3A7BB3;
@primary-color-light: #E8F1FB;
@layout-body-background: #F0F5FF;
@layout-sider-background: #FFFFFF;
@component-background: #FFFFFF;
@border-color-base: #D6E4F0;
@heading-color: #2C3E50;
@text-color: #5A6C7D;
@success-color: #52C41A;
@warning-color: #FAAD14;
@error-color: #FF4D4F;
@info-color: #5B9BD5;
```

- [ ] **Step 2: 配置 main.ts**

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'
import router from './router'
import './assets/styles/theme.less'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(Antd)
app.mount('#app')
```

- [ ] **Step 3: 创建 MainLayout.vue**

布局包含：左侧边栏菜单（可折叠）、顶部导航栏（搜索、通知、用户头像）、主内容区。

侧边栏菜单项：
- 工作台 (Dashboard)
- 项目列表
- 我的任务
- 文档中心
- 报表统计
- 系统设置

使用 Ant Design Vue 的 `a-layout`、`a-layout-sider`、`a-menu` 组件。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/
git commit -m "feat: add light-blue theme, main layout with sidebar and topbar"
```

---

## Task 13: 前端 API 层与类型定义

**Files:**
- Create: `frontend/src/types/index.ts`
- Create: `frontend/src/api/request.ts`
- Create: `frontend/src/api/auth.ts`
- Create: `frontend/src/api/user.ts`
- Create: `frontend/src/api/workspace.ts`
- Create: `frontend/src/api/project.ts`
- Create: `frontend/src/api/task.ts`
- Create: `frontend/src/api/sprint.ts`
- Create: `frontend/src/api/document.ts`
- Create: `frontend/src/api/report.ts`

- [ ] **Step 1: 创建 TypeScript 类型定义**

```typescript
// types/index.ts
export interface User {
  id: number
  username: string
  email: string
  avatar: string | null
  status: number
  createdAt: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: User
}

export interface Workspace {
  id: number
  name: string
  description: string | null
  owner: User
  createdAt: string
}

export interface WorkspaceMember {
  id: number
  user: User
  role: string
}

export interface Project {
  id: number
  workspaceId: number
  name: string
  description: string | null
  status: string
  startDate: string | null
  endDate: string | null
  createdBy: User
  createdAt: string
}

export interface ProjectMember {
  id: number
  user: User
  role: string
}

export interface Task {
  id: number
  projectId: number
  sprintId: number | null
  parentId: number | null
  title: string
  description: string | null
  status: string
  priority: string
  assignee: User | null
  creator: User
  startDate: string | null
  dueDate: string | null
  sortOrder: number
  tags: Tag[]
  createdAt: string
  updatedAt: string
}

export interface Tag {
  id: number
  name: string
  color: string
}

export interface Comment {
  id: number
  taskId: number
  user: User
  content: string
  createdAt: string
  updatedAt: string
}

export interface Sprint {
  id: number
  projectId: number
  name: string
  goal: string | null
  status: string
  startDate: string | null
  endDate: string | null
  createdAt: string
}

export interface Document {
  id: number
  projectId: number
  title: string
  type: string
  currentVersion: number
  createdBy: User
  currentVersionInfo: DocumentVersion | null
  createdAt: string
  updatedAt: string
}

export interface DocumentVersion {
  id: number
  documentId: number
  versionNo: number
  fileName: string
  fileSize: number
  uploadedBy: User
  changeSummary: string | null
  downloadUrl: string | null
  createdAt: string
}

export interface Attachment {
  id: number
  taskId: number
  fileName: string
  fileSize: number
  uploadedBy: User
  downloadUrl: string | null
  createdAt: string
}

export interface ActivityLog {
  id: number
  projectId: number
  taskId: number | null
  user: User
  action: string
  detail: string | null
  createdAt: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface Dashboard {
  todoCount: number
  inProgressCount: number
  inReviewCount: number
  doneCount: number
  projects: ProjectProgress[]
  myTodoTasks: Task[]
  recentActivities: ActivityLog[]
}

export interface ProjectProgress {
  projectId: number
  projectName: string
  totalTasks: number
  doneTasks: number
  progress: number
}

export interface TaskDistribution {
  todoCount: number
  inProgressCount: number
  inReviewCount: number
  doneCount: number
}

export interface Burndown {
  dates: string[]
  idealRemaining: number[]
  actualRemaining: number[]
}

export interface MemberWorkload {
  userId: number
  username: string
  totalTasks: number
  todoCount: number
  inProgressCount: number
  doneCount: number
}
```

- [ ] **Step 2: 创建 Axios 请求封装**

```typescript
// api/request.ts
import axios from 'axios'
import type { Result } from '@/types'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(config => {
  const authStore = useAuthStore()
  if (authStore.accessToken) {
    config.headers.Authorization = `Bearer ${authStore.accessToken}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const data = response.data as Result<any>
    if (data.code !== 200) {
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  async error => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      try {
        await authStore.refreshToken()
        return request(error.config)
      } catch {
        authStore.logout()
        router.push('/login')
      }
    }
    return Promise.reject(error)
  }
)

export default request
```

- [ ] **Step 3: 创建各模块 API 文件**

每个 API 文件封装对应模块的请求函数，例如 `auth.ts`：

```typescript
// api/auth.ts
import request from './request'
import type { AuthResponse } from '@/types'

export function login(username: string, password: string) {
  return request.post<any, { data: AuthResponse }>('/auth/login', { username, password })
}

export function register(username: string, email: string, password: string) {
  return request.post<any, { data: AuthResponse }>('/auth/register', { username, email, password })
}

export function refreshToken(refreshToken: string) {
  return request.post<any, { data: AuthResponse }>('/auth/refresh', { refreshToken })
}

export function logout() {
  return request.post('/auth/logout')
}
```

其余模块（user, workspace, project, task, sprint, document, report）按相同模式创建。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/types/ frontend/src/api/
git commit -m "feat: add TypeScript types, Axios interceptors, all API modules"
```

---

## Task 14: 前端 Store 与路由

**Files:**
- Create: `frontend/src/stores/auth.ts`
- Create: `frontend/src/stores/project.ts`
- Create: `frontend/src/stores/task.ts`
- Create: `frontend/src/router/index.ts`

- [ ] **Step 1: 创建 Pinia stores**

**auth.ts:**
```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'
import * as authApi from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const user = ref<User | null>(null)

  async function login(username: string, password: string) {
    const res = await authApi.login(username, password)
    accessToken.value = res.data.accessToken
    refreshToken.value = res.data.refreshToken
    user.value = res.data.user
    localStorage.setItem('accessToken', res.data.accessToken)
    localStorage.setItem('refreshToken', res.data.refreshToken)
  }

  async function doRefreshToken() {
    const res = await authApi.refreshToken(refreshToken.value)
    accessToken.value = res.data.accessToken
    localStorage.setItem('accessToken', res.data.accessToken)
  }

  function logout() {
    accessToken.value = ''
    refreshToken.value = ''
    user.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  return { accessToken, refreshToken, user, login, doRefreshToken, logout }
})
```

- [ ] **Step 2: 创建路由配置**

```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue')
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('@/views/dashboard/Index.vue') },
      { path: 'projects', name: 'ProjectList', component: () => import('@/views/project/List.vue') },
      { path: 'projects/:id', name: 'ProjectDetail', component: () => import('@/views/project/Detail.vue') },
      { path: 'documents', name: 'DocumentCenter', component: () => import('@/views/document/Index.vue') },
      { path: 'reports', name: 'Report', component: () => import('@/views/report/Index.vue') },
      { path: 'settings', name: 'Settings', component: () => import('@/views/settings/Index.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.accessToken) {
    next('/login')
  } else {
    next()
  }
})

export default router
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/stores/ frontend/src/router/
git commit -m "feat: add Pinia auth/project/task stores and router with auth guards"
```

---

## Task 15: 前端登录/注册页面

**Files:**
- Create: `frontend/src/views/auth/Login.vue`
- Create: `frontend/src/views/auth/Register.vue`

- [ ] **Step 1: 创建 Login.vue**

淡蓝色渐变背景，白色居中登录卡片，Ant Design Vue 表单组件。包含用户名/密码输入框和登录按钮，支持跳转注册页。

- [ ] **Step 2: 创建 Register.vue**

类似登录页布局，包含用户名/邮箱/密码/确认密码字段。

- [ ] **Step 3: 验证登录流程**

启动前后端，测试注册 → 登录 → 跳转工作台。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/auth/
git commit -m "feat: add login and register pages with light-blue gradient theme"
```

---

## Task 16: 前端工作台 Dashboard 页面

**Files:**
- Create: `frontend/src/views/dashboard/Index.vue`

- [ ] **Step 1: 创建 Dashboard 页面**

包含：
- 四个统计卡片（待办/进行中/待审核/已完成）使用 Ant Design 的 `a-card` + `a-statistic`
- 我的待办任务列表
- 参与项目的进度条
- 近期活动时间轴（使用 `a-timeline`）

调用 `GET /api/reports/dashboard` 获取数据。

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/dashboard/
git commit -m "feat: add dashboard page with task stats, todo list, project progress, activity feed"
```

---

## Task 17: 前端项目列表与详情页

**Files:**
- Create: `frontend/src/views/project/List.vue`
- Create: `frontend/src/views/project/Detail.vue`
- Create: `frontend/src/components/TaskDrawer.vue`

- [ ] **Step 1: 创建项目列表页**

卡片/列表切换，支持搜索和新建项目对话框。

- [ ] **Step 2: 创建项目详情页**

顶部项目信息，Tab 切换四种视图，右侧可打开任务详情抽屉。

- [ ] **Step 3: 创建 TaskDrawer 组件**

任务详情抽屉（Ant Design `a-drawer`），包含：
- 标题（可编辑）
- 状态/优先级选择
- 指派人选择
- 描述
- 评论列表
- 附件列表
- 活动日志

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/project/ frontend/src/components/TaskDrawer.vue
git commit -m "feat: add project list and detail pages with task drawer"
```

---

## Task 18: 前端看板视图

**Files:**
- Create: `frontend/src/components/KanbanBoard.vue`
- Create: `frontend/src/components/TaskCard.vue`

- [ ] **Step 1: 创建 TaskCard 组件**

任务卡片显示：标题、优先级标签、指派人头像、截止日期、标签色块。

- [ ] **Step 2: 创建 KanbanBoard 组件**

四列布局（待办/进行中/待审核/已完成），使用 CSS 拖拽或引入 `vuedraggable` 库实现拖拽排序和跨列移动。

拖拽完成后调用 `PUT /api/tasks/{id}/status` 更新状态。

- [ ] **Step 3: 安装拖拽库**

Run: `cd frontend && npm install vuedraggable@next`

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/KanbanBoard.vue frontend/src/components/TaskCard.vue
git commit -m "feat: add kanban board with drag-and-drop task cards"
```

---

## Task 19: 前端列表视图

**Files:**
- Create: `frontend/src/components/TaskList.vue`

- [ ] **Step 1: 创建 TaskList 组件**

使用 Ant Design `a-table`，列包括：标题、状态、优先级、指派人、截止日期、标签。支持筛选、排序、分页。行点击打开 TaskDrawer。

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/TaskList.vue
git commit -m "feat: add task list view with table, filters, pagination"
```

---

## Task 20: 前端甘特图视图

**Files:**
- Create: `frontend/src/components/GanttChart.vue`

- [ ] **Step 1: 安装甘特图库**

Run: `cd frontend && npm install @antv/g2plot` 或使用自定义 Canvas/SVG 实现

或者使用更轻量的方案：基于 `dayjs` + CSS Grid 自绘甘特图条，不引入额外重依赖。

- [ ] **Step 2: 创建 GanttChart 组件**

- 左侧任务列表，右侧时间轴甘特条
- 时间轴按天/周/月缩放
- 任务条颜色根据状态区分
- 支持拖拽调整日期（调用 TaskService.update）
- 子任务缩进显示

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/GanttChart.vue
git commit -m "feat: add Gantt chart view with timeline, drag-to-resize, task hierarchy"
```

---

## Task 21: 前端日历视图

**Files:**
- Create: `frontend/src/components/CalendarView.vue`

- [ ] **Step 1: 创建 CalendarView 组件**

使用 Ant Design `a-calendar` 组件，在日期格子中标注任务（根据 due_date）。点击任务弹出 TaskDrawer。支持月份切换。

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/CalendarView.vue
git commit -m "feat: add calendar view with task markers on due dates"
```

---

## Task 22: 前端文档中心页面

**Files:**
- Create: `frontend/src/views/document/Index.vue`
- Create: `frontend/src/components/DocumentVersionPanel.vue`
- Create: `frontend/src/components/FileUpload.vue`
- Create: `frontend/src/components/CommentList.vue`

- [ ] **Step 1: 创建 FileUpload 通用组件**

封装 Ant Design `a-upload`，支持拖拽上传，调用 MinIO 相关 API。

- [ ] **Step 2: 创建 CommentList 组件**

评论列表 + 新增评论输入框。

- [ ] **Step 3: 创建 DocumentVersionPanel 组件**

侧边面板显示文档版本历史，每个版本显示：版本号、上传者、时间、变更说明、下载按钮。

- [ ] **Step 4: 创建文档中心页面**

左侧按类型分组的文档树，右侧文档列表。支持上传新文档、上传新版本、查看版本历史、下载。

- [ ] **Step 5: Commit**

```bash
git add frontend/src/views/document/ frontend/src/components/DocumentVersionPanel.vue frontend/src/components/FileUpload.vue frontend/src/components/CommentList.vue
git commit -m "feat: add document center with version management, file upload, version history panel"
```

---

## Task 23: 前端报表统计页面

**Files:**
- Create: `frontend/src/views/report/Index.vue`

- [ ] **Step 1: 创建报表页面**

使用 ECharts（通过 vue-echarts）渲染四个图表：
1. 项目进度饼图（`GET /api/reports/project/{id}/progress`）
2. 任务状态分布柱状图（`GET /api/reports/project/{id}/task-distribution`）
3. 燃尽图折线图（`GET /api/reports/sprint/{id}/burndown`）
4. 成员工作量横向柱状图（`GET /api/reports/project/{id}/member-workload`）

顶部选择项目/迭代，下方展示图表网格。

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/report/
git commit -m "feat: add report page with ECharts - progress pie, distribution bar, burndown line, workload chart"
```

---

## Task 24: 前端系统设置页面

**Files:**
- Create: `frontend/src/views/settings/Index.vue`

- [ ] **Step 1: 创建设置页面**

包含：
- 个人信息修改（用户名、邮箱、头像）
- 密码修改
- 退出登录

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/settings/
git commit -m "feat: add settings page with profile edit, password change, logout"
```

---

## Task 25: 集成测试与修复

**Files:**
- 可能修改任何已有文件

- [ ] **Step 1: 启动后端**

确保 PostgreSQL、Redis、MinIO 均在运行。

Run: `cd backend && mvn spring-boot:run`
Expected: Application started on port 8080

- [ ] **Step 2: 启动前端**

Run: `cd frontend && npm run dev`
Expected: Vite running on port 3000

- [ ] **Step 3: 端到端流程测试**

测试完整流程：
1. 注册新用户
2. 登录
3. 创建工作空间
4. 创建项目
5. 创建任务
6. 看板拖拽
7. 列表视图筛选
8. 创建迭代
9. 上传文档、上传新版本
10. 查看报表
11. 甘特图、日历视图
12. 退出登录

- [ ] **Step 4: 修复发现的问题**

- [ ] **Step 5: Final commit**

```bash
git add -A
git commit -m "fix: integration testing fixes and polish"
```
