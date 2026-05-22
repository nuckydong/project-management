package com.pm.service.impl;

import com.pm.dto.request.SprintCreateRequest;
import com.pm.dto.request.SprintUpdateRequest;
import com.pm.dto.response.SprintResponse;
import com.pm.entity.Project;
import com.pm.entity.Sprint;
import com.pm.repository.ProjectRepository;
import com.pm.repository.SprintRepository;
import com.pm.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;

    @Override
    public List<SprintResponse> list(Long projectId) {
        return sprintRepository.findByProjectId(projectId).stream()
                .map(this::toSprintResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SprintResponse create(Long projectId, SprintCreateRequest request) {
        Project project = projectRepository.getReferenceById(projectId);

        Sprint sprint = new Sprint();
        sprint.setProject(project);
        sprint.setName(request.getName());
        sprint.setGoal(request.getGoal());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint = sprintRepository.save(sprint);

        return toSprintResponse(sprint);
    }

    @Override
    @Transactional
    public SprintResponse update(Long id, SprintUpdateRequest request) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        if (request.getName() != null) {
            sprint.setName(request.getName());
        }
        if (request.getGoal() != null) {
            sprint.setGoal(request.getGoal());
        }
        if (request.getStatus() != null) {
            sprint.setStatus(request.getStatus());
        }
        if (request.getStartDate() != null) {
            sprint.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            sprint.setEndDate(request.getEndDate());
        }

        sprint = sprintRepository.save(sprint);
        return toSprintResponse(sprint);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sprintRepository.deleteById(id);
    }

    @Override
    public SprintResponse getById(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));
        return toSprintResponse(sprint);
    }

    private SprintResponse toSprintResponse(Sprint sprint) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .projectId(sprint.getProject().getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .createdAt(sprint.getCreatedAt())
                .build();
    }
}
