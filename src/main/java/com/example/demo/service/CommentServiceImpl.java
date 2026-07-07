package com.example.demo.service;

import com.example.demo.entity.Answer;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Question;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Override
    @Transactional
    public Comment addQuestionComment(long questionId, long userId, String body) {
        validateBody(body);
        questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setQuestionId(questionId);
        comment.setBody(body.trim());
        comment.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Comment addAnswerComment(long answerId, long userId, String body) {
        validateBody(body);
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setAnswerId(answerId);
        comment.setQuestionId(answer.getQuestionId());
        comment.setBody(body.trim());
        comment.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Comment addReply(long parentCommentId, long userId, String body) {
        validateBody(body);
        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));

        Comment reply = new Comment();
        reply.setUserId(userId);
        reply.setParentCommentId(parentCommentId);
        // Inherit the target context from the parent so it belongs to the same question/answer
        reply.setQuestionId(parent.getQuestionId());
        reply.setAnswerId(parent.getAnswerId());
        reply.setBody(body.trim());
        reply.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return commentRepository.save(reply);
    }

    @Override
    @Transactional
    public void deleteComment(long commentId, long userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (comment.getUserId() != userId && !isAdmin) {
            throw new IllegalStateException("You can only delete your own comment");
        }

        // Detach any replies first (self-referencing FK has NO ACTION)
        commentRepository.nullifyParentReferences(commentId);
        commentRepository.delete(comment);
    }

    private void validateBody(String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }
    }
}
