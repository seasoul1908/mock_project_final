package com.example.demo.service;

import com.example.demo.entity.Answer;
import com.example.demo.entity.Question;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
public class AnswerServiceImpl implements AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Override
    @Transactional
    public Answer postAnswer(long questionId, long userId, String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer body cannot be empty");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (question.isIsClosed()) {
            throw new IllegalStateException("This question is closed");
        }

        Answer answer = new Answer();
        answer.setQuestionId(questionId);
        answer.setUserId(userId);
        answer.setBody(body.trim());
        answer.setScore(0);
        answer.setIsEdited(false);
        answer.setIsAccepted(false);
        answer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        answer.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        Answer saved = answerRepository.save(answer);
        questionRepository.touchUpdatedAt(questionId);
        return saved;
    }

    @Override
    @Transactional
    public void toggleAcceptAnswer(long questionId, long answerId, long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (question.getUserId() != userId) {
            throw new IllegalStateException("Only the question owner can accept an answer");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));

        if (answer.getQuestionId() != questionId) {
            throw new IllegalArgumentException("Answer does not belong to this question");
        }

        Long currentAccepted = question.getAcceptedAnswerId();
        if (currentAccepted != null && currentAccepted.equals(answerId)) {
            question.setAcceptedAnswerId(null);
            answerRepository.clearAcceptedForQuestion(questionId);
        } else {
            answerRepository.clearAcceptedForQuestion(questionId);
            answerRepository.markAccepted(answerId);
            question.setAcceptedAnswerId(answerId);
        }

        questionRepository.save(question);
        questionRepository.touchUpdatedAt(questionId);
    }

    @Override
    @Transactional
    public void editAnswer(long answerId, long userId, String body, String codeSnippet, boolean isAdmin) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer body cannot be empty");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));

        if (answer.getUserId() != userId && !isAdmin) {
            throw new IllegalStateException("You can only edit your own answer");
        }

        answer.setBody(body.trim());
        if (codeSnippet != null && !codeSnippet.trim().isEmpty()) {
            answer.setCodeSnippet(codeSnippet.trim());
        }
        answer.setIsEdited(true);
        answer.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        answerRepository.save(answer);
    }

    @Override
    @Transactional
    public void deleteAnswer(long answerId, long userId, boolean isAdmin) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));

        if (answer.getUserId() != userId && !isAdmin) {
            throw new IllegalStateException("You can only delete your own answer");
        }

        // Cascade: remove comments and votes tied to this answer (FKs have NO ACTION)
        commentRepository.deleteByAnswerIdNative(answerId);
        voteRepository.deleteByAnswerIdNative(answerId);

        // If this was the accepted answer, clear the question's accepted_answer_id (FK is NO ACTION)
        Question question = questionRepository.findById(answer.getQuestionId()).orElse(null);
        if (question != null && answerId == (question.getAcceptedAnswerId() == null ? -1 : question.getAcceptedAnswerId())) {
            question.setAcceptedAnswerId(null);
            questionRepository.save(question);
        }

        answerRepository.delete(answer);
    }
}
