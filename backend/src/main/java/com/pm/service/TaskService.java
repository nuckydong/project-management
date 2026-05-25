package com.pm.service;

import com.pm.common.PageResult;
import com.pm.dto.request.TaskBatchRequest;
import com.pm.dto.request.TaskCreateRequest;
import com.pm.dto.request.TaskQueryRequest;
import com.pm.dto.request.TaskUpdateRequest;
import com.pm.dto.response.TaskResponse;

import java.util.List;

public interface TaskService {

    List<TaskResponse> listMyTasks(Long userId, String status, String keyword);

    PageResult<TaskResponse> list(Long projectId, TaskQueryRequest request);

    List<TaskResponse> listByStatus(Long projectId, String status);

    TaskResponse create(Long projectId, Long userId, TaskCreateRequest request);

    TaskResponse update(Long id, TaskUpdateRequest request);

    void delete(Long id);

    TaskResponse getById(Long id);

    TaskResponse updateStatus(Long id, String status, Long userId);

    TaskResponse assign(Long id, Long assigneeId, Long userId);

    void batch(TaskBatchRequest request, Long userId);
}
