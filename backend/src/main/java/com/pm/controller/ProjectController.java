package com.pm.controller;

import com.pm.common.Result;
import com.pm.dto.request.MemberAddRequest;
import com.pm.dto.request.MemberRoleUpdateRequest;
import com.pm.dto.request.ProjectCreateRequest;
import com.pm.dto.request.ProjectUpdateRequest;
import com.pm.dto.response.ProjectMemberResponse;
import com.pm.dto.response.ProjectResponse;
import com.pm.service.ProjectService;
import com.pm.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public Result<List<ProjectResponse>> list(@RequestParam(required = false) Long workspaceId) {
        if (workspaceId != null) {
            return Result.success(projectService.listByWorkspace(workspaceId));
        }
        return Result.success(projectService.listByUser(securityUtil.getCurrentUserId()));
    }

    @PostMapping
    public Result<ProjectResponse> create(@Valid @RequestBody ProjectCreateRequest request) {
        return Result.success(projectService.create(securityUtil.getCurrentUserId(), request));
    }

    @GetMapping("/{id}")
    public Result<ProjectResponse> getById(@PathVariable Long id) {
        return Result.success(projectService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<ProjectResponse> update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
        return Result.success(projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/members")
    public Result<List<ProjectMemberResponse>> getMembers(@PathVariable Long id) {
        return Result.success(projectService.getMembers(id));
    }

    @PostMapping("/{id}/members")
    public Result<ProjectMemberResponse> addMember(@PathVariable Long id, @Valid @RequestBody MemberAddRequest request) {
        return Result.success(projectService.addMember(id, request));
    }

    @PutMapping("/{id}/members/{uid}")
    public Result<ProjectMemberResponse> updateMemberRole(@PathVariable Long id, @PathVariable Long uid,
                                                          @Valid @RequestBody MemberRoleUpdateRequest request) {
        return Result.success(projectService.updateMemberRole(id, uid, request));
    }

    @DeleteMapping("/{id}/members/{uid}")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long uid) {
        projectService.removeMember(id, uid);
        return Result.success();
    }
}
