package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Article;
import com.entity.Collect;
import com.entity.Message;
import com.entity.User;
import com.mapper.ArticleMapper;
import com.mapper.CollectMapper;
import com.mapper.UserMapper;
import com.service.CollectService;
import com.service.MessageService;
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

    @Resource
    private MessageService messageService;

    @Resource
    private CollectMapper collectMapper;

    @Override
    public void add(Collect collect) {
        QueryWrapper<Collect> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", collect.getUserId());

        if (collect.getArticleId() != null) {
            queryWrapper.eq("article_id", collect.getArticleId());
        } else if (collect.getVideoId() != null) {
            queryWrapper.eq("video_id", collect.getVideoId());
        } else if (collect.getPublicityId() != null) {
            queryWrapper.eq("publicity_id", collect.getPublicityId());
        } else if (collect.getActivityId() != null) {
            queryWrapper.eq("activity_id", collect.getActivityId());
        }

        Collect existingCollect = collectMapper.selectOne(queryWrapper);
        // 如果已存在则删除（取消收藏），否则新增（收藏）
        if (existingCollect != null) {
            collectMapper.deleteById(existingCollect.getId());
        } else {
            collect.setTime(LocalDateTime.now());
            collectMapper.insert(collect);

            // 帖子收藏消息通知
            if (collect.getArticleId() != null && collect.getUserId() != null) {
                Article article = articleMapper.selectById(collect.getArticleId());
                User user = userMapper.selectById(collect.getUserId());

                // 只有当帖子作者不是收藏者本人时才发送通知
                if (article != null && article.getUserId() != null && !article.getUserId().equals(collect.getUserId())) {
                    Message message = new Message();
                    message.setUserId(article.getUserId()); // 接收者：帖子作者
                    message.setFromUserId(collect.getUserId()); // 发送者：收藏者
                    message.setFromUserName(user != null ? user.getName() : "未知用户");
                    message.setArticleId(collect.getArticleId()); // 关联帖子ID
                    message.setArticleTitle(article.getTitle()); // 关联帖子标题
                    message.setType("collect"); // 消息类型：收藏
                    message.setContent((user != null ? user.getName() : "未知用户") + " 收藏了你的帖子《" + article.getTitle() + "》"); // 消息内容
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
    public List<Collect> selectAll(Collect collect) {
        QueryWrapper<Collect> queryWrapper = new QueryWrapper<>();
        if (collect.getUserId() != null) {
            queryWrapper.eq("user_id", collect.getUserId());
        }
        if (collect.getArticleId() != null) {
            queryWrapper.eq("article_id", collect.getArticleId());
        }
        if (collect.getVideoId() != null) {
            queryWrapper.eq("video_id", collect.getVideoId());
        }
        if (collect.getPublicityId() != null) {
            queryWrapper.eq("publicity_id", collect.getPublicityId());
        }
        if (collect.getActivityId() != null) {
            queryWrapper.eq("activity_id", collect.getActivityId());
        }
        return collectMapper.selectList(queryWrapper);
    }

    @Override
    public Page<Collect> selectPage(Collect collect, Integer pageNum, Integer pageSize) {
        QueryWrapper<Collect> queryWrapper = new QueryWrapper<>();
        if (collect.getUserName() != null && !collect.getUserName().isEmpty()) {
            queryWrapper.like("user_name", collect.getUserName());
        }
        if (collect.getArticleTitle() != null && !collect.getArticleTitle().isEmpty()) {
            queryWrapper.like("article_title", collect.getArticleTitle());
        }
        if (collect.getVideoTitle() != null && !collect.getVideoTitle().isEmpty()) {
            queryWrapper.like("video_title", collect.getVideoTitle());
        }
        if (collect.getPublicityTitle() != null && !collect.getPublicityTitle().isEmpty()) {
            queryWrapper.like("publicity_title", collect.getPublicityTitle());
        }
        if (collect.getActivityTitle() != null && !collect.getActivityTitle().isEmpty()) {
            queryWrapper.like("activity_title", collect.getActivityTitle());
        }
        queryWrapper.orderByDesc("time");
        return collectMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
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
