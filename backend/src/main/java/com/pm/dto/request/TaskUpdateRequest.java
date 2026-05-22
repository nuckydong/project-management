package com.pm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
