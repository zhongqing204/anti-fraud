package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Article;
import com.entity.Collect;
import com.entity.User;
import com.mapper.ArticleMapper;
import com.mapper.CollectMapper;
import com.mapper.UserMapper;
import com.service.CollectService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Override
    public void add(Collect collect) {
        // 自动填充用户名
        if (collect.getUserId() != null) {
            User user = userMapper.selectById(collect.getUserId());
            if (user != null) {
                collect.setUserName(user.getName());
            }
        }

        // 自动填充帖子标题
        if (collect.getArticleId() != null) {
            Article article = articleMapper.selectById(collect.getArticleId());
            if (article != null) {
                collect.setArticleTitle(article.getTitle());
            }
        }

        // 设置收藏时间
        collect.setTime(LocalDateTime.now());

        // 判断是否已收藏
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Collect::getUserId, collect.getUserId())
                .eq(Collect::getArticleId, collect.getArticleId());
        List<Collect> existingCollects = this.list(queryWrapper);

        if (existingCollects == null || existingCollects.isEmpty()) {
            this.save(collect);
        } else {
            this.removeById(existingCollects.get(0).getId());
        }
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<Collect> selectAll(Collect collect) {
        LambdaQueryWrapper<Collect> queryWrapper = buildQueryWrapper(collect);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Collect> selectPage(Collect collect, Integer pageNum, Integer pageSize) {
        Page<Collect> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Collect> queryWrapper = buildQueryWrapper(collect);
        return this.page(page, queryWrapper);
    }

    private LambdaQueryWrapper<Collect> buildQueryWrapper(Collect collect) {
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        if (collect != null) {
            if (collect.getUserId() != null) {
                queryWrapper.eq(Collect::getUserId, collect.getUserId());
            }
            if (collect.getArticleId() != null) {
                queryWrapper.eq(Collect::getArticleId, collect.getArticleId());
            }
            if (collect.getUserName() != null && !collect.getUserName().isEmpty()) {
                queryWrapper.like(Collect::getUserName, collect.getUserName());
            }
            if (collect.getArticleTitle() != null && !collect.getArticleTitle().isEmpty()) {
                queryWrapper.like(Collect::getArticleTitle, collect.getArticleTitle());
            }
        }
        queryWrapper.orderByDesc(Collect::getTime);
        return queryWrapper;
    }
}

