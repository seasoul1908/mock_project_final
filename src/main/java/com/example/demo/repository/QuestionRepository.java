package com.example.demo.repository;

import com.example.demo.entity.Question;
import com.example.demo.dto.QuestionSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = """
        SELECT q.question_id as questionId, q.title as title, q.body as body,
               q.view_count as viewCount, q.Score as score, q.created_at as createdAt, q.updated_at as updatedAt,
               u.username as authorName, up.avatar_url as authorAvatar,
               (SELECT COUNT(*) FROM Answers a WHERE a.question_id = q.question_id) as answerCount
        FROM Questions q
        JOIN Users u ON q.user_id = u.user_id
        LEFT JOIN User_Profile up ON u.user_id = up.user_id
        WHERE ISNULL(q.is_deleted, 0) = 0
          AND (:keyword = '' OR q.title LIKE :keywordSearch OR q.body LIKE :keywordSearch)
          AND (:filter != 'unanswered' OR (SELECT COUNT(*) FROM Answers a WHERE a.question_id = q.question_id) = 0)
          AND (:tag = '' OR q.question_id IN (SELECT qt.question_id FROM Question_Tags qt JOIN Tags t ON qt.tag_id = t.tag_id WHERE t.tag_name = :tag))
    """, countQuery = """
        SELECT COUNT(q.question_id)
        FROM Questions q
        WHERE ISNULL(q.is_deleted, 0) = 0
          AND (:keyword = '' OR q.title LIKE :keywordSearch OR q.body LIKE :keywordSearch)
          AND (:filter != 'unanswered' OR (SELECT COUNT(*) FROM Answers a WHERE a.question_id = q.question_id) = 0)
          AND (:tag = '' OR q.question_id IN (SELECT qt.question_id FROM Question_Tags qt JOIN Tags t ON qt.tag_id = t.tag_id WHERE t.tag_name = :tag))
    """, nativeQuery = true)
    Page<QuestionSummaryProjection> searchQuestions(
            @Param("keyword") String keyword,
            @Param("keywordSearch") String keywordSearch,
            @Param("filter") String filter,
            @Param("tag") String tag,
            Pageable pageable
    );

    @Query(value = """
        SELECT t.tag_name
        FROM Tags t
        JOIN Question_Tags qt ON t.tag_id = qt.tag_id
        GROUP BY t.tag_name
        ORDER BY COUNT(qt.question_id) DESC
        OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY
    """, nativeQuery = true)
    List<String> getPopularTags(@Param("limit") int limit);

    @Query(value = "SELECT t.tag_name FROM Tags t JOIN Question_Tags qt ON t.tag_id = qt.tag_id WHERE qt.question_id = :questionId", nativeQuery = true)
    List<String> findTagsByQuestionId(@Param("questionId") Long questionId);
}