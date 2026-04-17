package com.controller;

import com.common.Result;
import com.entity.Likes;
import com.service.LikesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/like")
public class LikesController {
    @Resource
    private LikesService likeService;

    @PostMapping("/add")
    public Result add(@RequestBody Likes likes) {
        likeService.add(likes);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        likeService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        likeService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/select/{id}")
    public Result getById(@PathVariable Integer id) {
        return Result.success(likeService.getById(id));
    }

    @GetMapping("/selectAll")
    public Result selectAll(Likes likes) {
        return Result.success(likeService.selectAll(likes));
    }
}
