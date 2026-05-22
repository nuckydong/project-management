package com.pm.controller;

import com.pm.common.Result;
import com.pm.dto.response.*;
import com.pm.service.ReportService;
import com.pm.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final SecurityUtil securityUtil;

    @GetMapping("/dashboard")
    public Result<DashboardResponse> getDashboard() {
        return Result.success(reportService.getDashboard(securityUtil.getCurrentUserId()));
    }

    @GetMapping("/project/{id}/progress")
    public Result<ProjectProgressResponse> getProjectProgress(@PathVariable Long id) {
        return Result.success(reportService.getProjectProgress(id));
    }

    @GetMapping("/project/{id}/task-distribution")
    public Result<TaskDistributionResponse> getTaskDistribution(@PathVariable Long id) {
        return Result.success(reportService.getTaskDistribution(id));
    }

    @GetMapping("/sprint/{id}/burndown")
    public Result<BurndownResponse> getBurndown(@PathVariable Long id) {
        return Result.success(reportService.getBurndown(id));
    }

    @GetMapping("/project/{id}/member-workload")
    public Result<List<MemberWorkloadResponse>> getMemberWorkload(@PathVariable Long id) {
        return Result.success(reportService.getMemberWorkload(id));
    }
}
