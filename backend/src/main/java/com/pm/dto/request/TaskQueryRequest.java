package com.pm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskQueryRequest {

    private String status;
    private Long assigneeId;
    private Long sprintId;
    private String priority;
    private String keyword;

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer pageSize = 20;
}
