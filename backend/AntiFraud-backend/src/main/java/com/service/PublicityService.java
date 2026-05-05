package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.Publicity;

import java.util.List;

public interface PublicityService extends IService<Publicity> {
    void add(Publicity publicity);

    void deleteBatch(List<Integer> ids);

    List<Publicity> selectAll(Publicity publicity);

    Page<Publicity> selectPage(Publicity publicity, Integer pageNum, Integer pageSize);

    List<Publicity> selectTop4();
}
