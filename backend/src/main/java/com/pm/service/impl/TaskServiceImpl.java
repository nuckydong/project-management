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
    public TaskResponse update(Long id, Long userId, TaskUpdateRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Long projectId = task.getProject().getId();

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null && !request.getPriority().equals(task.getPriority())) {
            String oldVal = translatePriority(task.getPriority());
            String newVal = translatePriority(request.getPriority());
            task.setPriority(request.getPriority());
            activityLogService.log(projectId, id, userId, "PRIORITY_CHANGE",
                    "将优先级从 " + oldVal + " 改为 " + newVal);
        }
        if (request.getAssigneeId() != null) {
            String oldAssignee = task.getAssignee() != null ? task.getAssignee().getUsername() : "未分配";
            User newAssignee = userRepository.getReferenceById(request.getAssigneeId());
            String newAssigneeName = newAssignee.getUsername();
            if (!request.getAssigneeId().equals(task.getAssignee() != null ? task.getAssignee().getId() : null)) {
                task.setAssignee(newAssignee);
                activityLogService.log(projectId, id, userId, "ASSIGNEE_CHANGE",
                        "将负责人从 " + oldAssignee + " 改为 " + newAssigneeName);
            }
        }
        if (request.getSprintId() != null) {
            task.setSprint(request.getSprintId() != null ? sprintRepository.getReferenceById(request.getSprintId()) : null);
        }
        if (request.getStartDate() != null && !request.getStartDate().equals(task.getStartDate())) {
            String oldDate = task.getStartDate() != null ? task.getStartDate().toString() : "未设置";
            activityLogService.log(projectId, id, userId, "START_DATE_CHANGE",
                    "将开始日期从 " + oldDate + " 改为 " + request.getStartDate());
            task.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null && !request.getDueDate().equals(task.getDueDate())) {
            String oldDate = task.getDueDate() != null ? task.getDueDate().toString() : "未设置";
            activityLogService.log(projectId, id, userId, "DUE_DATE_CHANGE",
                    "将截止日期从 " + oldDate + " 改为 " + request.getDueDate());
            task.setDueDate(request.getDueDate());
        }
        if (request.getSortOrder() != null) {
            task.setSortOrder(request.getSortOrder());
        }
        if (request.getProgress() != null && !request.getProgress().equals(task.getProgress())) {
            int oldProgress = task.getProgress() != null ? task.getProgress() : 0;
            activityLogService.log(projectId, id, userId, "PROGRESS_CHANGE",
                    "将进度从 " + oldProgress + "% 更新为 " + request.getProgress() + "%");
            task.setProgress(request.getProgress());
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

        String oldStatus = task.getStatus();
        task.setStatus(status);
        task = taskRepository.save(task);

        activityLogService.log(task.getProject().getId(), id, userId, "STATUS_CHANGE",
                "将状态从 " + translateStatus(oldStatus) + " 改为 " + translateStatus(status));

        return toTaskResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse assign(Long id, Long assigneeId, Long userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        String oldAssignee = task.getAssignee() != null ? task.getAssignee().getUsername() : "未分配";
        if (assigneeId != null) {
            task.setAssignee(userRepository.getReferenceById(assigneeId));
        } else {
            task.setAssignee(null);
        }
        task = taskRepository.save(task);

        String newAssignee = assigneeId != null ? task.getAssignee().getUsername() : "未分配";
        activityLogService.log(task.getProject().getId(), id, userId, "ASSIGNEE_CHANGE",
                "将负责人从 " + oldAssignee + " 改为 " + newAssignee);

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
                .progress(task.getProgress() != null ? task.getProgress() : 0)
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

    private String translatePriority(String priority) {
        return switch (priority) {
            case "LOW" -> "低";
            case "MEDIUM" -> "中";
            case "HIGH" -> "高";
            case "URGENT" -> "紧急";
            default -> priority;
        };
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "TODO" -> "待办";
            case "IN_PROGRESS" -> "进行中";
            case "IN_REVIEW" -> "待审核";
            case "DONE" -> "已完成";
            default -> status;
        };
    }
}
