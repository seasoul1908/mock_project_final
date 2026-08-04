package com.example.demo.service;
import java.util.Date;

import com.example.demo.entity.Answer;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Question;
import com.example.demo.entity.Report;
import com.example.demo.entity.User;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.ReportRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.NotificationType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportRepository reportRepository;
    @Autowired
private NotificationService notificationService;

@Autowired
private QuestionRepository questionRepository;

@Autowired
private AnswerRepository answerRepository;
@Autowired
private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Override
    @Transactional
    public Report reportContent(long reporterId, String targetType, long targetId, String reason, String note) {
        validateReporter(reporterId);
        validateReason(reason);
        validateTargetType(targetType);
        validateSelfReport(reporterId, targetType, targetId);

        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason.trim());
        report.setNote(note);
        report.setStatus("open");
        report.setCreatedAt(new Date());
        Report saved = reportRepository.save(report);
        if ("question".equals(targetType)) {

            Question question = questionRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

            notificationService.createNotification(
                question.getUserId(),
                reporterId,
                NotificationType.REPORT_QUESTION,
            "Someone has reported your question",
                question.getQuestionId(),
            "QUESTION"
    );

        } else if ("answer".equals(targetType)) {

    Answer answer = answerRepository.findById(targetId)
            .orElseThrow(() -> new IllegalArgumentException("Answer not found"));

    notificationService.createNotification(
            answer.getUserId(),
            reporterId,
            NotificationType.REPORT_ANSWER,
            "Someone has reported your answer",
            answer.getQuestionId(),
            "ANSWER"
    );
}else if ("comment".equals(targetType)) {

    Comment comment = commentRepository.findById(targetId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

    notificationService.createNotification(
            comment.getUserId(),          // Chủ comment
            reporterId,                   // Người report
            NotificationType.REPORT_COMMENT,
            "Someone has reported your comment",
            comment.getCommentId(),      // Redirect về Question
            "COMMENT"
    );
}
return saved;
    }

    @Override
    @Transactional
    public Report suggestDeletion(long reporterId, String targetType, long targetId, String reason) {
        validateReporter(reporterId);
        validateReason(reason);
        validateTargetType(targetType);
        validateSelfReport(reporterId, targetType, targetId);

        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason.trim());
        // Deletion suggestions are stored as Reports with a note marker (no extra column needed)
        report.setNote("DELETION_SUGGESTION");
        report.setStatus("open");
        report.setCreatedAt(new Date());
        return reportRepository.save(report);
    }

    private void validateSelfReport(long reporterId, String targetType, long targetId) {
        if ("comment".equalsIgnoreCase(targetType)) {
            Comment comment = commentRepository.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
            if (comment.getUserId() == reporterId) {
                throw new IllegalArgumentException("You cannot report your own comment");
            }
        } else if ("question".equalsIgnoreCase(targetType)) {
            Question question = questionRepository.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("Question not found"));
            if (question.getUserId() == reporterId) {
                throw new IllegalArgumentException("You cannot report your own question");
            }
        } else if ("answer".equalsIgnoreCase(targetType)) {
            Answer answer = answerRepository.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
            if (answer.getUserId() == reporterId) {
                throw new IllegalArgumentException("You cannot report your own answer");
            }
        }
    }

    private void validateReporter(long reporterId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int reputation = reporter.getReputation() != null ? reporter.getReputation() : 0;
        if (reputation < 15) {
            throw new IllegalArgumentException("You need at least 15 reputation to report content");
        }

        LocalDate today = LocalDate.now();
        Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        long countToday = reportRepository.countByReporterIdAndCreatedAtBetween(reporterId, startOfDay, endOfDay);
        if (countToday >= 5) {
            throw new IllegalArgumentException("You have reached the daily limit of 5 reports");
        }
    }

    private void validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be empty");
        }
    }

    private void validateTargetType(String targetType) {
        if (!"question".equals(targetType) && !"answer".equals(targetType) && !"comment".equals(targetType)) {
            throw new IllegalArgumentException("Invalid target type");
        }
    }
}
