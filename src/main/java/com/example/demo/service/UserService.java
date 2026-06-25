package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.QuestionDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import com.example.demo.dto.GithubUser;
import com.example.demo.dto.GoogleUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Gọi công cụ mã hóa xịn sò từ SecurityConfig
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO convertToDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus() != null ? user.getStatus() : "active");
        dto.setReputation(user.getReputation() != null ? user.getReputation() : 0);
        dto.setCreatedAt(user.getCreatedAt());
        dto.setBio(user.getBio());
        dto.setLocation(user.getLocation());
        dto.setWebsite(user.getWebsite());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    // ĐĂNG KÝ: Tự động mã hóa mật khẩu chuẩn BCrypt
    public void register(String username, String email, String rawPassword) throws Exception {
        String hash = passwordEncoder.encode(rawPassword);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(hash);
        user.setRole("member");
        user.setStatus("active");
        user.setReputation(0);
        user.setProvider("local");
        userRepository.save(user);
    }

    // ĐĂNG NHẬP: Dùng .matches() của BCrypt để so sánh mật khẩu an toàn
    public User loginModel(String email, String rawPassword) throws Exception {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.getPasswordHash() != null && passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public UserDTO login(String email, String rawPassword) throws Exception {
        User user = loginModel(email, rawPassword);
        return convertToDTO(user);
    }

    public User loginWithGoogle(GoogleUser gUser) {
        return loginOrRegister(gUser.id, gUser.email, gUser.name, "google");
    }

    public User loginWithGithub(GithubUser gitUser) {
        String displayName = (gitUser.name != null && !gitUser.name.isEmpty()) ? gitUser.name : gitUser.login;
        return loginOrRegister(String.valueOf(gitUser.id), gitUser.email, displayName, "github");
    }

    private User loginOrRegister(String providerId, String email, String name, String providerType) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        } else {
            return createNewUser(providerId, email, name, providerType);
        }
    }

    private User createNewUser(String providerId, String email, String name, String providerType) {
        String safeName = (name != null ? name : "User").replaceAll("\\s+", "") + "_" + (int) (Math.random() * 10000);
        if (safeName.length() > 50) {
            safeName = safeName.substring(0, 50);
        }

        User newUser = new User();
        newUser.setUsername(safeName);
        newUser.setEmail(email);
        newUser.setPasswordHash(UUID.randomUUID().toString());
        newUser.setRole("member");
        newUser.setStatus("active");
        newUser.setReputation(0);
        newUser.setProvider(providerType);
        newUser.setProviderId(providerId);

        return userRepository.save(newUser);
    }

    public UserDTO getUserProfileById(long id) {
        return convertToDTO(userRepository.findById(id).orElse(null));
    }

    // ==================== ADMIN USER MANAGEMENT ====================

    public int getUserCount() {
        return (int) userRepository.count();
    }

    public List<UserDTO> getAllUsers(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findAll(pageable).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<UserDTO> searchUsers(String keyword, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return userRepository.searchUsersAdmin(keyword, pageable).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public UserDTO getUserById(long userId) {
        return convertToDTO(userRepository.findById(userId).orElse(null));
    }

    public boolean createUser(String username, String email, String rawPassword, String role) {
        try {
            String hash = passwordEncoder.encode(rawPassword);
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(hash);
            user.setRole(role);
            user.setStatus("active");
            user.setReputation(0);
            user.setProvider("local");
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateUser(long userId, String role, String status) {
        return userRepository.updateUserRoleAndStatus(userId, role, status) > 0;
    }

    public boolean toggleUserStatus(long userId) {
        return userRepository.toggleUserStatus(userId) > 0;
    }

    public int getUserCountByFilter(String role, String status) {
        return userRepository.countByRoleAndStatus(role, status);
    }

    public List<UserDTO> getUsersByFilter(String role, String status, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findByRoleAndStatus(role, status, pageable).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<UserDTO> getNewestUsers(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findAll(pageable).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ==================== DASHBOARD STATS ====================

    public int getQuestionCount() {
        return userRepository.getQuestionCount();
    }

    public int getAnswerCount() {
        return userRepository.getAnswerCount();
    }

    public List<Map<String, Object>> getCurrentMonthQuestionCountByTag(int limit) {
        return userRepository.getCurrentMonthQuestionCountByTag(PageRequest.of(0, limit));
    }

    public List<Map<String, Object>> getUserRegistrationTrend(int days) {
        List<Map<String, Object>> trend = userRepository.getUserRegistrationTrend(days);
        for (Map<String, Object> map : trend) {
            map.put("date", new java.sql.Date(((java.util.Date) map.get("date")).getTime()));
        }
        return trend;
    }

    public List<Map<String, Object>> getQuestionTrend(int days) {
        List<Map<String, Object>> trend = userRepository.getQuestionTrend(days);
        for (Map<String, Object> map : trend) {
            map.put("date", new java.sql.Date(((java.util.Date) map.get("date")).getTime()));
        }
        return trend;
    }

    public List<String> getReputationChanges(long userId, int limit) {
        List<String> formattedChanges = new ArrayList<>();
        List<Map<String, Object>> rows = userRepository.getReputationChanges(userId, PageRequest.of(0, limit));
        for (Map<String, Object> row : rows) {
            int delta = (int) row.get("delta");
            String reason = (String) row.get("reason");
            String formatted = (delta >= 0 ? "+" : "") + delta + " reputation"
                    + (reason != null && !reason.trim().isEmpty() ? " (" + reason + ")" : "");
            formattedChanges.add(formatted);
        }
        return formattedChanges;
    }

    public void changPassword(String email, String newPassword) throws Exception {
        String hash = passwordEncoder.encode(newPassword);
        userRepository.changePassword(email, hash);
    }

    // ==================== USER FOR USER ====================

    public List<UserDTO> getTopUsers() {
        return userRepository.getTopUsers().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<UserDTO> getAllUsers(String keyword, String sort) {
        return userRepository.getAllUsersWithSort(keyword, sort).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public boolean isFollowing(long followerId, long followingId) {
        return userRepository.countFollowing(followerId, followingId) > 0;
    }

    public void followUser(long followerId, long followingId) {
        if (!isFollowing(followerId, followingId)) {
            userRepository.followUser(followerId, followingId);
        }
    }

    public void unfollowUser(long followerId, long followingId) {
        userRepository.unfollowUser(followerId, followingId);
    }

    public List<UserDTO> getFollowingList(long userId) {
        return userRepository.getFollowingListRaw(userId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public int countQuestionsByUser(long userId) {
        return userRepository.countQuestionsByUser(userId);
    }

    public List<QuestionDTO> getQuestionsByUser(long userId, String filter, int page) {
        int pageSize = 10;
        
        Sort sortObj;
        if ("newest".equals(filter)) {
            sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        } else if ("name".equals(filter)) {
            sortObj = Sort.by(Sort.Direction.ASC, "title");
        } else {
            sortObj = Sort.by(Sort.Direction.DESC, "score");
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sortObj);
        Page<Map<String, Object>> resultPage = userRepository.getQuestionsByUserRaw(userId, pageable);

        List<QuestionDTO> list = new ArrayList<>();
        for (Map<String, Object> rs : resultPage.getContent()) {
            QuestionDTO q = new QuestionDTO();
            q.setQuestionId(((Number) rs.get("questionId")).longValue());
            q.setTitle((String) rs.get("title"));
            q.setBody((String) rs.get("body"));
            q.setScore(((Number) rs.get("score")).intValue());
            q.setViewCount(((Number) rs.get("viewCount")).intValue());
            
            Object createdAt = rs.get("createdAt");
            if (createdAt instanceof Timestamp) {
                q.setCreatedAt((Timestamp) createdAt);
            } else if (createdAt instanceof java.util.Date) {
                q.setCreatedAt(new Timestamp(((java.util.Date) createdAt).getTime()));
            }

            Boolean isClosed = (Boolean) rs.get("isClosed");
            q.setIsClosed(isClosed != null && isClosed);
            
            q.setAnswerCount(((Number) rs.get("answerCount")).intValue());
            list.add(q);
        }
        return list;
    }
}