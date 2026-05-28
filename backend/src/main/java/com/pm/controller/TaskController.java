package com.pm.controller;

import com.pm.common.PageResult;
import com.pm.common.Result;
import com.pm.dto.request.*;
import com.pm.dto.response.TaskResponse;
import com.pm.service.TaskService;
import com.pm.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final SecurityUtil securityUtil;

    @GetMapping("/api/projects/{pid}/tasks")
    public Result<PageResult<TaskResponse>> list(@PathVariable Long pid, TaskQueryRequest queryRequest) {
        return Result.success(taskService.list(pid, queryRequest));
    }

    @GetMapping("/api/my/tasks")
    public Result<List<TaskResponse>> listMyTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return Result.success(taskService.listMyTasks(securityUtil.getCurrentUserId(), status, keyword));
    }

    @PostMapping("/api/projects/{pid}/tasks")
    public Result<TaskResponse> create(@PathVariable Long pid, @Valid @RequestBody TaskCreateRequest request) {
        return Result.success(taskService.create(pid, securityUtil.getCurrentUserId(), request));
    }

    @GetMapping("/api/tasks/{id}")
    public Result<TaskResponse> getById(@PathVariable Long id) {
        return Result.success(taskService.getById(id));
    }

    @PutMapping("/api/tasks/{id}")
    public Result<TaskResponse> update(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest request) {
        return Result.success(taskService.update(id, securityUtil.getCurrentUserId(), request));
    }

    @DeleteMapping("/api/tasks/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return Result.success();
    }

    @PutMapping("/api/tasks/{id}/status")
    public Result<TaskResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusUpdateRequest request) {
        return Result.success(taskService.updateStatus(id, request.getStatus(), securityUtil.getCurrentUserId()));
    }

    @PutMapping("/api/tasks/{id}/assignee")
    public Result<TaskResponse> assign(@PathVariable Long id, @Valid @RequestBody TaskAssignRequest request) {
        return Result.success(taskService.assign(id, request.getAssigneeId(), securityUtil.getCurrentUserId()));
    }

    @PutMapping("/api/tasks/batch")
    public Result<Void> batch(@Valid @RequestBody TaskBatchRequest request) {
        taskService.batch(request, securityUtil.getCurrentUserId());
        return Result.success();
    }
}
