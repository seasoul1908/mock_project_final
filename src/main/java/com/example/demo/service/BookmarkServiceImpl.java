package com.example.demo.service;

import com.example.demo.repository.BookmarkRepository;
import com.example.demo.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Override
    @Transactional
    public boolean toggleBookmark(long userId, long questionId) {
        questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (bookmarkRepository.countBookmark(userId, questionId) > 0) {
            bookmarkRepository.deleteBookmarkPermanent(userId, questionId);
            return false;
        }

        bookmarkRepository.insertBookmark(userId, questionId);
        return true;
    }
}
