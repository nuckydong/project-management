package com.pm.service;

import com.pm.dto.request.MemberAddRequest;
import com.pm.dto.request.MemberRoleUpdateRequest;
import com.pm.dto.request.WorkspaceCreateRequest;
import com.pm.dto.request.WorkspaceUpdateRequest;
import com.pm.dto.response.WorkspaceMemberResponse;
import com.pm.dto.response.WorkspaceResponse;

import java.util.List;

public interface WorkspaceService {

    List<WorkspaceResponse> listByUser(Long userId);

    WorkspaceResponse create(Long userId, WorkspaceCreateRequest request);

    WorkspaceResponse update(Long id, WorkspaceUpdateRequest request);

    void delete(Long id);

    WorkspaceResponse getById(Long id);

    List<WorkspaceMemberResponse> getMembers(Long workspaceId);

    WorkspaceMemberResponse addMember(Long workspaceId, MemberAddRequest request);

    WorkspaceMemberResponse updateMemberRole(Long workspaceId, Long userId, MemberRoleUpdateRequest request);

    void removeMember(Long workspaceId, Long userId);
}
