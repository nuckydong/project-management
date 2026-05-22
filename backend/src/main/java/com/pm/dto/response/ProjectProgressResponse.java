package com.pm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectProgressResponse {

    private Long projectId;
    private String projectName;
    private long totalTasks;
    private long doneTasks;
    private double progress;
}
