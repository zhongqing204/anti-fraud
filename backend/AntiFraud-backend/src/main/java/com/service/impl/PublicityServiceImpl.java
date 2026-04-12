package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Publicity;
import com.mapper.PublicityMapper;
import com.service.PublicityService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PublicityServiceImpl extends ServiceImpl<PublicityMapper, Publicity> implements PublicityService {
    @Override
    public void add(Publicity publicity) {
        publicity.setCreateTime(LocalDateTime.now());
        publicity.setUpdateTime(LocalDateTime.now());
        save(publicity);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<Publicity> selectAll(Publicity publicity) {
        LambdaQueryWrapper<Publicity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(publicity.getTitle())) {
            wrapper.like(Publicity::getTitle, publicity.getTitle());
        }
        wrapper.orderByDesc(Publicity::getCreateTime);
        return list(wrapper);
    }

    @Override
    public Page<Publicity> selectPage(Publicity publicity, Integer pageNum, Integer pageSize) {
        Page<Publicity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Publicity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(publicity.getTitle())) {
            wrapper.like(Publicity::getTitle, publicity.getTitle());
        }
        wrapper.orderByDesc(Publicity::getCreateTime);
        return page(page, wrapper);
    }
}
