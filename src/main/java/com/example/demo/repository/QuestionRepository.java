package com.example.demo.repository;

import com.example.demo.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = "SELECT q.question_id, q.title, q.body, q.Score, q.view_count, q.created_at, q.updated_at, q.is_closed, " +
            "u.username, up.avatar_url, " +
            "(SELECT COUNT(*) FROM Answers a WHERE a.question_id = q.question_id) as answer_count " +
            "FROM Questions q " +
            "JOIN Question_Tags qt ON q.question_id = qt.question_id " +
            "JOIN Users u ON q.user_id = u.user_id " +
            "LEFT JOIN User_Profile up ON u.user_id = up.user_id " +
            "WHERE qt.tag_id = :tagId " +
            "AND (:filter <> 'unanswered' OR NOT EXISTS (SELECT 1 FROM Answers a2 WHERE a2.question_id = q.question_id)) " +
            "ORDER BY " +
            "CASE WHEN :filter = 'voted' THEN q.Score END DESC, " +
            "CASE WHEN :filter = 'newest' OR :filter IS NULL OR :filter = 'unanswered' THEN q.created_at END DESC " +
            "OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY", nativeQuery = true)
    List<Object[]> findQuestionsByTagNative(
            @Param("tagId") Long tagId,
            @Param("filter") String filter,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    @Query(value = "SELECT COUNT(DISTINCT q.question_id) " +
            "FROM Questions q " +
            "JOIN Question_Tags qt ON q.question_id = qt.question_id " +
            "WHERE qt.tag_id = :tagId " +
            "AND (:filter <> 'unanswered' OR NOT EXISTS (SELECT 1 FROM Answers a WHERE a.question_id = q.question_id))", nativeQuery = true)
    int countQuestionsByTagNative(
            @Param("tagId") Long tagId,
            @Param("filter") String filter);

    @Query(value = "SELECT t.tag_name " +
            "FROM Tags t " +
            "JOIN Question_Tags qt ON t.tag_id = qt.tag_id " +
            "WHERE qt.question_id = :questionId AND t.IsActive = 1", nativeQuery = true)
    List<String> findTagNamesByQuestionIdNative(@Param("questionId") Long questionId);
}