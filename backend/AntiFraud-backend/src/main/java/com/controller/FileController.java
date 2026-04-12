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

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private static final String filePath = System.getProperty("user.dir") + "/files/";

    @Value("${fileBaseUrl:}")
    private String fileBaseUrl;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        try {
            if (!FileUtil.isDirectory(filePath)) {
                FileUtil.mkdir(filePath);
            }
            fileName = System.currentTimeMillis() + "-" + fileName;
            String realFilePath = filePath + fileName;
            // 文件存储形式：时间戳-文件名
            FileUtil.writeBytes(file.getBytes(), realFilePath);
        } catch (Exception e) {
            log.error(fileName + "--文件上传失败", e);
        }
        String url = "/files/" + fileName;
        return Result.success(url);
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
        return "application/octet-stream";
    }
}
