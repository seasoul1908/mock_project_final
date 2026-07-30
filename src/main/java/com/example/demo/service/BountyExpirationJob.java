package com.example.demo.service;

import com.example.demo.entity.Answer;
import com.example.demo.entity.Question;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class BountyExpirationJob {

    private static final Logger logger = LoggerFactory.getLogger(BountyExpirationJob.class);

    private static final int MIN_SCORE_FOR_AUTO_AWARD = 2;
    private static final double AUTO_AWARD_RATIO = 0.5;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void processExpiredBounties() {
        List<Question> expiredQuestions = questionRepository.findExpiredActiveBounties();

        if (expiredQuestions.isEmpty()) {
            return;
        }

        logger.info("Processing {} expired bounties", expiredQuestions.size());

        for (Question question : expiredQuestions) {
            try {
                processOne(question);
            } catch (Exception e) {
                logger.error("Failed to process expired bounty for questionId={}", question.getQuestionId(), e);
            }
        }
    }

    private void processOne(Question question) {
        long questionId = question.getQuestionId();
        int bountyAmount = question.getBountyAmount();

        Answer topAnswer = answerRepository.findTopScoringAnswer(questionId);

        if (topAnswer != null && topAnswer.getScore() >= MIN_SCORE_FOR_AUTO_AWARD) {
            int autoAwardAmount = (int) Math.round(bountyAmount * AUTO_AWARD_RATIO);
            long authorId = topAnswer.getUserId();

            userRepository.addReputation(authorId, autoAwardAmount);
            userRepository.insertReputationHistory(authorId, autoAwardAmount,
                    "Bounty expired - auto awarded to top answer", "bounty_auto_award",
                    "answer", topAnswer.getAnswerId(), null);

            logger.info("Auto-awarded {} reputation to userId={} for answerId={} (expired bounty on questionId={})",
                    autoAwardAmount, authorId, topAnswer.getAnswerId(), questionId);
        } else {
            logger.info("Bounty on questionId={} expired with no qualifying answer - amount forfeited", questionId);
        }

        question.setBountyAmount(0);
        question.setBountyAwarderId(null);
        question.setBountyStartedAt(null);
        question.setBountyExpiresAt(null);
        questionRepository.save(question);
    }
}