package com.example.demo.dto;

import java.sql.Timestamp;

public interface QuestionDTO {

    Long getQuestionId();
    Long getUserId();
    String getTitle();
    String getBody();
    Integer getViewCount();
    Integer getScore();
    Timestamp getCreatedAt();
    Timestamp getUpdatedAt();

    String getAuthorName();
    String getAuthorAvatar();

    Integer getAnswerCount();
}
