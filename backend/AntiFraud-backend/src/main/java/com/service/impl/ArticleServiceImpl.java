package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Article;
import com.mapper.ArticleMapper;
import com.service.ArticleService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
    @Override
    public void add(Article article) {
        article.setTime(LocalDateTime.now());
        this.save(article);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<Article> selectAll(Article article) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        if (article != null){
            if (StringUtils.hasText(article.getTitle())){
                queryWrapper.like(Article::getTitle,article.getTitle());
            }
            if (article.getUserId() != null){
                queryWrapper.eq(Article::getUserId,article.getUserId());
            }
            if (article.getCategoryId() != null){
                queryWrapper.eq(Article::getCategoryId,article.getCategoryId());
            }
            if (StringUtils.hasText(article.getStatus())){
                queryWrapper.eq(Article::getStatus,article.getStatus());
            }
            queryWrapper.orderByDesc(Article::getTime);
        }
        return this.list(queryWrapper);
    }

    @Override
    public Page<Article> selectPage(String userName, String title, Integer userId, Integer pageNum, Integer pageSize) {
        Page<Article> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(userName)){
            queryWrapper.like(Article::getUserName,userName);
        }

        if (StringUtils.hasText(title)){
            queryWrapper.like(Article::getTitle,title);
        }

        if (userId != null){
            queryWrapper.eq(Article::getUserId,userId);
        }

        queryWrapper.orderByDesc(Article::getTime);

        return this.page(page,queryWrapper);
    }
}
