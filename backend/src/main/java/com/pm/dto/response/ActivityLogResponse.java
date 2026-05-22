package com.pm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityLogResponse {

    private Long id;
    private Long projectId;
    private Long taskId;
    private UserResponse user;
    private String action;
    private String detail;
    private LocalDateTime createdAt;
}
