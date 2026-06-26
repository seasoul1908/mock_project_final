package com.example.demo.repository;

import com.example.demo.entity.BlogComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, Integer> {

    List<BlogComment> findByBlogIdOrderByCreatedAtAsc(Integer blogId);

    List<BlogComment> findByParentId(Integer parentId);

    @Query("SELECT DISTINCT u.username FROM BlogComment c JOIN User u ON c.userId = u.userId WHERE c.blogId = :blogId")
    List<String> findCommentersByBlogId(@Param("blogId") Integer blogId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM BlogComments WHERE parent_id = :parentId", nativeQuery = true)
    void deleteByParentId(@Param("parentId") Integer parentId);
}
