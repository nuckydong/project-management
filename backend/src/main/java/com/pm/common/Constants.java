package com.pm.common;

import lombok.Getter;

public final class Constants {

    private Constants() {
    }

    public static final String TOKEN_PREFIX = "Bearer ";

    // Redis key prefixes
    public static final String REDIS_TOKEN_PREFIX = "pm:token:";
    public static final String REDIS_REFRESH_TOKEN_PREFIX = "pm:refresh:";
    public static final String REDIS_USER_PREFIX = "pm:user:";

    // User status
    @Getter
    public enum UserStatus {
        INACTIVE(0),
        ACTIVE(1),
        DISABLED(2);

        private final int value;

        UserStatus(int value) {
            this.value = value;
        }

    }

    // Task status
    public enum TaskStatus {
        TODO,
        IN_PROGRESS,
        IN_REVIEW,
        DONE
    }

    // Task priority
    public enum TaskPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    // Project status
    public enum ProjectStatus {
        ACTIVE,
        ARCHIVED,
        COMPLETED
    }

    // Sprint status
    public enum SprintStatus {
        PLANNING,
        ACTIVE,
        COMPLETED
    }

    // Document type
    public enum DocumentType {
        REQUIREMENT,
        DESIGN,
        API,
        OTHER
    }

    // Workspace role
    public enum WorkspaceRole {
        OWNER,
        ADMIN,
        MEMBER
    }

    // Project role
    public enum ProjectRole {
        OWNER,
        ADMIN,
        MEMBER,
        VIEWER
    }
}
