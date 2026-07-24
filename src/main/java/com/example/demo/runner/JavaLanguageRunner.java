package com.example.demo.runner;

import com.example.demo.dto.CodeExecutionRequest;
import com.example.demo.dto.CodeExecutionResult;
import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JavaLanguageRunner implements LanguageRunner {

    private static final long TIMEOUT_SECONDS = 5L;
    private static final Pattern PUBLIC_CLASS_PATTERN = Pattern.compile("public\\s+class\\s+([A-Za-z0-9_$]+)");
    private static final Pattern ANY_CLASS_PATTERN = Pattern.compile("class\\s+([A-Za-z0-9_$]+)");

    @Override
    public String getSupportedLanguage() {
        return "java";
    }

    @Override
    public CodeExecutionResult execute(CodeExecutionRequest request) {
        String code = request.getCode();
        if (code == null || code.trim().isEmpty()) {
            return new CodeExecutionResult("ERROR", "", "Code cannot be empty.", -1, 0L);
        }

        String preparedCode = prepareJavaCode(code.trim());
        String className = extractClassName(preparedCode);

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("devquery_java_runner_");
            File sourceFile = new File(tempDir.toFile(), className + ".java");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(sourceFile, StandardCharsets.UTF_8))) {
                writer.write(preparedCode);
            }

            long startTime = System.currentTimeMillis();

            // 1. Compile step
            ByteArrayOutputStream compileErrStream = new ByteArrayOutputStream();
            boolean compileSuccess = compileJavaFile(sourceFile, tempDir, compileErrStream);
            long compileTime = System.currentTimeMillis() - startTime;

            if (!compileSuccess) {
                String compileErrors = compileErrStream.toString(StandardCharsets.UTF_8);
                return new CodeExecutionResult("COMPILATION_ERROR", "", compileErrors, 1, compileTime);
            }

            // 2. Execute step
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", tempDir.toAbsolutePath().toString(), className);
            pb.directory(tempDir.toFile());

            long execStartTime = System.currentTimeMillis();
            Process process = pb.start();

            // Handle optional stdin input if provided
            if (request.getStdin() != null && !request.getStdin().isEmpty()) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(request.getStdin().getBytes(StandardCharsets.UTF_8));
                    os.flush();
                } catch (IOException ignored) {
                }
            } else {
                process.getOutputStream().close();
            }

            boolean finishedInTime = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            long totalExecutionTime = (System.currentTimeMillis() - execStartTime) + compileTime;

            if (!finishedInTime) {
                process.destroyForcibly();
                return new CodeExecutionResult("TIMEOUT", "", "Execution timed out after " + TIMEOUT_SECONDS + " seconds.", -1, totalExecutionTime);
            }

            String stdout = readStream(process.getInputStream());
            String stderr = readStream(process.getErrorStream());
            int exitCode = process.exitValue();

            String status = (exitCode == 0) ? "SUCCESS" : "RUNTIME_ERROR";
            return new CodeExecutionResult(status, stdout, stderr, exitCode, totalExecutionTime);

        } catch (Exception e) {
            return new CodeExecutionResult("ERROR", "", "Internal execution error: " + e.getMessage(), -1, 0L);
        } finally {
            if (tempDir != null) {
                deleteDirectoryRecursively(tempDir);
            }
        }
    }

    private String prepareJavaCode(String rawCode) {
        // If snippet has no class definition at all, auto-wrap in public class Main
        if (!rawCode.contains("class ")) {
            return "public class Main {\n" +
                   "    public static void main(String[] args) {\n" +
                   "        " + rawCode + "\n" +
                   "    }\n" +
                   "}";
        }
        return rawCode;
    }

    private String extractClassName(String code) {
        Matcher publicMatcher = PUBLIC_CLASS_PATTERN.matcher(code);
        if (publicMatcher.find()) {
            return publicMatcher.group(1);
        }
        Matcher classMatcher = ANY_CLASS_PATTERN.matcher(code);
        if (classMatcher.find()) {
            return classMatcher.group(1);
        }
        return "Main";
    }

    private boolean compileJavaFile(File sourceFile, Path tempDir, ByteArrayOutputStream errStream) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler != null) {
            int result = compiler.run(null, null, errStream, sourceFile.getAbsolutePath());
            return result == 0;
        } else {
            // Fallback to ProcessBuilder if SystemJavaCompiler is null
            ProcessBuilder pb = new ProcessBuilder("javac", sourceFile.getAbsolutePath());
            pb.directory(tempDir.toFile());
            Process process = pb.start();
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                errStream.write("Compilation timed out.".getBytes(StandardCharsets.UTF_8));
                return false;
            }
            String stderr = readStream(process.getErrorStream());
            if (!stderr.isEmpty()) {
                errStream.write(stderr.getBytes(StandardCharsets.UTF_8));
            }
            return process.exitValue() == 0;
        }
    }

    private String readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private void deleteDirectoryRecursively(Path dir) {
        try {
            Files.walk(dir)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
        } catch (IOException ignored) {
        }
    }
}
