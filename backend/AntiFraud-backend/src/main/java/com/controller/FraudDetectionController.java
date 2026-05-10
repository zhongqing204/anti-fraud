package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.FraudDetection;
import com.service.FraudDetectionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 诈骗识别控制器
 */
@RestController
@RequestMapping("/fraud-detection")
public class FraudDetectionController {
    
    @Resource
    private FraudDetectionService fraudDetectionService;
    
    /**
     * 检测文本是否为诈骗信息
     */
    @PostMapping("/detect")
    public Result detectFraud(@RequestBody Map<String, Object> params) {
        String content = (String) params.get("content");
        Integer userId = (Integer) params.get("userId");
        String userName = (String) params.get("userName");
        
        if (content == null || content.trim().isEmpty()) {
            return Result.error("检测内容不能为空");
        }
        
        Map<String, Object> result = fraudDetectionService.detectFraud(content, userId, userName);
        return Result.success(result);
    }
    
    /**
     * 获取用户的检测历史
     */
    @GetMapping("/history")
    public Result getDetectionHistory(@RequestParam Integer userId,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<FraudDetection> page = fraudDetectionService.getUserDetectionHistory(userId, pageNum, pageSize);
        return Result.success(page);
    }
}
