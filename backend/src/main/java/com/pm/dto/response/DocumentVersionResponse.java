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
public class DocumentVersionResponse {

    private Long id;
    private Long documentId;
    private Integer versionNo;
    private String fileName;
    private Long fileSize;
    private UserResponse uploadedBy;
    private String changeSummary;
    private String downloadUrl;
    private LocalDateTime createdAt;
}
