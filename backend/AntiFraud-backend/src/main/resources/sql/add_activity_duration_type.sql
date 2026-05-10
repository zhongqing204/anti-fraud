-- 为activity表添加活动类型和持续天数字段
ALTER TABLE `activity` 
ADD COLUMN `activity_duration_type` VARCHAR(20) DEFAULT 'short' COMMENT '活动持续时间类型：short-短期活动，long-长期活动',
ADD COLUMN `duration_days` INT DEFAULT 1 COMMENT '持续天数（仅长期活动使用）';

-- 更新现有数据，默认为短期活动
UPDATE `activity` SET `activity_duration_type` = 'short', `duration_days` = 1 WHERE `activity_duration_type` IS NULL;

-- 修改activity_schedule表，添加每天独立的报名人数字段
ALTER TABLE `activity_schedule` 
ADD COLUMN `max_participants` INT DEFAULT 0 COMMENT '该天限制人数（0表示不限制）',
ADD COLUMN `current_participants` INT DEFAULT 0 COMMENT '该天当前报名人数';

-- 更新现有数据
UPDATE `activity_schedule` SET `max_participants` = 0, `current_participants` = 0 WHERE `max_participants` IS NULL;

-- 修改activity_signup表，添加报名日期字段
ALTER TABLE `activity_signup` 
ADD COLUMN `schedule_date` DATE DEFAULT NULL COMMENT '报名的日期（仅长期活动使用）';
