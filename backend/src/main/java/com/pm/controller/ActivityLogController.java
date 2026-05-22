package com.pm.controller;

import com.pm.common.Result;
import com.pm.dto.response.ActivityLogResponse;
import com.pm.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping("/api/tasks/{id}/activities")
    public Result<List<ActivityLogResponse>> getByTask(@PathVariable Long id) {
        return Result.success(activityLogService.getByTask(id));
    }

    @GetMapping("/api/projects/{id}/activities")
    public Result<List<ActivityLogResponse>> getByProject(@PathVariable Long id) {
        return Result.success(activityLogService.getByProject(id));
    }
}
