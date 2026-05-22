package com.pm.service.impl;

import com.pm.service.MinioService;
import com.pm.util.MinioUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioUtil minioUtil;

    @Override
    public String uploadDocument(Long projectId, MultipartFile file) {
        String objectName = "projects/" + projectId + "/docs/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        minioUtil.uploadFile(minioUtil.getDocsBucket(), objectName, file);
        return objectName;
    }

    @Override
    public String uploadAttachment(Long taskId, MultipartFile file) {
        String objectName = "tasks/" + taskId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        minioUtil.uploadFile(minioUtil.getAttachmentsBucket(), objectName, file);
        return objectName;
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        String objectName = "users/" + userId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        minioUtil.uploadFile(minioUtil.getAvatarsBucket(), objectName, file);
        return objectName;
    }

    @Override
    public String getPresignedUrl(String bucket, String objectName) {
        return minioUtil.getPresignedUrl(bucket, objectName);
    }

    @Override
    public void deleteFile(String bucket, String objectName) {
        minioUtil.deleteFile(bucket, objectName);
    }

    @Override
    public void initBuckets() {
        minioUtil.createBucket(minioUtil.getDocsBucket());
        minioUtil.createBucket(minioUtil.getAttachmentsBucket());
        minioUtil.createBucket(minioUtil.getAvatarsBucket());
        log.info("All MinIO buckets initialized");
    }
}
