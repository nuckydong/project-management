package com.pm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberWorkloadResponse {

    private Long userId;
    private String username;
    private long totalTasks;
    private long todoCount;
    private long inProgressCount;
    private long doneCount;
}
