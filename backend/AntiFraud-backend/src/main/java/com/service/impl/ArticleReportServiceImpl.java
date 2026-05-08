package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Article;
import com.entity.ArticleReport;
import com.entity.Message;
import com.entity.User;
import com.mapper.ArticleMapper;
import com.mapper.ArticleReportMapper;
import com.mapper.UserMapper;
import com.service.ArticleReportService;
import com.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleReportServiceImpl extends ServiceImpl<ArticleReportMapper, ArticleReport> implements ArticleReportService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private MessageService messageService;

    @Override
    public void add(ArticleReport articleReport) {
        if (articleReport.getUserId() != null) {
            User user = userMapper.selectById(articleReport.getUserId());
            if (user != null) {
                articleReport.setUserName(user.getName());
            }
        }

        if (articleReport.getArticleId() != null) {
            Article article = articleMapper.selectById(articleReport.getArticleId());
            if (article != null) {
                articleReport.setArticleTitle(article.getTitle());
            }
        }

        articleReport.setTime(LocalDateTime.now());

        if (!StringUtils.hasText(articleReport.getStatus())) {
            articleReport.setStatus("待处理");
        }

        this.save(articleReport);

        // 【新增】发送消息通知给举报人
        if (articleReport.getUserId() != null) {
            Message message = new Message();
            message.setUserId(articleReport.getUserId());
            message.setFromUserId(0);
            message.setFromUserName("系统");
            message.setArticleId(articleReport.getArticleId());
            message.setArticleTitle(articleReport.getArticleTitle());
            message.setType("article_report");
            
            String contentPreview = articleReport.getArticleTitle();
            if (contentPreview != null && contentPreview.length() > 30) {
                contentPreview = contentPreview.substring(0, 30) + "...";
            }
            
            message.setContent("您举报的帖子《" + contentPreview + "》已提交，等待处理");
            message.setIsRead(0);
            message.setCreatedTime(LocalDateTime.now());
            messageService.add(message);
        }
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<ArticleReport> selectAll(ArticleReport articleReport) {
        LambdaQueryWrapper<ArticleReport> queryWrapper = buildQueryWrapper(articleReport);
        return this.list(queryWrapper);
    }

    @Override
    public Page<ArticleReport> selectPage(ArticleReport articleReport, Integer pageNum, Integer pageSize) {
        Page<ArticleReport> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ArticleReport> queryWrapper = buildQueryWrapper(articleReport);
        return this.page(page, queryWrapper);
    }

    @Override
    public void batchUpdateStatus(List<Integer> ids, String status, String reason) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<ArticleReport> reports = this.listByIds(ids);
        for (ArticleReport report : reports) {
            String oldStatus = report.getStatus();
            report.setStatus(status);
            if (StringUtils.hasText(reason)) {
                report.setReason(reason);
            }

            // 【修复】只要状态发生变化且不是"待处理"就发送通知
            if (report.getUserId() != null && !"待处理".equals(status) && !status.equals(oldStatus)) {
                Message message = new Message();
                message.setUserId(report.getUserId());
                message.setFromUserId(0);
                message.setFromUserName("系统");
                message.setArticleId(report.getArticleId());
                message.setArticleTitle(report.getArticleTitle());
                message.setType("article_report");

                String contentPreview = report.getArticleTitle();
                if (contentPreview != null && contentPreview.length() > 30) {
                    contentPreview = contentPreview.substring(0, 30) + "...";
                }

                String contentText = "";
                if ("处理中".equals(status)) {
                    contentText = "您对帖子《" + contentPreview + "》的举报正在处理中，请耐心等待";
                } else if ("已处理".equals(status)) {
                    contentText = "您对帖子《" + contentPreview + "》的举报已处理完成";
                    if (StringUtils.hasText(reason)) {
                        contentText += "。处理说明：" + reason;
                    }
                } else {
                    contentText = "您对帖子《" + contentPreview + "》的举报状态已更新为：" + status;
                    if (StringUtils.hasText(reason)) {
                        contentText += "。处理说明：" + reason;
                    }
                }

                message.setContent(contentText);
                message.setIsRead(0);
                message.setCreatedTime(LocalDateTime.now());
                messageService.add(message);
            }
        }
        this.updateBatchById(reports);
    }

    private LambdaQueryWrapper<ArticleReport> buildQueryWrapper(ArticleReport articleReport) {
        LambdaQueryWrapper<ArticleReport> queryWrapper = new LambdaQueryWrapper<>();
        if (articleReport != null) {
            if (articleReport.getUserId() != null) {
                queryWrapper.eq(ArticleReport::getUserId, articleReport.getUserId());
            }
            if (StringUtils.hasText(articleReport.getArticleTitle())) {
                queryWrapper.like(ArticleReport::getArticleTitle, articleReport.getArticleTitle());
            }
            if (StringUtils.hasText(articleReport.getUserName())) {
                queryWrapper.like(ArticleReport::getUserName, articleReport.getUserName());
            }
            if (StringUtils.hasText(articleReport.getReportType())) {
                queryWrapper.eq(ArticleReport::getReportType, articleReport.getReportType());
            }
            if (StringUtils.hasText(articleReport.getStatus())) {
                queryWrapper.eq(ArticleReport::getStatus, articleReport.getStatus());
            }
        }
        queryWrapper.orderByDesc(ArticleReport::getTime);
        return queryWrapper;
    }
}
