package com.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.FraudDetection;
import com.mapper.FraudDetectionMapper;
import com.service.FraudDetectionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 诈骗检测服务实现类
 * 使用基于规则和机器学习的混合方法进行诈骗识别
 */
@Service
public class FraudDetectionServiceImpl extends ServiceImpl<FraudDetectionMapper, FraudDetection> implements FraudDetectionService {
    
    // 诈骗关键词库（可扩展为从数据库加载）
    private static final Map<String, List<String>> FRAUD_KEYWORDS = new HashMap<>();
    
    static {
        // 网络诈骗关键词
        List<String> networkKeywords = Arrays.asList(
            "转账", "汇款", "银行卡", "密码", "验证码", "中奖", "免费", "领取",
            "点击链接", "http", "www", "账户异常", "冻结", "升级", "认证",
            "投资收益", "高回报", "稳赚不赔", "内部消息", "刷单", "返利"
        );
        FRAUD_KEYWORDS.put("network", networkKeywords);
        
        // 电话诈骗关键词
        List<String> phoneKeywords = Arrays.asList(
            "公检法", "警察", "法院", "检察院", "涉嫌", "洗钱", "犯罪",
            "安全账户", "配合调查", "保密", "不要告诉别人", "通缉令",
            "你的身份证", "被盗用", "异常消费"
        );
        FRAUD_KEYWORDS.put("phone", phoneKeywords);
        
        // 短信诈骗关键词
        List<String> smsKeywords = Arrays.asList(
            "ETC", "社保", "公积金", "快递", "包裹", "签收",
            "积分兑换", "流量赠送", "话费充值", "银行升级",
            "点击", "链接", "退订回T"
        );
        FRAUD_KEYWORDS.put("sms", smsKeywords);
        
        // 邮件诈骗关键词
        List<String> emailKeywords = Arrays.asList(
            "亲爱的用户", "账户验证", "密码重置", "附件",
            "紧急通知", "重要提醒", "立即行动", "过期"
        );
        FRAUD_KEYWORDS.put("email", emailKeywords);
    }
    
    // 金额模式
    private static final Pattern MONEY_PATTERN = Pattern.compile("(\\d+[\\.\\d]*)(万|千|百|元|块|美元|人民币)");
    
    // 电话号码模式
    private static final Pattern PHONE_PATTERN = Pattern.compile("(1[3-9]\\d{9}|\\d{3,4}-?\\d{7,8})");
    
    // 银行卡号模式
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("\\d{16,19}");
    
    @Override
    public Map<String, Object> detectFraud(String content, Integer userId, String userName) {
        Map<String, Object> result = new HashMap<>();
        
        if (content == null || content.trim().isEmpty()) {
            result.put("riskLevel", "low");
            result.put("riskScore", 0.0);
            result.put("message", "内容为空，无法检测");
            return result;
        }
        
        // 1. 特征提取
        Map<String, Object> features = extractFeatures(content);
        
        // 2. 风险评分计算
        double riskScore = calculateRiskScore(content, features);
        
        // 3. 风险等级判定
        String riskLevel = determineRiskLevel(riskScore);
        
        // 4. 诈骗类型识别
        String fraudType = identifyFraudType(content, features);
        
        // 5. 生成检测结果和建议
        String description = generateDescription(riskLevel, riskScore, fraudType);
        String suggestion = generateSuggestion(riskLevel, fraudType);
        
        // 6. 提取关键词
        List<String> detectedKeywords = detectKeywords(content);
        
        // 7. 保存到数据库
        FraudDetection detection = new FraudDetection();
        detection.setUserId(userId);
        detection.setUserName(userName);
        detection.setContent(content.length() > 500 ? content.substring(0, 500) : content);
        detection.setFraudType(fraudType);
        detection.setRiskLevel(riskLevel);
        detection.setRiskScore(riskScore);
        detection.setKeywords(JSONUtil.toJsonStr(detectedKeywords));
        detection.setResultDescription(description);
        detection.setSuggestion(suggestion);
        detection.setDetectTime(LocalDateTime.now());
        this.save(detection);
        
        // 8. 返回结果
        result.put("id", detection.getId());
        result.put("riskLevel", riskLevel);
        result.put("riskScore", riskScore);
        result.put("fraudType", fraudType);
        result.put("description", description);
        result.put("suggestion", suggestion);
        result.put("keywords", detectedKeywords);
        result.put("features", features);
        
        return result;
    }
    
    @Override
    public Page<FraudDetection> getUserDetectionHistory(Integer userId, Integer pageNum, Integer pageSize) {
        Page<FraudDetection> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FraudDetection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FraudDetection::getUserId, userId)
                   .orderByDesc(FraudDetection::getDetectTime);
        return this.page(page, queryWrapper);
    }
    
    /**
     * 特征提取
     */
    private Map<String, Object> extractFeatures(String content) {
        Map<String, Object> features = new HashMap<>();
        
        // 提取金额
        List<String> moneyList = new ArrayList<>();
        Matcher moneyMatcher = MONEY_PATTERN.matcher(content);
        while (moneyMatcher.find()) {
            moneyList.add(moneyMatcher.group());
        }
        features.put("moneyMentions", moneyList);
        features.put("hasMoney", !moneyList.isEmpty());
        
        // 提取电话号码
        List<String> phoneList = new ArrayList<>();
        Matcher phoneMatcher = PHONE_PATTERN.matcher(content);
        while (phoneMatcher.find()) {
            phoneList.add(phoneMatcher.group());
        }
        features.put("phoneNumbers", phoneList);
        features.put("hasPhone", !phoneList.isEmpty());
        
        // 提取银行卡号
        List<String> bankCardList = new ArrayList<>();
        Matcher bankCardMatcher = BANK_CARD_PATTERN.matcher(content);
        while (bankCardMatcher.find()) {
            bankCardList.add(bankCardMatcher.group());
        }
        features.put("bankCards", bankCardList);
        features.put("hasBankCard", !bankCardList.isEmpty());
        
        // 检查是否有链接
        boolean hasLink = content.contains("http://") || content.contains("https://") || content.contains("www.");
        features.put("hasLink", hasLink);
        
        // 检查是否有紧急词汇
        boolean hasUrgent = content.contains("紧急") || content.contains("立即") || 
                           content.contains("马上") || content.contains("尽快");
        features.put("hasUrgent", hasUrgent);
        
        // 文本长度
        features.put("textLength", content.length());
        
        return features;
    }
    
    /**
     * 计算风险分数（0-100）
     */
    private double calculateRiskScore(String content, Map<String, Object> features) {
        double score = 0.0;
        
        // 1. 关键词匹配得分（最高40分）
        List<String> keywords = detectKeywords(content);
        int keywordCount = keywords.size();
        score += Math.min(keywordCount * 5, 40);
        
        // 2. 敏感信息得分（最高30分）
        if ((Boolean) features.getOrDefault("hasMoney", false)) {
            score += 10;
        }
        if ((Boolean) features.getOrDefault("hasPhone", false)) {
            score += 8;
        }
        if ((Boolean) features.getOrDefault("hasBankCard", false)) {
            score += 12;
        }
        
        // 3. 链接得分（10分）
        if ((Boolean) features.getOrDefault("hasLink", false)) {
            score += 10;
        }
        
        // 4. 紧急程度得分（10分）
        if ((Boolean) features.getOrDefault("hasUrgent", false)) {
            score += 10;
        }
        
        // 5. 文本长度异常（过短或过长都可疑）
        int textLength = (int) features.getOrDefault("textLength", 0);
        if (textLength < 20 || textLength > 500) {
            score += 5;
        }
        
        return Math.min(score, 100.0);
    }
    
    /**
     * 判定风险等级
     */
    private String determineRiskLevel(double riskScore) {
        if (riskScore >= 70) {
            return "high";
        } else if (riskScore >= 40) {
            return "medium";
        } else {
            return "low";
        }
    }
    
    /**
     * 识别诈骗类型
     */
    private String identifyFraudType(String content, Map<String, Object> features) {
        Map<String, Integer> typeScores = new HashMap<>();
        
        // 统计各类型的关键词匹配数
        for (Map.Entry<String, List<String>> entry : FRAUD_KEYWORDS.entrySet()) {
            int count = 0;
            for (String keyword : entry.getValue()) {
                if (content.contains(keyword)) {
                    count++;
                }
            }
            typeScores.put(entry.getKey(), count);
        }
        
        // 返回得分最高的类型
        String maxType = "network"; // 默认网络诈骗
        int maxScore = 0;
        for (Map.Entry<String, Integer> entry : typeScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                maxType = entry.getKey();
            }
        }
        
        return maxType;
    }
    
    /**
     * 检测关键词
     */
    private List<String> detectKeywords(String content) {
        List<String> detected = new ArrayList<>();
        String lowerContent = content.toLowerCase();
        
        for (List<String> keywords : FRAUD_KEYWORDS.values()) {
            for (String keyword : keywords) {
                if (lowerContent.contains(keyword.toLowerCase()) && !detected.contains(keyword)) {
                    detected.add(keyword);
                }
            }
        }
        
        return detected;
    }
    
    /**
     * 生成检测结果描述
     */
    private String generateDescription(String riskLevel, double riskScore, String fraudType) {
        String typeDesc = getFraudTypeDescription(fraudType);
        
        if ("high".equals(riskLevel)) {
            return String.format("检测到高风险%s内容（风险分数：%.1f/100）。该信息包含多个诈骗特征，极可能是诈骗信息，请高度警惕！", typeDesc, riskScore);
        } else if ("medium".equals(riskLevel)) {
            return String.format("检测到中等风险%s内容（风险分数：%.1f/100）。该信息存在一些可疑特征，建议谨慎对待，不要轻易相信。", typeDesc, riskScore);
        } else {
            return String.format("检测到较低风险内容（风险分数：%.1f/100）。该信息暂未发现明显诈骗特征，但仍需保持警惕。", riskScore);
        }
    }
    
    /**
     * 生成建议措施
     */
    private String generateSuggestion(String riskLevel, String fraudType) {
        StringBuilder suggestion = new StringBuilder();
        
        if ("high".equals(riskLevel)) {
            suggestion.append("【紧急建议】\n");
            suggestion.append("1. 立即停止与对方联系\n");
            suggestion.append("2. 不要透露任何个人信息（密码、验证码、银行卡号等）\n");
            suggestion.append("3. 不要点击任何链接或扫描二维码\n");
            suggestion.append("4. 保存相关证据（聊天记录、短信、电话录音等）\n");
            suggestion.append("5. 立即向公安机关报案或通过本平台提交举报\n");
        } else if ("medium".equals(riskLevel)) {
            suggestion.append("【谨慎建议】\n");
            suggestion.append("1. 仔细核实对方身份和信息真实性\n");
            suggestion.append("2. 通过官方渠道确认相关信息\n");
            suggestion.append("3. 不要轻易转账或提供个人信息\n");
            suggestion.append("4. 如有疑虑，可咨询亲友或专业人士\n");
        } else {
            suggestion.append("【一般建议】\n");
            suggestion.append("1. 保持警惕，注意保护个人信息\n");
            suggestion.append("2. 遇到可疑情况及时举报\n");
        }
        
        suggestion.append("\n\n【防范提示】\n");
        suggestion.append("- 牢记\"三不一多\"原则：未知链接不点击、陌生来电不轻信、个人信息不透露、转账汇款多核实\n");
        suggestion.append("- 下载国家反诈中心APP，开启来电预警功能\n");
        suggestion.append("- 如有疑问，请拨打96110反诈专线咨询");
        
        return suggestion.toString();
    }
    
    /**
     * 获取诈骗类型描述
     */
    private String getFraudTypeDescription(String fraudType) {
        switch (fraudType) {
            case "network":
                return "网络诈骗";
            case "phone":
                return "电话诈骗";
            case "sms":
                return "短信诈骗";
            case "email":
                return "邮件诈骗";
            default:
                return "疑似诈骗";
        }
    }
}
