package com.example.demo.service;

import com.example.demo.dto.AnswerViewDTO;
import com.example.demo.dto.CommentViewDTO;
import com.example.demo.dto.QuestionDetailDTO;
import com.example.demo.dto.TrendingQuestionDTO;
import com.example.demo.entity.Answer;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Question;
import com.example.demo.entity.User;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.BookmarkRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionDetailServiceImpl implements QuestionDetailService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public QuestionDetailDTO getQuestionDetail(long questionId, Long currentUserId, boolean isAdmin) {
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isEmpty()) {
            return null;
        }

        Question question = questionOpt.get();

        // Hide soft-deleted questions entirely
        if (question.isIsDeleted()) {
            return null;
        }

        QuestionDetailDTO dto = new QuestionDetailDTO();
        dto.setQuestionId(question.getQuestionId());
        dto.setAuthorId(question.getUserId());
        dto.setTitle(question.getTitle());
        dto.setBody(question.getBody());
        dto.setCodeSnippet(question.getCodeSnippet());
        dto.setScore(question.getScore());
        dto.setViewCount(question.getViewCount());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getUpdatedAt());
        dto.setIsClosed(question.isIsClosed());
        dto.setAcceptedAnswerId(question.getAcceptedAnswerId());

        List<String> tags = questionRepository.findTagsByQuestionId(questionId);
        dto.setTags(tags != null ? tags : new java.util.ArrayList<>());

        // Bounty fields (columns already exist on Questions)
        dto.setBountyAmount(question.getBountyAmount());
        dto.setBountyExpiresAt(question.getBountyExpiresAt());
        dto.setHasActiveBounty(question.hasActiveBounty());

        // "Edited" indicator: updated after creation
        if (question.getUpdatedAt() != null && question.getCreatedAt() != null
                && question.getUpdatedAt().after(question.getCreatedAt())) {
            dto.setEdited(true);
        }

        Optional<User> authorOpt = userRepository.findById(question.getUserId());
        if (authorOpt.isPresent()) {
            User author = authorOpt.get();
            dto.setAuthorName(author.getUsername());
            dto.setAuthorAvatar(author.getDisplayAvatar());
        }

        List<Answer> answers = answerRepository.findByQuestionIdOrderByScoreDescCreatedAtAsc(questionId);
        dto.setAnswerCount(answers.size());

        // Threaded question comments (top-level + one level of replies)
        List<Comment> questionComments =
                commentRepository.findByQuestionIdAndAnswerIdIsNullAndParentCommentIdIsNullOrderByCreatedAtAsc(questionId);
        dto.setComments(mapCommentsThreaded(questionComments, currentUserId, isAdmin));

        if (currentUserId != null) {
            boolean owner = currentUserId.equals(question.getUserId());
            dto.setOwner(owner);
            dto.setBookmarked(bookmarkRepository.countBookmark(currentUserId, questionId) > 0);
            dto.setCanEdit(owner);
            dto.setCanDelete(owner || isAdmin);
            voteRepository.findQuestionVote(currentUserId, questionId)
                    .ifPresent(v -> dto.setUserVote(v.getVoteType()));
        }

        return dto;
    }

    @Override
    public List<AnswerViewDTO> getAnswersForQuestion(long questionId, Long currentUserId, boolean isAdmin, String sort) {
        List<Answer> answers;
        if (sort == null || "votes".equalsIgnoreCase(sort)) {
            answers = answerRepository.findByQuestionIdOrderByScoreDescCreatedAtAsc(questionId);
        } else if ("newest".equalsIgnoreCase(sort)) {
            answers = answerRepository.findByQuestionIdOrderByCreatedAtDesc(questionId);
        } else if ("oldest".equalsIgnoreCase(sort)) {
            answers = answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId);
        } else {
            answers = answerRepository.findByQuestionIdOrderByScoreDescCreatedAtAsc(questionId);
        }

        List<AnswerViewDTO> result = new ArrayList<>();
        for (Answer answer : answers) {
            AnswerViewDTO dto = new AnswerViewDTO();
            dto.setAnswerId(answer.getAnswerId());
            dto.setQuestionId(answer.getQuestionId());
            dto.setAuthorId(answer.getUserId());
            dto.setBody(answer.getBody());
            dto.setCodeSnippet(answer.getCodeSnippet());
            dto.setScore(answer.getScore());
            dto.setCreatedAt(answer.getCreatedAt());
            dto.setAccepted(answer.isIsAccepted());
            dto.setEdited(answer.isIsEdited());

            Optional<User> authorOpt = userRepository.findById(answer.getUserId());
            if (authorOpt.isPresent()) {
                User author = authorOpt.get();
                dto.setAuthorName(author.getUsername());
                dto.setAuthorAvatar(author.getDisplayAvatar());
            }

            // Threaded answer comments
            List<Comment> answerComments =
                    commentRepository.findByAnswerIdAndParentCommentIdIsNullOrderByCreatedAtAsc(answer.getAnswerId());
            dto.setComments(mapCommentsThreaded(answerComments, currentUserId, isAdmin));

            if (currentUserId != null) {
                boolean owner = currentUserId.equals(answer.getUserId());
                dto.setCanEdit(owner);
                dto.setCanDelete(owner || isAdmin);
                voteRepository.findAnswerVote(currentUserId, answer.getAnswerId())
                        .ifPresent(v -> dto.setUserVote(v.getVoteType()));
            }

            result.add(dto);
        }

        return result;
    }

    @Override
    public List<TrendingQuestionDTO> getTrendingQuestions(int limit) {
        List<Object[]> rows = questionRepository.findTrendingNative(PageRequest.of(0, Math.max(1, limit)));
        List<TrendingQuestionDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            TrendingQuestionDTO dto = new TrendingQuestionDTO();
            dto.setQuestionId(((Number) row[0]).longValue());
            dto.setTitle((String) row[1]);
            dto.setScore(row[2] != null ? ((Number) row[2]).intValue() : 0);
            dto.setViewCount(row[3] != null ? ((Number) row[3]).intValue() : 0);
            dto.setAnswerCount(row[4] != null ? ((Number) row[4]).intValue() : 0);
            dto.setTrendingScore(row[5] != null ? ((Number) row[5]).intValue() : 0);
            result.add(dto);
        }
        return result;
    }

    @Override
    public void incrementViewCount(long questionId) {
        questionRepository.incrementViewCount(questionId);
    }

    // Build a threaded comment list: each top-level comment gets its direct replies loaded.
    private List<CommentViewDTO> mapCommentsThreaded(List<Comment> topLevel) {
        return mapCommentsThreaded(topLevel, null, false);
    }

    private List<CommentViewDTO> mapCommentsThreaded(List<Comment> topLevel, Long currentUserId, boolean isAdmin) {
        List<CommentViewDTO> result = new ArrayList<>();
        for (Comment comment : topLevel) {
            CommentViewDTO dto = mapComment(comment, currentUserId, isAdmin);
            // Load one level of replies
            List<Comment> replies = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(comment.getCommentId());
            for (Comment reply : replies) {
                dto.getReplies().add(mapComment(reply, currentUserId, isAdmin));
            }
            result.add(dto);
        }
        return result;
    }

    private CommentViewDTO mapComment(Comment comment) {
        CommentViewDTO dto = new CommentViewDTO();
        dto.setCommentId(comment.getCommentId());
        dto.setUserId(comment.getUserId());
        dto.setParentCommentId(comment.getParentCommentId());
        dto.setBody(comment.getBody());
        dto.setCreatedAt(comment.getCreatedAt());

        userRepository.findById(comment.getUserId()).ifPresent(user -> {
            dto.setAuthorName(user.getUsername());
            dto.setAuthorAvatar(user.getDisplayAvatar());
        });

        return dto;
    }

    // Overload with current user context to set canDelete
    private CommentViewDTO mapComment(Comment comment, Long currentUserId, boolean isAdmin) {
        CommentViewDTO dto = mapComment(comment);
        if (currentUserId != null) {
            dto.setCanDelete(currentUserId.equals(comment.getUserId()) || isAdmin);
        }
        return dto;
    }
}
