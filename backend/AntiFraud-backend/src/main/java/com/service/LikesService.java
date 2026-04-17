package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.Likes;

import java.util.List;

public interface LikesService extends IService<Likes> {
    void  add(Likes likes);
    void  deleteBatch(List<Integer> ids);
    List<Likes> selectAll(Likes likes);
    Page<Likes> selectPage(Likes likes, Integer pageNum, Integer pageSize);
}
