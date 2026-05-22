package com.pm.controller;

import com.pm.common.Result;
import com.pm.dto.request.SprintCreateRequest;
import com.pm.dto.request.SprintUpdateRequest;
import com.pm.dto.response.SprintResponse;
import com.pm.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @GetMapping("/api/projects/{pid}/sprints")
    public Result<List<SprintResponse>> list(@PathVariable Long pid) {
        return Result.success(sprintService.list(pid));
    }

    @PostMapping("/api/projects/{pid}/sprints")
    public Result<SprintResponse> create(@PathVariable Long pid, @Valid @RequestBody SprintCreateRequest request) {
        return Result.success(sprintService.create(pid, request));
    }

    @GetMapping("/api/sprints/{id}")
    public Result<SprintResponse> getById(@PathVariable Long id) {
        return Result.success(sprintService.getById(id));
    }

    @PutMapping("/api/sprints/{id}")
    public Result<SprintResponse> update(@PathVariable Long id, @Valid @RequestBody SprintUpdateRequest request) {
        return Result.success(sprintService.update(id, request));
    }

    @DeleteMapping("/api/sprints/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sprintService.delete(id);
        return Result.success();
    }
}
