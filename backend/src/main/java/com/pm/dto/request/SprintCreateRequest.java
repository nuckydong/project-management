package com.pm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SprintCreateRequest {

    @NotBlank(message = "Sprint name is required")
    private String name;

    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
}
