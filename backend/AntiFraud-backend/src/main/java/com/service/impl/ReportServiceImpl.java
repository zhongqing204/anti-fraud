package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Report;
import com.entity.User;
import com.mapper.ReportMapper;
import com.mapper.UserMapper;
import com.service.ReportService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Resource
    private UserMapper userMapper;

    @Override
    public void add(Report report) {
        report.setTime(LocalDateTime.now());
        if (report.getUserId() != null) {
            User user = userMapper.selectById(report.getUserId());
            if (user != null) {
                report.setUserName(user.getName());
            }
        }
        this.save(report);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<Report> selectAll(Report report) {
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        if (report != null && StringUtils.hasText(report.getContent())){
            queryWrapper.like(Report::getContent, report.getContent());
        }
        queryWrapper.orderByDesc(Report::getTime);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Report> selectPage(Report report, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        if (report != null && StringUtils.hasText(report.getContent())) {
            queryWrapper.like(Report::getContent, report.getContent());
        }
        if (report != null && report.getUserId() != null){
            queryWrapper.eq(Report::getUserId,report.getUserId());
        }
        queryWrapper.orderByDesc(Report::getTime);
        return this.page(new Page<>(pageNum,pageSize),queryWrapper);
    }
}
