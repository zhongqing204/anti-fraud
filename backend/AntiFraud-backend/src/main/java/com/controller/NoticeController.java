package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.Notice;
import com.service.NoticeService;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 添加
     */
    @PostMapping("/add")
    public Result add(@RequestBody Notice notice) {
        noticeService.add(notice);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Notice notice) {
        noticeService.updateById(notice);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        noticeService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        noticeService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Notice notice = noticeService.getById(id);
        return Result.success(notice);
    }

    @GetMapping("/selectAll")
    public Result selectAll(Notice notice) {
        List<Notice> list = noticeService.selectAll(notice);
        return Result.success(list);
    }

    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(required = false) String title,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        Notice notice = new Notice();
        if (StringUtils.hasText(title)) {
            notice.setTitle(title);
        }
        Page<Notice> page = noticeService.selectPage(notice, pageNum, pageSize);
        return Result.success(page);
    }
}
