package com.pm.controller;

import com.pm.common.Result;
import com.pm.dto.request.DocumentCreateRequest;
import com.pm.dto.request.DocumentUpdateRequest;
import com.pm.dto.response.DocumentResponse;
import com.pm.dto.response.DocumentVersionResponse;
import com.pm.service.DocumentService;
import com.pm.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final SecurityUtil securityUtil;

    @GetMapping("/api/projects/{pid}/documents")
    public Result<List<DocumentResponse>> list(@PathVariable Long pid) {
        return Result.success(documentService.list(pid));
    }

    @PostMapping("/api/projects/{pid}/documents")
    public Result<DocumentResponse> create(@PathVariable Long pid, @Valid @RequestBody DocumentCreateRequest request) {
        return Result.success(documentService.create(pid, securityUtil.getCurrentUserId(), request));
    }

    @GetMapping("/api/documents/{id}")
    public Result<DocumentResponse> getById(@PathVariable Long id) {
        return Result.success(documentService.getById(id));
    }

    @PutMapping("/api/documents/{id}")
    public Result<DocumentResponse> update(@PathVariable Long id, @Valid @RequestBody DocumentUpdateRequest request) {
        return Result.success(documentService.update(id, request));
    }

    @DeleteMapping("/api/documents/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return Result.success();
    }

    @PostMapping("/api/documents/{id}/versions")
    public Result<DocumentVersionResponse> uploadVersion(@PathVariable Long id,
                                                         @RequestParam("file") MultipartFile file,
                                                         @RequestParam(value = "changeSummary", required = false) String changeSummary) {
        return Result.success(documentService.uploadVersion(id, file, securityUtil.getCurrentUserId(), changeSummary));
    }

    @GetMapping("/api/documents/{id}/versions")
    public Result<List<DocumentVersionResponse>> getVersions(@PathVariable Long id) {
        return Result.success(documentService.getVersions(id));
    }

    @GetMapping("/api/document-versions/{vid}/download")
    public Result<String> downloadVersion(@PathVariable Long vid) {
        return Result.success(documentService.downloadVersion(vid));
    }

    @GetMapping("/api/document-versions/{vid}/preview")
    public Result<String> previewVersion(@PathVariable Long vid) {
        return Result.success(documentService.previewVersion(vid));
    }
}
