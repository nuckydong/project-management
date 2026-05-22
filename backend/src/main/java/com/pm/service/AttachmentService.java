package com.pm.service;

import com.pm.dto.response.AttachmentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    AttachmentResponse upload(Long taskId, Long userId, MultipartFile file);

    List<AttachmentResponse> list(Long taskId);

    void delete(Long id);
}
