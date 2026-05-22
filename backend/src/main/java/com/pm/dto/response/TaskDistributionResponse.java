package com.pm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDistributionResponse {

    private long todoCount;
    private long inProgressCount;
    private long inReviewCount;
    private long doneCount;
}
