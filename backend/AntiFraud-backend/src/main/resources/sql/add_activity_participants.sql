-- 为activity表添加限制人数和当前报名人数字段
ALTER TABLE `activity` 
ADD COLUMN `max_participants` INT DEFAULT 0 COMMENT '限制人数（0表示不限制）',
ADD COLUMN `current_participants` INT DEFAULT 0 COMMENT '当前报名人数';

-- 更新现有数据，设置默认值
UPDATE `activity` SET `max_participants` = 0, `current_participants` = 0 WHERE `max_participants` IS NULL;
