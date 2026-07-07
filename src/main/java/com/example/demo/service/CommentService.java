package com.example.demo.service;

import com.example.demo.entity.Comment;

public interface CommentService {

    Comment addQuestionComment(long questionId, long userId, String body);

    Comment addAnswerComment(long answerId, long userId, String body);

    Comment addReply(long parentCommentId, long userId, String body);

    void deleteComment(long commentId, long userId, boolean isAdmin);
}
