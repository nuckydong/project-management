-- User table
CREATE TABLE IF NOT EXISTS user_table (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(500),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_username ON user_table(username);
CREATE INDEX idx_user_email ON user_table(email);

-- Workspace table
CREATE TABLE IF NOT EXISTS workspace (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_workspace_owner FOREIGN KEY (owner_id) REFERENCES user_table(id) ON DELETE CASCADE
);

CREATE INDEX idx_workspace_owner ON workspace(owner_id);

-- Workspace Member table
CREATE TABLE IF NOT EXISTS workspace_member (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_ws_member_workspace FOREIGN KEY (workspace_id) REFERENCES workspace(id) ON DELETE CASCADE,
    CONSTRAINT fk_ws_member_user FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    CONSTRAINT uk_ws_member UNIQUE (workspace_id, user_id)
);

CREATE INDEX idx_ws_member_workspace ON workspace_member(workspace_id);
CREATE INDEX idx_ws_member_user ON workspace_member(user_id);

-- Project table
CREATE TABLE IF NOT EXISTS project (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    start_date DATE,
    end_date DATE,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_project_workspace FOREIGN KEY (workspace_id) REFERENCES workspace(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_creator FOREIGN KEY (created_by) REFERENCES user_table(id)
);

CREATE INDEX idx_project_workspace ON project(workspace_id);
CREATE INDEX idx_project_creator ON project(created_by);

-- Project Member table
CREATE TABLE IF NOT EXISTS project_member (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_project_member_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_member_user FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    CONSTRAINT uk_project_member UNIQUE (project_id, user_id)
);

CREATE INDEX idx_project_member_project ON project_member(project_id);
CREATE INDEX idx_project_member_user ON project_member(user_id);

-- Sprint table
CREATE TABLE IF NOT EXISTS sprint (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    goal TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_sprint_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

CREATE INDEX idx_sprint_project ON sprint(project_id);

-- Tag table
CREATE TABLE IF NOT EXISTS tag (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tag_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

CREATE INDEX idx_tag_project ON tag(project_id);

-- Task table
CREATE TABLE IF NOT EXISTS task (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    sprint_id BIGINT,
    parent_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    assignee_id BIGINT,
    creator_id BIGINT NOT NULL,
    start_date DATE,
    due_date DATE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_sprint FOREIGN KEY (sprint_id) REFERENCES sprint(id) ON DELETE SET NULL,
    CONSTRAINT fk_task_parent FOREIGN KEY (parent_id) REFERENCES task(id) ON DELETE SET NULL,
    CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) REFERENCES user_table(id),
    CONSTRAINT fk_task_creator FOREIGN KEY (creator_id) REFERENCES user_table(id)
);

CREATE INDEX idx_task_project ON task(project_id);
CREATE INDEX idx_task_sprint ON task(sprint_id);
CREATE INDEX idx_task_parent ON task(parent_id);
CREATE INDEX idx_task_assignee ON task(assignee_id);
CREATE INDEX idx_task_creator ON task(creator_id);
CREATE INDEX idx_task_status ON task(status);

-- Task Tag table
CREATE TABLE IF NOT EXISTS task_tag (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    CONSTRAINT fk_task_tag_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE,
    CONSTRAINT uk_task_tag UNIQUE (task_id, tag_id)
);

CREATE INDEX idx_task_tag_task ON task_tag(task_id);
CREATE INDEX idx_task_tag_tag ON task_tag(tag_id);

-- Comment table
CREATE TABLE IF NOT EXISTS comment (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_comment_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES user_table(id)
);

CREATE INDEX idx_comment_task ON comment(task_id);
CREATE INDEX idx_comment_user ON comment(user_id);

-- Document table
CREATE TABLE IF NOT EXISTS document (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    current_version INTEGER NOT NULL DEFAULT 0,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_document_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_document_creator FOREIGN KEY (created_by) REFERENCES user_table(id)
);

CREATE INDEX idx_document_project ON document(project_id);
CREATE INDEX idx_document_creator ON document(created_by);

-- Document Version table
CREATE TABLE IF NOT EXISTS document_version (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    version_no INTEGER NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    change_summary VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_doc_version_document FOREIGN KEY (document_id) REFERENCES document(id) ON DELETE CASCADE,
    CONSTRAINT fk_doc_version_uploader FOREIGN KEY (uploaded_by) REFERENCES user_table(id)
);

CREATE INDEX idx_doc_version_document ON document_version(document_id);

-- Attachment table
CREATE TABLE IF NOT EXISTS attachment (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_attachment_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    CONSTRAINT fk_attachment_uploader FOREIGN KEY (uploaded_by) REFERENCES user_table(id)
);

CREATE INDEX idx_attachment_task ON attachment(task_id);

-- Activity Log table
CREATE TABLE IF NOT EXISTS activity_log (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    task_id BIGINT,
    user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    detail TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_activity_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE SET NULL,
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES user_table(id)
);

CREATE INDEX idx_activity_project ON activity_log(project_id);
CREATE INDEX idx_activity_task ON activity_log(task_id);
CREATE INDEX idx_activity_user ON activity_log(user_id);
