package com.example.demo.repository;

import com.example.demo.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    @Query(value = "SELECT t.tag_id as id, t.tag_name as tagName, t.description as description, t.IsActive as isActive, " +
            "(SELECT COUNT(*) FROM Question_Tags qt WHERE qt.tag_id = t.tag_id) as questionCount, " +
            "0 as followerCount " +
            "FROM Tags t " +
            "WHERE t.IsActive = 1 " +
            "AND (:keyword IS NULL OR LOWER(t.tag_name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY " +
            "CASE WHEN :sort = 'popular' THEN (SELECT COUNT(*) FROM Question_Tags qt2 WHERE qt2.tag_id = t.tag_id) END DESC, " +
            "CASE WHEN :sort = 'newest' THEN t.tag_id END DESC, " +
            "CASE WHEN :sort = 'name' OR :sort IS NULL THEN t.tag_name END ASC", nativeQuery = true)
    List<Object[]> searchAndSortTagsNative(@Param("keyword") String keyword, @Param("sort") String sort);

    @Query(value = "SELECT t.tag_id as id, t.tag_name as tagName, t.description as description, t.IsActive as isActive, " +
            "(SELECT COUNT(*) FROM Question_Tags qt WHERE qt.tag_id = t.tag_id) as questionCount, " +
            "0 as followerCount " +
            "FROM Tags t " +
            "WHERE t.tag_id = :id AND t.IsActive = 1", nativeQuery = true)
    List<Object[]> findTagByIdNative(@Param("id") Long id);
}
