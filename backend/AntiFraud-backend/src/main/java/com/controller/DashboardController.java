package com.controller;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.common.Result;
import com.entity.Activity;
import com.entity.Article;
import com.entity.Category;
import com.entity.Publicity;
import com.entity.Report;
import com.entity.User;
import com.entity.Video;
import com.service.ActivityService;
import com.service.ArticleService;
import com.service.CategoryService;
import com.service.PublicityService;
import com.service.ReportService;
import com.service.UserService;
import com.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    @Resource
    private ArticleService articleService;
    @Resource
    private PublicityService publicityService;
    @Resource
    private ActivityService activityService;
    @Resource
    private UserService userService;
    @Resource
    private VideoService videoService;
    @Resource
    private ReportService reportService;
    @Resource
    private CategoryService categoryService;


    @GetMapping("/base")
    public Result base() {
        Map<String, Integer> map = new HashMap<>();
        List<Article> articles = articleService.selectAll(new Article()).stream().filter(x -> "审核通过".equals(x.getStatus())).collect(Collectors.toList());
        map.put("article", articles.size());
        map.put("publicity", publicityService.selectAll(new Publicity()).size());
        map.put("activity", activityService.selectAll(new Activity()).size());
        map.put("user", userService.selectAll(new User()).size());
        map.put("video", videoService.selectAll(new Video()).size());
        return Result.success(map);
    }

    @GetMapping("/line")
    public Result line() {
        Map<String, Object> map = new HashMap<>();
        List<Long> yList = new ArrayList<>();

        Date today = new Date();
        cn.hutool.core.date.DateTime start = DateUtil.offsetDay(today, -7);
        List<String> xList = DateUtil.rangeToList(start, today, DateField.DAY_OF_YEAR).stream().map(DateUtil::formatDate).toList();

        List<Report> reports = reportService.selectAll(new Report());

        for (String day : xList) {
            long count = reports.stream().filter(x -> x.getTime().toString().contains(day)).count();
            yList.add(count);
        }

        map.put("x", xList);
        map.put("y", yList);
        return Result.success(map);
    }

    @GetMapping("/pie1")
    public Result pie1() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Category> categories = categoryService.selectAll(new Category());
        List<Publicity> publicities = publicityService.selectAll(new Publicity());
        for (Category category : categories) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", category.getName());
            map.put("value", publicities.stream().filter(x -> x.getCategoryId() != null && x.getCategoryId().equals(category.getId())).count());
            list.add(map);
        }
        return Result.success(list);
    }

    @GetMapping("/pie2")
    public Result pie2() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Category> categories = categoryService.selectAll(new Category());
        List<Activity> activities = activityService.selectAll(new Activity());
        for (Category category : categories) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", category.getName());
            map.put("value", activities.stream().filter(x -> x.getCategoryId() != null && x.getCategoryId().equals(category.getId())).count());
            list.add(map);
        }
        return Result.success(list);
    }

    @GetMapping("/pie3")
    public Result pie3() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Category> categories = categoryService.selectAll(new Category());
        List<Video> videos = videoService.selectAll(new Video());
        for (Category category : categories) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", category.getName());
            map.put("value", videos.stream().filter(x -> x.getCategoryId() != null && x.getCategoryId().equals(category.getId())).count());
            list.add(map);
        }
        return Result.success(list);
    }
}

