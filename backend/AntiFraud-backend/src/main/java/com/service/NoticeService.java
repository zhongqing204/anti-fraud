package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.Notice;

import java.util.List;

public interface NoticeService extends IService<Notice> {
    void add(Notice notice);

    void deleteBatch(List<Integer> ids);

    List<Notice> selectAll(Notice notice);

    Page<Notice> selectPage(Notice notice, Integer pageNum, Integer pageSize);
}
