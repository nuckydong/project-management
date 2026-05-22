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
public class AttachmentResponse {

    private Long id;
    private Long taskId;
    private String fileName;
    private Long fileSize;
    private UserResponse uploadedBy;
    private String downloadUrl;
    private LocalDateTime createdAt;
}
