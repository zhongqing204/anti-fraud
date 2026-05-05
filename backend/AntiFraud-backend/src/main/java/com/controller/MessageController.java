package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.Message;
import com.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    @PostMapping("/add")
    public Result add(@RequestBody Message message) {
        messageService.add(message);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Message message) {
        messageService.updateById(message);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        messageService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        messageService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Message message = messageService.getById(id);
        return Result.success(message);
    }

    @GetMapping("/selectAll")
    public Result selectAll(Message message) {
        List<Message> list = messageService.selectAll(message);
        return Result.success(list);
    }

    @GetMapping("/selectPage")
    public Result selectPage(Message message,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Message> page = messageService.selectPage(message, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/unreadCount")
    public Result unreadCount(@RequestParam Integer userId) {
        Integer count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PostMapping("/markAsRead")
    public Result markAsRead(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) params.get("ids");
        messageService.markAsRead(ids);
        return Result.success();
    }

    @PostMapping("/markAllAsRead")
    public Result markAllAsRead(@RequestBody Map<String, Object> params) {
        Integer userId = (Integer) params.get("userId");
        messageService.markAllAsRead(userId);
        return Result.success();
    }
}
