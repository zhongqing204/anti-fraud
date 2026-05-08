package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.Carousel;
import com.service.CarouselService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carousel")
public class CarouselController {

    @Resource
    private CarouselService carouselService;

    @PostMapping("/add")
    public Result add(@RequestBody Carousel carousel) {
        carouselService.add(carousel);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Carousel carousel) {
        carouselService.updateById(carousel);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        carouselService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        carouselService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Carousel carousel = carouselService.selectById(id);
        return Result.success(carousel);
    }

    @GetMapping("/selectAll")
    public Result selectAll(Carousel carousel) {
        List<Carousel> list = carouselService.selectAll(carousel);
        return Result.success(list);
    }

    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Carousel carousel = new Carousel();
        Page<Carousel> page = carouselService.selectPage(carousel, pageNum, pageSize);
        return Result.success(page);
    }
}

