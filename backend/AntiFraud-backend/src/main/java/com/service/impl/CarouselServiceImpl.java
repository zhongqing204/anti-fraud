package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Carousel;
import com.mapper.CarouselMapper;
import com.service.CarouselService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarouselServiceImpl extends ServiceImpl<CarouselMapper, Carousel> implements CarouselService {

    @Override
    public void add(Carousel carousel) {
        this.save(carousel);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    public Carousel selectById(Integer id) {
        return this.getById(id);
    }

    @Override
    public List<Carousel> selectAll(Carousel carousel) {
        LambdaQueryWrapper<Carousel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Carousel::getId);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Carousel> selectPage(Carousel carousel, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Carousel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Carousel::getId);
        return this.page(new Page<>(pageNum, pageSize), queryWrapper);
    }
}
