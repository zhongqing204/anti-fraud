package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.Article;

import java.util.List;

public interface ArticleService extends IService<Article> {
    void add(Article article);

    void deleteBatch(List<Integer> ids);

    List<Article> selectAll(Article article);

    Page<Article> selectPage(String userName, String title, Integer userId, Integer pageNum, Integer pageSize);
}
