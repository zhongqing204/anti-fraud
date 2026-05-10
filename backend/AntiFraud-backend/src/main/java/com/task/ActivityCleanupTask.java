package com.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.entity.Activity;
import com.service.ActivityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动自动清理定时任务
 */
@Slf4j
@Component
public class ActivityCleanupTask {

    @Resource
    private ActivityService activityService;

    /**
     * 每天凌晨2点执行，删除结束时间超过14天的活动
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredActivities() {
        log.info("开始执行活动自动清理任务...");
        
        // 计算14天前的时间点
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(14);
        
        // 查询结束时间早于14天前的所有活动
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Activity::getEndTime, cutoffTime);
        
        List<Activity> expiredActivities = activityService.list(wrapper);
        
        if (expiredActivities.isEmpty()) {
            log.info("没有需要清理的过期活动");
            return;
        }
        
        log.info("找到 {} 个过期活动，开始清理...", expiredActivities.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (Activity activity : expiredActivities) {
            try {
                boolean removed = activityService.removeById(activity.getId());
                if (removed) {
                    successCount++;
                    log.info("已删除过期活动: ID={}, 标题={}", activity.getId(), activity.getTitle());
                } else {
                    failCount++;
                    log.warn("删除活动失败: ID={}", activity.getId());
                }
            } catch (Exception e) {
                failCount++;
                log.error("删除活动时发生异常: ID={}, 错误={}", activity.getId(), e.getMessage());
            }
        }
        
        log.info("活动清理任务完成！成功: {}, 失败: {}", successCount, failCount);
    }
}
