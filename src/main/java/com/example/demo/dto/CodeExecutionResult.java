package com.example.demo.dto;

public class CodeExecutionResult {
    private String status; // SUCCESS, COMPILATION_ERROR, RUNTIME_ERROR, TIMEOUT, UNSUPPORTED_LANGUAGE, ERROR
    private String output;
    private String error;
    private Integer exitCode;
    private Long executionTimeMs;

    public CodeExecutionResult() {
    }

    public CodeExecutionResult(String status, String output, String error, Integer exitCode, Long executionTimeMs) {
        this.status = status;
        this.output = output;
        this.error = error;
        this.exitCode = exitCode;
        this.executionTimeMs = executionTimeMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
}
