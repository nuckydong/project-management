package com.pm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {

    private long todoCount;
    private long inProgressCount;
    private long inReviewCount;
    private long doneCount;
    private List<ProjectProgressResponse> projects;
    private List<TaskResponse> myTodoTasks;
    private List<ActivityLogResponse> recentActivities;
}
