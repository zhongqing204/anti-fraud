package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.Publicity;
import com.service.PublicityService;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/publicity")
public class PublicityController {

    @Resource
    private PublicityService publicityService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Publicity publicity) {
        publicityService.add(publicity);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Publicity publicity) {
        publicityService.updateById(publicity);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        publicityService.removeById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        publicityService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Publicity publicity = publicityService.getById(id);
        return Result.success(publicity);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Publicity publicity) {
        List<Publicity> list = publicityService.selectAll(publicity);
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
        Publicity publicity = new Publicity();
        if (StringUtils.hasText(title)) {
            publicity.setTitle(title);
        }
        if (categoryId != null){
            publicity.setCategoryId(categoryId);
        }
        Page<Publicity> page = publicityService.selectPage(publicity, pageNum, pageSize);
        System.out.println("Publicity Page - Total: " + page.getTotal() + ", Records: " + page.getRecords().size());
        return Result.success(page);
    }

    @GetMapping("/selectTop4")
    public Result selectTop4() {
        List<Publicity> list = publicityService.selectTop4();
        return Result.success(list);
    }
}
