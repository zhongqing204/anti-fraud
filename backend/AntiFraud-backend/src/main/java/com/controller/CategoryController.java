package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.Category;
import com.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 添加
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Category category) {
        categoryService.add(category);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        categoryService.removeById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        categoryService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Category category = categoryService.getById(id);
        return Result.success(category);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Category category) {
        List<Category> list = categoryService.selectAll(category);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(required = false) String name,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Category category = new Category();
        if (StringUtils.hasText(name)) {
            category.setName(name);
        }
        Page<Category> page = categoryService.selectPage(category, pageNum, pageSize);
        return Result.success(page);
    }
}
