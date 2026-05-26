package com.pm.config;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.Setter;
import okhttp3.HttpUrl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Getter
@Setter
public class MinioConfig {

    private String endpoint;

    private String accessKey;

    private String secretKey;
    /**
     * 文档中心的统一上传bucket
     */
    private String bucketsDocs;
    /**
     * 项目中的任务有关的 附件
     */
    private String bucketsAttachments;
    /**
     * 头像
     */
    private String bucketsAvatars;

    /**
     *  初始minio连接客户端实例
     * @return MinioClient
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(HttpUrl.get(endpoint))
                .credentials(accessKey, secretKey)
                .region("ChengDu")
                .build();
    }

}
