package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Article;
import com.entity.Collect;
import com.mapper.ArticleMapper;
import com.mapper.CollectMapper;
import com.service.CollectService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {

    @Resource
    private ArticleMapper articleMapper;

    @Override
    public void add(Collect collect) {
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Collect::getUserId, collect.getUserId())
                .eq(Collect::getArticleId, collect.getArticleId());
        List<Collect> collects = this.list(queryWrapper);

        if (collects == null || collects.isEmpty()) {
            // 未收藏，执行收藏
            this.save(collect);
        } else {
            // 已收藏，取消收藏（删除记录）
            this.removeById(collects.get(0).getId());
        }
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<Collect> selectAll(Collect collect) {
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        if (collect != null) {
            if (collect.getUserId() != null) {
                queryWrapper.eq(Collect::getUserId, collect.getUserId());
            }
            if (collect.getArticleId() != null) {
                queryWrapper.eq(Collect::getArticleId, collect.getArticleId());
            }
        }
        queryWrapper.orderByDesc(Collect::getId);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Collect> selectPage(Collect collect, Integer pageNum, Integer pageSize) {
        Page<Collect> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();

        if (collect != null) {
            if (collect.getUserId() != null) {
                queryWrapper.eq(Collect::getUserId, collect.getUserId());
            }
            if (collect.getArticleId() != null) {
                queryWrapper.eq(Collect::getArticleId, collect.getArticleId());
            }
        }

        queryWrapper.orderByDesc(Collect::getId);

        // 执行分页查询
        Page<Collect> collectPage = this.page(page, queryWrapper);

        // 关联查询Article信息，避免N+1问题
        if (collectPage.getRecords() != null && !collectPage.getRecords().isEmpty()) {
            List<Integer> articleIds = collectPage.getRecords().stream()
                    .map(Collect::getArticleId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());

            if (!articleIds.isEmpty()) {
                List<Article> articles = articleMapper.selectBatchIds(articleIds);
                // 修改：存储完整Article对象而非仅标题，供前端显示文章完整信息
                Map<Integer, Article> articleMap = articles.stream()
                        .collect(java.util.stream.Collectors.toMap(Article::getId, a -> a));

                // 为每个收藏记录设置对应的文章对象
                collectPage.getRecords().forEach(c -> {
                    if (c.getArticleId() != null && articleMap.containsKey(c.getArticleId())) {
                        Article article = articleMap.get(c.getArticleId());
                        c.setArticleTitle(article.getTitle());
                        // 新增：设置完整文章对象，包含cover等字段
                        c.setArticle(article);
                    }
                });
            }
        }
        return collectPage;
    }
}
