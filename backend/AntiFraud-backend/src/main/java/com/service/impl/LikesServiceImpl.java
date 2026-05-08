package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Article;
import com.entity.Likes;
import com.entity.Message;
import com.entity.User;
import com.mapper.ArticleMapper;
import com.mapper.LikesMapper;
import com.mapper.UserMapper;
import com.service.LikesService;
import com.service.MessageService;
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

    @Resource
    private MessageService messageService;

    @Resource
    private LikesMapper likesMapper;

    @Override
    public void add(Likes likes) {
        QueryWrapper<Likes> queryWrapper = new QueryWrapper<>();
        // 根据不同类型的ID构建查询条件
        if (likes.getArticleId() != null) {
            queryWrapper.eq("user_id", likes.getUserId()).eq("article_id", likes.getArticleId());
        } else if (likes.getVideoId() != null) {
            queryWrapper.eq("user_id", likes.getUserId()).eq("video_id", likes.getVideoId());
        } else if (likes.getPublicityId() != null) {
            queryWrapper.eq("user_id", likes.getUserId()).eq("publicity_id", likes.getPublicityId());
        } else if (likes.getActivityId() != null) {
            queryWrapper.eq("user_id", likes.getUserId()).eq("activity_id", likes.getActivityId());
        }

        Likes existingLike = likesMapper.selectOne(queryWrapper);
        // 如果已存在则删除（取消点赞），否则新增（点赞）
        if (existingLike != null) {
            likesMapper.deleteById(existingLike.getId());
        } else {
            likes.setTime(LocalDateTime.now());
            likesMapper.insert(likes);

            // 帖子点赞消息通知
            if (likes.getArticleId() != null && likes.getUserId() != null) {
                Article article = articleMapper.selectById(likes.getArticleId());
                User user = userMapper.selectById(likes.getUserId());

                // 只有当帖子作者不是点赞者本人时才发送通知
                if (article != null && article.getUserId() != null && !article.getUserId().equals(likes.getUserId())) {
                    Message message = new Message();
                    message.setUserId(article.getUserId()); // 接收者：帖子作者
                    message.setFromUserId(likes.getUserId()); // 发送者：点赞者
                    message.setFromUserName(user != null ? user.getName() : "未知用户");
                    message.setArticleId(likes.getArticleId()); // 关联帖子ID
                    message.setArticleTitle(article.getTitle()); // 关联帖子标题
                    message.setType("like"); // 消息类型：点赞
                    message.setContent((user != null ? user.getName() : "未知用户") + " 点赞了你的帖子《" + article.getTitle() + "》"); // 消息内容
                    message.setIsRead(0); // 未读状态
                    message.setCreatedTime(LocalDateTime.now()); // 创建时间
                    messageService.add(message); // 保存消息
                }
            }
        }
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<Likes> selectAll(Likes likes) {
        QueryWrapper<Likes> queryWrapper = new QueryWrapper<>();
        if (likes.getUserId() != null) {
            queryWrapper.eq("user_id", likes.getUserId());
        }
        if (likes.getArticleId() != null) {
            queryWrapper.eq("article_id", likes.getArticleId());
        }
        if (likes.getVideoId() != null) {
            queryWrapper.eq("video_id", likes.getVideoId());
        }
        if (likes.getPublicityId() != null) {
            queryWrapper.eq("publicity_id", likes.getPublicityId());
        }
        if (likes.getActivityId() != null) {
            queryWrapper.eq("activity_id", likes.getActivityId());
        }
        return likesMapper.selectList(queryWrapper);
    }

    @Override
    public Page<Likes> selectPage(Likes likes, Integer pageNum, Integer pageSize) {
        QueryWrapper<Likes> queryWrapper = new QueryWrapper<>();
        if (likes.getUserName() != null && !likes.getUserName().isEmpty()) {
            queryWrapper.like("user_name", likes.getUserName());
        }
        if (likes.getArticleTitle() != null && !likes.getArticleTitle().isEmpty()) {
            queryWrapper.like("article_title", likes.getArticleTitle());
        }
        // 新增：支持按视频标题搜索
        if (likes.getVideoTitle() != null && !likes.getVideoTitle().isEmpty()) {
            queryWrapper.like("video_title", likes.getVideoTitle());
        }
        if (likes.getPublicityTitle() != null && !likes.getPublicityTitle().isEmpty()) {
            queryWrapper.like("publicity_title", likes.getPublicityTitle());
        }
        if (likes.getActivityTitle() != null && !likes.getActivityTitle().isEmpty()) {
            queryWrapper.like("activity_title", likes.getActivityTitle());
        }
        queryWrapper.orderByDesc("time");
        Page<Likes> page = likesMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        
        // 填充帖子标题和用户名称
        for (Likes like : page.getRecords()) {
            if (like.getArticleId() != null) {
                Article article = articleMapper.selectById(like.getArticleId());
                if (article != null) {
                    like.setArticleTitle(article.getTitle());
                }
            }
            if (like.getUserId() != null) {
                User user = userMapper.selectById(like.getUserId());
                if (user != null) {
                    like.setUserName(user.getName());
                }
            }
        }
        
        return page;
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
