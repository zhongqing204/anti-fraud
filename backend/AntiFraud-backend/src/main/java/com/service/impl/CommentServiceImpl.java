package com.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.*;
import com.mapper.*;
import com.service.CommentService;
import com.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private MessageService messageService;

    @Resource
    private PublicityMapper publicityMapper;

    @Resource
    private ActivityMapper activityMapper;

    /**
     * 新增评论
     * 支持 articleId、videoId、publicityId、activityId 四种类型，实现评论独立
     */
    @Override
    public void add(Comment comment) {
        comment.setTime(DateUtil.now());
        this.save(comment);

        // 处理帖子评论消息通知（通知帖子作者）
        if (comment.getArticleId() != null && comment.getUserId() != null) {
            Article article = articleMapper.selectById(comment.getArticleId());
            User user = userMapper.selectById(comment.getUserId());

            if (article != null && article.getUserId() != null && !article.getUserId().equals(comment.getUserId())) {
                Message message = new Message();
                message.setUserId(article.getUserId());
                message.setFromUserId(comment.getUserId());
                message.setFromUserName(user != null ? user.getName() : "未知用户");
                message.setArticleId(comment.getArticleId());
                message.setArticleTitle(article.getTitle());
                message.setType("comment");
                message.setContent((user != null ? user.getName() : "未知用户") + " 评论了你的帖子《" + article.getTitle() + "》");
                message.setIsRead(0);
                message.setCreatedTime(LocalDateTime.now());
                messageService.add(message);
            }
        }

        // 处理视频评论消息通知（通知评论者自己）
        if (comment.getVideoId() != null && comment.getUserId() != null) {
            Video video = videoMapper.selectById(comment.getVideoId());
            User user = userMapper.selectById(comment.getUserId());

            if (video != null && video.getId() != null) {
                Message message = new Message();
                message.setUserId(comment.getUserId());
                message.setFromUserId(comment.getUserId());
                message.setFromUserName(user != null ? user.getName() : "未知用户");
                message.setVideoId(comment.getVideoId());
                message.setVideoTitle(video.getTitle());
                message.setType("comment");
                message.setContent("你评论了视频《" + video.getTitle() + "》");
                message.setIsRead(0);
                message.setCreatedTime(LocalDateTime.now());
                messageService.add(message);
            }
        }

        // 处理反诈宣传评论消息通知（通知评论者自己）
        if (comment.getPublicityId() != null && comment.getUserId() != null) {
            Publicity publicity = publicityMapper.selectById(comment.getPublicityId());
            User user = userMapper.selectById(comment.getUserId());

            if (publicity != null) {
                Message message = new Message();
                message.setUserId(comment.getUserId());
                message.setFromUserId(comment.getUserId());
                message.setFromUserName(user != null ? user.getName() : "未知用户");
                message.setPublicityId(comment.getPublicityId());
                message.setPublicityTitle(publicity.getTitle());
                message.setType("comment");
                message.setContent("你评论了宣传《" + publicity.getTitle() + "》");
                message.setIsRead(0);
                message.setCreatedTime(LocalDateTime.now());
                messageService.add(message);
            }
        }

        // 处理活动评论消息通知（通知评论者自己）
        if (comment.getActivityId() != null && comment.getUserId() != null) {
            Activity activity = activityMapper.selectById(comment.getActivityId());
            User user = userMapper.selectById(comment.getUserId());

            if (activity != null) {
                Message message = new Message();
                message.setUserId(comment.getUserId());
                message.setFromUserId(comment.getUserId());
                message.setFromUserName(user != null ? user.getName() : "未知用户");
                message.setActivityId(comment.getActivityId());
                message.setActivityTitle(activity.getTitle());
                message.setType("comment");
                message.setContent("你评论了活动《" + activity.getTitle() + "》");
                message.setIsRead(0);
                message.setCreatedTime(LocalDateTime.now());
                messageService.add(message);
            }
        }
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    /**
     * 查询所有评论
     * 支持按 articleId、videoId 分别查询，确保数据独立
     */
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
            if (comment.getVideoId() != null) {
                queryWrapper.eq(Comment::getVideoId, comment.getVideoId());
            }
            if (comment.getPublicityId() != null) {
                queryWrapper.eq(Comment::getPublicityId, comment.getPublicityId());
            }
            if (comment.getActivityId() != null) {
                queryWrapper.eq(Comment::getActivityId, comment.getActivityId());
            }
        }
        queryWrapper.orderByDesc(Comment::getId);
        List<Comment> list = this.list(queryWrapper);

        if (list != null && !list.isEmpty()) {
            List<Integer> userIds = list.stream()
                    .map(Comment::getUserId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(userIds);
                Map<Integer, String> userNameMap = users.stream()
                        .collect(Collectors.toMap(User::getId, User::getName));
                Map<Integer, String> userAvatarMap = users.stream()
                        .collect(Collectors.toMap(User::getId, User::getAvatar));

                list.forEach(c -> {
                    if (c.getUserId() != null) {
                        c.setUserName(userNameMap.get(c.getUserId()));
                        c.setUserAvatar(userAvatarMap.get(c.getUserId()));
                    }
                });
            }
        }

        return list;
    }

    /**
     * 分页查询评论
     * 支持按 articleId、videoId 分别查询，并填充标题
     */
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
            if (comment.getVideoId() != null) {
                queryWrapper.eq(Comment::getVideoId, comment.getVideoId());
            }
            if (comment.getPublicityId() != null) {
                queryWrapper.eq(Comment::getPublicityId, comment.getPublicityId());
            }
            if (comment.getActivityId() != null) {
                queryWrapper.eq(Comment::getActivityId, comment.getActivityId());
            }
        }

        queryWrapper.orderByDesc(Comment::getId);

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

            // 批量查询视频信息
            List<Integer> videoIds = commentPage.getRecords().stream()
                    .map(Comment::getVideoId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询宣传信息
            List<Integer> publicityIds = commentPage.getRecords().stream()
                    .map(Comment::getPublicityId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询活动信息
            List<Integer> activityIds = commentPage.getRecords().stream()
                    .map(Comment::getActivityId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(userIds);
                Map<Integer, String> userNameMap = users.stream()
                        .collect(Collectors.toMap(User::getId, User::getName));
                Map<Integer, String> userAvatarMap = users.stream()
                        .collect(Collectors.toMap(User::getId, User::getAvatar));

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

                commentPage.getRecords().forEach(c -> {
                    if (c.getArticleId() != null) {
                        c.setArticleTitle(articleTitleMap.get(c.getArticleId()));
                    }
                });
            }

            if (!videoIds.isEmpty()) {
                List<Video> videos = videoMapper.selectBatchIds(videoIds);
                Map<Integer, String> videoTitleMap = videos.stream()
                        .collect(Collectors.toMap(Video::getId, Video::getTitle));

                commentPage.getRecords().forEach(c -> {
                    if (c.getVideoId() != null) {
                        c.setVideoTitle(videoTitleMap.get(c.getVideoId()));
                    }
                });
            }

            // 添加宣传标题填充逻辑
            if (!publicityIds.isEmpty()) {
                List<Publicity> publicities = publicityMapper.selectBatchIds(publicityIds);
                Map<Integer, String> publicityTitleMap = publicities.stream()
                        .collect(Collectors.toMap(Publicity::getId, Publicity::getTitle));

                commentPage.getRecords().forEach(c -> {
                    if (c.getPublicityId() != null) {
                        c.setPublicityTitle(publicityTitleMap.get(c.getPublicityId()));
                    }
                });
            }

            if (!activityIds.isEmpty()) {
                List<Activity> activities = activityMapper.selectBatchIds(activityIds);
                Map<Integer, String> activityTitleMap = activities.stream()
                        .collect(Collectors.toMap(Activity::getId, Activity::getTitle));

                commentPage.getRecords().forEach(c -> {
                    if (c.getActivityId() != null) {
                        c.setActivityTitle(activityTitleMap.get(c.getActivityId()));
                    }
                });
            }
        }

        return commentPage;
    }
}