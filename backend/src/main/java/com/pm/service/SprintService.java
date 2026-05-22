package com.pm.service;

import com.pm.dto.request.SprintCreateRequest;
import com.pm.dto.request.SprintUpdateRequest;
import com.pm.dto.response.SprintResponse;

import java.util.List;

public interface SprintService {

    List<SprintResponse> list(Long projectId);

    SprintResponse create(Long projectId, SprintCreateRequest request);

    SprintResponse update(Long id, SprintUpdateRequest request);

    void delete(Long id);

    SprintResponse getById(Long id);
}
