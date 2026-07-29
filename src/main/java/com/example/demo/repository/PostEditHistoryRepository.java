package com.example.demo.repository;

import com.example.demo.entity.PostEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostEditHistoryRepository extends JpaRepository<PostEditHistory, Long> {
    List<PostEditHistory> findByPostTypeAndPostIdOrderByEditedAtDesc(String postType, long postId);
    List<PostEditHistory> findByPostTypeAndPostIdOrderByEditedAtAsc(String postType, long postId);
}
