package com.pm.service;

import com.pm.dto.request.MemberAddRequest;
import com.pm.dto.request.MemberRoleUpdateRequest;
import com.pm.dto.request.ProjectCreateRequest;
import com.pm.dto.request.ProjectUpdateRequest;
import com.pm.dto.response.ProjectMemberResponse;
import com.pm.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {

    List<ProjectResponse> listByWorkspace(Long workspaceId);

    List<ProjectResponse> listByUser(Long userId);

    ProjectResponse create(Long userId, ProjectCreateRequest request);

    ProjectResponse update(Long id, ProjectUpdateRequest request);

    void delete(Long id);

    ProjectResponse getById(Long id);

    List<ProjectMemberResponse> getMembers(Long projectId);

    ProjectMemberResponse addMember(Long projectId, MemberAddRequest request);

    ProjectMemberResponse updateMemberRole(Long projectId, Long userId, MemberRoleUpdateRequest request);

    void removeMember(Long projectId, Long userId);
}
