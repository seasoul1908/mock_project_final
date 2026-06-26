package com.example.demo.service;

import com.example.demo.dto.QuestionDTO;
import com.example.demo.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionRecommendationService {

    private static final Set<String> RECOMMENDATION_STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "and", "for", "with", "that", "this", "from", "into", "have", "has",
            "how", "why", "when", "where", "what", "which", "can", "cannot", "cant", "not",
            "are", "was", "were", "your", "you", "use", "using", "used", "get", "got",
            "then", "than", "them", "they", "their", "there", "here", "about", "after",
            "before", "been", "being", "would", "could", "should", "will", "just", "like",
            "make", "made", "need", "want", "help", "question", "questions", "code", "error",
            "java", "jsp", "sql", "html", "css"
    ));

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private QuestionRepository questionRepository;

    public List<QuestionDTO> getRecommendedQuestions(List<Long> viewedIds, int limit) {
        if (viewedIds == null || viewedIds.isEmpty()) {
            return getPopularQuestions(0, limit);
        }

        List<String> tags = extractTagsFromViewed(viewedIds);
        List<String> keywords = extractKeywordsFromViewed(viewedIds, 8);
        List<QuestionDTO> recommended = findRecommendedByProfile(tags, keywords, viewedIds, limit);

        if (recommended.size() < limit) {
            List<QuestionDTO> fallback = getPopularQuestions(0, limit * 2);
            recommended.addAll(fallback);
            recommended = distinctAndLimit(recommended, viewedIds, limit);
        }

        return recommended;
    }

    public List<String> extractTagsFromViewed(List<Long> viewedIds) {
        if (viewedIds == null || viewedIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> tags = new ArrayList<>();
        for (Long qId : viewedIds) {
            tags.addAll(questionRepository.findTagsByQuestionId(qId));
        }

        return tags.stream()
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<String> extractKeywordsFromViewed(List<Long> viewedIds, int limit) {
        Map<String, Integer> frequencies = new HashMap<>();

        if (viewedIds == null || viewedIds.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT title, body FROM Questions WHERE question_id IN (");
        appendPlaceholders(sql, viewedIds.size());
        sql.append(") AND ISNULL(is_deleted, 0) = 0");

        List<Object> params = new ArrayList<>(viewedIds);

        jdbcTemplate.query(sql.toString(), rs -> {
            collectKeywordFrequency(frequencies, rs.getString("title"));
            collectKeywordFrequency(frequencies, rs.getString("body"));
        }, params.toArray());

        return frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<QuestionDTO> findRecommendedByProfile(List<String> tags, List<String> keywords,
            List<Long> excludeIds, int limit) {
        List<QuestionDTO> list = new ArrayList<>();
        boolean hasTags = tags != null && !tags.isEmpty();
        boolean hasKeywords = keywords != null && !keywords.isEmpty();

        if (!hasTags && !hasKeywords) {
            return list;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TOP (?) q.*, u.username, u.Reputation AS author_reputation, up.avatar_url, ")
                .append("(SELECT COUNT(*) FROM Answers a WHERE a.question_id = q.question_id) as ans_count, ");

        List<String> scoreParts = new ArrayList<>();
        if (hasTags) {
            StringBuilder tagScore = new StringBuilder();
            tagScore.append("(SELECT COUNT(DISTINCT qt.tag_id) * 8.0 FROM Question_Tags qt ")
                    .append("JOIN Tags t ON qt.tag_id = t.tag_id ")
                    .append("WHERE qt.question_id = q.question_id AND t.tag_name IN (");
            appendPlaceholders(tagScore, tags.size());
            tagScore.append("))");
            scoreParts.add(tagScore.toString());
        }

        if (hasKeywords) {
            for (int i = 0; i < keywords.size(); i++) {
                scoreParts.add("CASE WHEN q.title LIKE ? THEN 5.0 ELSE 0 END");
                scoreParts.add("CASE WHEN q.body LIKE ? THEN 2.5 ELSE 0 END");
            }
        }

        scoreParts.add("(q.Score * 1.5)");
        scoreParts.add("(q.view_count * 0.08)");
        scoreParts.add("CASE WHEN q.accepted_answer_id IS NOT NULL THEN 2.0 ELSE 0 END");

        sql.append("CAST((")
                .append(String.join(" + ", scoreParts))
                .append(") AS FLOAT) AS recommendation_score ")
                .append("FROM Questions q ")
                .append("JOIN Users u ON q.user_id = u.user_id ")
                .append("LEFT JOIN User_Profile up ON u.user_id = up.user_id ")
                .append("WHERE ISNULL(q.is_deleted, 0) = 0 ");

        if (excludeIds != null && !excludeIds.isEmpty()) {
            sql.append("AND q.question_id NOT IN (");
            appendPlaceholders(sql, excludeIds.size());
            sql.append(") ");
        }

        sql.append("AND (");
        List<String> matchParts = new ArrayList<>();
        if (hasTags) {
            StringBuilder tagMatch = new StringBuilder();
            tagMatch.append("EXISTS (SELECT 1 FROM Question_Tags qt ")
                    .append("JOIN Tags t ON qt.tag_id = t.tag_id ")
                    .append("WHERE qt.question_id = q.question_id AND t.tag_name IN (");
            appendPlaceholders(tagMatch, tags.size());
            tagMatch.append("))");
            matchParts.add(tagMatch.toString());
        }
        if (hasKeywords) {
            for (int i = 0; i < keywords.size(); i++) {
                matchParts.add("q.title LIKE ?");
                matchParts.add("q.body LIKE ?");
            }
        }
        sql.append(String.join(" OR ", matchParts))
                .append(") ")
                .append("ORDER BY recommendation_score DESC, q.view_count DESC, q.created_at DESC");

        List<Object> params = new ArrayList<>();
        params.add(limit);

        if (hasTags) {
            params.addAll(tags);
        }
        if (hasKeywords) {
            for (String keyword : keywords) {
                String pattern = "%" + keyword + "%";
                params.add(pattern);
                params.add(pattern);
            }
        }
        if (excludeIds != null && !excludeIds.isEmpty()) {
            params.addAll(excludeIds);
        }
        if (hasTags) {
            params.addAll(tags);
        }
        if (hasKeywords) {
            for (String keyword : keywords) {
                String pattern = "%" + keyword + "%";
                params.add(pattern);
                params.add(pattern);
            }
        }

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            QuestionDTO question = mapRow(rs);
            question.setPopularScore(rs.getDouble("recommendation_score"));
            return question;
        }, params.toArray());
    }

    public List<QuestionDTO> getPopularQuestions(long excludeQuestionId, int limit) {
        String sql = "SELECT q.*, u.username, u.Reputation AS author_reputation, up.avatar_url, "
                + "(SELECT COUNT(*) FROM Answers a WHERE a.question_id = q.question_id) as ans_count, "
                + "CAST((q.Score * 2.0) + (q.view_count / 10.0) - DATEDIFF(DAY, q.created_at, GETDATE()) AS FLOAT) AS popular_score "
                + "FROM Questions q "
                + "JOIN Users u ON q.user_id = u.user_id "
                + "LEFT JOIN User_Profile up ON u.user_id = up.user_id "
                + "WHERE q.question_id <> ? AND ISNULL(q.is_deleted, 0) = 0 "
                + "ORDER BY popular_score DESC, q.view_count DESC, q.created_at DESC "
                + "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            QuestionDTO question = mapRow(rs);
            question.setPopularScore(rs.getDouble("popular_score"));
            return question;
        }, excludeQuestionId, limit);
    }

    private List<QuestionDTO> distinctAndLimit(List<QuestionDTO> list, List<Long> excludeIds, int limit) {
        List<QuestionDTO> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();

        for (QuestionDTO q : list) {
            if (seen.contains(q.getQuestionId())) {
                continue;
            }
            if (excludeIds != null && excludeIds.contains(q.getQuestionId())) {
                continue;
            }

            seen.add(q.getQuestionId());
            result.add(q);

            if (result.size() >= limit) {
                break;
            }
        }

        return result;
    }

    private void collectKeywordFrequency(Map<String, Integer> frequencies, String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        String normalized = text.toLowerCase(Locale.ENGLISH)
                .replaceAll("<[^>]+>", " ")
                .replaceAll("[^a-z0-9#+._-]", " ");

        for (String token : normalized.split("\\s+")) {
            String clean = token.trim();
            if (clean.length() < 3 || clean.length() > 24) {
                continue;
            }
            if (RECOMMENDATION_STOP_WORDS.contains(clean)) {
                continue;
            }
            if (!clean.matches(".*[a-z].*")) {
                continue;
            }
            frequencies.merge(clean, 1, Integer::sum);
        }
    }

    private void appendPlaceholders(StringBuilder sql, int count) {
        for (int i = 0; i < count; i++) {
            sql.append("?");
            if (i < count - 1) {
                sql.append(",");
            }
        }
    }

    private QuestionDTO mapRow(ResultSet rs) throws SQLException {
        QuestionDTO q = new QuestionDTO();
        q.setQuestionId(rs.getLong("question_id"));
        q.setUserId(rs.getLong("user_id"));
        q.setTitle(rs.getString("title"));
        q.setBody(rs.getString("body"));
        q.setViewCount(rs.getInt("view_count"));
        q.setIsClosed(rs.getBoolean("is_closed"));
        q.setScore(rs.getInt("Score"));
        q.setCreatedAt(rs.getTimestamp("created_at"));
        q.setAuthorName(rs.getString("username"));
        q.setAuthorAvatar(getNullableString(rs, "avatar_url"));
        Integer authorReputation = getNullableInt(rs, "author_reputation");
        if (authorReputation != null) {
            q.setAuthorReputation(authorReputation);
        }
        q.setAnswerCount(rs.getInt("ans_count"));
        q.setTags(questionRepository.findTagsByQuestionId(q.getQuestionId()));
        return q;
    }

    private String getNullableString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private Integer getNullableInt(ResultSet rs, String column) {
        try {
            int value = rs.getInt(column);
            return rs.wasNull() ? null : value;
        } catch (SQLException e) {
            return null;
        }
    }
}
