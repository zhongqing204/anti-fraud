package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Message;
import com.entity.Report;
import com.entity.User;
import com.mapper.ReportMapper;
import com.mapper.UserMapper;
import com.service.MessageService;
import com.service.ReportService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private MessageService messageService;

    @Override
    public void add(Report report) {
        report.setTime(LocalDateTime.now());
        if (StringUtils.hasText(report.getStatus()) == false) {
            report.setStatus("待处理");
        }
        if (StringUtils.hasText(report.getPriority()) == false) {
            report.setPriority("一般");
        }
        if (report.getUserId() != null) {
            User user = userMapper.selectById(report.getUserId());
            if (user != null) {
                report.setUserName(user.getName());
            }
        }
        this.save(report);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<Report> selectAll(Report report) {
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        if (report != null && StringUtils.hasText(report.getContent())){
            queryWrapper.like(Report::getContent, report.getContent());
        }
        if (report != null && StringUtils.hasText(report.getCategory())){
            queryWrapper.eq(Report::getCategory, report.getCategory());
        }
        if (report != null && StringUtils.hasText(report.getStatus())){
            queryWrapper.eq(Report::getStatus, report.getStatus());
        }
        if (report != null && StringUtils.hasText(report.getPriority())){
            queryWrapper.eq(Report::getPriority, report.getPriority());
        }
        queryWrapper.orderByDesc(Report::getTime);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Report> selectPage(Report report, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        if (report != null && StringUtils.hasText(report.getContent())) {
            queryWrapper.like(Report::getContent, report.getContent());
        }
        // 按举报类型筛选
        if (report != null && StringUtils.hasText(report.getCategory())){
            queryWrapper.eq(Report::getCategory, report.getCategory());
        }
        // 按处理状态筛选
        if (report != null && StringUtils.hasText(report.getStatus())){
            queryWrapper.eq(Report::getStatus, report.getStatus());
        }
        // 按优先级筛选
        if (report != null && StringUtils.hasText(report.getPriority())){
            queryWrapper.eq(Report::getPriority, report.getPriority());
        }
        if (report != null && report.getUserId() != null){
            queryWrapper.eq(Report::getUserId,report.getUserId());
        }
        queryWrapper.orderByDesc(Report::getTime);
        return this.page(new Page<>(pageNum,pageSize),queryWrapper);
    }

    @Override
    public boolean checkDuplicate(Integer userId, String content) {
        if (!StringUtils.hasText(content) || userId == null) {
            return false;
        }
        // 查询该用户最近的相似举报记录
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Report::getUserId, userId)
                .like(Report::getContent, content)
                .orderByDesc(Report::getTime)
                .last("LIMIT 1");
        Report lastReport = this.getOne(queryWrapper);
        if (lastReport != null) {
            // 计算距离上次举报的天数
            long daysBetween = java.time.Duration.between(
                    lastReport.getTime(),
                    LocalDateTime.now()
            ).toDays();
            // 7天内的相似内容视为重复举报
            return daysBetween <= 7;
        }
        return false;
    }

    @Override
    public void batchUpdateStatus(List<Integer> ids, String status, String reason) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 批量更新选中的举报记录
        List<Report> reports = this.listByIds(ids);
        for (Report report : reports) {
            // 【新增】记录旧状态，用于判断是否从"待处理"变更为其他状态
            String oldStatus = report.getStatus();
            report.setStatus(status);
            if (StringUtils.hasText(reason)) {
                report.setReason(reason);
            }

            // 【新增】发送消息通知给举报人
            // 条件：举报人有ID、新状态不是"待处理"、旧状态也不是"待处理"（避免重复通知）
            if (report.getUserId() != null && !"待处理".equals(status) && !"待处理".equals(oldStatus)) {
                Message message = new Message();
                message.setUserId(report.getUserId()); // 接收者：举报人
                message.setArticleId(null); // 反诈举报不关联具体帖子
                message.setArticleTitle(null);
                message.setType("report"); // 消息类型：反诈举报
                message.setContent("您的反诈举报已更新状态为：" + status + (StringUtils.hasText(reason) ? "，处理说明：" + reason : "")); // 消息内容
                message.setIsRead(0); // 未读状态
                message.setCreatedTime(LocalDateTime.now()); // 创建时间
                messageService.add(message); // 保存消息
            }
        }
        this.updateBatchById(reports);
    }
}
