package com.pm.service;

import com.pm.dto.request.DocumentCreateRequest;
import com.pm.dto.request.DocumentUpdateRequest;
import com.pm.dto.response.DocumentResponse;
import com.pm.dto.response.DocumentVersionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    List<DocumentResponse> list(Long projectId);

    DocumentResponse create(Long projectId, Long userId, DocumentCreateRequest request);

    DocumentResponse update(Long id, DocumentUpdateRequest request);

    void delete(Long id);

    DocumentResponse getById(Long id);

    DocumentVersionResponse uploadVersion(Long documentId, MultipartFile file, Long userId, String changeSummary);

    List<DocumentVersionResponse> getVersions(Long documentId);

    String downloadVersion(Long versionId);

    String previewVersion(Long versionId);
}
