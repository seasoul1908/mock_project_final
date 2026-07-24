package com.example.demo.runner;

import com.example.demo.dto.CodeExecutionRequest;
import com.example.demo.dto.CodeExecutionResult;
import com.example.demo.service.CodeRunnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class CodeRunnerServiceTest {

    private CodeRunnerService codeRunnerService;

    @BeforeEach
    void setUp() {
        JavaLanguageRunner javaRunner = new JavaLanguageRunner();
        codeRunnerService = new CodeRunnerService(Collections.singletonList(javaRunner));
    }

    @Test
    void testSuccessfulJavaExecution() {
        String code = "public class Main {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        System.out.println(\"Hello DevQuery!\");\n" +
                      "    }\n" +
                      "}";
        CodeExecutionRequest request = new CodeExecutionRequest("java", code);
        CodeExecutionResult result = codeRunnerService.runCode(request);

        assertEquals("SUCCESS", result.getStatus());
        assertTrue(result.getOutput().contains("Hello DevQuery!"));
        assertEquals(0, result.getExitCode());
        assertNotNull(result.getExecutionTimeMs());
    }

    @Test
    void testJavaCompilationError() {
        String code = "public class Main {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        System.out.println(\"Missing semicolon\")\n" +
                      "    }\n" +
                      "}";
        CodeExecutionRequest request = new CodeExecutionRequest("java", code);
        CodeExecutionResult result = codeRunnerService.runCode(request);

        assertEquals("COMPILATION_ERROR", result.getStatus());
        assertTrue(result.getError().contains(";") || result.getError().contains("error"));
    }

    @Test
    void testJavaRuntimeException() {
        String code = "public class Main {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        int val = 10 / 0;\n" +
                      "    }\n" +
                      "}";
        CodeExecutionRequest request = new CodeExecutionRequest("java", code);
        CodeExecutionResult result = codeRunnerService.runCode(request);

        assertEquals("RUNTIME_ERROR", result.getStatus());
        assertTrue(result.getError().contains("ArithmeticException") || result.getError().contains("by zero"));
    }

    @Test
    void testAutoWrapSimpleJavaSnippet() {
        String code = "System.out.println(\"Auto wrapped snippet test\");";
        CodeExecutionRequest request = new CodeExecutionRequest("java", code);
        CodeExecutionResult result = codeRunnerService.runCode(request);

        assertEquals("SUCCESS", result.getStatus());
        assertTrue(result.getOutput().contains("Auto wrapped snippet test"));
    }

    @Test
    void testUnsupportedLanguage() {
        CodeExecutionRequest request = new CodeExecutionRequest("python", "print('hello')");
        CodeExecutionResult result = codeRunnerService.runCode(request);

        assertEquals("UNSUPPORTED_LANGUAGE", result.getStatus());
    }
}
