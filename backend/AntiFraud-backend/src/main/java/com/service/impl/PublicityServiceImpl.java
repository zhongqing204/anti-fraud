package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.*;
import com.mapper.PublicityMapper;
import com.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class PublicityServiceImpl extends ServiceImpl<PublicityMapper, Publicity> implements PublicityService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private LikesService likesService;

    @Autowired
    private CollectService collectService;

    @Autowired
    private CommentService commentService;

    @Override
    public void add(Publicity publicity) {
        publicity.setCreateTime(LocalDateTime.now());
        publicity.setUpdateTime(LocalDateTime.now());
        if (publicity.getCategoryId() != null){
            Category category = categoryService.getById(publicity.getCategoryId());
            if (category != null){
                publicity.setCategoryName(category.getName());
            }
        }
        save(publicity);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        for (Integer publicityId : ids) {
            likesService.remove(new LambdaQueryWrapper<Likes>().eq(Likes::getPublicityId, publicityId));
            collectService.remove(new LambdaQueryWrapper<Collect>().eq(Collect::getPublicityId, publicityId));
            commentService.remove(new LambdaQueryWrapper<Comment>().eq(Comment::getPublicityId, publicityId));
        }
        this.removeByIds(ids);
    }

    @Override
    public List<Publicity> selectAll(Publicity publicity) {
        LambdaQueryWrapper<Publicity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(publicity.getTitle())) {
            wrapper.like(Publicity::getTitle, publicity.getTitle());
        }
        if (publicity.getCategoryId() != null){
            wrapper.eq(Publicity::getCategoryId, publicity.getCategoryId());
        }
        wrapper.orderByDesc(Publicity::getCreateTime);
        List<Publicity> list = list(wrapper);

        for (Publicity p : list) {
            if (p.getCategoryId() != null){
                Category category = categoryService.getById(p.getCategoryId());
                if (category != null){
                    p.setCategoryName(category.getName());
                }
            }
        }
        return list;
    }

    @Override
    public Page<Publicity> selectPage(Publicity publicity, Integer pageNum, Integer pageSize) {
        Page<Publicity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Publicity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(publicity.getTitle())) {
            wrapper.like(Publicity::getTitle, publicity.getTitle());
        }
        if (publicity.getCategoryId() != null){
            wrapper.eq(Publicity::getCategoryId, publicity.getCategoryId());
        }
        wrapper.orderByDesc(Publicity::getCreateTime);
        Page<Publicity> pageData = page(page, wrapper);
        for (Publicity p : pageData.getRecords()){
            if (p.getCategoryId() != null){
                Category category = categoryService.getById(p.getCategoryId());
                if (category != null){
                    p.setCategoryName(category.getName());
                }
            }
        }
        return pageData;
    }

    @Override
    public List<Publicity> selectTop4() {
        // 查询所有宣传内容
        List<Publicity> allPublicities = this.list();
        // 随机打乱列表
        Collections.shuffle(allPublicities);
        // 返回前4条，如果不足4条则返回全部
        return allPublicities.subList(0, Math.min(4, allPublicities.size()));
    }
}
