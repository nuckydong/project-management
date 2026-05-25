package com.pm.service.impl;

import com.pm.common.PageResult;
import com.pm.dto.request.TaskBatchRequest;
import com.pm.dto.request.TaskCreateRequest;
import com.pm.dto.request.TaskQueryRequest;
import com.pm.dto.request.TaskUpdateRequest;
import com.pm.dto.response.TaskResponse;
import com.pm.dto.response.UserResponse;
import com.pm.entity.Project;
import com.pm.entity.Sprint;
import com.pm.entity.Tag;
import com.pm.entity.Task;
import com.pm.entity.TaskTag;
import com.pm.entity.User;
import com.pm.repository.ProjectRepository;
import com.pm.repository.SprintRepository;
import com.pm.repository.TaskRepository;
import com.pm.repository.TaskTagRepository;
import com.pm.repository.UserRepository;
import com.pm.service.ActivityLogService;
import com.pm.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SprintRepository sprintRepository;
    private final TaskTagRepository taskTagRepository;
    private final ActivityLogService activityLogService;

    @Override
    public List<TaskResponse> listMyTasks(Long userId, String status, String keyword) {
        boolean keywordEmpty = keyword == null || keyword.isBlank();
        return taskRepository.findByAssigneeIdWithFilters(userId, status, keyword, keywordEmpty).stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<TaskResponse> list(Long projectId, TaskQueryRequest request) {
        boolean keywordEmpty = request.getKeyword() == null || request.getKeyword().isBlank();
        List<Task> tasks = taskRepository.findByFilters(
                projectId,
                request.getStatus(),
                request.getAssigneeId(),
                request.getSprintId(),
                request.getPriority(),
                request.getKeyword(),
                keywordEmpty
        );

        int page = request.getPage() != null ? request.getPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 20;
        long total = tasks.size();

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, tasks.size());

        List<TaskResponse> content;
        if (fromIndex >= tasks.size()) {
            content = Collections.emptyList();
        } else {
            content = tasks.subList(fromIndex, toIndex).stream()
                    .map(this::toTaskResponse)
                    .collect(Collectors.toList());
        }

        return new PageResult<>(content, total, page, pageSize);
    }

    @Override
    public List<TaskResponse> listByStatus(Long projectId, String status) {
        return taskRepository.findByProjectIdAndStatus(projectId, status).stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskResponse create(Long projectId, Long userId, TaskCreateRequest request) {
        Project project = projectRepository.getReferenceById(projectId);
        User creator = userRepository.getReferenceById(userId);

        Task task = new Task();
        task.setProject(project);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : "TODO");
        task.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        task.setCreator(creator);
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        if (request.getAssigneeId() != null) {
            task.setAssignee(userRepository.getReferenceById(request.getAssigneeId()));
        }
        if (request.getSprintId() != null) {
            task.setSprint(sprintRepository.getReferenceById(request.getSprintId()));
        }
        if (request.getParentId() != null) {
            task.setParent(taskRepository.getReferenceById(request.getParentId()));
        }

        task = taskRepository.save(task);

        activityLogService.log(projectId, task.getId(), userId, "CREATE_TASK", "Created task: " + task.getTitle());

        return toTaskResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse update(Long id, TaskUpdateRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getAssigneeId() != null) {
            task.setAssignee(userRepository.getReferenceById(request.getAssigneeId()));
        }
        if (request.getSprintId() != null) {
            task.setSprint(sprintRepository.getReferenceById(request.getSprintId()));
        }
        if (request.getStartDate() != null) {
            task.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getSortOrder() != null) {
            task.setSortOrder(request.getSortOrder());
        }

        task = taskRepository.save(task);
        return toTaskResponse(task);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public TaskResponse getById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return toTaskResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateStatus(Long id, String status, Long userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(status);
        task = taskRepository.save(task);

        activityLogService.log(task.getProject().getId(), id, userId, "UPDATE_STATUS", "Changed status to: " + status);

        return toTaskResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse assign(Long id, Long assigneeId, Long userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (assigneeId != null) {
            task.setAssignee(userRepository.getReferenceById(assigneeId));
        } else {
            task.setAssignee(null);
        }
        task = taskRepository.save(task);

        String detail = assigneeId != null ? "Assigned task to user: " + assigneeId : "Unassigned task";
        activityLogService.log(task.getProject().getId(), id, userId, "ASSIGN_TASK", detail);

        return toTaskResponse(task);
    }

    @Override
    @Transactional
    public void batch(TaskBatchRequest request, Long userId) {
        for (Long taskId : request.getTaskIds()) {
            Task task = taskRepository.findById(taskId).orElse(null);
            if (task == null) continue;

            if (request.getStatus() != null) {
                task.setStatus(request.getStatus());
            }
            if (request.getPriority() != null) {
                task.setPriority(request.getPriority());
            }
            if (request.getAssigneeId() != null) {
                task.setAssignee(userRepository.getReferenceById(request.getAssigneeId()));
            }
            if (request.getSprintId() != null) {
                task.setSprint(sprintRepository.getReferenceById(request.getSprintId()));
            }

            taskRepository.save(task);
        }
    }

    private TaskResponse toTaskResponse(Task task) {
        List<TaskTag> taskTags = taskTagRepository.findByTaskId(task.getId());

        List<TaskResponse.TagResponse> tags = taskTags.stream()
                .map(tt -> {
                    Tag tag = tt.getTag();
                    return TaskResponse.TagResponse.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .color(tag.getColor())
                            .build();
                })
                .collect(Collectors.toList());

        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .parentId(task.getParent() != null ? task.getParent().getId() : null)
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assignee(task.getAssignee() != null ? toUserResponse(task.getAssignee()) : null)
                .creator(toUserResponse(task.getCreator()))
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .sortOrder(task.getSortOrder())
                .tags(tags)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
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
