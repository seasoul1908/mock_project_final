package com.example.demo.repository;

import com.example.demo.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("SELECT v FROM Vote v WHERE v.userId = :userId AND v.questionId = :questionId AND v.answerId IS NULL")
    Optional<Vote> findQuestionVote(@Param("userId") long userId, @Param("questionId") long questionId);

    @Query("SELECT v FROM Vote v WHERE v.userId = :userId AND v.answerId = :answerId")
    Optional<Vote> findAnswerVote(@Param("userId") long userId, @Param("answerId") long answerId);

    // Cascade helper: remove votes tied to an answer before hard-deleting it (FK has NO ACTION)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Votes WHERE answer_id = :answerId", nativeQuery = true)
    void deleteByAnswerIdNative(@Param("answerId") long answerId);
}
