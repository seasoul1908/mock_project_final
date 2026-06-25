package com.example.demo.repository;

import com.example.demo.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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
            "CASE WHEN :filter = 'newest' OR :filter IS NULL OR :filter = 'unanswered' THEN q.created_at END DESC", 
            nativeQuery = true)
    List<Object[]> findQuestionsByTagNative(
            @Param("tagId") Long tagId,
            @Param("filter") String filter,
            Pageable pageable);

    @Query(value = """
        SELECT q.question_id as questionId, q.user_id as userId, q.title as title, q.body as body,
               q.view_count as viewCount, q.Score as score, q.created_at as createdAt,
               u.username as authorName, up.avatar_url as authorAvatar,
               (SELECT COUNT(*) FROM Answers a WHERE a.question_id = q.question_id) as answerCount
        FROM Questions q
        JOIN Users u ON q.user_id = u.user_id
        LEFT JOIN User_Profile up ON u.user_id = up.user_id
        WHERE ISNULL(q.is_deleted, 0) = 0
          AND (:keyword = '' OR q.title LIKE :keywordSearch OR q.body LIKE :keywordSearch)
          AND (:filter != 'unanswered' OR (SELECT COUNT(*) FROM Answers a WHERE a.question_id = q.question_id) = 0)
          AND (:tag = '' OR q.question_id IN (SELECT qt.question_id FROM Question_Tags qt JOIN Tags t ON qt.tag_id = t.tag_id WHERE t.tag_name = :tag))
        ORDER BY
          CASE WHEN :sortBy = 'views' THEN q.view_count END DESC,
          CASE WHEN :sortBy = 'active' THEN q.updated_at END DESC,
          CASE WHEN :sortBy = 'voted' THEN q.Score END DESC,
          CASE WHEN :sortBy = 'newest' THEN q.created_at END DESC,
          q.question_id DESC
    """, countQuery = """
        SELECT COUNT(q.question_id)
        FROM Questions q
        WHERE ISNULL(q.is_deleted, 0) = 0
          AND (:keyword = '' OR q.title LIKE :keywordSearch OR q.body LIKE :keywordSearch)
          AND (:filter != 'unanswered' OR (SELECT COUNT(*) FROM Answers a WHERE a.question_id = q.question_id) = 0)
          AND (:tag = '' OR q.question_id IN (SELECT qt.question_id FROM Question_Tags qt JOIN Tags t ON qt.tag_id = t.tag_id WHERE t.tag_name = :tag))
    """, nativeQuery = true)
    Page<Map<String, Object>> searchQuestions(
            @Param("keyword") String keyword,
            @Param("keywordSearch") String keywordSearch,
            @Param("filter") String filter,
            @Param("tag") String tag,
            @Param("sortBy") String sortBy,
            Pageable pageable
    );

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

    @Query(value = "SELECT t.tag_name FROM Tags t JOIN Question_Tags qt ON t.tag_id = qt.tag_id GROUP BY t.tag_name ORDER BY COUNT(qt.question_id) DESC", nativeQuery = true)
    List<String> getPopularTags(Pageable pageable);

    // Fetch tags for a specific question
    @Query(value = "SELECT t.tag_name FROM Tags t JOIN Question_Tags qt ON t.tag_id = qt.tag_id WHERE qt.question_id = :questionId", nativeQuery = true)
    List<String> findTagsByQuestionId(@Param("questionId") Long questionId);

    @Query(value = """
        SELECT q.question_id as questionId, q.user_id as userId, q.title as title, q.body as body,
               q.view_count as viewCount, q.Score as score, q.created_at as createdAt,
               q.bounty_amount as bountyAmount, q.bounty_expires_at as bountyExpiresAt,
               u.username as authorName, up.avatar_url as authorAvatar,
               (SELECT COUNT(*) FROM Answers a WHERE a.question_id = q.question_id) as answerCount
        FROM Questions q
        JOIN Users u ON q.user_id = u.user_id
        LEFT JOIN User_Profile up ON u.user_id = up.user_id
        WHERE ISNULL(q.is_deleted, 0) = 0
          AND ISNULL(q.bounty_amount, 0) > 0
          AND q.bounty_expires_at IS NOT NULL
          AND q.bounty_expires_at > GETDATE()
        ORDER BY
          CASE WHEN :sortBy = 'expiring' THEN q.bounty_expires_at END ASC,
          CASE WHEN :sortBy = 'newest' THEN q.created_at END DESC,
          CASE WHEN :sortBy = 'amount' OR :sortBy IS NULL OR :sortBy = '' THEN q.bounty_amount END DESC,
          q.question_id DESC
    """, nativeQuery = true)
    List<Map<String, Object>> findActiveBountiesNative(@Param("sortBy") String sortBy);
}