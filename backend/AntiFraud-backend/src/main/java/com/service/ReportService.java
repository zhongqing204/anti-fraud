package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.Report;

import java.util.List;

public interface ReportService extends IService<Report> {
    void add(Report report);

    void deleteBatch(List<Integer> ids);

    List<Report> selectAll(Report report);

    Page<Report> selectPage(Report report, Integer pageNum, Integer pageSize);

}
