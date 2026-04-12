package com.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Notice;
import com.mapper.NoticeMapper;
import com.service.NoticeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {
    @Override
    public void add(Notice notice) {
        notice.setTime(LocalDateTime.now());
        this.save(notice);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<Notice> selectAll(Notice notice) {
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();

        if (notice != null && StringUtils.hasText(notice.getTitle())){
            queryWrapper.like(Notice::getTitle, notice.getTitle());
        }
        queryWrapper.orderByDesc(Notice::getTime);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Notice> selectPage(Notice notice, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        if (notice != null && StringUtils.hasText(notice.getTitle())) {
            queryWrapper.like(Notice::getTitle, notice.getTitle());
        }
        queryWrapper.orderByDesc(Notice::getTime);
        return this.page(new Page<>(pageNum,pageSize),queryWrapper);
    }
}
