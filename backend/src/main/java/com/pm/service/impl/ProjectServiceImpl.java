package com.pm.service.impl;

import com.pm.dto.request.MemberAddRequest;
import com.pm.dto.request.MemberRoleUpdateRequest;
import com.pm.dto.request.ProjectCreateRequest;
import com.pm.dto.request.ProjectUpdateRequest;
import com.pm.dto.response.ProjectMemberResponse;
import com.pm.dto.response.ProjectResponse;
import com.pm.dto.response.UserResponse;
import com.pm.entity.Project;
import com.pm.entity.ProjectMember;
import com.pm.entity.User;
import com.pm.entity.Workspace;
import com.pm.repository.ProjectMemberRepository;
import com.pm.repository.ProjectRepository;
import com.pm.repository.UserRepository;
import com.pm.repository.WorkspaceRepository;
import com.pm.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Override
    public List<ProjectResponse> listByWorkspace(Long workspaceId) {
        return projectRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> listByUser(Long userId) {
        List<ProjectMember> memberships = projectMemberRepository.findByUserId(userId);
        return memberships.stream()
                .map(ProjectMember::getProject)
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProjectResponse create(Long userId, ProjectCreateRequest request) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        Project project = new Project();
        project.setWorkspace(workspace);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setCreatedBy(creator);
        project = projectRepository.save(project);

        // Auto-add creator as OWNER member
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(creator);
        member.setRole("OWNER");
        projectMemberRepository.save(member);

        return toProjectResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse update(Long id, ProjectUpdateRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }

        project = projectRepository.save(project);
        return toProjectResponse(project);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        projectRepository.deleteById(id);
    }

    @Override
    public ProjectResponse getById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return toProjectResponse(project);
    }

    @Override
    public List<ProjectMemberResponse> getMembers(Long projectId) {
        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(this::toProjectMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProjectMemberResponse addMember(Long projectId, MemberAddRequest request) {
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.getUserId())) {
            throw new RuntimeException("User is already a member of this project");
        }

        Project project = projectRepository.getReferenceById(projectId);
        User user = userRepository.getReferenceById(request.getUserId());

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(request.getRole());
        member = projectMemberRepository.save(member);

        return toProjectMemberResponse(member);
    }

    @Override
    @Transactional
    public ProjectMemberResponse updateMemberRole(Long projectId, Long userId, MemberRoleUpdateRequest request) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found in this project"));

        member.setRole(request.getRole());
        member = projectMemberRepository.save(member);

        return toProjectMemberResponse(member);
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found in this project"));

        projectMemberRepository.delete(member);
    }

    private ProjectResponse toProjectResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .workspaceId(project.getWorkspace().getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .createdBy(toUserResponse(project.getCreatedBy()))
                .createdAt(project.getCreatedAt())
                .build();
    }

    private ProjectMemberResponse toProjectMemberResponse(ProjectMember member) {
        return ProjectMemberResponse.builder()
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
