package com.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {
    void add(Comment comment);

    void deleteBatch(List<Integer> ids);

    List<Comment> selectAll(Comment comment);

    Page<Comment> selectPage(Comment comment, Integer pageNum, Integer pageSize);

}
