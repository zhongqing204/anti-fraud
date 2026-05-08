package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.Carousel;

import java.util.List;

public interface CarouselService extends IService<Carousel> {
    void add(Carousel carousel);

    void deleteBatch(List<Integer> ids);

    Carousel selectById(Integer id);

    List<Carousel> selectAll(Carousel carousel);

    Page<Carousel> selectPage(Carousel carousel, Integer pageNum, Integer pageSize);
}

