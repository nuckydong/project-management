package com.pm.service;

import com.pm.dto.response.*;

import java.util.List;

public interface ReportService {

    DashboardResponse getDashboard(Long userId);

    ProjectProgressResponse getProjectProgress(Long projectId);

    TaskDistributionResponse getTaskDistribution(Long projectId);

    BurndownResponse getBurndown(Long sprintId);

    List<MemberWorkloadResponse> getMemberWorkload(Long projectId);
}
