package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Article;
import com.entity.User;
import com.mapper.ArticleMapper;
import com.mapper.UserMapper;
import com.service.ArticleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Resource
    private UserMapper userMapper;

    @Override
    public void add(Article article) {
        article.setTime(LocalDateTime.now());
        article.setStatus("审核通过");
        if (article.getUserId() != null) {
            User user = userMapper.selectById(article.getUserId());
            if (user != null) {
                article.setUserAvatar(user.getAvatar());
                article.setUserName(user.getName());
            }
        }
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
            if (StringUtils.hasText(article.getStatus())){
                queryWrapper.eq(Article::getStatus,article.getStatus());
            }
            queryWrapper.orderByDesc(Article::getTime);
        }
        List<Article> list = this.list(queryWrapper);
        setUserInfo(list);
        return list;
    }

    @Override
    public Page<Article> selectPage(String userName, String title, Integer userId, String status, Integer pageNum, Integer pageSize) {
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

        if (StringUtils.hasText(status)){
            queryWrapper.eq(Article::getStatus,status);
        }

        queryWrapper.orderByDesc(Article::getTime);

        Page<Article> result = this.page(page,queryWrapper);
        setUserInfo(result.getRecords());
        return result;
    }

    @Override
    public Article selectById(Integer id) {
        Article article = this.getById(id);
        if (article != null && article.getUserId() != null) {
            User user = userMapper.selectById(article.getUserId());
            if (user != null) {
                article.setUserAvatar(user.getAvatar());
            }
        }
        return article;
    }

    private void setUserInfo(List<Article> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        List<Integer> userIds = list.stream()
                .map(Article::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return;
        }

        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u, (u1, u2) -> u1));

        list.forEach(article -> {
            if (article.getUserId() != null) {
                User user = userMap.get(article.getUserId());
                if (user != null) {
                    article.setUserAvatar(user.getAvatar());
                }
            }
        });
    }
}
