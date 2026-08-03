package com.example.demo.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.demo.dto.GithubUser;
import com.example.demo.dto.GoogleUser;
import com.example.demo.dto.QuestionDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserPageDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Lazy
    @Autowired(required = false)
    private SessionRegistry sessionRegistry;

    // Inject BCrypt password encoder configured in SecurityConfig
    @Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO convertToDTO(User user) {
        if (user == null)
            return null;
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
        if (sessionRegistry != null && user.getEmail() != null) {
            String email = user.getEmail();
            boolean online = sessionRegistry.getAllPrincipals().stream()
                .filter(p -> !sessionRegistry.getAllSessions(p, false).isEmpty())
                .anyMatch(p -> {
                    if (p instanceof UserDetails) return ((UserDetails) p).getUsername().equals(email);
                    if (p instanceof OAuth2User) return email.equals(((OAuth2User) p).getAttribute("email"));
                    return false;
                });
            dto.setOnline(online);
        }
        return dto;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // Force-expires every active session for this email, logging the user out on their very next request
    public void expireSessionsForEmail(String email) {
        if (sessionRegistry == null || email == null)
            return;
        sessionRegistry.getAllPrincipals().stream()
                .filter(p -> {
                    if (p instanceof UserDetails) return ((UserDetails) p).getUsername().equals(email);
                    if (p instanceof OAuth2User) return email.equals(((OAuth2User) p).getAttribute("email"));
                    return false;
                })
                .forEach(p -> sessionRegistry.getAllSessions(p, false)
                        .forEach(org.springframework.security.core.session.SessionInformation::expireNow));
    }

    // Returns the IDs of users with at least one active session, used to live-refresh the admin Online column
    public List<Long> getOnlineUserIds() {
        if (sessionRegistry == null)
            return List.of();
        List<String> onlineEmails = sessionRegistry.getAllPrincipals().stream()
                .filter(p -> !sessionRegistry.getAllSessions(p, false).isEmpty())
                .map(p -> {
                    if (p instanceof UserDetails)
                        return ((UserDetails) p).getUsername();
                    if (p instanceof OAuth2User)
                        return (String) ((OAuth2User) p).getAttribute("email");
                    return null;
                })
                .filter(email -> email != null)
                .collect(Collectors.toList());
        if (onlineEmails.isEmpty())
            return List.of();
        return userRepository.findByEmailIn(onlineEmails).stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

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

    public User loginModel(String email, String rawPassword) throws Exception {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.getPasswordHash() != null
                && passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
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

    public User loginOrRegister(String providerId, String email, String name, String providerType) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        } else {
            return createNewUser(providerId, email, name, providerType);
        }
    }

    private User createNewUser(String providerId, String email, String name, String providerType) {
        // 1. Generate a safe username (Remove whitespaces, limit to 50 characters)
        String baseName = (name != null && !name.trim().isEmpty()) ? name : providerType + "_user";
        String safeName = baseName.replaceAll("\\s+", "") + "_" + (int) (Math.random() * 10000);
        if (safeName.length() > 50) {
            safeName = safeName.substring(0, 50);
        }

        // 2. Handle missing email thoroughly (GitHub often hides the real email)
        String safeEmail = (email != null && !email.trim().isEmpty()) ? email
                : safeName + "@" + providerType + ".local";

        // 3. Initialize the User with all core fields
        User newUser = new User();
        newUser.setUsername(safeName);
        newUser.setEmail(safeEmail);

        // Encode the dummy password instead of using a raw UUID string to avoid length
        // constraints on the password_hash column
        newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setRole("member");
        newUser.setStatus("active");
        newUser.setReputation(0);
        newUser.setProvider(providerType);
        newUser.setProviderId(providerId);
        newUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        // 4. Pre-populate Profile data (Prevents Hibernate @SecondaryTable constraint
        // violations)
        newUser.setAvatarUrl("/assets/img/default-avatar.png");
        newUser.setBio("New member");
        newUser.setLocation("Earth");
        newUser.setWebsite("");

        User savedUser = null;
        try {
            // Force save to the database
            savedUser = userRepository.save(newUser);
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Database rejected the OAuth2 user save.");
            System.err.println("Username: " + newUser.getUsername() + ", Email: " + newUser.getEmail());
            e.printStackTrace();
            // Throw exception to abort the Ghost Login flow
            throw new RuntimeException("DB save error during social login", e);
        }

        // 5. Native Query fallback: Force create Profile & Avatar if Hibernate missed
        // it
        if (savedUser != null) {
            try {
                updateProfile(savedUser.getUserId(), savedUser.getUsername(), "New member", "Earth", "");
                updateAvatar(savedUser.getUserId(), "/assets/img/default-avatar.png");
            } catch (Exception e) {
                System.err.println("Warning: Error executing Native Query for Profile creation: " + e.getMessage());
            }
        }

        return savedUser;
    }

    public UserDTO getUserProfileById(long id) {
        return convertToDTO(userRepository.findById(id).orElse(null));
    }

    // ADMIN USER MANAGEMENT

    public int getUserCount() {
        return (int) userRepository.count();
    }

    public List<UserDTO> getAllUsers(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findAll(pageable).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<UserDTO> searchUsers(String keyword, int limit) {
        return searchUsers(keyword, limit, null, null);
    }

    public List<UserDTO> searchUsers(String keyword, int limit, Long excludeUserId) {
        return searchUsers(keyword, limit, excludeUserId, null);
    }

    public List<UserDTO> searchUsers(String keyword, int limit, Long excludeUserId, String excludeRole) {
        Pageable pageable = PageRequest.of(0, limit);
        return userRepository.searchUsersAdmin(keyword, excludeUserId, excludeRole, pageable).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    // Looks up a user's id by email, used to identify the logged-in admin/moderator
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email).map(User::getUserId).orElse(null);
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
        return getUserCountByFilter(role, status, null, null);
    }

    public int getUserCountByFilter(String role, String status, Long excludeUserId) {
        return getUserCountByFilter(role, status, excludeUserId, null);
    }

    public int getUserCountByFilter(String role, String status, Long excludeUserId, String excludeRole) {
        return userRepository.countByRoleAndStatus(role, status, excludeRole, excludeUserId);
    }

    public List<UserDTO> getUsersByFilter(String role, String status, int page, int pageSize) {
        return getUsersByFilter(role, status, page, pageSize, null, null);
    }

    public List<UserDTO> getUsersByFilter(String role, String status, int page, int pageSize, Long excludeUserId) {
        return getUsersByFilter(role, status, page, pageSize, excludeUserId, null);
    }

    public List<UserDTO> getUsersByFilter(String role, String status, int page, int pageSize, Long excludeUserId,
            String excludeRole) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findByRoleAndStatus(role, status, excludeUserId, excludeRole, pageable).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<UserDTO> getNewestUsers(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findAll(pageable).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // DASHBOARD STATS

    public int getQuestionCount() {
        return userRepository.getQuestionCount();
    }

    public int getAnswerCount() {
        return userRepository.getAnswerCount();
    }

    public List<Map<String, Object>> getCurrentMonthQuestionCountByTag(int limit) {
        return userRepository.getTopTagsByQuestionCount(PageRequest.of(0, limit));
    }

    public List<Map<String, Object>> getUserRegistrationTrend(int days) {
        return userRepository.getUserRegistrationTrend(days);
    }

    public List<Map<String, Object>> getQuestionTrend(int days) {
        return userRepository.getQuestionTrend(days);
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

    public void validatePassword(String password, String confirmPassword) {

        List<String> errors = new ArrayList<>();

        if (password == null || password.isBlank()) {
            errors.add("Password is required.");
        } else {

        if (password.length() < 8) {
            errors.add("Password must contain at least 8 characters.");
        }

        if (!password.matches(".*[A-Z].*")) {
            errors.add("Password must contain at least one uppercase letter.");
        }

        if (!password.matches(".*[a-z].*")) {
            errors.add("Password must contain at least one lowercase letter.");
        }

        if (!password.matches(".*\\d.*")) {
            errors.add("Password must contain at least one number.");
        }

        if (!password.matches(".*[@$!%*?&.#^()_+\\-=:;,/\\\\].*")) {
            errors.add("Password must contain at least one special character.");
        }
    }

        if (!password.equals(confirmPassword)) {
            errors.add("Confirm password does not match.");
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join("<br>", errors));
        }
    }

    // USER FOR USER

    public List<UserDTO> getTopUsers() {
        return userRepository.getTopUsers().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<UserDTO> getAllUsers(String keyword, String sort) {
        return userRepository.getAllUsersWithSort(keyword, sort).stream().map(this::convertToDTO)
                .collect(Collectors.toList());
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

    public boolean updateProfile(long userId, String displayName, String bio, String location, String websiteJson) {
        try {
            int updated = userRepository.updateDisplayName(userId, displayName);
            if (updated == 0) {
                return false;
            }

            int profileCount = userRepository.checkProfileExists(userId);
            if (profileCount > 0) {
                userRepository.updateProfileInfoOnly(userId, bio, location, websiteJson);
            } else {
                userRepository.insertProfileInfoOnly(userId, bio, location, websiteJson);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateAvatar(long userId, String avatarUrl) {
        try {
            int profileCount = userRepository.checkProfileExists(userId);
            if (profileCount > 0) {
                userRepository.updateAvatarOnly(userId, avatarUrl);
            } else {
                userRepository.insertAvatarOnly(userId, avatarUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Page<UserPageDTO> getUsersForUserPage(String keyword, String filter, int page,Long currentUserId) {
    if (keyword == null) {
        keyword = "";
    }

    keyword = keyword.trim();

    if (filter == null
            || (!filter.equals("reputation")
            && !filter.equals("voted")
            && !filter.equals("new"))) {
        filter = "reputation";
    }

    if (page < 0) {
        page = 0;
    }

    Pageable pageable = PageRequest.of(page, 16);

    return userRepository.findUsersForUserPage(keyword, filter,currentUserId, pageable);
}
public void changePasswordWithOldPassword(
        String email,
        String oldPassword,
        String newPassword,
        String confirmPassword)
        throws Exception {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("User not found."));

    if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
        throw new RuntimeException("Current password is incorrect.");
    }
    validatePassword(newPassword, confirmPassword);
    changPassword(email, newPassword);
    
    String hash = passwordEncoder.encode(newPassword);

    userRepository.changePassword(email, hash);
}
public boolean canChangePassword(String email) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                new IllegalStateException(
                    "Authenticated user '" + email + "' does not exist in database."
                )
            );

    return "local".equalsIgnoreCase(user.getProvider());
}
public boolean isLocalAccount(String email) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new IllegalStateException("User not found."));

    return "local".equalsIgnoreCase(user.getProvider());
}
public void validateRegisterPassword(String password, String confirmPassword){
    if (password == null || password.isBlank()) {
        throw new RuntimeException("Password is required.");
    }

    if (!password.equals(confirmPassword)) {
        throw new RuntimeException("Passwords do not match.");
    }

    int score = 0;

    if (password.length() >= 8) score++;
    if (password.matches(".*[a-z].*")) score++;
    if (password.matches(".*[A-Z].*")) score++;
    if (password.matches(".*\\d.*")) score++;
    if (password.matches(".*[@$!%*?&.#^()_+\\-=:;,/\\\\].*")) score++;

    // Weak (score <= 2)
    if (score <= 2) {
        throw new RuntimeException("Password is too weak.");
    }
}
}