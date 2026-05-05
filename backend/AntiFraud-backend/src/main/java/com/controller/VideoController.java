package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.Video;
import com.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视频Controller（管理员管理）
 */
@RestController
@RequestMapping("/video")
public class VideoController {

    @Resource
    private VideoService videoService;

    /**
     * 新增视频
     */
    @PostMapping("/add")
    public Result add(@RequestBody Video video) {
        videoService.add(video);
        return Result.success();
    }

    /**
     * 修改视频
     */
    @PutMapping("/update")
    public Result update(@RequestBody Video video) {
        videoService.updateById(video);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        videoService.removeById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        videoService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Video video = videoService.getById(id);
        return Result.success(video);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Video video) {
        List<Video> list = videoService.selectAll(video);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(required = false) String title,
                             @RequestParam(required = false) Integer categoryId,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        Video video = new Video();
        if (StringUtils.hasText(title)) {
            video.setTitle(title);
        }
        if (categoryId != null){
            video.setCategoryId(categoryId);
        }
        Page<Video> page = videoService.selectPage(video, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 查询热门视频（Top4）
     */
    @GetMapping("/selectTop4")
    public Result selectTop4() {
        List<Video> list = videoService.selectTop4();
        return Result.success(list);
    }
}

