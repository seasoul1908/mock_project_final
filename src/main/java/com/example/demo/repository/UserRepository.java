package com.example.demo.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.UserPageDTO;
import com.example.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

        // --- Basic Checks ---
        boolean existsByEmail(String email);

        boolean existsByUsername(String username);

        Optional<User> findByEmail(String email);

        Optional<User> findByUsername(String username);

        @Query("SELECT u FROM User u WHERE u.email = :email AND u.passwordHash = :hash")
        Optional<User> findByEmailAndPasswordHash(@Param("email") String email, @Param("hash") String hash);

        // --- Admin Search & Filter ---
        @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword%")
        Page<User> searchUsersAdmin(@Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT u FROM User u WHERE (:role IS NULL OR :role = '' OR u.role = :role) AND (:status IS NULL OR :status = '' OR u.status = :status)")
        Page<User> findByRoleAndStatus(@Param("role") String role, @Param("status") String status, Pageable pageable);

        @Query("SELECT COUNT(u) FROM User u WHERE (:role IS NULL OR :role = '' OR u.role = :role) AND (:status IS NULL OR :status = '' OR u.status = :status)")
        int countByRoleAndStatus(@Param("role") String role, @Param("status") String status);

        // --- Admin Actions ---
        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.role = :role, u.status = :status WHERE u.userId = :userId")
        int updateUserRoleAndStatus(@Param("userId") long userId, @Param("role") String role,
                        @Param("status") String status);

        @Modifying
        @Transactional
        @Query(value = "UPDATE Users SET status = CASE WHEN status = 'active' THEN 'inactive' ELSE 'active' END, updated_at = GETDATE() WHERE user_id = :userId", nativeQuery = true)
        int toggleUserStatus(@Param("userId") long userId);

        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.passwordHash = :hash WHERE u.email = :email")
        void changePassword(@Param("email") String email, @Param("hash") String hash);

        // --- Dashboard Stats (Native Queries) ---
        @Query(value = "SELECT COUNT(*) FROM Questions", nativeQuery = true)
        int getQuestionCount();

        @Query(value = "SELECT COUNT(*) FROM Answers", nativeQuery = true)
        int getAnswerCount();

        @Query(value = "SELECT COUNT(*) FROM Questions WHERE user_id = :userId", nativeQuery = true)
        int countQuestionsByUser(@Param("userId") long userId);

        @Query(value = "SELECT t.tag_name AS tagName, COUNT(*) AS questionCount " +
                        "FROM Questions q " +
                        "JOIN Question_Tags qt ON q.question_id = qt.question_id " +
                        "JOIN Tags t ON qt.tag_id = t.tag_id " +
                        "GROUP BY t.tag_name " +
                        "ORDER BY questionCount DESC, t.tag_name ASC", nativeQuery = true)
        List<Map<String, Object>> getTopTagsByQuestionCount(Pageable pageable);

        @Query(value = "SELECT CAST(created_at AS DATE) as date, COUNT(*) as count FROM Users WHERE created_at >= DATEADD(DAY, -:days, GETDATE()) GROUP BY CAST(created_at AS DATE) ORDER BY date", nativeQuery = true)
        List<Map<String, Object>> getUserRegistrationTrend(@Param("days") int days);

        @Query(value = "SELECT CAST(created_at AS DATE) as date, COUNT(*) as count FROM Questions WHERE created_at >= DATEADD(DAY, -:days, GETDATE()) GROUP BY CAST(created_at AS DATE) ORDER BY date", nativeQuery = true)
        List<Map<String, Object>> getQuestionTrend(@Param("days") int days);

        @Query(value = "SELECT delta, reason FROM Reputation_History " +
                        "WHERE user_id = :userId " +
                        "ORDER BY created_at DESC, history_id DESC", nativeQuery = true)
        List<Map<String, Object>> getReputationChanges(@Param("userId") long userId, Pageable pageable);

        // --- User for User (Top users) ---
        @Query(value = "SELECT TOP 10 u.*, p.avatar_url, p.bio, p.location, p.website FROM Users u LEFT JOIN User_Profile p ON u.user_id = p.user_id WHERE u.role != 'admin' AND u.role != 'bot' ORDER BY u.Reputation DESC", nativeQuery = true)
        List<User> getTopUsers();

        @Query(value = "SELECT u.*, p.avatar_url, p.bio, p.location, p.website FROM Users u LEFT JOIN User_Profile p ON u.user_id = p.user_id WHERE u.role != 'admin' AND u.role != 'bot' AND (:keyword IS NULL OR :keyword = '' OR u.username LIKE CONCAT('%', :keyword, '%')) ORDER BY CASE WHEN :sort = 'date' THEN u.created_at END DESC, CASE WHEN :sort = 'reputation' THEN u.Reputation END DESC, CASE WHEN :sort NOT IN ('date', 'reputation') THEN u.username END ASC", nativeQuery = true)
        List<User> getAllUsersWithSort(@Param("keyword") String keyword, @Param("sort") String sort);

        // --- Follow System ---
        @Query(value = "SELECT COUNT(*) FROM UserFollow WHERE follower_id = :followerId AND following_id = :followingId", nativeQuery = true)
        int countFollowing(@Param("followerId") long followerId, @Param("followingId") long followingId);

        @Modifying
        @Transactional
        @Query(value = "INSERT INTO UserFollow (follower_id, following_id, followed_at) VALUES (:followerId, :followingId, GETDATE())", nativeQuery = true)
        void followUser(@Param("followerId") long followerId, @Param("followingId") long followingId);

        @Modifying
        @Transactional
        @Query(value = "DELETE FROM UserFollow WHERE follower_id = :followerId AND following_id = :followingId", nativeQuery = true)
        void unfollowUser(@Param("followerId") long followerId, @Param("followingId") long followingId);

        @Query(value = "SELECT u.*, p.avatar_url, p.bio, p.location, p.website FROM UserFollow uf JOIN Users u ON uf.following_id = u.user_id LEFT JOIN User_Profile p ON u.user_id = p.user_id WHERE uf.follower_id = :userId ORDER BY uf.followed_at DESC", nativeQuery = true)
        List<User> getFollowingListRaw(@Param("userId") long userId);

        // --- Questions by User (Mapping for QuestionDTO) ---
        @Query(value = "SELECT q.question_id as questionId, q.title as title, q.body as body, q.Score as score, q.view_count as viewCount, q.created_at as createdAt, q.is_closed as isClosed, COUNT(a.answer_id) as answerCount FROM Questions q LEFT JOIN Answers a ON q.question_id = a.question_id WHERE q.user_id = :userId GROUP BY q.question_id, q.title, q.body, q.Score, q.view_count, q.created_at, q.is_closed", countQuery = "SELECT COUNT(*) FROM Questions WHERE user_id = :userId", nativeQuery = true)
        Page<Map<String, Object>> getQuestionsByUserRaw(@Param("userId") long userId, Pageable pageable);

        @Query(value = "SELECT COUNT(*) FROM Answers WHERE user_id = :userId", nativeQuery = true)
        int countAnswersByUser(@Param("userId") long userId);

        @Query(value = "SELECT ISNULL(SUM(view_count), 0) FROM Questions WHERE user_id = :userId", nativeQuery = true)
        int countTotalViewByUser(@Param("userId") long userId);

        @Query(value = "SELECT b.name as name, b.description as description, COUNT(ub.badge_id) as badgeCount " +
                        "FROM Badges b JOIN User_Badges ub ON b.badge_id = ub.badge_id " +
                        "WHERE ub.user_id = :userId AND b.type = :type " +
                        "GROUP BY b.badge_id, b.name, b.description", nativeQuery = true)
        List<Map<String, Object>> getBadgesByUserAndType(@Param("userId") long userId, @Param("type") String type);

        // --- Activity Tab Queries ---

        @Query(value = "SELECT FORMAT(q.created_at, 'MMM yyyy') as month, COUNT(*) as count " +
                        "FROM Questions q " +
                        "WHERE q.user_id = :userId " +
                        "AND q.created_at >= DATEADD(MONTH, -6, GETDATE()) " +
                        "GROUP BY FORMAT(q.created_at, 'MMM yyyy'), YEAR(q.created_at), MONTH(q.created_at) " +
                        "ORDER BY YEAR(q.created_at) ASC, MONTH(q.created_at) ASC", nativeQuery = true)
        List<Map<String, Object>> getQuestionsActivityChart(@Param("userId") long userId);

        @Query(value = "SELECT t.tag_name AS tagName, SUM(q.Score) AS score, COUNT(DISTINCT q.question_id) AS postCount "
                        +
                        "FROM Questions q " +
                        "JOIN Question_Tags qt ON q.question_id = qt.question_id " +
                        "JOIN Tags t ON qt.tag_id = t.tag_id " +
                        "WHERE q.user_id = :userId " +
                        "GROUP BY t.tag_name " +
                        "ORDER BY score DESC", countQuery = "SELECT COUNT(DISTINCT qt.tag_id) FROM Questions q JOIN Question_Tags qt ON q.question_id = qt.question_id WHERE q.user_id = :userId", nativeQuery = true)
        Page<Map<String, Object>> getTagsActivityByUser(@Param("userId") long userId, Pageable pageable);

        @Query(value = "SELECT a.answer_id as answerId, a.is_accepted as isAccepted, a.Score as score, a.question_id as questionId, q.title as questionTitle, a.created_at as createdAt, a.body as body "
                        +
                        "FROM Answers a " +
                        "JOIN Questions q ON a.question_id = q.question_id " +
                        "WHERE a.user_id = :userId " +
                        "ORDER BY a.created_at DESC", countQuery = "SELECT COUNT(*) FROM Answers WHERE user_id = :userId", nativeQuery = true)
        Page<Map<String, Object>> getAnswersActivityByUser(@Param("userId") long userId, Pageable pageable);

        @Query(value = "SELECT c.comment_id as commentId, c.question_id as questionId, COALESCE(q.title, qa.title) as questionTitle, c.created_at as createdAt, c.body as body "
                        +
                        "FROM Comments c " +
                        "LEFT JOIN Questions q ON c.question_id = q.question_id " +
                        "LEFT JOIN Answers a ON c.answer_id = a.answer_id " +
                        "LEFT JOIN Questions qa ON a.question_id = qa.question_id " +
                        "WHERE c.user_id = :userId " +
                        "ORDER BY c.created_at DESC", countQuery = "SELECT COUNT(*) FROM Comments WHERE user_id = :userId", nativeQuery = true)
        Page<Map<String, Object>> getCommentsActivityByUser(@Param("userId") long userId, Pageable pageable);

        @Query(value = "SELECT q.Score as score, q.question_id as questionId, q.title as title, b.created_at as savedAt "
                        +
                        "FROM Bookmarks b " +
                        "JOIN Questions q ON b.question_id = q.question_id " +
                        "WHERE b.user_id = :userId " +
                        "ORDER BY b.created_at DESC", countQuery = "SELECT COUNT(*) FROM Bookmarks WHERE user_id = :userId", nativeQuery = true)
        Page<Map<String, Object>> getBookmarksActivityByUser(@Param("userId") long userId, Pageable pageable);

        @Query(value = "SELECT v.vote_type as voteType, " +
                        "CASE WHEN v.question_id IS NOT NULL THEN 'question' ELSE 'answer' END as postType, " +
                        "COALESCE(v.question_id, a.question_id) as questionId, " +
                        "COALESCE(q.title, qa.title) as title, " +
                        "v.created_at as createdAt " +
                        "FROM Votes v " +
                        "LEFT JOIN Questions q ON v.question_id = q.question_id " +
                        "LEFT JOIN Answers a ON v.answer_id = a.answer_id " +
                        "LEFT JOIN Questions qa ON a.question_id = qa.question_id " +
                        "WHERE v.user_id = :userId " +
                        "ORDER BY v.created_at DESC", countQuery = "SELECT COUNT(*) FROM Votes WHERE user_id = :userId", nativeQuery = true)
        Page<Map<String, Object>> getVotesActivityByUser(@Param("userId") long userId, Pageable pageable);

        @Query(value = "SELECT COUNT(*) FROM Users WHERE email = :email", nativeQuery = true)
        int countByEmailNative(@Param("email") String email);

        @Modifying
        @Transactional
        @Query(value = "UPDATE Users SET password_hash = :passwordHash WHERE email = :email", nativeQuery = true)
        int updatePasswordByEmail(@Param("email") String email,
                        @Param("passwordHash") String passwordHash);

        @Modifying
        @Transactional
        @Query(value = "UPDATE Users SET username = :displayName WHERE user_id = :userId", nativeQuery = true)
        int updateDisplayName(@Param("userId") long userId, @Param("displayName") String displayName);

        @Query(value = "SELECT COUNT(*) FROM User_Profile WHERE user_id = :userId", nativeQuery = true)
        int checkProfileExists(@Param("userId") long userId);

        @Modifying
        @Transactional
        @Query(value = "UPDATE User_Profile SET bio = :bio, location = :location, website = :website WHERE user_id = :userId", nativeQuery = true)
        void updateProfileInfoOnly(@Param("userId") long userId, @Param("bio") String bio,
                        @Param("location") String location, @Param("website") String website);

        @Modifying
        @Transactional
        @Query(value = "INSERT INTO User_Profile (user_id, bio, location, website) VALUES (:userId, :bio, :location, :website)", nativeQuery = true)
        void insertProfileInfoOnly(@Param("userId") long userId, @Param("bio") String bio,
                        @Param("location") String location, @Param("website") String website);

        @Modifying
        @Transactional
        @Query(value = "UPDATE User_Profile SET avatar_url = :avatarUrl WHERE user_id = :userId", nativeQuery = true)
        void updateAvatarOnly(@Param("userId") long userId, @Param("avatarUrl") String avatarUrl);

        @Modifying
        @Transactional
        @Query(value = "INSERT INTO User_Profile (user_id, avatar_url) VALUES (:userId, :avatarUrl)", nativeQuery = true)
        void insertAvatarOnly(@Param("userId") long userId, @Param("avatarUrl") String avatarUrl);

        @Modifying
        @Transactional
        @Query(value = "UPDATE Users SET Reputation = ISNULL(Reputation, 0) + :delta WHERE user_id = :userId", nativeQuery = true)
        void addReputation(@Param("userId") long userId, @Param("delta") int delta);

        @Modifying
        @Transactional
        @Query(value = "INSERT INTO Reputation_History (user_id, delta, reason, event_type, related_post_type, related_post_id, actor_user_id) " +
                "VALUES (:userId, :delta, :reason, :eventType, :postType, :postId, :actorUserId)", nativeQuery = true)
        void insertReputationHistory(@Param("userId") long userId, @Param("delta") int delta,
                @Param("reason") String reason, @Param("eventType") String eventType,
                @Param("postType") String postType, @Param("postId") Long postId,
                @Param("actorUserId") Long actorUserId);
        
        @Query(value = """
        SELECT u.user_id AS userId, u.username AS username, p.avatar_url AS avatarUrl, p.location AS location, ISNULL(u.Reputation, 0) AS reputation, p.bio AS bio, u.created_at AS createdAt,
        (SELECT COUNT(*) FROM User_Badges ub JOIN Badges b ON ub.badge_id = b.badge_id WHERE ub.user_id = u.user_id AND b.type = 'gold') AS goldBadges,
        (SELECT COUNT(*) FROM User_Badges ub JOIN Badges b ON ub.badge_id = b.badge_id WHERE ub.user_id = u.user_id AND b.type = 'silver') AS silverBadges,
        (SELECT COUNT(*) FROM User_Badges ub JOIN Badges b ON ub.badge_id = b.badge_id WHERE ub.user_id = u.user_id AND b.type = 'bronze') AS bronzeBadges,
        ISNULL((SELECT SUM(CASE WHEN v.vote_type = 'up' THEN 1 WHEN v.vote_type = 'down' THEN -1 ELSE 0 END) FROM Votes v LEFT JOIN Questions q ON v.question_id = q.question_id LEFT JOIN Answers a ON v.answer_id = a.answer_id WHERE q.user_id = u.user_id OR a.user_id = u.user_id), 0) AS voteScore
        FROM Users u LEFT JOIN User_Profile p ON u.user_id = p.user_id
        WHERE u.role != 'admin' AND u.role != 'bot' AND u.user_id <> :currentUserId AND (:keyword IS NULL OR :keyword = '' OR u.username LIKE CONCAT('%', :keyword, '%'))
        ORDER BY CASE WHEN :filter = 'reputation' THEN ISNULL(u.Reputation, 0) END DESC,
        CASE WHEN :filter = 'voted' THEN ISNULL((SELECT SUM(CASE WHEN v.vote_type = 'up' THEN 1 WHEN v.vote_type = 'down' THEN -1 ELSE 0 END) FROM Votes v LEFT JOIN Questions q ON v.question_id = q.question_id LEFT JOIN Answers a ON v.answer_id = a.answer_id WHERE q.user_id = u.user_id OR a.user_id = u.user_id), 0) END DESC,
        CASE WHEN :filter = 'new' THEN u.created_at END DESC, u.username ASC
        """, countQuery = """
        SELECT COUNT(*) FROM Users u WHERE u.role != 'admin' AND u.role != 'bot' AND u.user_id <> :currentUserId AND (:keyword IS NULL OR :keyword = '' OR u.username LIKE CONCAT('%', :keyword, '%'))
        """, nativeQuery = true)
        Page<UserPageDTO> findUsersForUserPage(@Param("keyword") String keyword, @Param("filter") String filter,@Param("currentUserId") Long currentUserId, Pageable pageable);
}
