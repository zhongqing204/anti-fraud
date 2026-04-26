package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.Collect;
import com.service.CollectService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

    @RestController
    @RequestMapping("/collect")
    public class CollectController {
        @Resource
        private CollectService collectService;

        @PostMapping("/add")
        public Result add(@RequestBody Collect collect) {
            collectService.add(collect);
            return Result.success();
        }

        @PutMapping("/update")
        public Result update(@RequestBody Collect collect) {
            collectService.updateById(collect);
            return Result.success();
        }

        @DeleteMapping("/delete/{id}")
        public Result delete(@PathVariable Integer id) {
            collectService.removeById(id);
            return Result.success();
        }

        @DeleteMapping("/delete/batch")
        public Result deleteBatch(@RequestBody List<Integer> ids) {
            collectService.deleteBatch(ids);
            return Result.success();
        }

        @GetMapping("/selectById/{id}")
        public Result selectById(@PathVariable Integer id) {
            Collect collect = collectService.getById(id);
            return Result.success(collect);
        }

        @GetMapping("/selectAll")
        public Result selectAll(Collect collect) {
            List<Collect> list = collectService.selectAll(collect);
            return Result.success(list);
        }

        @GetMapping("/selectPage")
        public Result selectPage(Collect collect,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize) {
            Page<Collect> page = collectService.selectPage(collect, pageNum, pageSize);
            return Result.success(page);
        }
    }
