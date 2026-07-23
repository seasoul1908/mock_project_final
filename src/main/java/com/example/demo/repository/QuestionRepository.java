package com.example.demo.repository;

import com.example.demo.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
            "AND COALESCE(q.is_deleted, 0) = 0 AND COALESCE(q.is_draft, 0) = 0 " +
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
        WHERE COALESCE(q.is_deleted, 0) = 0 AND COALESCE(q.is_draft, 0) = 0
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
        WHERE COALESCE(q.is_deleted, 0) = 0 AND COALESCE(q.is_draft, 0) = 0
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
            "AND COALESCE(q.is_deleted, 0) = 0 AND COALESCE(q.is_draft, 0) = 0 " +
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

    @Query(value = "SELECT t.tag_name " +
            "FROM Tags t " +
            "JOIN Question_Tags qt ON t.tag_id = qt.tag_id " +
            "WHERE qt.question_id = :questionId AND t.IsActive = 1", nativeQuery = true)
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
        WHERE COALESCE(q.is_deleted, 0) = 0 AND COALESCE(q.is_draft, 0) = 0
          AND COALESCE(q.bounty_amount, 0) > 0
          AND q.bounty_expires_at IS NOT NULL
          AND q.bounty_expires_at > CURRENT_TIMESTAMP
        ORDER BY
          -- sort expiring
          CASE WHEN :sortBy = 'expiring' THEN q.bounty_expires_at END ASC,
          CASE WHEN :sortBy = 'expiring' THEN q.bounty_amount END DESC,
          CASE WHEN :sortBy = 'expiring' THEN q.created_at END DESC,
          -- sort newest
          CASE WHEN :sortBy = 'newest' THEN q.created_at END DESC,
          CASE WHEN :sortBy = 'newest' THEN q.bounty_amount END DESC,
          CASE WHEN :sortBy = 'newest' THEN q.bounty_expires_at END ASC,
          -- sort amount (default)
          CASE WHEN :sortBy = 'amount' OR :sortBy IS NULL OR :sortBy = '' THEN q.bounty_amount END DESC,
          CASE WHEN :sortBy = 'amount' OR :sortBy IS NULL OR :sortBy = '' THEN q.bounty_expires_at END ASC,
          CASE WHEN :sortBy = 'amount' OR :sortBy IS NULL OR :sortBy = '' THEN q.created_at END DESC,
          q.question_id DESC
    """, nativeQuery = true)
    List<Map<String, Object>> findActiveBountiesNative(@Param("sortBy") String sortBy);

    @Query(value = "SELECT tag_id FROM Tags WHERE LOWER(tag_name) = LOWER(:tagName) AND IsActive = 1", nativeQuery = true)
    Long findTagIdByName(@Param("tagName") String tagName);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO Question_Tags (question_id, tag_id) VALUES (:questionId, :tagId)", nativeQuery = true)
    void insertQuestionTag(@Param("questionId") Long questionId, @Param("tagId") Long tagId);

    @Query(value = "SELECT follower_id FROM UserFollow WHERE following_id = :authorId", nativeQuery = true)
    List<Long> findFollowersByAuthorId(@Param("authorId") Long authorId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Questions SET view_count = view_count + 1 WHERE question_id = :questionId", nativeQuery = true)
    void incrementViewCount(@Param("questionId") long questionId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Questions SET updated_at = CURRENT_TIMESTAMP WHERE question_id = :questionId", nativeQuery = true)
    void touchUpdatedAt(@Param("questionId") long questionId);

    @Query(value = "SELECT DISTINCT tf.user_id " +
            "FROM TagFollow tf " +
            "JOIN Question_Tags qt ON tf.tag_id = qt.tag_id " +
            "WHERE qt.question_id = :questionId AND tf.user_id <> :authorId", nativeQuery = true)
    List<Long> findFollowersByQuestionTags(@Param("questionId") Long questionId, @Param("authorId") Long authorId);

    @Query(value = "SELECT t.tag_name " +
            "FROM TagFollow tf " +
            "JOIN Tags t ON tf.tag_id = t.tag_id " +
            "JOIN Question_Tags qt ON t.tag_id = qt.tag_id " +
            "WHERE qt.question_id = :questionId AND tf.user_id = :userId", nativeQuery = true)
    List<String> findFollowedTagsForQuestion(@Param("questionId") Long questionId, @Param("userId") Long userId);

    // Trending questions: activity score over the last 7 days (score*3 + views + answers*2)
    @Query(value = "SELECT q.question_id AS questionId, q.title AS title, q.Score AS score, q.view_count AS viewCount, " +
            "(SELECT COUNT(*) FROM Answers a WHERE a.question_id = q.question_id) AS answerCount, " +
            "(COALESCE(q.Score, 0) * 3 + COALESCE(q.view_count, 0) + (SELECT COUNT(*) FROM Answers a WHERE a.question_id = q.question_id) * 2) AS trendingScore " +
            "FROM Questions q " +
            "WHERE COALESCE(q.is_deleted, 0) = 0 AND COALESCE(q.is_draft, 0) = 0 " +
            "AND q.created_at > DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) " +
            "ORDER BY trendingScore DESC, q.created_at DESC",
            nativeQuery = true)
    List<Object[]> findTrendingNative(Pageable pageable);

    // Popular questions: all-time activity score (score*3 + views + answers*2 + comments)
    @Query(value = "SELECT q.question_id AS questionId, q.title AS title, q.Score AS score, q.view_count AS viewCount, " +
            "(SELECT COUNT(*) FROM Answers a WHERE a.question_id = q.question_id) AS answerCount, " +
            "(COALESCE(q.Score, 0) * 3 + COALESCE(q.view_count, 0) + (SELECT COUNT(*) FROM Answers a WHERE a.question_id = q.question_id) * 2 + (SELECT COUNT(*) FROM Comments c WHERE c.question_id = q.question_id) * 1) AS popularScore " +
            "FROM Questions q " +
            "WHERE COALESCE(q.is_deleted, 0) = 0 " +
            "ORDER BY popularScore DESC, q.created_at DESC",
            nativeQuery = true)
    List<Object[]> findPopularNative(Pageable pageable);

    // Soft-delete a question (the is_deleted column already exists in the schema)
    @Modifying
    @Transactional
    @Query(value = "UPDATE Questions SET is_deleted = 1, deleted_at = CURRENT_TIMESTAMP, deleted_by = :deletedBy WHERE question_id = :questionId", nativeQuery = true)
    void softDeleteQuestion(@Param("questionId") long questionId, @Param("deletedBy") long deletedBy);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Question_Tags WHERE question_id = :questionId", nativeQuery = true)
    void deleteQuestionTagsByQuestionId(@Param("questionId") Long questionId);

    List<Question> findByUserIdAndIsDraftTrueAndIsDeletedFalseOrderByCreatedAtDesc(long userId);
}