package com.pm.controller;

import com.pm.common.Result;
import com.pm.dto.request.CommentCreateRequest;
import com.pm.dto.request.CommentUpdateRequest;
import com.pm.dto.response.CommentResponse;
import com.pm.service.CommentService;
import com.pm.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final SecurityUtil securityUtil;

    @GetMapping("/api/tasks/{id}/comments")
    public Result<List<CommentResponse>> list(@PathVariable Long id) {
        return Result.success(commentService.list(id));
    }

    @PostMapping("/api/tasks/{id}/comments")
    public Result<CommentResponse> create(@PathVariable Long id, @Valid @RequestBody CommentCreateRequest request) {
        return Result.success(commentService.create(id, securityUtil.getCurrentUserId(), request));
    }

    @PutMapping("/api/comments/{id}")
    public Result<CommentResponse> update(@PathVariable Long id, @Valid @RequestBody CommentUpdateRequest request) {
        return Result.success(commentService.update(id, request));
    }

    @DeleteMapping("/api/comments/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success();
    }
}
