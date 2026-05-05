package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.Report;
import com.entity.User;
import com.entity.Message;
import com.mapper.UserMapper;
import com.service.ReportService;
import com.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Resource
    private ReportService reportService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private MessageService messageService;

    /**
     * 添加
     */
    @PostMapping("/add")
    public Result add(@RequestBody Report report) {
        boolean isDuplicate = reportService.checkDuplicate(report.getUserId(),report.getContent());
        if (isDuplicate) {
            return Result.error("您在7天内已提交过相似内容的举报，请勿重复举报");
        }
        reportService.add(report);

        // 发送举报提交消息
        sendReportMessage(report.getUserId(), report.getContent(), "待处理", null);

        return Result.success("添加成功");
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Report report) {
        Report oldReport = reportService.getById(report.getId());

        if (report.getUserId() != null) {
            User user = userMapper.selectById(report.getUserId());
            if (user != null) {
                report.setUserName(user.getName());
            }
        }
        reportService.updateById(report);

        // 如果状态发生变化，发送消息通知
        if (oldReport != null && report.getStatus() != null && !report.getStatus().equals(oldReport.getStatus())) {
            sendReportMessage(report.getUserId(), report.getContent(), report.getStatus(), report.getReason());
        }

        return Result.success("修改成功");
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        reportService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        reportService.deleteBatch(ids);
        return Result.success("批量删除成功");
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Report report = reportService.getById(id);
        return Result.success(report);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Report report) {
        List<Report> list = reportService.selectAll(report);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Report report,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Report> page = reportService.selectPage(report, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 批量处理
     */
    @PostMapping("/batchUpdate")
    public Result batchUpdate(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) params.get("ids");
        String status = (String) params.get("status");
        String reason = (String) params.get("reason");

        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要处理的举报");
        }
        if (status == null || status.isEmpty()) {
            return Result.error("请选择处理状态");
        }

        reportService.batchUpdateStatus(ids, status, reason);

        // 为每个举报发送消息通知
        for (Integer id : ids) {
            Report report = reportService.getById(id);
            if (report != null && report.getUserId() != null) {
                sendReportMessage(report.getUserId(), report.getContent(), status, reason);
            }
        }

        return Result.success("批量处理成功");
    }

    /**
     * 发送举报状态变更消息
     */
    private void sendReportMessage(Integer userId, String content, String status, String reason) {
        if (userId == null) return;

        Message message = new Message();
        message.setUserId(userId);
        message.setType("report");

        String contentPreview = content;
        if (content != null && content.length() > 50) {
            contentPreview = content.substring(0, 50) + "...";
        }

        String contentText = "";
        if ("处理中".equals(status)) {
            contentText = "您的举报「" + contentPreview + "」正在处理中，请耐心等待";
        } else if ("已处理".equals(status)) {
            contentText = "您的举报「" + contentPreview + "」已处理完成";
            if (reason != null && !reason.isEmpty()) {
                contentText += "。处理说明：" + reason;
            }
        } else if ("待处理".equals(status)) {
            contentText = "您的举报「" + contentPreview + "」已提交，等待处理";
        }

        message.setContent(contentText);
        message.setIsRead(0);

        messageService.add(message);
    }
}
