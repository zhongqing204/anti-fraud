package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.Collect;
import com.entity.Comment;
import com.entity.Likes;
import com.entity.Video;
import com.mapper.VideoMapper;
import com.service.CollectService;
import com.service.CommentService;
import com.service.LikesService;
import com.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 视频Service实现类
 */
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private LikesService likesService;

    @Resource
    private CollectService collectService;

    @Resource
    private CommentService commentService;

    @Override
    public void add(Video video) {
        videoMapper.insert(video);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        for (Integer videoId : ids) {
            likesService.remove(new LambdaQueryWrapper<Likes>().eq(Likes::getVideoId, videoId));
            collectService.remove(new LambdaQueryWrapper<Collect>().eq(Collect::getVideoId, videoId));
            commentService.remove(new LambdaQueryWrapper<Comment>().eq(Comment::getVideoId, videoId));
        }
        videoMapper.deleteBatchIds(ids);
    }

    @Override
    public List<Video> selectAll(Video video) {
        LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(video.getTitle())) {
            queryWrapper.like(Video::getTitle, video.getTitle());
        }
        if (video.getCategoryId() != null) {
            queryWrapper.eq(Video::getCategoryId, video.getCategoryId());
        }
        queryWrapper.orderByDesc(Video::getCreateTime);
        return videoMapper.selectList(queryWrapper);
    }

    @Override
    public Page<Video> selectPage(Video video, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(video.getTitle())) {
            queryWrapper.like(Video::getTitle, video.getTitle());
        }
        if (video.getCategoryId() != null) {
            queryWrapper.eq(Video::getCategoryId, video.getCategoryId());
        }
        queryWrapper.orderByDesc(Video::getCreateTime);
        return videoMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Override
    public List<Video> selectTop4() {
        LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Video::getViewCount);
        queryWrapper.last("LIMIT 4");
        return videoMapper.selectList(queryWrapper);
    }
}

