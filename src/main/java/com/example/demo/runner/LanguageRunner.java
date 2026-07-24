package com.example.demo.runner;

import com.example.demo.dto.CodeExecutionRequest;
import com.example.demo.dto.CodeExecutionResult;

public interface LanguageRunner {
    String getSupportedLanguage();
    CodeExecutionResult execute(CodeExecutionRequest request);
}
