package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.FraudDetection;

import java.util.Map;

public interface FraudDetectionService extends IService<FraudDetection> {
    
    /**
     * 检测文本是否为诈骗信息
     * @param content 待检测文本
     * @param userId 用户ID
     * @param userName 用户姓名
     * @return 检测结果
     */
    Map<String, Object> detectFraud(String content, Integer userId, String userName);
    
    /**
     * 获取用户的检测历史
     */
    Page<FraudDetection> getUserDetectionHistory(Integer userId, Integer pageNum, Integer pageSize);
}
