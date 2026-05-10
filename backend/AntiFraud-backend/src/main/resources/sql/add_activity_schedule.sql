-- 创建活动日程表，支持多天多时段设置
CREATE TABLE IF NOT EXISTS `activity_schedule` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `activity_id` INT NOT NULL COMMENT '活动ID',
  `schedule_date` DATE NOT NULL COMMENT '日期',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_schedule_date` (`schedule_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动日程表';

-- 为activity_signup表添加取消报名相关字段
ALTER TABLE `activity_signup` 
ADD COLUMN `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
ADD COLUMN `cancel_status` VARCHAR(20) DEFAULT NULL COMMENT '取消状态：待审批、已同意、已拒绝',
ADD COLUMN `cancel_time` DATETIME DEFAULT NULL COMMENT '申请取消时间';
