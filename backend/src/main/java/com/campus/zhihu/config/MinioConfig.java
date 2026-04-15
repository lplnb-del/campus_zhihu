package com.campus.zhihu.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 对象存储配置类
 *
 * @author CampusZhihu Team
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /**
     * MinIO 服务地址
     */
    private String endpoint;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 密钥
     */
    private String secretKey;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 创建 MinIO 客户端
     * 如果配置不完整，返回null，由使用方检查
     */
    @Bean
    @ConditionalOnProperty(prefix = "minio", name = "endpoint")
    public MinioClient minioClient() {
        if (endpoint == null || endpoint.isEmpty()) {
            return null;
        }
        if (accessKey == null || accessKey.isEmpty()) {
            return null;
        }
        if (secretKey == null || secretKey.isEmpty()) {
            return null;
        }
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}