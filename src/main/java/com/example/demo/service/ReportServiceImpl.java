package com.example.demo.service;

import com.example.demo.entity.Report;
import com.example.demo.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Override
    @Transactional
    public Report reportContent(long reporterId, String targetType, long targetId, String reason, String note) {
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

    private void validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be empty");
        }
    }

    // DB CHECK constraint only allows 'question' and 'answer'
    private void validateTargetType(String targetType) {
        if (!"question".equals(targetType) && !"answer".equals(targetType)) {
            throw new IllegalArgumentException("Invalid target type");
        }
    }
}
