package com.example.demo.controller;

import com.example.demo.dto.CodeExecutionRequest;
import com.example.demo.dto.CodeExecutionResult;
import com.example.demo.service.CodeRunnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
public class CodeRunnerRestController {

    private final CodeRunnerService codeRunnerService;

    @Autowired
    public CodeRunnerRestController(CodeRunnerService codeRunnerService) {
        this.codeRunnerService = codeRunnerService;
    }

    @PostMapping("/run")
    public ResponseEntity<CodeExecutionResult> runCode(@RequestBody CodeExecutionRequest request) {
        CodeExecutionResult result = codeRunnerService.runCode(request);
        return ResponseEntity.ok(result);
    }
}
