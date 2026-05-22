package com.pm.service;

import com.pm.dto.request.CommentCreateRequest;
import com.pm.dto.request.CommentUpdateRequest;
import com.pm.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {

    List<CommentResponse> list(Long taskId);

    CommentResponse create(Long taskId, Long userId, CommentCreateRequest request);

    CommentResponse update(Long id, CommentUpdateRequest request);

    void delete(Long id);
}
