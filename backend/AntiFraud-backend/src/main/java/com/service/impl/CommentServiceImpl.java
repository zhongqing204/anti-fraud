package com.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Article;
import com.entity.Comment;
import com.entity.User;
import com.mapper.ArticleMapper;
import com.mapper.CommentMapper;
import com.mapper.UserMapper;
import com.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Override
    public void add(Comment comment) {
        comment.setTime(DateUtil.now());
        this.save(comment);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<Comment> selectAll(Comment comment) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        if (comment != null) {
            if (comment.getUserId() != null) {
                queryWrapper.eq(Comment::getUserId, comment.getUserId());
            }
            if (comment.getArticleId() != null) {
                queryWrapper.eq(Comment::getArticleId, comment.getArticleId());
            }
        }
        queryWrapper.orderByDesc(Comment::getId);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Comment> selectPage(Comment comment, Integer pageNum, Integer pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();

        if (comment != null) {
            if (comment.getUserId() != null) {
                queryWrapper.eq(Comment::getUserId, comment.getUserId());
            }
            if (comment.getArticleId() != null) {
                queryWrapper.eq(Comment::getArticleId, comment.getArticleId());
            }
        }

        queryWrapper.orderByDesc(Comment::getId);

        // 执行分页查询
        Page<Comment> commentPage = this.page(page, queryWrapper);

        if (commentPage.getRecords() != null && !commentPage.getRecords().isEmpty()) {
            // 批量查询用户信息
            List<Integer> userIds = commentPage.getRecords().stream()
                    .map(Comment::getUserId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询文章信息
            List<Integer> articleIds = commentPage.getRecords().stream()
                    .map(Comment::getArticleId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(userIds);
                Map<Integer, String> userNameMap = users.stream()
                        .collect(Collectors.toMap(User::getId, User::getName));
                Map<Integer, String> userAvatarMap = users.stream()
                        .collect(Collectors.toMap(User::getId, User::getAvatar));

                // 设置评论的用户名和头像
                commentPage.getRecords().forEach(c -> {
                    if (c.getUserId() != null) {
                        c.setUserName(userNameMap.get(c.getUserId()));
                        c.setUserAvatar(userAvatarMap.get(c.getUserId()));
                    }
                });
            }

            if (!articleIds.isEmpty()) {
                List<Article> articles = articleMapper.selectBatchIds(articleIds);
                Map<Integer, String> articleTitleMap = articles.stream()
                        .collect(Collectors.toMap(Article::getId, Article::getTitle));

                // 新增：创建文章对象映射，用于后续标题过滤
                Map<Integer, Article> articleMap = articles.stream()
                        .collect(Collectors.toMap(Article::getId, a -> a));

                // 设置评论的文章标题
                commentPage.getRecords().forEach(c -> {
                    if (c.getArticleId() != null) {
                        c.setArticleTitle(articleTitleMap.get(c.getArticleId()));
                    }
                });

                // 新增：如果传入了文章标题参数，进行模糊匹配过滤（管理员页面搜索功能）
                if (comment != null && comment.getArticleTitle() != null && !comment.getArticleTitle().isEmpty()) {
                    commentPage.getRecords().removeIf(c -> {
                        Article article = articleMap.get(c.getArticleId());
                        return article == null || !article.getTitle().contains(comment.getArticleTitle());
                    });

                    // 重新设置总数为过滤后的数量
                    commentPage.setTotal(commentPage.getRecords().size());
                }
            }
        }

        return commentPage;
    }
}
