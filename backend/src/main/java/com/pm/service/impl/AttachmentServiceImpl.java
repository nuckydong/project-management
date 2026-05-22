package com.pm.service.impl;

import com.pm.dto.response.AttachmentResponse;
import com.pm.dto.response.UserResponse;
import com.pm.entity.Attachment;
import com.pm.entity.Task;
import com.pm.entity.User;
import com.pm.repository.AttachmentRepository;
import com.pm.repository.TaskRepository;
import com.pm.repository.UserRepository;
import com.pm.service.AttachmentService;
import com.pm.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;

    @Override
    @Transactional
    public AttachmentResponse upload(Long taskId, Long userId, MultipartFile file) {
        Task task = taskRepository.getReferenceById(taskId);
        User user = userRepository.getReferenceById(userId);

        String filePath = minioService.uploadAttachment(taskId, file);

        Attachment attachment = new Attachment();
        attachment.setTask(task);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFilePath(filePath);
        attachment.setFileSize(file.getSize());
        attachment.setUploadedBy(user);
        attachment = attachmentRepository.save(attachment);

        return toAttachmentResponse(attachment);
    }

    @Override
    public List<AttachmentResponse> list(Long taskId) {
        return attachmentRepository.findByTaskId(taskId).stream()
                .map(this::toAttachmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        minioService.deleteFile("attachments", attachment.getFilePath());
        attachmentRepository.delete(attachment);
    }

    private AttachmentResponse toAttachmentResponse(Attachment attachment) {
        String downloadUrl = minioService.getPresignedUrl("attachments", attachment.getFilePath());

        return AttachmentResponse.builder()
                .id(attachment.getId())
                .taskId(attachment.getTask().getId())
                .fileName(attachment.getFileName())
                .fileSize(attachment.getFileSize())
                .uploadedBy(toUserResponse(attachment.getUploadedBy()))
                .downloadUrl(downloadUrl)
                .createdAt(attachment.getCreatedAt())
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
