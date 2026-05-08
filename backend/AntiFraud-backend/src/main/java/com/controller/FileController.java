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
@RequestMapping("/file")
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
            log.info("开始上传文件: {}, 大小: {} bytes, MIME类型: {}", fileName, file.getSize(), file.getContentType());
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
                log.info("开始上传文件: {}, MIME类型: {}", fileName, file.getContentType());
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
     * 文件下载/访问（支持CORS）
     */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        try {
            // 处理可能包含子目录的文件名（如 videos/123.mp4）
            String cleanFileName = fileName.replace("%2F", "/").replace("%2f", "/");
            java.io.File file = new java.io.File(filePath + cleanFileName);
            
            log.info("尝试访问文件: {}", file.getAbsolutePath());
            
            if (!file.exists()) {
                log.error("文件不存在: {}", file.getAbsolutePath());
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 设置CORS响应头
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            
            // 设置文件MIME类型
            String mimeType = FileUtil.getMimeType(cleanFileName);
            if (mimeType != null) {
                response.setContentType(mimeType);
            } else {
                response.setContentType("application/octet-stream");
            }
            
            // 设置Content-Length
            response.setContentLengthLong(file.length());
            
            log.info("成功返回文件: {}, 大小: {} bytes, MIME: {}", cleanFileName, file.length(), mimeType);
            
            // 写入文件
            try (OutputStream os = response.getOutputStream()) {
                os.write(FileUtil.readBytes(file));
                os.flush();
            }
        } catch (Exception e) {
            log.error("文件下载失败: " + fileName, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
