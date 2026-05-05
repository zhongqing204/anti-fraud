package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.Video;

import java.util.List;

/**
 * 视频Service接口
 */
public interface VideoService extends IService<Video> {
    /**
     * 新增视频
     */
    void add(Video video);

    /**
     * 批量删除视频
     */
    void deleteBatch(List<Integer> ids);

    /**
     * 查询所有视频
     */
    List<Video> selectAll(Video video);

    /**
     * 分页查询视频
     */
    Page<Video> selectPage(Video video, Integer pageNum, Integer pageSize);

    /**
     * 查询热门视频（Top4）
     */
    List<Video> selectTop4();
}

