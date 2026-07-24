package com.example.demo.dto;

public class CodeExecutionRequest {
    private String language;
    private String code;
    private String stdin;

    public CodeExecutionRequest() {
    }

    public CodeExecutionRequest(String language, String code) {
        this.language = language;
        this.code = code;
    }

    public CodeExecutionRequest(String language, String code, String stdin) {
        this.language = language;
        this.code = code;
        this.stdin = stdin;
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

    public String getStdin() {
        return stdin;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }
}
