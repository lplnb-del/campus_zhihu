package com.campus.zhihu.util;

import com.campus.zhihu.config.MinioConfig;
import com.campus.zhihu.dto.FileUploadDTO;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 工具类
 *
 * @author CampusZhihu Team
 */
@Slf4j
@Component
public class MinioUtil {

    @Autowired(required = false)
    private MinioClient minioClient;

    @Autowired(required = false)
    private MinioConfig minioConfig;

    /**
     * 检查存储桶是否存在，不存在则创建（安全版本，不抛异常）
     */
    public void checkBucketExistsSafe() {
        if (minioClient == null || minioConfig == null) {
            log.warn("MinIO客户端未配置，跳过存储桶检查");
            return;
        }
        try {
            String bucketName = minioConfig.getBucketName();
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!found) {
                // 创建存储桶
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );

                // 设置存储桶策略为公开读取
                String policy = """
                        {
                            "Version": "2012-10-17",
                            "Statement": [
                                {
                                    "Effect": "Allow",
                                    "Principal": "*",
                                    "Action": ["s3:GetObject"],
                                    "Resource": ["arn:aws:s3:::%s/*"]
                                }
                            ]
                        }
                        """.formatted(bucketName);

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policy)
                                .build()
                );

                log.info("存储桶 {} 创建成功", bucketName);
            }
        } catch (Exception e) {
            log.error("检查存储桶失败，将延迟创建", e);
            // 不抛出异常，允许应用启动
        }
    }

    /**
     * 检查存储桶是否存在，不存在则创建（抛出版本）
     */
    public void checkBucketExists() {
        try {
            String bucketName = minioConfig.getBucketName();
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!found) {
                // 创建存储桶
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );

                // 设置存储桶策略为公开读取
                String policy = """
                        {
                            "Version": "2012-10-17",
                            "Statement": [
                                {
                                    "Effect": "Allow",
                                    "Principal": "*",
                                    "Action": ["s3:GetObject"],
                                    "Resource": ["arn:aws:s3:::%s/*"]
                                }
                            ]
                        }
                        """.formatted(bucketName);

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policy)
                                .build()
                );

                log.info("存储桶 {} 创建成功", bucketName);
            }
        } catch (Exception e) {
            log.error("检查存储桶失败", e);
            throw new RuntimeException("检查存储桶失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件上传信息
     */
    public FileUploadDTO uploadFile(MultipartFile file) {
        if (minioClient == null || minioConfig == null) {
            throw new RuntimeException("MinIO服务未配置，无法上传文件");
        }
        try {
            // 检查存储桶
            checkBucketExistsSafe();

            // 生成文件名（UUID + 原始扩展名）
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String objectName = generateObjectName(extension);

            // 获取文件输入流
            InputStream inputStream = file.getInputStream();

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 关闭输入流
            inputStream.close();

            // 构建文件URL
            String fileUrl = getFileUrl(objectName);

            // 构建返回信息
            FileUploadDTO uploadDTO = new FileUploadDTO();
            uploadDTO.setFilename(originalFilename);
            uploadDTO.setUrl(fileUrl);
            uploadDTO.setSize(file.getSize());
            uploadDTO.setContentType(file.getContentType());
            uploadDTO.setExtension(extension);
            uploadDTO.setBucketName(minioConfig.getBucketName());
            uploadDTO.setObjectName(objectName);

            log.info("文件上传成功: {}", objectName);
            return uploadDTO;

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) {
        if (minioClient == null || minioConfig == null) {
            throw new RuntimeException("MinIO服务未配置，无法删除文件");
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件URL
     *
     * @param objectName 对象名称
     * @return 文件URL
     */
    public String getFileUrl(String objectName) {
        if (minioConfig == null) {
            throw new RuntimeException("MinIO配置未找到");
        }
        return String.format("%s/%s/%s",
                minioConfig.getEndpoint(),
                minioConfig.getBucketName(),
                objectName);
    }

    /**
     * 获取临时访问URL（带过期时间）
     *
     * @param objectName 对象名称
     * @param expires    过期时间（秒）
     * @return 临时访问URL
     */
    public String getPresignedUrl(String objectName, int expires) {
        if (minioClient == null || minioConfig == null) {
            throw new RuntimeException("MinIO服务未配置，无法获取临时URL");
        }
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .expiry(expires, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取临时URL失败", e);
            throw new RuntimeException("获取临时URL失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 生成对象名称（UUID + 扩展名）
     *
     * @param extension 扩展名
     * @return 对象名称
     */
    private String generateObjectName(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (extension == null || extension.isEmpty()) {
            return uuid;
        }
        return uuid + "." + extension;
    }

    /**
     * 验证文件类型
     *
     * @param contentType MIME类型
     * @return 是否允许上传
     */
    public boolean isValidFileType(String contentType) {
        // 允许的图片类型
        String[] allowedTypes = {
                "image/jpeg",
                "image/jpg",
                "image/png",
                "image/gif",
                "image/webp",
                "image/svg+xml"
        };

        for (String type : allowedTypes) {
            if (type.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证文件大小
     *
     * @param size 文件大小（字节）
     * @return 是否允许上传
     */
    public boolean isValidFileSize(long size) {
        // 最大10MB
        long maxSize = 10 * 1024 * 1024;
        return size > 0 && size <= maxSize;
    }
}