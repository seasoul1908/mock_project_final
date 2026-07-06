package com.example.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Repository
public interface BookmarkRepository extends JpaRepository<com.example.demo.entity.User, Long> {

    // Lấy list bài đã lưu (All saves)
    @Query(value = "SELECT b.question_id, q.title as questionTitle, b.created_at, b.collection_id " +
            "FROM Bookmarks b JOIN Questions q ON b.question_id = q.question_id " +
            "WHERE b.user_id = :userId ORDER BY b.created_at DESC", countQuery = "SELECT COUNT(*) FROM Bookmarks WHERE user_id = :userId", nativeQuery = true)
    Page<Map<String, Object>> getAllBookmarksRaw(@Param("userId") long userId, Pageable pageable);

    // Lấy list bài đã lưu trong 1 Collection cụ thể
    @Query(value = "SELECT b.question_id, q.title as questionTitle, b.created_at, b.collection_id " +
            "FROM Bookmarks b JOIN Questions q ON b.question_id = q.question_id " +
            "WHERE b.user_id = :userId AND b.collection_id = :collectionId ORDER BY b.created_at DESC", countQuery = "SELECT COUNT(*) FROM Bookmarks WHERE user_id = :userId AND collection_id = :collectionId", nativeQuery = true)
    Page<Map<String, Object>> getBookmarksByCollectionRaw(@Param("userId") long userId,
            @Param("collectionId") int collectionId, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Bookmarks SET collection_id = :collectionId WHERE user_id = :userId AND question_id = :questionId", nativeQuery = true)
    void moveBookmarkToCollection(@Param("userId") long userId, @Param("questionId") long questionId,
            @Param("collectionId") Integer collectionId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Bookmarks SET collection_id = NULL WHERE user_id = :userId AND question_id = :questionId", nativeQuery = true)
    void removeBookmarkFromCollection(@Param("userId") long userId, @Param("questionId") long questionId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Bookmarks WHERE user_id = :userId AND question_id = :questionId", nativeQuery = true)
    void deleteBookmarkPermanent(@Param("userId") long userId, @Param("questionId") long questionId);

    @Query(value = "SELECT COUNT(*) FROM Bookmarks WHERE user_id = :userId AND question_id = :questionId", nativeQuery = true)
    int countBookmark(@Param("userId") long userId, @Param("questionId") long questionId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO Bookmarks (user_id, question_id, created_at) VALUES (:userId, :questionId, GETDATE())", nativeQuery = true)
    void insertBookmark(@Param("userId") long userId, @Param("questionId") long questionId);
}