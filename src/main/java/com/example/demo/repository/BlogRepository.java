package com.example.demo.repository;

import com.example.demo.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Integer> {

    @Query("SELECT b FROM Blog b WHERE b.status = 1 AND (:search IS NULL OR :search = '' OR b.title LIKE %:search%)")
    Page<Blog> searchBlogs(@Param("search") String search, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Blog b SET b.viewCount = b.viewCount + 1 WHERE b.blogId = :blogId")
    void increaseViewCount(@Param("blogId") Integer blogId);

    @Transactional
    @Modifying
    @Query("UPDATE Blog b SET b.commentCount = b.commentCount + 1 WHERE b.blogId = :blogId")
    void increaseCommentCount(@Param("blogId") Integer blogId);

    @Transactional
    @Modifying
    @Query("UPDATE Blog b SET b.commentCount = (SELECT COUNT(c) FROM BlogComment c WHERE c.blogId = :blogId) WHERE b.blogId = :blogId")
    void syncCommentCount(@Param("blogId") Integer blogId);
}
