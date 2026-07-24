package com.example.demo.config;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class DevQueryAdminDynamicTools {

    private final JdbcTemplate jdbcTemplate;

    // Security constraints
    private static final String[] FORBIDDEN_KEYWORDS = {
            "INSERT", "UPDATE", "DELETE", "DROP", "ALTER", "TRUNCATE", "EXEC", "GRANT"
    };

    // Pre-compile Regex patterns with word boundaries to avoid false positives
    private static final Pattern[] FORBIDDEN_PATTERNS = new Pattern[FORBIDDEN_KEYWORDS.length];

    static {
        for (int i = 0; i < FORBIDDEN_KEYWORDS.length; i++) {
            FORBIDDEN_PATTERNS[i] = Pattern.compile("\\b" + FORBIDDEN_KEYWORDS[i] + "\\b");
        }
    }

    // Removed ObjectMapper from the constructor
    public DevQueryAdminDynamicTools(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(description = "Executes a dynamic Text-to-SQL query against the Microsoft SQL Server database. " +
            "You MUST pass a valid T-SQL SELECT query based on the DevQuery Admin schema. " +
            "Allowed tables: Users, Tags, Reports, Rules, Badges, Blogs, Feedbacks. " +
            "Do NOT attempt to execute DML or DDL commands. Only SELECT statements are permitted. " +
            "If a SQL error occurs, analyze the provided exception message and attempt to correct your query.")
    public String executeTextToSql(String sqlQuery) {
        if (sqlQuery == null || sqlQuery.isBlank()) {
            return "Error: SQL query cannot be empty.";
        }

        String upperQuery = sqlQuery.toUpperCase();

        // THE SECURITY BARRIER (Maintained the Agent's excellent Regex logic)
        for (Pattern pattern : FORBIDDEN_PATTERNS) {
            if (pattern.matcher(upperQuery).find()) {
                return "SECURITY ALERT: DML/DDL commands are strictly forbidden. Only SELECT is allowed.";
            }
        }

        try {
            // Execution
            List<Map<String, Object>> queryResults = jdbcTemplate.queryForList(sqlQuery);

            // CHANGED: Using default .toString() for List/Map instead of ObjectMapper
            return queryResults.toString();

        } catch (DataAccessException e) {
            // Error Handling: Feed the exception back to the AI for self-correction
            return "SQL Execution Error (DataAccessException): " + e.getMostSpecificCause().getMessage();
        }
    }
}