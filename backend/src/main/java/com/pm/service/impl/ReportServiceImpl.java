package com.pm.service.impl;

import com.pm.dto.response.*;
import com.pm.entity.Project;
import com.pm.entity.ProjectMember;
import com.pm.entity.Sprint;
import com.pm.entity.Task;
import com.pm.entity.User;
import com.pm.repository.*;
import com.pm.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SprintRepository sprintRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardResponse getDashboard(Long userId) {
        // Aggregate task counts across all user's projects
        List<ProjectMember> memberships = projectMemberRepository.findByUserId(userId);
        List<Long> projectIds = memberships.stream()
                .map(pm -> pm.getProject().getId())
                .collect(Collectors.toList());

        long todoCount = 0;
        long inProgressCount = 0;
        long inReviewCount = 0;
        long doneCount = 0;

        for (Long projectId : projectIds) {
            todoCount += taskRepository.countByProjectIdAndStatus(projectId, "TODO");
            inProgressCount += taskRepository.countByProjectIdAndStatus(projectId, "IN_PROGRESS");
            inReviewCount += taskRepository.countByProjectIdAndStatus(projectId, "IN_REVIEW");
            doneCount += taskRepository.countByProjectIdAndStatus(projectId, "DONE");
        }

        // Project progress
        List<ProjectProgressResponse> projectProgresses = projectIds.stream()
                .map(this::getProjectProgress)
                .collect(Collectors.toList());

        // User's todo tasks
        List<Task> myTodoTasks = taskRepository.findByAssigneeIdAndStatus(userId, "TODO");
        List<TaskResponse> myTodoTaskResponses = myTodoTasks.stream()
                .map(this::toTaskResponseSimple)
                .collect(Collectors.toList());

        // Recent activities
        List<ActivityLogResponse> recentActivities = activityLogRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .filter(al -> projectIds.contains(al.getProject().getId()))
                .map(this::toActivityLogResponse)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .todoCount(todoCount)
                .inProgressCount(inProgressCount)
                .inReviewCount(inReviewCount)
                .doneCount(doneCount)
                .projects(projectProgresses)
                .myTodoTasks(myTodoTaskResponses)
                .recentActivities(recentActivities)
                .build();
    }

    @Override
    public ProjectProgressResponse getProjectProgress(Long projectId) {
        long totalTasks = taskRepository.countByProjectId(projectId);
        long doneTasks = taskRepository.countByProjectIdAndStatus(projectId, "DONE");
        double progress = totalTasks > 0 ? (double) doneTasks / totalTasks * 100 : 0;

        // Get project name
        String projectName = "";
        for (ProjectMember pm : projectMemberRepository.findByProjectId(projectId)) {
            projectName = pm.getProject().getName();
            break;
        }

        return ProjectProgressResponse.builder()
                .projectId(projectId)
                .projectName(projectName)
                .totalTasks(totalTasks)
                .doneTasks(doneTasks)
                .progress(progress)
                .build();
    }

    @Override
    public TaskDistributionResponse getTaskDistribution(Long projectId) {
        return TaskDistributionResponse.builder()
                .todoCount(taskRepository.countByProjectIdAndStatus(projectId, "TODO"))
                .inProgressCount(taskRepository.countByProjectIdAndStatus(projectId, "IN_PROGRESS"))
                .inReviewCount(taskRepository.countByProjectIdAndStatus(projectId, "IN_REVIEW"))
                .doneCount(taskRepository.countByProjectIdAndStatus(projectId, "DONE"))
                .build();
    }

    @Override
    public BurndownResponse getBurndown(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        List<String> dates = new ArrayList<>();
        List<Long> idealRemaining = new ArrayList<>();
        List<Long> actualRemaining = new ArrayList<>();

        if (sprint.getStartDate() != null && sprint.getEndDate() != null) {
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(sprint.getStartDate(), sprint.getEndDate());
            if (totalDays <= 0) totalDays = 1;

            long totalTasksInSprint = taskRepository.countBySprintId(sprintId);

            LocalDate current = sprint.getStartDate();
            LocalDate end = sprint.getEndDate();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            while (!current.isAfter(end)) {
                dates.add(current.format(formatter));

                long dayIndex = java.time.temporal.ChronoUnit.DAYS.between(sprint.getStartDate(), current);
                long ideal = Math.max(0, totalTasksInSprint - (totalTasksInSprint * dayIndex / totalDays));
                idealRemaining.add(ideal);

                long doneTasks = taskRepository.countBySprintIdAndStatus(sprintId, "DONE");
                actualRemaining.add(totalTasksInSprint - doneTasks);

                current = current.plusDays(1);
            }
        }

        return BurndownResponse.builder()
                .dates(dates)
                .idealRemaining(idealRemaining)
                .actualRemaining(actualRemaining)
                .build();
    }

    @Override
    public List<MemberWorkloadResponse> getMemberWorkload(Long projectId) {
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        List<MemberWorkloadResponse> result = new ArrayList<>();

        for (ProjectMember member : members) {
            User user = member.getUser();
            long totalTasks = taskRepository.countByAssigneeIdAndProjectId(user.getId(), projectId);
            long todoCount = taskRepository.countByAssigneeIdAndProjectIdAndStatus(user.getId(), projectId, "TODO");
            long inProgressCount = taskRepository.countByAssigneeIdAndProjectIdAndStatus(user.getId(), projectId, "IN_PROGRESS");
            long doneCount = taskRepository.countByAssigneeIdAndProjectIdAndStatus(user.getId(), projectId, "DONE");

            result.add(MemberWorkloadResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .totalTasks(totalTasks)
                    .todoCount(todoCount)
                    .inProgressCount(inProgressCount)
                    .doneCount(doneCount)
                    .build());
        }

        return result;
    }

    private TaskResponse toTaskResponseSimple(Task task) {
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
                .tags(null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private ActivityLogResponse toActivityLogResponse(com.pm.entity.ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .projectId(log.getProject().getId())
                .taskId(log.getTask() != null ? log.getTask().getId() : null)
                .user(toUserResponse(log.getUser()))
                .action(log.getAction())
                .detail(log.getDetail())
                .createdAt(log.getCreatedAt())
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
