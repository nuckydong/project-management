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
public class WorkspaceResponse {

    private Long id;
    private String name;
    private String description;
    private UserResponse owner;
    private LocalDateTime createdAt;
}
