package com.pm.controller;

import com.pm.common.Result;
import com.pm.dto.response.AttachmentResponse;
import com.pm.service.AttachmentService;
import com.pm.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final SecurityUtil securityUtil;

    @PostMapping("/api/tasks/{id}/attachments")
    public Result<AttachmentResponse> upload(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return Result.success(attachmentService.upload(id, securityUtil.getCurrentUserId(), file));
    }

    @GetMapping("/api/tasks/{id}/attachments")
    public Result<List<AttachmentResponse>> list(@PathVariable Long id) {
        return Result.success(attachmentService.list(id));
    }

    @DeleteMapping("/api/attachments/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        attachmentService.delete(id);
        return Result.success();
    }
}
