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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LikesServiceImpl extends ServiceImpl<LikesMapper, Likes> implements LikesService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Override
    public void add(Likes likes) {
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
        List<Likes> list = this.list(queryWrapper);
        setUserInfo(list);
        return list;
    }

    @Override
    public Page<Likes> selectPage(Likes likes, Integer pageNum, Integer pageSize) {
        Page<Likes> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Likes> queryWrapper = buildQueryWrapper(likes);
        Page<Likes> result = this.page(page, queryWrapper);
        setUserInfo(result.getRecords());
        return result;
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
        }
        return queryWrapper;
    }

    private void setUserInfo(List<Likes> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        List<Integer> userIds = list.stream()
                .map(Likes::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        List<Integer> articleIds = list.stream()
                .map(Likes::getArticleId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (userIds.isEmpty() && articleIds.isEmpty()) {
            return;
        }

        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u, (u1, u2) -> u1));

            list.forEach(like -> {
                if (like.getUserId() != null) {
                    User user = userMap.get(like.getUserId());
                    if (user != null) {
                        like.setUserName(user.getName());
                    }
                }
            });
        }

        if (!articleIds.isEmpty()) {
            List<Article> articles = articleMapper.selectBatchIds(articleIds);
            Map<Integer, Article> articleMap = articles.stream()
                    .collect(Collectors.toMap(Article::getId, a -> a, (a1, a2) -> a1));

            list.forEach(like -> {
                if (like.getArticleId() != null) {
                    Article article = articleMap.get(like.getArticleId());
                    if (article != null) {
                        like.setArticleTitle(article.getTitle());
                    }
                }
            });
        }
    }
}
