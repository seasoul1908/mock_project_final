package com.example.demo.repository;

import com.example.demo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByQuestionIdAndAnswerIdIsNullAndParentCommentIdIsNullOrderByCreatedAtAsc(long questionId);

    List<Comment> findByAnswerIdAndParentCommentIdIsNullOrderByCreatedAtAsc(long answerId);

    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Comments SET parent_comment_id = NULL WHERE parent_comment_id = :commentId", nativeQuery = true)
    void nullifyParentReferences(@Param("commentId") long commentId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Comments WHERE answer_id = :answerId", nativeQuery = true)
    void deleteByAnswerIdNative(@Param("answerId") long answerId);
}
