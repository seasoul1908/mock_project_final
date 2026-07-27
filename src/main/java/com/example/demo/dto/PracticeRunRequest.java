package com.example.demo.dto;

public class PracticeRunRequest {
    private String language;
    private String code;

    public PracticeRunRequest() {
    }

    public PracticeRunRequest(String language, String code) {
        this.language = language;
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
