package com.pm.service;

import com.pm.dto.response.ActivityLogResponse;

import java.util.List;

public interface ActivityLogService {

    void log(Long projectId, Long taskId, Long userId, String action, String detail);

    List<ActivityLogResponse> getByProject(Long projectId);

    List<ActivityLogResponse> getByTask(Long taskId);

    List<ActivityLogResponse> getByUser(Long userId);
}
