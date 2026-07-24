package com.example.demo.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class FeedbackEmailScheduler {
    private final MailService mailService;
    private final TaskScheduler taskScheduler;

    public FeedbackEmailScheduler(MailService mailService, TaskScheduler taskScheduler) {
        this.mailService = mailService;
        this.taskScheduler = taskScheduler;
    }
    public void scheduleThankYouEmail(String email, String username , String feedbackMessage) {

        taskScheduler.schedule(
                () -> mailService.sendThankYouEmail(email, username , email , feedbackMessage),
                Instant.now().plus(1, ChronoUnit.MINUTES)
        );

    }
}
