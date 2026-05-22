package com.pm.controller;

import com.pm.common.Result;
import com.pm.dto.request.MemberAddRequest;
import com.pm.dto.request.MemberRoleUpdateRequest;
import com.pm.dto.request.WorkspaceCreateRequest;
import com.pm.dto.request.WorkspaceUpdateRequest;
import com.pm.dto.response.WorkspaceMemberResponse;
import com.pm.dto.response.WorkspaceResponse;
import com.pm.service.WorkspaceService;
import com.pm.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public Result<List<WorkspaceResponse>> list() {
        return Result.success(workspaceService.listByUser(securityUtil.getCurrentUserId()));
    }

    @PostMapping
    public Result<WorkspaceResponse> create(@Valid @RequestBody WorkspaceCreateRequest request) {
        return Result.success(workspaceService.create(securityUtil.getCurrentUserId(), request));
    }

    @GetMapping("/{id}")
    public Result<WorkspaceResponse> getById(@PathVariable Long id) {
        return Result.success(workspaceService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<WorkspaceResponse> update(@PathVariable Long id, @Valid @RequestBody WorkspaceUpdateRequest request) {
        return Result.success(workspaceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        workspaceService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/members")
    public Result<List<WorkspaceMemberResponse>> getMembers(@PathVariable Long id) {
        return Result.success(workspaceService.getMembers(id));
    }

    @PostMapping("/{id}/members")
    public Result<WorkspaceMemberResponse> addMember(@PathVariable Long id, @Valid @RequestBody MemberAddRequest request) {
        return Result.success(workspaceService.addMember(id, request));
    }

    @PutMapping("/{id}/members/{uid}")
    public Result<WorkspaceMemberResponse> updateMemberRole(@PathVariable Long id, @PathVariable Long uid,
                                                            @Valid @RequestBody MemberRoleUpdateRequest request) {
        return Result.success(workspaceService.updateMemberRole(id, uid, request));
    }

    @DeleteMapping("/{id}/members/{uid}")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long uid) {
        workspaceService.removeMember(id, uid);
        return Result.success();
    }
}
