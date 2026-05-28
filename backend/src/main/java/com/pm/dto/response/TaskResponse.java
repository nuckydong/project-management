package com.pm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private Integer progress;
    private List<TagResponse> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TagResponse {
        private Long id;
        private String name;
        private String color;
    }
}
