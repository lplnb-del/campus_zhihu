package com.campus.zhihu.controller;

import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.FileUploadDTO;
import com.campus.zhihu.util.MinioUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 *
 * @author CampusZhihu Team
 */
@Slf4j
@RestController
@RequestMapping("/file")
@Tag(name = "文件管理", description = "文件上传、删除等操作")
public class FileController {

    @Autowired
    private MinioUtil minioUtil;

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件上传信息
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传图片文件，支持jpg、png、gif、webp等格式，最大10MB")
    public Result<FileUploadDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件是否为空
            if (file == null || file.isEmpty()) {
                return Result.error(4001, "文件不能为空");
            }

            // 验证文件类型
            if (!minioUtil.isValidFileType(file.getContentType())) {
                return Result.error(4002, "不支持的文件类型，仅支持图片格式");
            }

            // 验证文件大小
            if (!minioUtil.isValidFileSize(file.getSize())) {
                return Result.error(4003, "文件大小超过限制，最大支持10MB");
            }

            // 上传文件
            FileUploadDTO uploadDTO = minioUtil.uploadFile(file);

            return Result.success(uploadDTO);

        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error(5000, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     * @return 操作结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "根据对象名称删除文件")
    public Result<Void> deleteFile(@RequestParam("objectName") String objectName) {
        try {
            if (objectName == null || objectName.trim().isEmpty()) {
                return Result.error(4001, "对象名称不能为空");
            }

            minioUtil.deleteFile(objectName);
            return Result.success();

        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error(5000, "文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件URL
     *
     * @param objectName 对象名称
     * @return 文件URL
     */
    @GetMapping("/url")
    @Operation(summary = "获取文件URL", description = "根据对象名称获取文件访问URL")
    public Result<String> getFileUrl(@RequestParam("objectName") String objectName) {
        try {
            if (objectName == null || objectName.trim().isEmpty()) {
                return Result.error(4001, "对象名称不能为空");
            }

            String url = minioUtil.getFileUrl(objectName);
            return Result.success(url);

        } catch (Exception e) {
            log.error("获取文件URL失败", e);
            return Result.error(5000, "获取文件URL失败: " + e.getMessage());
        }
    }

    /**
     * 获取临时访问URL
     *
     * @param objectName 对象名称
     * @param expires    过期时间（秒），默认3600秒（1小时）
     * @return 临时访问URL
     */
    @GetMapping("/presigned-url")
    @Operation(summary = "获取临时访问URL", description = "获取带过期时间的临时访问URL")
    public Result<String> getPresignedUrl(
            @RequestParam("objectName") String objectName,
            @RequestParam(value = "expires", defaultValue = "3600") Integer expires) {
        try {
            if (objectName == null || objectName.trim().isEmpty()) {
                return Result.error(4001, "对象名称不能为空");
            }

            if (expires == null || expires <= 0) {
                return Result.error(4002, "过期时间必须大于0");
            }

            String url = minioUtil.getPresignedUrl(objectName, expires);
            return Result.success(url);

        } catch (Exception e) {
            log.error("获取临时URL失败", e);
            return Result.error(5000, "获取临时URL失败: " + e.getMessage());
        }
    }
}