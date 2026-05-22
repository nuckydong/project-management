package com.pm.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {

    String uploadDocument(Long projectId, MultipartFile file);

    String uploadAttachment(Long taskId, MultipartFile file);

    String uploadAvatar(Long userId, MultipartFile file);

    String getPresignedUrl(String bucket, String objectName);

    void deleteFile(String bucket, String objectName);

    void initBuckets();
}
