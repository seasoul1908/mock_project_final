package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;

public class ChatbotResponse {
    private String reply;
    private List<String> suggestedQuestions = new ArrayList<>();

    public ChatbotResponse() {}

    public ChatbotResponse(String reply) {
        this.reply = reply;
    }

    public ChatbotResponse(String reply, List<String> suggestedQuestions) {
        this.reply = reply;
        this.suggestedQuestions = suggestedQuestions;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<String> getSuggestedQuestions() {
        return suggestedQuestions;
    }

    public void setSuggestedQuestions(List<String> suggestedQuestions) {
        this.suggestedQuestions = suggestedQuestions;
    }
}
