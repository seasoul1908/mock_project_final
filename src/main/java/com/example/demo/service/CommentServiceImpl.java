package com.example.demo.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Answer;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Question;
import com.example.demo.entity.User;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.NotificationType;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private NotificationService notificationService;

     @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Comment addQuestionComment(long questionId, long userId, String body) {
        validateBody(body);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setQuestionId(questionId);
        comment.setBody(body.trim());
        comment.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        Comment saved = commentRepository.save(comment);
        if (question.getUserId() != userId) {
            String commenterUsername = userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("");
        notificationService.createNotification(
            question.getUserId(),          // người nhận
            userId,                      // người comment
            NotificationType.COMMENT_QUESTION,
            commenterUsername + " has commented on your question",
            saved.getCommentId(),
             "COMMENT"
            );
        }

        return saved;
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
        Comment saved = commentRepository.save(comment);
        if (answer.getUserId() != userId) {
            String commenterUsername = userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("");
        notificationService.createNotification(
            answer.getUserId(),          // người nhận
            userId,                      // người comment
            NotificationType.COMMENT_ANSWER,
            commenterUsername + "has commented on your answer",
            saved.getCommentId(),
             "COMMENT"
            );
        }

        return saved;
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
        Comment saved = commentRepository.save(reply);

        if (parent.getUserId() != userId) {

        String replierUsername = userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("");
        // Reuse the existing COMMENT_QUESTION/COMMENT_ANSWER types depending on which
        // thread the parent comment belongs to, so no new NotificationType/schema change is needed.
        NotificationType type = parent.getAnswerId() != null
                ? NotificationType.COMMENT_ANSWER
                : NotificationType.COMMENT_QUESTION;
        notificationService.createNotification(
            parent.getUserId(),          // người nhận (tác giả comment cha)
            userId,                      // người reply
            type,
            replierUsername + " has reply your comment.",
            saved.getCommentId(),
             "COMMENT"
            );
        }
        return saved;
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
