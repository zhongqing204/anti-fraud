package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.Likes;
import com.service.LikesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/likes")
public class LikesController {
    @Resource
    private LikesService likesService;

    @PostMapping("/add")
    public Result add(@RequestBody Likes likes) {
        likesService.add(likes);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        likesService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        likesService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/select/{id}")
    public Result getById(@PathVariable Integer id) {
        return Result.success(likesService.getById(id));
    }

    @GetMapping("/selectAll")
    public Result selectAll(Likes likes) {
        return Result.success(likesService.selectAll(likes));
    }

    @GetMapping("/selectPage")
    public Result selectPage(Likes likes,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Likes> page = likesService.selectPage(likes, pageNum, pageSize);
        return Result.success(page);
    }
}
