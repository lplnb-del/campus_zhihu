package com.campus.zhihu.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传响应 DTO
 *
 * @author CampusZhihu Team
 */
@Data
public class FileUploadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件URL
     */
    private String url;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 文件类型（MIME类型）
     */
    private String contentType;

    /**
     * 文件扩展名
     */
    private String extension;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 对象名称（在MinIO中的路径）
     */
    private String objectName;
}