package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.ActivitySignup;
import com.service.ActivitySignUpService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activitySignUp")
public class ActivitySignUpController {
    @Resource
    private ActivitySignUpService activitySignUpService;

    /**
     * 新增报名
     */
    @PostMapping("/add")
    public Result add(@RequestBody ActivitySignup activitySignup) {
        activitySignUpService.add(activitySignup);
        return Result.success("报名成功");
    }

    /**
     * 修改报名信息
     */
    @PutMapping("/update")
    public Result update(@RequestBody ActivitySignup activitySignup) {
        activitySignUpService.updateById(activitySignup);
        return Result.success("修改成功");
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        activitySignUpService.removeById(id);
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
                             @RequestParam(required = false) String activityName,
                             @RequestParam(required = false) Integer userId,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ActivitySignup> pageInfo = activitySignUpService.selectPage(userName,activityName,userId, pageNum, pageSize);
        return Result.success(pageInfo);
    }
}
