package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.ArticleReport;

import java.util.List;

public interface ArticleReportService extends IService<ArticleReport> {
    void add(ArticleReport articleReport);

    void deleteBatch(List<Integer> ids);

    List<ArticleReport> selectAll(ArticleReport articleReport);

    Page<ArticleReport> selectPage(ArticleReport articleReport, Integer pageNum, Integer pageSize);

    void batchUpdateStatus(List<Integer> ids, String status, String reason);
}
