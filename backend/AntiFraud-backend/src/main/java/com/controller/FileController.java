package com.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.common.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private static final String filePath = System.getProperty("user.dir") + "/files/";

    @Value("${fileBaseUrl:}")
    private String fileBaseUrl;

    /**
     * 单文件上传
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        try {
            log.info("开始上传文件: {}, 大小: {} bytes", fileName, file.getSize());
            if (!FileUtil.isDirectory(filePath)) {
                FileUtil.mkdir(filePath);
            }
            fileName = System.currentTimeMillis() + "-" + fileName;
            String realFilePath = filePath + fileName;
            file.transferTo(new java.io.File(realFilePath));
            log.info("文件上传成功：" + fileName);
        } catch (Exception e) {
            log.error(fileName + "--文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
        String url = "/files/" + fileName;
        return Result.success(url);
    }

    /**
     * 多文件上传
     */
    @PostMapping("/upload/batch")
    public Result uploadBatch(@RequestParam("files") MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        try {
            if (!FileUtil.isDirectory(filePath)) {
                FileUtil.mkdir(filePath);
            }
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                fileName = System.currentTimeMillis() + "-" + fileName;
                String realFilePath = filePath + fileName;
                file.transferTo(new java.io.File(realFilePath));
                urls.add("/files/" + fileName);
            }
        } catch (Exception e) {
            log.error("多文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
        return Result.success(urls);
    }

    /**
     * 获取文件
     */
    @GetMapping("/{fileName}")
    public void get(@PathVariable String fileName, HttpServletResponse response) {
        OutputStream os;
        try {
            if (StrUtil.isNotEmpty(fileName)) {
                byte[] bytes = FileUtil.readBytes(filePath + fileName);

                // 根据文件类型设置 Content-Type
                String contentType = getContentType(fileName);
                response.setContentType(contentType);
                response.addHeader("Content-Disposition", "inline;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

                os = response.getOutputStream();
                os.write(bytes);
                os.flush();
                os.close();
            }
        } catch (Exception e) {
            log.warn("文件获取失败：" + fileName);
        }
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".png")) {
            return "image/png";
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        if (fileName.endsWith(".webp")) {
            return "image/webp";
        }
        if (fileName.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            return "application/msword";
        }
        if (fileName.endsWith(".zip")) {
            return "application/zip";
        }
        if (fileName.endsWith(".txt")) {
            return "text/plain";
        }
        if (fileName.endsWith(".mp4")) {
            return "video/mp4";
        }
        // 新增：支持AVI视频格式
        if (fileName.endsWith(".avi")) {
            return "video/x-msvideo";
        }
        // 新增：支持MOV视频格式
        if (fileName.endsWith(".mov")) {
            return "video/quicktime";
        }
        // 新增：支持WMV视频格式
        if (fileName.endsWith(".wmv")) {
            return "video/x-ms-wmv";
        }
        // 新增：支持FLV视频格式
        if (fileName.endsWith(".flv")) {
            return "video/x-flv";
        }
        // 新增：支持MKV视频格式
        if (fileName.endsWith(".mkv")) {
            return "video/x-matroska";
        }
        return "application/octet-stream";
    }
}
