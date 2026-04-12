package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
    void add(Category category);

    void deleteBatch(List<Integer> ids);

    List<Category> selectAll(Category category);

    Page<Category> selectPage(Category category, Integer pageNum, Integer pageSize);
}
