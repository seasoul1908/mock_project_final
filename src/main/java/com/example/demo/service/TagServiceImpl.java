package com.example.demo.service;

import com.example.demo.dto.TagDTO;
import com.example.demo.dto.QuestionViewDTO;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public TagServiceImpl(TagRepository tagRepository, QuestionRepository questionRepository) {
        this.tagRepository = tagRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public List<TagDTO> searchAndSortTags(String keyword, String sort) {
        List<Object[]> results = tagRepository.searchAndSortTagsNative(keyword, sort);
        List<TagDTO> dtos = new ArrayList<>();
        for (Object[] row : results) {
            dtos.add(mapRowToTagDTO(row));
        }
        return dtos;
    }

    @Override
    public TagDTO getTagById(Long id) {
        List<Object[]> results = tagRepository.findTagByIdNative(id);
        if (results != null && !results.isEmpty()) {
            return mapRowToTagDTO(results.get(0));
        }
        return null;
    }

    @Override
    public List<QuestionViewDTO> getQuestionsByTag(Long tagId, String filter, int page, int pageSize) {
        List<Object[]> results = questionRepository.findQuestionsByTagNative(tagId, filter, org.springframework.data.domain.PageRequest.of(page - 1, pageSize));
        List<QuestionViewDTO> questions = new ArrayList<>();
        for (Object[] row : results) {
            Long qId = ((Number) row[0]).longValue();
            String title = (String) row[1];
            String body = (String) row[2];
            Integer score = ((Number) row[3]).intValue();
            Integer viewCount = ((Number) row[4]).intValue();
            Timestamp createdAt = (Timestamp) row[5];
            Timestamp updatedAt = (Timestamp) row[6];
            Boolean isClosed = (Boolean) row[7];
            String authorName = (String) row[8];
            String authorAvatar = (String) row[9];
            Integer answerCount = ((Number) row[10]).intValue();

            List<String> tags = questionRepository.findTagNamesByQuestionIdNative(qId);

            questions.add(new QuestionViewDTO(qId, title, body, score, viewCount, createdAt, updatedAt, isClosed, authorName, authorAvatar, answerCount, tags));
        }
        return questions;
    }

    @Override
    public int countQuestionsByTag(Long tagId, String filter) {
        return questionRepository.countQuestionsByTagNative(tagId, filter);
    }

    private TagDTO mapRowToTagDTO(Object[] row) {
        Long id = ((Number) row[0]).longValue();
        String tagName = (String) row[1];
        String description = (String) row[2];
        Boolean isActive = (Boolean) row[3];
        Integer questionCount = ((Number) row[4]).intValue();
        Integer followerCount = ((Number) row[5]).intValue();

        return new TagDTO(id, tagName, description, isActive, questionCount, followerCount);
    }
}
