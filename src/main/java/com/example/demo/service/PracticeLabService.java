package com.example.demo.service;

import com.example.demo.dto.PracticeRunRequest;
import com.example.demo.dto.PracticeRunResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class PracticeLabService {

    private static final Logger logger = LoggerFactory.getLogger(PracticeLabService.class);

    @Value("${practice-lab.piston.api-url:https://emkc.org/api/v2/piston/execute}")
    private String apiUrl;

    @Value("${practice-lab.timeout:10000}")
    private int timeout;

    @Value("${practice-lab.max-code-size:102400}")
    private int maxCodeSize;

    private RestTemplate restTemplate;

    private static final Map<String, String> SUPPORTED_LANGUAGES = new HashMap<>();

    static {
        SUPPORTED_LANGUAGES.put("java", "15.0.2");
        SUPPORTED_LANGUAGES.put("python", "3.12.0");
        SUPPORTED_LANGUAGES.put("javascript", "20.11.1");
        SUPPORTED_LANGUAGES.put("c", "10.2.0");
        SUPPORTED_LANGUAGES.put("c++", "10.2.0");
        SUPPORTED_LANGUAGES.put("c#", "6.12.0");
        SUPPORTED_LANGUAGES.put("csharp", "6.12.0");
        SUPPORTED_LANGUAGES.put("go", "1.16.2");
        SUPPORTED_LANGUAGES.put("rust", "1.68.2");
        SUPPORTED_LANGUAGES.put("kotlin", "1.8.20");
    }

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.restTemplate = new RestTemplate(factory);
    }

    public PracticeRunResponse executeCode(PracticeRunRequest request) {
        long startTime = System.currentTimeMillis();

        // Validate basic request fields
        if (request == null || request.getCode() == null || request.getLanguage() == null) {
            return logAndReturnError(request != null ? request.getLanguage() : "unknown", 
                    "Invalid request: Code and Language must not be null.", startTime);
        }

        String rawLanguage = request.getLanguage().trim().toLowerCase();
        String code = request.getCode().trim();

        // Validate empty code
        if (code.isEmpty()) {
            return logAndReturnError(rawLanguage, "Code cannot be empty.", startTime);
        }

        // Validate max code size
        if (code.getBytes().length > maxCodeSize) {
            return logAndReturnError(rawLanguage, "Code size exceeds maximum limit of " + maxCodeSize + " bytes.", startTime);
        }

        // Map and validate language
        String pistonLang = mapLanguage(rawLanguage);
        if (!SUPPORTED_LANGUAGES.containsKey(pistonLang)) {
            return logAndReturnError(rawLanguage, "Unsupported language: " + rawLanguage, startTime);
        }

        String version = SUPPORTED_LANGUAGES.get(pistonLang);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("language", pistonLang);
        requestMap.put("version", version);

        Map<String, String> fileMap = new HashMap<>();
        fileMap.put("content", code);
        requestMap.put("files", Arrays.asList(fileMap));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestMap, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (response == null) {
                return logAndReturnError(rawLanguage, "Invalid response from execution engine.", startTime);
            }

            return parsePistonResponse(rawLanguage, response, executionTime);

        } catch (RestClientException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            String errorMsg = "Execution engine error: " + e.getMessage();
            if (e.getCause() != null && e.getCause() instanceof java.net.SocketTimeoutException) {
                errorMsg = "Execution Timeout";
            }
            logger.error("Language: {}, Execution time: {}ms, Exception: {}", rawLanguage, executionTime, errorMsg);
            return new PracticeRunResponse(false, "", errorMsg, executionTime);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Language: {}, Execution time: {}ms, Exception: {}", rawLanguage, executionTime, e.getMessage(), e);
            return new PracticeRunResponse(false, "", "Unknown Error: " + e.getMessage(), executionTime);
        }
    }

    private PracticeRunResponse parsePistonResponse(String language, Map<String, Object> response, long executionTime) {
        // Parse Compile Phase
        if (response.containsKey("compile")) {
            Map<String, Object> compile = (Map<String, Object>) response.get("compile");
            if (compile != null && compile.containsKey("code")) {
                int compileCode = ((Number) compile.get("code")).intValue();
                if (compileCode != 0) {
                    String compileStderr = getSafeString(compile, "stderr");
                    String compileOutput = getSafeString(compile, "output");
                    String errorMsg = !compileStderr.isEmpty() ? compileStderr : compileOutput;
                    
                    logger.info("Language: {}, Execution time: {}ms, Success: false (Compilation Error)", language, executionTime);
                    return new PracticeRunResponse(false, compileOutput, "Compilation Error:\n" + errorMsg, executionTime);
                }
            }
        }

        // Parse Run Phase
        if (response.containsKey("run")) {
            Map<String, Object> run = (Map<String, Object>) response.get("run");
            if (run != null && run.containsKey("code")) {
                int runCode = ((Number) run.get("code")).intValue();
                String stdout = getSafeString(run, "stdout");
                String stderr = getSafeString(run, "stderr");
                String output = getSafeString(run, "output");

                boolean success = (runCode == 0);
                String errorResult = "";

                if (!success) {
                    errorResult = "Runtime Error:\n" + (!stderr.isEmpty() ? stderr : output);
                }
                
                logger.info("Language: {}, Execution time: {}ms, Success: {}", language, executionTime, success);
                return new PracticeRunResponse(success, output, errorResult, executionTime);
            }
        }

        // Missing run phase and compile succeeded (or no compile block)
        logger.error("Language: {}, Execution time: {}ms, Exception: Missing run payload in response.", language, executionTime);
        return new PracticeRunResponse(false, "", "Execution failed: No run output from engine.", executionTime);
    }

    private String getSafeString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? String.valueOf(val) : "";
    }

    private PracticeRunResponse logAndReturnError(String language, String errorMessage, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;
        logger.warn("Language: {}, Execution time: {}ms, Validation Error: {}", language, executionTime, errorMessage);
        return new PracticeRunResponse(false, "", errorMessage, executionTime);
    }

    private String mapLanguage(String language) {
        if (language.equals("c++") || language.equals("cpp")) return "c++";
        if (language.equals("c#")) return "csharp";
        if (language.equals("nodejs") || language.equals("js")) return "javascript";
        return language;
    }
}
