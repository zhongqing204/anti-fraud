package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Article;
import com.entity.Likes;
import com.entity.User;
import com.mapper.ArticleMapper;
import com.mapper.LikesMapper;
import com.mapper.UserMapper;
import com.service.LikesService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LikesServiceImpl extends ServiceImpl<LikesMapper, Likes> implements LikesService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Override
    public void add(Likes likes) {
        // 自动填充用户名
        if (likes.getUserId() != null) {
            User user = userMapper.selectById(likes.getUserId());
            if (user != null) {
                likes.setUserName(user.getName());
            }
        }

        // 自动填充帖子标题
        if (likes.getArticleId() != null) {
            Article article = articleMapper.selectById(likes.getArticleId());
            if (article != null) {
                likes.setArticleTitle(article.getTitle());
            }
        }

        // 设置点赞时间
        likes.setTime(LocalDateTime.now());

        // 判断是否已点赞
        LambdaQueryWrapper<Likes> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Likes::getUserId, likes.getUserId())
                .eq(Likes::getArticleId, likes.getArticleId());
        List<Likes> existingLikes = this.list(queryWrapper);

        if (existingLikes == null || existingLikes.isEmpty()) {
            this.save(likes);
        } else {
            this.removeById(existingLikes.get(0).getId());
        }
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<Likes> selectAll(Likes likes) {
        LambdaQueryWrapper<Likes> queryWrapper = buildQueryWrapper(likes);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Likes> selectPage(Likes likes, Integer pageNum, Integer pageSize) {
        Page<Likes> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Likes> queryWrapper = buildQueryWrapper(likes);
        return this.page(page, queryWrapper);
    }

    private LambdaQueryWrapper<Likes> buildQueryWrapper(Likes likes) {
        LambdaQueryWrapper<Likes> queryWrapper = new LambdaQueryWrapper<>();
        if (likes != null) {
            if (likes.getUserId() != null) {
                queryWrapper.eq(Likes::getUserId, likes.getUserId());
            }
            if (likes.getArticleId() != null) {
                queryWrapper.eq(Likes::getArticleId, likes.getArticleId());
            }
            if (likes.getUserName() != null && !likes.getUserName().isEmpty()) {
                queryWrapper.like(Likes::getUserName, likes.getUserName());
            }
            if (likes.getArticleTitle() != null && !likes.getArticleTitle().isEmpty()) {
                queryWrapper.like(Likes::getArticleTitle, likes.getArticleTitle());
            }
        }
        queryWrapper.orderByDesc(Likes::getTime);
        return queryWrapper;
    }
}
