package com.pm.service.impl;

import com.pm.dto.request.CommentCreateRequest;
import com.pm.dto.request.CommentUpdateRequest;
import com.pm.dto.response.CommentResponse;
import com.pm.dto.response.UserResponse;
import com.pm.entity.Comment;
import com.pm.entity.Task;
import com.pm.entity.User;
import com.pm.repository.CommentRepository;
import com.pm.repository.TaskRepository;
import com.pm.repository.UserRepository;
import com.pm.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    public List<CommentResponse> list(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponse create(Long taskId, Long userId, CommentCreateRequest request) {
        Task task = taskRepository.getReferenceById(taskId);
        User user = userRepository.getReferenceById(userId);

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);

        return toCommentResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse update(Long id, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);

        return toCommentResponse(comment);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        commentRepository.deleteById(id);
    }

    private CommentResponse toCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTask().getId())
                .user(toUserResponse(comment.getUser()))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
