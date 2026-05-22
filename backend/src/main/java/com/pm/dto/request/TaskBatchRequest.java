package com.pm.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskBatchRequest {

    @NotEmpty(message = "Task IDs cannot be empty")
    private List<Long> taskIds;

    private String status;
    private String priority;
    private Long assigneeId;
    private Long sprintId;
}
