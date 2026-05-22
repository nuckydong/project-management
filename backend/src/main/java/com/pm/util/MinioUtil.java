package com.pm.util;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtil {

    private final MinioClient minioClient;

    @Value("${minio.buckets.docs}")
    private String docsBucket;

    @Value("${minio.buckets.attachments}")
    private String attachmentsBucket;

    @Value("${minio.buckets.avatars}")
    private String avatarsBucket;

    public String getDocsBucket() {
        return docsBucket;
    }

    public String getAttachmentsBucket() {
        return attachmentsBucket;
    }

    public String getAvatarsBucket() {
        return avatarsBucket;
    }

    public void createBucket(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Created bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Error creating bucket {}: {}", bucketName, e.getMessage());
            throw new RuntimeException("Failed to create bucket: " + bucketName, e);
        }
    }

    public void uploadFile(String bucketName, String objectName, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            log.info("Uploaded file: {}/{}", bucketName, objectName);
        } catch (Exception e) {
            log.error("Error uploading file to {}/{}: {}", bucketName, objectName, e.getMessage());
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public String getPresignedUrl(String bucketName, String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(1, TimeUnit.HOURS)
                    .build());
        } catch (Exception e) {
            log.error("Error generating presigned URL for {}/{}: {}", bucketName, objectName, e.getMessage());
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    public void deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            log.info("Deleted file: {}/{}", bucketName, objectName);
        } catch (Exception e) {
            log.error("Error deleting file {}/{}: {}", bucketName, objectName, e.getMessage());
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
