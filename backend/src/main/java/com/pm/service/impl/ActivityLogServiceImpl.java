package com.pm.service.impl;

import com.pm.dto.response.ActivityLogResponse;
import com.pm.dto.response.UserResponse;
import com.pm.entity.ActivityLog;
import com.pm.repository.ActivityLogRepository;
import com.pm.repository.ProjectRepository;
import com.pm.repository.TaskRepository;
import com.pm.repository.UserRepository;
import com.pm.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void log(Long projectId, Long taskId, Long userId, String action, String detail) {
        ActivityLog activityLog = new ActivityLog();
        activityLog.setProject(projectRepository.getReferenceById(projectId));
        if (taskId != null) {
            activityLog.setTask(taskRepository.getReferenceById(taskId));
        }
        activityLog.setUser(userRepository.getReferenceById(userId));
        activityLog.setAction(action);
        activityLog.setDetail(detail);
        activityLogRepository.save(activityLog);
    }

    @Override
    public List<ActivityLogResponse> getByProject(Long projectId) {
        return activityLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityLogResponse> getByTask(Long taskId) {
        return activityLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityLogResponse> getByUser(Long userId) {
        return activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
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

    private UserResponse toUserResponse(com.pm.entity.User user) {
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
