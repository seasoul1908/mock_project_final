package com.example.demo.service;

import com.example.demo.entity.Report;
import com.example.demo.entity.User;
import com.example.demo.repository.ReportRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Report reportContent(long reporterId, String targetType, long targetId, String reason, String note) {
        validateReporter(reporterId);
        validateReason(reason);
        validateTargetType(targetType);

        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason.trim());
        report.setNote(note);
        report.setStatus("open");
        report.setCreatedAt(new Date());
        return reportRepository.save(report);
    }

    @Override
    @Transactional
    public Report suggestDeletion(long reporterId, String targetType, long targetId, String reason) {
        validateReporter(reporterId);
        validateReason(reason);
        validateTargetType(targetType);

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
