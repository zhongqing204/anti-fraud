package com.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.Result;
import com.entity.ArticleReport;
import com.service.ArticleReportService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/articleReport")
public class ArticleReportController {
    @Resource
    private ArticleReportService articleReportService;

    /**
     * 添加举报
     */
    @PostMapping("/add")
    public Result add(@RequestBody ArticleReport articleReport) {
        articleReportService.add(articleReport);
        return Result.success("举报成功");
    }

    /**
     * 修改举报
     */
    @PutMapping("/update")
    public Result update(@RequestBody ArticleReport articleReport) {
        articleReportService.updateById(articleReport);
        return Result.success("修改成功");
    }

    /**
     * 删除单个举报
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        articleReportService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除举报
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        articleReportService.deleteBatch(ids);
        return Result.success("删除成功");
    }

    /**
     * 查询所有举报
     */
    @GetMapping("/selectAll")
    public Result selectAll(ArticleReport articleReport) {
        List<ArticleReport> list = articleReportService.selectAll(articleReport);
        return Result.success(list);
    }

    /**
     * 分页查询举报
     */
    @GetMapping("/selectPage")
    public Result selectPage(ArticleReport articleReport,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ArticleReport> page = articleReportService.selectPage(articleReport, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 批量处理举报
     */
    @PostMapping("/batchUpdate")
    public Result batchUpdate(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) params.get("ids");
        String status = (String) params.get("status");
        String reason = (String) params.get("reason");

        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要处理的举报");
        }
        if (status == null || status.isEmpty()) {
            return Result.error("请选择处理状态");
        }

        articleReportService.batchUpdateStatus(ids, status, reason);
        return Result.success("批量处理成功");
    }
}
