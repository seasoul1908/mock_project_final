package com.example.demo.service;

import com.example.demo.dto.CodeExecutionRequest;
import com.example.demo.dto.CodeExecutionResult;
import com.example.demo.runner.LanguageRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CodeRunnerService {

    private final Map<String, LanguageRunner> runnerMap = new HashMap<>();

    @Autowired
    public CodeRunnerService(List<LanguageRunner> runners) {
        if (runners != null) {
            for (LanguageRunner runner : runners) {
                if (runner.getSupportedLanguage() != null) {
                    runnerMap.put(runner.getSupportedLanguage().toLowerCase(), runner);
                }
            }
        }
    }

    public CodeExecutionResult runCode(CodeExecutionRequest request) {
        if (request == null || request.getLanguage() == null) {
            return new CodeExecutionResult("ERROR", "", "Language must be specified.", -1, 0L);
        }

        String langKey = request.getLanguage().trim().toLowerCase();
        LanguageRunner runner = runnerMap.get(langKey);

        if (runner == null) {
            return new CodeExecutionResult("UNSUPPORTED_LANGUAGE", "",
                    "Language '" + request.getLanguage() + "' is not supported. Supported languages: " + runnerMap.keySet(),
                    -1, 0L);
        }

        return runner.execute(request);
    }
}
