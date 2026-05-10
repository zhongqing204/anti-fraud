package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.ActivitySignup;
import com.entity.Message;
import com.service.ActivitySignUpService;
import com.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activitySignUp")
public class ActivitySignUpController {
    @Resource
    private ActivitySignUpService activitySignUpService;

    @Resource
    private MessageService messageService;

    /**
     * 新增报名
     */
    @PostMapping("/add")
    public Result add(@RequestBody ActivitySignup activitySignup) {
        activitySignUpService.add(activitySignup);

        // 发送报名提交消息
        sendActivitySignupMessage(activitySignup.getUserId(), activitySignup.getActivityName(),
                "待审核", null);

        return Result.success("报名成功");
    }

    /**
     * 修改报名信息（包含审核状态更新）
     */
    @PutMapping("/update")
    public Result update(@RequestBody ActivitySignup activitySignup) {
        ActivitySignup oldSignup = activitySignUpService.getById(activitySignup.getId());

        activitySignUpService.updateById(activitySignup);

        // 如果状态发生变化，发送消息通知
        if (oldSignup != null && activitySignup.getStatus() != null) {
            sendActivitySignupMessage(oldSignup.getUserId(), oldSignup.getActivityName(),
                    activitySignup.getStatus(), activitySignup.getReason());
        }

        return Result.success("修改成功");
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        // 使用deleteBatch方法来确保减少活动报名人数
        activitySignUpService.deleteBatch(List.of(id));
        return Result.success("删除成功");
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        activitySignUpService.deleteBatch(ids);
        return Result.success("批量删除成功");
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        ActivitySignup activitySignup = activitySignUpService.getById(id);
        return Result.success(activitySignup);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(ActivitySignup activitySignup) {
        List<ActivitySignup> list = activitySignUpService.selectAll(activitySignup);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(required = false) String userName,
                             @RequestParam(required = false) String realName,
                             @RequestParam(required = false) Integer activityId,
                             @RequestParam(required = false) String status,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ActivitySignup> pageInfo = activitySignUpService.selectPage(userName, realName, activityId, status, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 申请取消报名
     */
    @PostMapping("/applyCancel")
    public Result applyCancel(@RequestBody ActivitySignup activitySignup) {
        activitySignUpService.applyCancel(activitySignup.getId(), activitySignup.getCancelReason());
        
        // 发送消息通知管理员
        sendCancelApplyMessage(activitySignup.getUserId(), activitySignup.getActivityName());
        
        return Result.success("取消申请已提交，等待管理员审批");
    }

    /**
     * 审批取消报名申请
     */
    @PutMapping("/approveCancel")
    public Result approveCancel(@RequestBody ActivitySignup activitySignup) {
        activitySignUpService.approveCancel(activitySignup.getId(), 
            "已同意".equals(activitySignup.getCancelStatus()), 
            activitySignup.getReason());
        
        // 如果同意取消，发送消息通知用户
        if ("已同意".equals(activitySignup.getCancelStatus())) {
            sendCancelApprovedMessage(activitySignup.getUserId(), activitySignup.getActivityName());
        }
        
        return Result.success("审批成功");
    }

    /**
     * 发送活动报名状态变更消息
     */
    private void sendActivitySignupMessage(Integer userId, String activityName, String status, String reason) {
        if (userId == null) return;

        Message message = new Message();
        message.setUserId(userId);
        message.setType("activity_signup");

        String contentText = "";
        if ("审核通过".equals(status)) {
            contentText = "恭喜您，活动「" + activityName + "」的报名已审核通过！";
        } else if ("审核拒绝".equals(status)) {
            contentText = "很抱歉，活动「" + activityName + "」的报名审核未通过";
            if (reason != null && !reason.isEmpty()) {
                contentText += "。原因：" + reason;
            }
        } else if ("待审核".equals(status)) {
            contentText = "您报名的活动「" + activityName + "」已提交，等待审核";
        }

        message.setContent(contentText);
        message.setIsRead(0);

        messageService.add(message);
    }

    /**
     * 发送取消申请消息给管理员
     */
    private void sendCancelApplyMessage(Integer userId, String activityName) {
        // 获取所有管理员ID（这里简化处理，实际应该查询管理员列表）
        // 暂时发送给ID为1的管理员
        Message message = new Message();
        message.setUserId(1); // 管理员ID
        message.setType("activity_cancel_apply");
        message.setContent("用户申请取消活动「" + activityName + "」的报名，请及时审批");
        message.setIsRead(0);
        messageService.add(message);
    }

    /**
     * 发送取消审批结果消息给用户
     */
    private void sendCancelApprovedMessage(Integer userId, String activityName) {
        if (userId == null) return;
        
        Message message = new Message();
        message.setUserId(userId);
        message.setType("activity_cancel_result");
        message.setContent("您的活动「" + activityName + "」取消报名申请已通过，报名已取消");
        message.setIsRead(0);
        messageService.add(message);
    }
}
