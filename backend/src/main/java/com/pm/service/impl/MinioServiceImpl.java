package com.pm.service.impl;

import com.pm.config.MinioConfig;
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

    private final MinioConfig minioConfig;

    @Override
    public String uploadDocument(Long projectId, MultipartFile file) {
        String objectName = "projects/" + projectId + "/docs/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        minioUtil.uploadFile(minioConfig.getBucketsDocs(), objectName, file);
        return objectName;
    }

    @Override
    public String uploadAttachment(Long taskId, MultipartFile file) {
        String objectName = "tasks/" + taskId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        minioUtil.uploadFile(minioConfig.getBucketsAttachments(), objectName, file);
        return objectName;
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        String objectName = "users/" + userId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        minioUtil.uploadFile(minioConfig.getBucketsAvatars(), objectName, file);
        return objectName;
    }

    // TODO  修改替换
    @Deprecated
    @Override
    public String getPresignedUrl(String bucket, String objectName) {
        return minioUtil.getPresignedUrl(bucket, objectName);
    }

    @Override
    public String getPubDocUrl(String objectName) {
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucketsDocs() + "/" + objectName;
    }

    @Override
    public String getPubAvatarsUrl(String objectName) {
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucketsAvatars() + "/" + objectName;
    }

    @Override
    public String getPubAttachmentsUrl(String objectName) {
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucketsAttachments() + "/" + objectName;
    }


    @Override
    public void deleteFile(String bucket, String objectName) {
        minioUtil.deleteFile(bucket, objectName);
    }

    @Override
    public void initBuckets() {
        minioUtil.createBucket(minioConfig.getBucketsDocs());
        minioUtil.createBucket(minioConfig.getBucketsAttachments());
        minioUtil.createBucket(minioConfig.getBucketsAvatars());
        log.info("All MinIO buckets initialized");
    }
}
