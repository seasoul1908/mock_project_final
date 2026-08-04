package com.example.demo.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Answer;
import com.example.demo.entity.PostEditHistory;
import com.example.demo.entity.Question;
import com.example.demo.entity.User;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostEditHistoryRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VoteRepository;
import com.example.demo.util.NotificationType;

@Service
public class AnswerServiceImpl implements AnswerService {

    private static final int ACCEPTED_ANSWER_REP = 15;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostEditHistoryRepository postEditHistoryRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public Answer postAnswer(long questionId, long userId, String body, String codeSnippet) {
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
        if (codeSnippet != null && !codeSnippet.trim().isEmpty()) {
            answer.setCodeSnippet(codeSnippet.trim());
        }
        answer.setScore(0);
        answer.setIsEdited(false);
        answer.setIsAccepted(false);
        answer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        answer.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        Answer saved = answerRepository.save(answer);
        questionRepository.touchUpdatedAt(questionId);
        String answererUsername = userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("");
        notificationService.createNotification(
        question.getUserId(),              // Người nhận (chủ câu hỏi)
        userId,                            // Người gửi (người trả lời)
        NotificationType.NEW_ANSWER,
        answererUsername + " has answered your question",
        saved.getAnswerId(),
        "ANSWER"
        );
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
        long answerAuthorId = answer.getUserId();

        if (currentAccepted != null && currentAccepted.equals(answerId)) {
            // ---- UNACCEPT: clear + TRỪ điểm đã cộng trước đó ----
            question.setAcceptedAnswerId(null);
            answerRepository.clearAcceptedForQuestion(questionId);

            userRepository.addReputation(answerAuthorId, -ACCEPTED_ANSWER_REP);
            userRepository.insertReputationHistory(
                    answerAuthorId,
                    -ACCEPTED_ANSWER_REP,
                    "Answer unaccepted",
                    "answer_unaccepted",
                    "answer",
                    answerId,
                    userId
            );
            notificationService.createNotification(
            answerAuthorId,                      // người nhận
            userId,                              // chủ câu hỏi
            NotificationType.ANSWER_UNACCEPTED,
    "doesn't longer accepted your answer anymore",
            answerId,
    "ANSWER"
            );

        } else {
            // ---- ACCEPT (mới, hoặc chuyển từ answer khác sang) ----

            // Nếu đang có answer khác được accept, phải TRỪ điểm answer đó trước
            if (currentAccepted != null) {
                Answer previousAccepted = answerRepository.findById(currentAccepted)
                        .orElse(null);
                if (previousAccepted != null) {
                    long previousAuthorId = previousAccepted.getUserId();
                    userRepository.addReputation(previousAuthorId, -ACCEPTED_ANSWER_REP);
                    userRepository.insertReputationHistory(
                            previousAuthorId,
                            -ACCEPTED_ANSWER_REP,
                            "Answer unaccepted (replaced by another answer)",
                            "answer_unaccepted",
                            "answer",
                            currentAccepted,
                            userId
                    );
                    
                }
            }

            answerRepository.clearAcceptedForQuestion(questionId);
            answerRepository.markAccepted(answerId);
            question.setAcceptedAnswerId(answerId);

            userRepository.addReputation(answerAuthorId, ACCEPTED_ANSWER_REP);
            notificationService.createNotification(
            answerAuthorId,               // người nhận
            userId,                       // người gửi (chủ câu hỏi)
            NotificationType.ANSWER_ACCEPTED,
            " has accepted your answer",
            answerId,
            "ANSWER"
            );
            userRepository.insertReputationHistory(
                    answerAuthorId,
                    ACCEPTED_ANSWER_REP,
                    "Answer accepted",
                    "answer_accepted",
                    "answer",
                    answerId,
                    userId
            );
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
        // Thêm vào cuối method editAnswer(), sau khi answerRepository.save(answer)
        postEditHistoryRepository.save(new PostEditHistory(
                "answer",
                answerId,
                null,                    // answer không có title
                answer.getBody(),
                answer.getCodeSnippet(),
                null,                    // answer không có tags
                userId,
                new Timestamp(System.currentTimeMillis())
        ));
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
