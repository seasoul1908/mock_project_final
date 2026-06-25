package com.example.demo.service;

import com.example.demo.dto.TagDTO;
import com.example.demo.dto.QuestionViewDTO;
import java.util.List;

public interface TagService {
    List<TagDTO> searchAndSortTags(String keyword, String sort);
    TagDTO getTagById(Long id);
    List<QuestionViewDTO> getQuestionsByTag(Long tagId, String filter, int page, int pageSize);
    int countQuestionsByTag(Long tagId, String filter);
    void followOrUnfollowTag(Long userId, Long tagId, String action);
    boolean isFollowing(Long userId, Long tagId);
}
