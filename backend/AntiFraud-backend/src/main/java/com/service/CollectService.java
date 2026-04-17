package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.Collect;

import java.util.List;

public interface CollectService extends IService<Collect> {
    void add(Collect collect);

    void deleteBatch(List<Integer> ids);

    List<Collect> selectAll(Collect collect);

    Page<Collect> selectPage(Collect collect, Integer pageNum, Integer pageSize);
}
