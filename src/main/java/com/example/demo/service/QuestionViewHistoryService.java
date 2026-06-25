package com.example.demo.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class QuestionViewHistoryService {

    private static final String SESSION_KEY = "viewedQuestionIds";
    private static final int MAX_HISTORY = 15;

    public List<Long> getViewedQuestionIds(HttpSession session) {
        if (session == null) {
            return Collections.emptyList();
        }

        Object history = session.getAttribute(SESSION_KEY);
        if (!(history instanceof List<?>)) {
            return Collections.emptyList();
        }

        List<Long> result = new ArrayList<>();
        for (Object item : (List<?>) history) {
            if (item instanceof Long) {
                result.add((Long) item);
            } else if (item instanceof Integer) {
                result.add(((Integer) item).longValue());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public void recordViewedQuestion(HttpSession session, long questionId) {
        if (session == null) {
            return;
        }

        Object existing = session.getAttribute(SESSION_KEY);
        List<Long> viewedIds = existing instanceof List<?>
                ? new ArrayList<>((List<Long>) existing)
                : new ArrayList<>();

        viewedIds.remove(questionId);
        viewedIds.add(0, questionId);

        if (viewedIds.size() > MAX_HISTORY) {
            viewedIds = new ArrayList<>(viewedIds.subList(0, MAX_HISTORY));
        }

        session.setAttribute(SESSION_KEY, viewedIds);
    }
}
