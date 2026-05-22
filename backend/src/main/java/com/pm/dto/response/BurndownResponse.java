package com.pm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BurndownResponse {

    private List<String> dates;
    private List<Long> idealRemaining;
    private List<Long> actualRemaining;
}
