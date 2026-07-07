package com.example.demo.repository;

import com.example.demo.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    // Sort: Votes (highest score first, then oldest)
    List<Answer> findByQuestionIdOrderByScoreDescCreatedAtAsc(long questionId);

    // Sort: Newest (most recently created first)
    List<Answer> findByQuestionIdOrderByCreatedAtDesc(long questionId);

    // Sort: Oldest (earliest created first)
    List<Answer> findByQuestionIdOrderByCreatedAtAsc(long questionId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Answers SET is_accepted = 0 WHERE question_id = :questionId", nativeQuery = true)
    void clearAcceptedForQuestion(@Param("questionId") long questionId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Answers SET is_accepted = 1 WHERE answer_id = :answerId", nativeQuery = true)
    void markAccepted(@Param("answerId") long answerId);
}
