package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.Activity;
import com.service.ActivityService;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Resource
    private ActivityService activityService;

    /**
     * 添加
     */
    @PostMapping("/add")
    public Result add(@RequestBody Activity activity) {
        activityService.add(activity);
        return Result.success("添加成功");
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Activity activity) {
        activityService.updateById(activity);
        return Result.success("修改成功");
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        activityService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        activityService.deleteBatch(ids);
        return Result.success("批量删除成功");
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Activity activity = activityService.getById(id);
        return Result.success(activity);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Activity activity) {
        List<Activity> list = activityService.selectAll(activity);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(required = false) String title,
                             @RequestParam(required = false) Integer categoryId,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String activityType,
                             @RequestParam(required = false) String activityDurationType,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        Activity activity = new Activity();
        if (StringUtils.hasText(title)) {
            activity.setTitle(title);
        }
        if (categoryId != null){
            activity.setCategoryId(categoryId);
        }
        if (StringUtils.hasText(status)){
            activity.setStatus(status);
        }
        if (StringUtils.hasText(activityType)){
            activity.setActivityType(activityType);
        }
        if (StringUtils.hasText(activityDurationType)){
            activity.setActivityDurationType(activityDurationType);
        }
        Page<Activity> page = activityService.selectPage(activity, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/selectTop4")
    public Result selectTop4() {
        List<Activity> list = activityService.selectTop4();
        return Result.success(list);
    }
}
