package com.example.demo.service;

import com.example.demo.dto.TagDTO;
import com.example.demo.dto.QuestionViewDTO;
import com.example.demo.entity.Tag;
import com.example.demo.entity.TagFollow;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.TagFollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final QuestionRepository questionRepository;
    private final TagFollowRepository tagFollowRepository;

    @Autowired
    public TagServiceImpl(TagRepository tagRepository, QuestionRepository questionRepository, TagFollowRepository tagFollowRepository) {
        this.tagRepository = tagRepository;
        this.questionRepository = questionRepository;
        this.tagFollowRepository = tagFollowRepository;
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
    public Map<String, Object> getTagsPaginated(String keyword, String sort, int page, int pageSize, Long userId) {
        List<TagDTO> allTags = searchAndSortTags(keyword, sort);
        if (userId != null) {
            for (TagDTO tag : allTags) {
                tag.setIsFollowed(tagFollowRepository.existsByUserIdAndTagId(userId, tag.getId()));
            }
        } else {
            for (TagDTO tag : allTags) {
                tag.setIsFollowed(false);
            }
        }

        int totalItems = allTags.size();
        if (pageSize <= 0) pageSize = 12;
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (page < 1) page = 1;
        if (page > totalPages && totalItems > 0) page = totalPages;

        int fromIndex = Math.min((page - 1) * pageSize, totalItems);
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<TagDTO> pagedTags = allTags.subList(fromIndex, toIndex);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("currentPage", page);
        response.put("totalPages", totalPages);
        response.put("totalItems", totalItems);
        response.put("data", pagedTags);
        return response;
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

    @Override
    public void followOrUnfollowTag(Long userId, Long tagId, String action) {
        toggleTagFollow(userId, tagId, action);
    }

    @Override
    public boolean toggleTagFollow(Long userId, Long tagId, String action) {
        boolean currentlyFollowing = tagFollowRepository.existsByUserIdAndTagId(userId, tagId);
        boolean shouldFollow;
        if ("follow".equalsIgnoreCase(action)) {
            shouldFollow = true;
        } else if ("unfollow".equalsIgnoreCase(action)) {
            shouldFollow = false;
        } else {
            shouldFollow = !currentlyFollowing;
        }

        if (shouldFollow) {
            if (!currentlyFollowing) {
                tagFollowRepository.save(new TagFollow(userId, tagId));
            }
            return true;
        } else {
            Optional<TagFollow> followOpt = tagFollowRepository.findByUserIdAndTagId(userId, tagId);
            followOpt.ifPresent(tagFollowRepository::delete);
            return false;
        }
    }


    @Override
    public boolean isFollowing(Long userId, Long tagId) {
        return tagFollowRepository.existsByUserIdAndTagId(userId, tagId);
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

    @Override
    public void createTag(String tagName, String description) {
        String cleanTagName = tagName.trim().toLowerCase();
        Optional<Tag> existing = tagRepository.findByTagNameIgnoreCase(cleanTagName);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Tag already exists");
        }
        Tag tag = new Tag();
        tag.setTagName(cleanTagName);
        tag.setDescription(description != null ? description.trim() : "");
        tag.setIsActive(true);
        tagRepository.save(tag);
    }
}
