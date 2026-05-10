-- ============================================
-- 反诈平台新增功能数据库表
-- 包含：实时聊天、诈骗识别功能
-- ============================================

-- 1. 聊天记录表
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `from_user_id` INT NOT NULL COMMENT '发送者ID',
  `from_user_name` VARCHAR(50) DEFAULT NULL COMMENT '发送者姓名',
  `to_user_id` INT DEFAULT 0 COMMENT '接收者ID（0表示群聊/管理员）',
  `to_user_name` VARCHAR(50) DEFAULT NULL COMMENT '接收者姓名',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `message_type` VARCHAR(20) DEFAULT 'text' COMMENT '消息类型：text-文本, image-图片, system-系统消息',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `send_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_user` (`from_user_id`),
  KEY `idx_to_user` (`to_user_id`),
  KEY `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天记录表';

-- 2. 诈骗检测记录表
CREATE TABLE IF NOT EXISTS `fraud_detection` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(50) DEFAULT NULL COMMENT '用户姓名',
  `content` TEXT NOT NULL COMMENT '待检测文本内容',
  `fraud_type` VARCHAR(20) DEFAULT NULL COMMENT '诈骗类型：network-网络诈骗, phone-电话诈骗, sms-短信诈骗, email-邮件诈骗',
  `risk_level` VARCHAR(20) DEFAULT NULL COMMENT '风险等级：low-低风险, medium-中风险, high-高风险',
  `risk_score` DOUBLE DEFAULT 0 COMMENT '风险分数（0-100）',
  `keywords` TEXT COMMENT '检测到的关键词（JSON格式）',
  `result_description` TEXT COMMENT '检测结果描述',
  `suggestion` TEXT COMMENT '建议措施',
  `detect_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_detect_time` (`detect_time`),
  KEY `idx_risk_level` (`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='诈骗检测记录表';

-- 插入测试数据（可选）
-- 聊天记录测试数据
INSERT INTO `chat_message` (`from_user_id`, `from_user_name`, `to_user_id`, `to_user_name`, `content`, `message_type`, `is_read`, `send_time`) VALUES
(1, '张三', 0, '管理员', '您好，我想咨询一下关于电信诈骗的问题', 'text', 0, NOW()),
(2, '管理员', 1, '张三', '您好！请问有什么可以帮助您的？', 'text', 1, NOW());

-- 诈骗检测测试数据
INSERT INTO `fraud_detection` (`user_id`, `user_name`, `content`, `fraud_type`, `risk_level`, `risk_score`, `keywords`, `result_description`, `suggestion`, `detect_time`) VALUES
(1, '张三', '恭喜您中奖100万元，请点击链接http://xxx.com领取，需要提供银行卡号和验证码', 'network', 'high', 85.0, '["中奖", "点击链接", "银行卡", "验证码"]', '检测到高风险网络诈骗内容（风险分数：85.0/100）。该信息包含多个诈骗特征，极可能是诈骗信息，请高度警惕！', '【紧急建议】\n1. 立即停止与对方联系\n2. 不要透露任何个人信息', NOW());
