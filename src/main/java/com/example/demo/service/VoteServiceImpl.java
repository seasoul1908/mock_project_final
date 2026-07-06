package com.example.demo.service;

import com.example.demo.entity.Answer;
import com.example.demo.entity.Question;
import com.example.demo.entity.Vote;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;

@Service
public class VoteServiceImpl implements VoteService {

    private static final int QUESTION_UP_REP = 10;
    private static final int QUESTION_DOWN_REP = -2;
    private static final int ANSWER_UP_REP = 10;
    private static final int ANSWER_DOWN_REP = -2;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void voteQuestion(long questionId, long userId, String voteType) {
        validateVoteType(voteType);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (question.getUserId() == userId) {
            throw new IllegalStateException("You cannot vote on your own question");
        }

        Optional<Vote> existingOpt = voteRepository.findQuestionVote(userId, questionId);
        applyVote(existingOpt, userId, voteType, questionId, null,
                question.getUserId(), question.getScore(), true, question);
    }

    @Override
    @Transactional
    public void voteAnswer(long answerId, long userId, String voteType) {
        validateVoteType(voteType);

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));

        if (answer.getUserId() == userId) {
            throw new IllegalStateException("You cannot vote on your own answer");
        }

        Optional<Vote> existingOpt = voteRepository.findAnswerVote(userId, answerId);
        applyVote(existingOpt, userId, voteType, null, answerId,
                answer.getUserId(), answer.getScore(), false, answer);
    }

    private void applyVote(Optional<Vote> existingOpt, long userId, String voteType,
            Long questionId, Long answerId, long authorId, int currentScore, boolean isQuestion, Object entity) {

        int scoreDelta = 0;
        int repDelta = 0;

        if (existingOpt.isEmpty()) {
            insertVote(userId, voteType, questionId, answerId);
            scoreDelta = "up".equals(voteType) ? 1 : -1;
            repDelta = isQuestion
                    ? ("up".equals(voteType) ? QUESTION_UP_REP : QUESTION_DOWN_REP)
                    : ("up".equals(voteType) ? ANSWER_UP_REP : ANSWER_DOWN_REP);
        } else {
            Vote existing = existingOpt.get();
            if (existing.getVoteType().equals(voteType)) {
                voteRepository.delete(existing);
                scoreDelta = "up".equals(voteType) ? -1 : 1;
                repDelta = isQuestion
                        ? ("up".equals(voteType) ? -QUESTION_UP_REP : -QUESTION_DOWN_REP)
                        : ("up".equals(voteType) ? -ANSWER_UP_REP : -ANSWER_DOWN_REP);
            } else {
                existing.setVoteType(voteType);
                voteRepository.save(existing);
                scoreDelta = "up".equals(voteType) ? 2 : -2;
                repDelta = isQuestion
                        ? ("up".equals(voteType) ? QUESTION_UP_REP - QUESTION_DOWN_REP : QUESTION_DOWN_REP - QUESTION_UP_REP)
                        : ("up".equals(voteType) ? ANSWER_UP_REP - ANSWER_DOWN_REP : ANSWER_DOWN_REP - ANSWER_UP_REP);
            }
        }

        if (scoreDelta != 0) {
            if (isQuestion) {
                Question q = (Question) entity;
                q.setScore(currentScore + scoreDelta);
                questionRepository.save(q);
            } else {
                Answer a = (Answer) entity;
                a.setScore(currentScore + scoreDelta);
                answerRepository.save(a);
            }
        }

        if (repDelta != 0) {
            userRepository.addReputation(authorId, repDelta);
            String postType = isQuestion ? "question" : "answer";
            Long postId = isQuestion ? questionId : answerId;
            userRepository.insertReputationHistory(authorId, repDelta,
                    "Vote on " + postType, "vote", postType, postId, userId);
        }
    }

    private void insertVote(long userId, String voteType, Long questionId, Long answerId) {
        Vote vote = new Vote();
        vote.setUserId(userId);
        vote.setVoteType(voteType);
        vote.setQuestionId(questionId);
        vote.setAnswerId(answerId);
        vote.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        voteRepository.save(vote);
    }

    private void validateVoteType(String voteType) {
        if (!"up".equals(voteType) && !"down".equals(voteType)) {
            throw new IllegalArgumentException("Invalid vote type");
        }
    }
}
