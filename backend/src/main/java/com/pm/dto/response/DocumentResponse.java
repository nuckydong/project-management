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
public class DocumentResponse {

    private Long id;
    private Long projectId;
    private String title;
    private String type;
    private Integer currentVersion;
    private UserResponse createdBy;
    private DocumentVersionResponse currentVersionInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
