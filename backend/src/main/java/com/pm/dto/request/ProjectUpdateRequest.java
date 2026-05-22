package com.pm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUpdateRequest {

    private String name;
    private String description;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}
