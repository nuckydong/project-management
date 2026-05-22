package com.pm.service.impl;

import com.pm.dto.request.MemberAddRequest;
import com.pm.dto.request.MemberRoleUpdateRequest;
import com.pm.dto.request.WorkspaceCreateRequest;
import com.pm.dto.request.WorkspaceUpdateRequest;
import com.pm.dto.response.UserResponse;
import com.pm.dto.response.WorkspaceMemberResponse;
import com.pm.dto.response.WorkspaceResponse;
import com.pm.entity.User;
import com.pm.entity.Workspace;
import com.pm.entity.WorkspaceMember;
import com.pm.repository.UserRepository;
import com.pm.repository.WorkspaceMemberRepository;
import com.pm.repository.WorkspaceRepository;
import com.pm.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    @Override
    public List<WorkspaceResponse> listByUser(Long userId) {
        List<WorkspaceMember> memberships = workspaceMemberRepository.findByUserId(userId);
        return memberships.stream()
                .map(WorkspaceMember::getWorkspace)
                .map(this::toWorkspaceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkspaceResponse create(Long userId, WorkspaceCreateRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        workspace.setOwner(owner);
        workspace = workspaceRepository.save(workspace);

        // Auto-add creator as OWNER member
        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(workspace);
        member.setUser(owner);
        member.setRole("OWNER");
        workspaceMemberRepository.save(member);

        return toWorkspaceResponse(workspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse update(Long id, WorkspaceUpdateRequest request) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        if (request.getName() != null) {
            workspace.setName(request.getName());
        }
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }

        workspace = workspaceRepository.save(workspace);
        return toWorkspaceResponse(workspace);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        workspaceRepository.deleteById(id);
    }

    @Override
    public WorkspaceResponse getById(Long id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
        return toWorkspaceResponse(workspace);
    }

    @Override
    public List<WorkspaceMemberResponse> getMembers(Long workspaceId) {
        return workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::toWorkspaceMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkspaceMemberResponse addMember(Long workspaceId, MemberAddRequest request) {
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, request.getUserId())) {
            throw new RuntimeException("User is already a member of this workspace");
        }

        Workspace workspace = workspaceRepository.getReferenceById(workspaceId);
        User user = userRepository.getReferenceById(request.getUserId());

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(workspace);
        member.setUser(user);
        member.setRole(request.getRole());
        member = workspaceMemberRepository.save(member);

        return toWorkspaceMemberResponse(member);
    }

    @Override
    @Transactional
    public WorkspaceMemberResponse updateMemberRole(Long workspaceId, Long userId, MemberRoleUpdateRequest request) {
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found in this workspace"));

        member.setRole(request.getRole());
        member = workspaceMemberRepository.save(member);

        return toWorkspaceMemberResponse(member);
    }

    @Override
    @Transactional
    public void removeMember(Long workspaceId, Long userId) {
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found in this workspace"));

        workspaceMemberRepository.delete(member);
    }

    private WorkspaceResponse toWorkspaceResponse(Workspace workspace) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .owner(toUserResponse(workspace.getOwner()))
                .createdAt(workspace.getCreatedAt())
                .build();
    }

    private WorkspaceMemberResponse toWorkspaceMemberResponse(WorkspaceMember member) {
        return WorkspaceMemberResponse.builder()
                .id(member.getId())
                .user(toUserResponse(member.getUser()))
                .role(member.getRole())
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
