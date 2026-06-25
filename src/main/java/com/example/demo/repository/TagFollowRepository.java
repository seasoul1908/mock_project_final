package com.example.demo.repository;

import com.example.demo.entity.TagFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagFollowRepository extends JpaRepository<TagFollow, Long> {
    Optional<TagFollow> findByUserIdAndTagId(Long userId, Long tagId);
    boolean existsByUserIdAndTagId(Long userId, Long tagId);
}
