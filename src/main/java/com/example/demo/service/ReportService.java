package com.example.demo.service;

import com.example.demo.entity.Report;

public interface ReportService {

    Report reportContent(long reporterId, String targetType, long targetId, String reason, String note);

    Report suggestDeletion(long reporterId, String targetType, long targetId, String reason);
}
