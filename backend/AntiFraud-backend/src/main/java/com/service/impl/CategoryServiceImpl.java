package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Category;
import com.mapper.CategoryMapper;
import com.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Override
    public void add(Category category) {
        this.save(category);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<Category> selectAll(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        if (category != null && StringUtils.hasText(category.getName())){
            queryWrapper.like(Category::getName, category.getName());
        }
        queryWrapper.orderByDesc(Category::getId);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Category> selectPage(Category category, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        if (category != null && StringUtils.hasText(category.getName())) {
            queryWrapper.like(Category::getName, category.getName());
        }
        queryWrapper.orderByDesc(Category::getId);
        return this.page(new Page<>(pageNum, pageSize), queryWrapper);
    }
}
