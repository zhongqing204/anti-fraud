package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.*;
import com.mapper.ArticleMapper;
import com.mapper.UserMapper;
import com.service.ArticleService;
import com.service.CollectService;
import com.service.CommentService;
import com.service.LikesService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private LikesService likesService;

    @Resource
    private CollectService collectService;

    @Resource
    private CommentService commentService;

    @Override
    public void add(Article article) {
        article.setTime(LocalDateTime.now());
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
            queryWrapper.orderByDesc(Article::getTime);
        }
        List<Article> list = this.list(queryWrapper);
        setUserInfo(list, null);
        return list;
    }

    @Override
    public Page<Article> selectPage(String userName, String title, Integer currentUserId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(userName)){
            queryWrapper.like(Article::getUserName,userName);
        }

        if (StringUtils.hasText(title)){
            queryWrapper.like(Article::getTitle,title);
        }

        queryWrapper.orderByDesc(Article::getTime);

        Page<Article> result = this.baseMapper.selectPage(new Page<>(pageNum,pageSize),queryWrapper);
        setUserInfo(result.getRecords(), currentUserId);
        return result;
    }

    @Override
    public Article selectById(Integer id) {
        Article article = this.getById(id);
        if (article != null) {
            if (article.getUserId() != null) {
                User user = userMapper.selectById(article.getUserId());
                if (user != null) {
                    article.setUserAvatar(user.getAvatar());
                    article.setUserName(user.getName());
                }
            }

            // 查询点赞数
            long likeCount = likesService.count(new LambdaQueryWrapper<Likes>().eq(Likes::getArticleId, id));
            article.setLikeCount((int) likeCount);

            // 查询收藏数
            long collectCount = collectService.count(new LambdaQueryWrapper<Collect>().eq(Collect::getArticleId, id));
            article.setCollectCount((int) collectCount);

            // 查询评论数
            long commentCount = commentService.count(new LambdaQueryWrapper<Comment>().eq(Comment::getArticleId, id));
            article.setCommentCount((int) commentCount);
        }
        return article;
    }

    private void setUserInfo(List<Article> list, Integer currentUserId) {
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

        // 查询用户信息
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u, (u1, u2) -> u1));

        // 新增：批量查询所有帖子的点赞、收藏、评论数据
        List<Integer> articleIds = list.stream()
                .map(Article::getId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        // 按文章ID分组点赞数据
        Map<Integer, List<Likes>> likeMap = likesService.list(new LambdaQueryWrapper<Likes>().in(Likes::getArticleId, articleIds)).stream()
                .collect(Collectors.groupingBy(Likes::getArticleId));


        // 按文章ID分组收藏数据
        Map<Integer, List<Collect>> collectMap = collectService.list(new LambdaQueryWrapper<Collect>().in(Collect::getArticleId, articleIds)).stream()
                .collect(Collectors.groupingBy(Collect::getArticleId));

        // 按文章ID分组评论数据
        Map<Integer, List<Comment>> commentMap = commentService.list(new LambdaQueryWrapper<Comment>().in(Comment::getArticleId, articleIds)).stream()
                .collect(Collectors.groupingBy(Comment::getArticleId));

        // 为每个帖子设置用户信息、点赞数、收藏数、评论数
        list.forEach(article -> {
            if (article.getUserId() != null) {
                User user = userMap.get(article.getUserId());
                if (user != null) {
                    article.setUserAvatar(user.getAvatar());
                }
            }

            if (article.getId() != null) {
                List<Likes> likes = likeMap.get(article.getId());
                article.setLikeCount(likes != null ? likes.size() : 0);

                // 检查当前用户是否已点赞
                if (currentUserId != null) {
                    boolean isLiked = likes != null && likes.stream()
                            .anyMatch(like -> like.getUserId().equals(currentUserId));
                    article.setLiked(isLiked);
                }

                List<Collect> collects = collectMap.get(article.getId());
                article.setCollectCount(collects != null ? collects.size() : 0);

                // 检查当前用户是否已收藏
                if (currentUserId != null) {
                    boolean isCollected = collects != null && collects.stream()
                            .anyMatch(collect -> collect.getUserId().equals(currentUserId));
                    article.setCollected(isCollected);
                }

                List<Comment> comments = commentMap.get(article.getId());
                article.setCommentCount(comments != null ? comments.size() : 0);
            }
        });
    }

    @Override
    public List<Article> selectTop10() {
        // 查询所有帖子
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        List<Article> allArticles = this.list(queryWrapper);

        // 随机打乱列表
        Collections.shuffle(allArticles);

        // 返回前10条，如果不足10条则返回全部
        return allArticles.subList(0, Math.min(10, allArticles.size()));
    }
}
