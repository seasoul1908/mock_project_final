USE [devquery]
GO

-- Bơm các cài đặt chuẩn để SQL Server không "khó tính" khi xóa dữ liệu
SET QUOTED_IDENTIFIER ON;
SET ARITHABORT ON;
GO

PRINT '--- ĐANG DỌN DẸP TOÀN BỘ DỮ LIỆU CŨ (BẢN NÂNG CẤP) ---';

-- Bước 1: Tạm thời vô hiệu hóa tất cả các Khóa ngoại (Foreign Keys)
EXEC sp_MSForEachTable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL';
GO

-- Bước 2: Bật ép QUOTED_IDENTIFIER cho từng bảng và Xóa sạch dữ liệu
EXEC sp_MSForEachTable 'SET QUOTED_IDENTIFIER ON; DELETE FROM ?';
GO

-- Bước 3: Reset lại toàn bộ cột ID tự tăng (Identity) về 0
EXEC sp_MSForEachTable 'IF OBJECTPROPERTY(OBJECT_ID(''?''), ''TableHasIdentity'') = 1 DBCC CHECKIDENT (''?'', RESEED, 0)';
GO

-- Bước 4: Bật lại toàn bộ Khóa ngoại để bảo vệ dữ liệu mới
EXEC sp_MSForEachTable 'ALTER TABLE ? WITH CHECK CHECK CONSTRAINT ALL';
GO

PRINT '--- DỌN DẸP HOÀN TẤT. SẴN SÀNG INSERT DỮ LIỆU MỚI ---';
GO

USE [devquery]
GO

PRINT '--- INSERTING MASSIVE DEMO DATA FOR DEVQUERY ---';

-- =============================================
-- 1. USERS (10 Records)
-- =============================================
SET IDENTITY_INSERT [dbo].[Users] ON;
INSERT INTO [dbo].[Users] ([user_id], [username], [email], [password_hash], [role], [status], [Reputation], [provider])
VALUES 
(1, 'admin_hiep', 'admin@devquery.com', '$2a$10$xyzDummyHashForPassword123', 'admin', 'active', 15000, 'local'),
(2, 'tech_guru99', 'guru@example.com', '$2a$10$xyzDummyHashForPassword123', 'member', 'active', 4500, 'local'),
(3, 'code_newbie', 'newbie@example.com', '$2a$10$xyzDummyHashForPassword123', 'member', 'active', 150, 'google'),
(4, 'frontend_ninja', 'uiux@example.com', '$2a$10$xyzDummyHashForPassword123', 'member', 'active', 3200, 'github'),
(5, 'backend_dev', 'backend@example.com', '$2a$10$xyzDummyHashForPassword123', 'member', 'active', 2800, 'local'),
(6, 'devops_master', 'devops@example.com', '$2a$10$xyzDummyHashForPassword123', 'member', 'active', 5100, 'local'),
(7, 'data_geek', 'data@example.com', '$2a$10$xyzDummyHashForPassword123', 'member', 'active', 1900, 'google'),
(8, 'mobile_dev', 'mobile@example.com', '$2a$10$xyzDummyHashForPassword123', 'member', 'active', 2100, 'github'),
(9, 'cloud_architect', 'cloud@example.com', '$2a$10$xyzDummyHashForPassword123', 'member', 'active', 6200, 'local'),
(10, 'fullstack_pro', 'fullstack@example.com', '$2a$10$xyzDummyHashForPassword123', 'member', 'active', 3900, 'local');
SET IDENTITY_INSERT [dbo].[Users] OFF;
GO

-- =============================================
-- 2. USER_PROFILE (10 Records)
-- =============================================
SET IDENTITY_INSERT [dbo].[User_Profile] ON;
INSERT INTO [dbo].[User_Profile] ([profile_id], [user_id], [bio], [avatar_url], [location], [website])
VALUES 
(1, 1, 'System Administrator and Lead Developer for DevQuery.', 'assets/img/admin.png', 'Hanoi, Vietnam', 'https://devquery.com'),
(2, 2, 'Senior Backend Engineer passionate about Java.', 'assets/img/default.png', 'London, UK', 'https://techguru.dev'),
(3, 3, 'CS Student learning the ropes.', 'assets/img/default.png', 'Sydney, Australia', NULL),
(4, 4, 'React and CSS enthusiast. I make things look pretty.', 'assets/img/default.png', 'San Francisco, CA', 'https://uiux.com'),
(5, 5, 'Spring Boot and Microservices advocate.', 'assets/img/default.png', 'Berlin, Germany', NULL),
(6, 6, 'Docker, Kubernetes, and CI/CD pipelines.', 'assets/img/default.png', 'Toronto, Canada', NULL),
(7, 7, 'Python, Pandas, and Machine Learning.', 'assets/img/default.png', 'New York, NY', NULL),
(8, 8, 'Flutter and Swift developer.', 'assets/img/default.png', 'Tokyo, Japan', NULL),
(9, 9, 'AWS Certified Solutions Architect.', 'assets/img/default.png', 'Seattle, WA', NULL),
(10, 10, 'Jack of all trades, master of some.', 'assets/img/default.png', 'Austin, TX', NULL);
SET IDENTITY_INSERT [dbo].[User_Profile] OFF;
GO

-- =============================================
-- 3. USER_PREFERENCES (10 Records)
-- =============================================
INSERT INTO [dbo].[user_preferences] ([user_id], [theme], [new_editor])
VALUES 
(1, 'dark', 1), (2, 'system', 1), (3, 'light', 0), (4, 'dark', 1), (5, 'dark', 1),
(6, 'system', 1), (7, 'light', 1), (8, 'dark', 0), (9, 'system', 1), (10, 'dark', 1);
GO

-- =============================================
-- 4. TAGS (30 Records)
-- =============================================
SET IDENTITY_INSERT [dbo].[Tags] ON;
INSERT INTO [dbo].[Tags] ([tag_id], [tag_name], [description], [IsActive])
VALUES 
(1, 'java', 'Object-oriented programming language.', 1), (2, 'spring-boot', 'Java framework.', 1),
(3, 'python', 'Dynamic, interpreted language.', 1), (4, 'javascript', 'Web scripting language.', 1),
(5, 'typescript', 'Typed superset of JS.', 1), (6, 'reactjs', 'UI library by Meta.', 1),
(7, 'angular', 'Web framework by Google.', 1), (8, 'vuejs', 'Progressive JS framework.', 1),
(9, 'nodejs', 'JS runtime environment.', 1), (10, 'csharp', 'Multi-paradigm language by Microsoft.', 1),
(11, 'dotnet', 'Microsoft software framework.', 1), (12, 'sql', 'Database query language.', 1),
(13, 'mysql', 'Relational database management system.', 1), (14, 'postgresql', 'Advanced open source DB.', 1),
(15, 'mongodb', 'NoSQL document database.', 1), (16, 'docker', 'Containerization platform.', 1),
(17, 'kubernetes', 'Container orchestration.', 1), (18, 'aws', 'Amazon Web Services.', 1),
(19, 'azure', 'Microsoft Cloud.', 1), (20, 'git', 'Version control system.', 1),
(21, 'linux', 'Open-source operating system.', 1), (22, 'html', 'Markup language.', 1),
(23, 'css', 'Style sheet language.', 1), (24, 'sass', 'CSS preprocessor.', 1),
(25, 'bash', 'Unix shell.', 1), (26, 'flutter', 'UI toolkit by Google.', 1),
(27, 'swift', 'Apple programming language.', 1), (28, 'kotlin', 'Modern JVM language.', 1),
(29, 'go', 'Compiled language by Google.', 1), (30, 'rust', 'Systems programming language.', 1);
SET IDENTITY_INSERT [dbo].[Tags] OFF;
GO

-- =============================================
-- 5. QUESTIONS (30 Records)
-- =============================================
SET IDENTITY_INSERT [dbo].[Questions] ON;
INSERT INTO [dbo].[Questions] ([question_id], [user_id], [title], [body], [code_snippet], [view_count], [Score], [bounty_amount])
VALUES 
(1, 3, 'How to fix NullPointerException in Java?', 'My app keeps crashing with a NPE. How do I trace it?', 'String s = null; s.length();', 150, 5, 0),
(2, 5, 'Best way to secure REST API in Spring Boot?', 'Should I use JWT or OAuth2 for a microservice?', NULL, 320, 12, 50),
(3, 7, 'SQL GROUP BY returning duplicates', 'My query returns duplicate rows when grouping by date.', 'SELECT date, COUNT(*) FROM logs GROUP BY date;', 85, 3, 0),
(4, 4, 'Center a div using Flexbox', 'I always forget how to center a div both vertically and horizontally.', '.container { display: flex; }', 500, 25, 0),
(5, 6, 'Docker container exits immediately', 'My node app container exits with code 0 right after starting.', 'CMD ["npm", "start"]', 210, 8, 0),
(6, 8, 'Flutter state management in 2026', 'Is Provider still relevant, or should I migrate to Riverpod?', NULL, 130, 6, 0),
(7, 9, 'AWS Lambda cold start issues with Java', 'My Java Lambda takes 5 seconds to boot. How to optimize?', NULL, 400, 18, 100),
(8, 10, 'React useEffect infinite loop', 'My component re-renders endlessly when I fetch data.', 'useEffect(() => { fetchData(); }, [data]);', 600, 30, 0),
(9, 2, 'PostgreSQL index not being used', 'I created a B-tree index but EXPLAIN ANALYZE shows a sequential scan.', NULL, 110, 7, 0),
(10, 3, 'Git merge vs rebase', 'What is the practical difference between merging and rebasing a branch?', NULL, 800, 40, 0),
(11, 4, 'TypeScript interface vs type', 'When should I use interface instead of type alias?', NULL, 450, 22, 0),
(12, 5, 'Handling transactions in Microservices', 'How do you handle distributed transactions across multiple Spring Boot services?', NULL, 280, 15, 150),
(13, 6, 'Kubernetes pods stuck in Pending', 'My new deployment shows pods in Pending state forever. Resources look fine.', NULL, 190, 9, 0),
(14, 7, 'Pandas merge two dataframes on multiple columns', 'I need to join two massive CSVs based on ID and Date.', NULL, 140, 5, 0),
(15, 8, 'Swift UI update UI from background thread', 'App crashes when I try to update a label after a network request.', NULL, 95, 4, 0),
(16, 9, 'Terraform state lock error', 'Error acquiring the state lock. How do I force unlock safely?', NULL, 160, 8, 0),
(17, 10, 'Node.js memory leak detection', 'My Express app memory usage grows steadily over 24 hours.', NULL, 220, 11, 50),
(18, 2, 'Java Stream API group by and sum', 'How to group a list of objects by a property and sum another property?', NULL, 310, 16, 0),
(19, 3, 'CSS Grid vs Flexbox for main layout', 'Which is better for defining the overall page skeleton?', NULL, 420, 20, 0),
(20, 4, 'Vue 3 Composition API reactive vs ref', 'I am confused about when to use reactive() and when to use ref().', NULL, 250, 14, 0),
(21, 5, 'Spring Data JPA N+1 problem', 'Fetching a list of parents also triggers 100 queries for their children.', NULL, 380, 19, 0),
(22, 6, 'Bash script to find and delete old files', 'Need a one-liner to find files older than 30 days and rm them.', 'find . -mtime +30', 120, 6, 0),
(23, 7, 'MongoDB aggregation pipeline slow', 'My $lookup stage is taking 5 seconds to execute.', NULL, 175, 7, 0),
(24, 8, 'Kotlin Coroutines vs RxJava', 'Starting a new Android project, which async framework is preferred now?', NULL, 215, 10, 0),
(25, 9, 'Azure DevOps pipeline conditional steps', 'How to run a job only if the branch is main?', NULL, 145, 5, 0),
(26, 10, 'C# Async Await deadlock in ASP.NET', 'Calling .Result on a Task causes my web app to hang.', NULL, 290, 13, 0),
(27, 2, 'Rust borrow checker explaining', 'Cannot borrow variable as mutable more than once at a time.', NULL, 340, 17, 50),
(28, 3, 'HTML5 canvas drawing performance', 'Drawing 10,000 rectangles makes the browser lag heavily.', NULL, 90, 3, 0),
(29, 4, 'Sass variables not compiling', 'I get an undefined variable error when importing partials.', NULL, 60, 2, 0),
(30, 5, 'Golang channel blocking forever', 'My goroutine is stuck waiting to send data to a channel.', NULL, 180, 9, 0);
SET IDENTITY_INSERT [dbo].[Questions] OFF;
GO

-- =============================================
-- 6. QUESTION_TAGS (Map tags to questions)
-- =============================================
INSERT INTO [dbo].[Question_Tags] ([question_id], [tag_id]) VALUES 
(1,1), (2,1), (2,2), (3,12), (3,13), (4,23), (4,22), (5,16), (5,9), (6,26),
(7,18), (7,1), (8,6), (8,4), (9,14), (9,12), (10,20), (11,5), (11,4), (12,2),
(13,17), (13,16), (14,3), (15,27), (16,18), (17,9), (18,1), (19,23), (20,8),
(21,1), (21,2), (22,25), (22,21), (23,15), (24,28), (25,19), (26,10), (27,30),
(28,22), (29,24), (30,29);
GO

-- =============================================
-- 7. ANSWERS (30 Records)
-- =============================================
SET IDENTITY_INSERT [dbo].[Answers] ON;
INSERT INTO [dbo].[Answers] ([answer_id], [question_id], [user_id], [body], [is_accepted], [Score])
VALUES 
(1, 1, 2, 'You must initialize the object before calling its methods. Use an if-null check.', 1, 10),
(2, 2, 9, 'JWT is the industry standard for stateless microservices. Use Spring Security OAuth2 Resource Server.', 1, 15),
(3, 3, 7, 'You have hidden columns in your SELECT that are not in the GROUP BY. Ensure all non-aggregated columns are grouped.', 1, 5),
(4, 4, 4, 'Use `justify-content: center; align-items: center;` on the flex container.', 1, 30),
(5, 5, 6, 'Node exits if there is no blocking event loop. Ensure your server.listen() is actually being called.', 0, 4),
(6, 8, 4, 'You forgot to remove `data` from the dependency array, causing it to trigger a fetch, which updates `data`, looping infinitely.', 1, 45),
(7, 10, 6, 'Merge creates a new commit preserving history. Rebase rewrites history for a cleaner, linear timeline.', 1, 50),
(8, 11, 10, 'Use Interfaces for public API definitions (they support declaration merging). Use Types for unions and intersections.', 1, 25),
(9, 21, 5, 'Use `@EntityGraph` or `JOIN FETCH` in your JPQL query to load children in a single query.', 1, 22),
(10, 22, 1, '`find /path -type f -mtime +30 -exec rm {} \;` is the standard way to do this safely.', 1, 15),
-- Padding out the rest with generic good answers
(11, 7, 9, 'Increase the memory allocation for your Lambda. CPU scales with memory, which speeds up Java cold starts.', 0, 8),
(12, 9, 7, 'PostgreSQL will ignore the index if it thinks reading the whole table sequentially is faster (e.g., table is too small).', 1, 12),
(13, 13, 6, 'Check your node taints and tolerations, or ensure you have enough CPU/Memory requests available in the cluster.', 1, 14),
(14, 15, 8, 'Wrap your UI update code inside `DispatchQueue.main.async { ... }`.', 1, 9),
(15, 17, 10, 'Use Chrome DevTools to take a heap snapshot. You likely have a global array storing requests without clearing them.', 0, 6),
(16, 18, 2, 'Use `Collectors.groupingBy` combined with `Collectors.summingInt`.', 1, 18),
(17, 19, 4, 'Grid is for 2D layouts (rows and columns). Flexbox is for 1D layouts (alignment in a single row or column). Use Grid for the skeleton.', 1, 20),
(18, 20, 10, 'Use `ref` for primitives (strings, booleans) and `reactive` for objects.', 1, 11),
(19, 23, 7, 'Ensure that the foreign key field you are looking up has an index on it in the target collection.', 1, 13),
(20, 24, 8, 'Coroutines are the official recommendation by Google for Android now. RxJava is powerful but has a steeper learning curve.', 1, 17),
(21, 26, 5, 'Never use `.Result` or `.Wait()` on tasks in ASP.NET classic. Always use `await` all the way up to avoid deadlocks.', 1, 21),
(22, 27, 2, 'Rust enforces that you can have either one mutable reference OR multiple immutable references, never both at once.', 1, 24),
(23, 30, 2, 'Channels block until there is a receiver. If you don''t have a goroutine reading from it, the sender will block forever.', 1, 16),
(24, 6, 8, 'Riverpod is highly recommended over Provider now as it offers compile-time safety.', 0, 5),
(25, 12, 9, 'Look into the Saga Pattern using a framework like Eventuate or use a message broker like Kafka for eventual consistency.', 1, 19),
(26, 14, 7, 'Use `pd.merge(df1, df2, on=[''ID'', ''Date''], how=''inner'')`.', 1, 7),
(27, 16, 6, 'Use `terraform force-unlock <LOCK_ID>`. Only do this if you are 100% sure no other process is applying.', 1, 10),
(28, 25, 6, 'Use `condition: and(succeeded(), eq(variables[''Build.SourceBranch''], ''refs/heads/main''))` in your YAML.', 1, 8),
(29, 28, 4, 'Batch your draw calls. Don''t call `beginPath()` and `stroke()` for every single rectangle, do it once for all.', 1, 14),
(30, 29, 4, 'Make sure your variables file is imported BEFORE the file that tries to use those variables.', 1, 4);
SET IDENTITY_INSERT [dbo].[Answers] OFF;
GO

-- =============================================
-- 8. COMMENTS (30 Records)
-- =============================================
SET IDENTITY_INSERT [dbo].[Comments] ON;
INSERT INTO [dbo].[Comments] ([comment_id], [user_id], [question_id], [answer_id], [body])
VALUES 
(1, 1, 1, NULL, 'Could you provide the exact line number where the error occurs?'),
(2, 3, NULL, 1, 'Thanks, the null check solved it entirely!'),
(3, 5, 2, NULL, 'Are you building a public-facing API or internal only?'),
(4, 9, NULL, 2, 'OAuth2 is overkill if it is just internal microservices. Simple JWT is fine.'),
(5, 7, 3, NULL, 'Please post your table schema so we can see the exact columns.'),
(6, 4, NULL, 4, 'This is the most elegant solution. Thanks!'),
(7, 6, 5, NULL, 'What does `docker logs <container_id>` show?'),
(8, 8, 6, NULL, 'I migrated to Riverpod recently and never looked back.'),
(9, 4, 8, NULL, 'I spent 3 hours on this bug. Thank you!'),
(10, 10, 10, NULL, 'Great question, this confuses every beginner.'),
(11, 6, NULL, 7, 'Rebasing can be dangerous if the branch is already pushed to remote though.'),
(12, 5, 12, NULL, 'Saga pattern is the way to go.'),
(13, 7, 14, NULL, 'Is your dataset larger than your available RAM?'),
(14, 2, 18, NULL, 'Can you show what you have tried so far with the Stream API?'),
(15, 4, 19, NULL, 'CSS Grid changed my life for dashboard layouts.'),
(16, 5, 21, NULL, 'N+1 is the classic Hibernate trap.'),
(17, 1, NULL, 10, 'Remember to test the `find` command without `rm` first to ensure it targets the right files!'),
(18, 8, 24, NULL, 'Coroutines are definitely the future.'),
(19, 2, 27, NULL, 'The borrow checker is strict but saves you from undefined behavior.'),
(20, 4, NULL, 30, 'Ah, order of imports! Such a silly mistake on my part, thanks.'),
(21, 10, 17, NULL, 'Are you using any global variables or closures that capture large objects?'),
(22, 6, 13, NULL, 'Run `kubectl describe pod <name>` to see the exact reason it is pending.'),
(23, 7, 23, NULL, 'Lookups are inherently slow on large collections. Consider denormalization.'),
(24, 9, 16, NULL, 'Be careful with force-unlock, you can corrupt your state file.'),
(25, 2, 1, NULL, 'Also consider using Optional<String> in modern Java.'),
(26, 4, 4, NULL, 'You can also use CSS Grid: `display: grid; place-items: center;`'),
(27, 3, NULL, 6, 'I totally missed the dependency array concept. Very helpful.'),
(28, 5, 26, NULL, 'ASP.NET Core doesn''t have a SynchronizationContext, so this is mostly a legacy .NET Framework issue.'),
(29, 8, 15, NULL, 'Always remember: UI updates ONLY on the main thread in iOS.'),
(30, 9, 7, NULL, 'Consider using GraalVM native images to completely eliminate cold starts in Java.');
SET IDENTITY_INSERT [dbo].[Comments] OFF;
GO

-- =============================================
-- 9. VOTES (30 Records)
-- =============================================
SET IDENTITY_INSERT [dbo].[Votes] ON;
INSERT INTO [dbo].[Votes] ([vote_id], [user_id], [question_id], [answer_id], [vote_type])
VALUES 
(1,2,1,NULL,'up'), (2,4,1,NULL,'up'), (3,5,1,NULL,'up'), (4,6,2,NULL,'up'), (5,7,2,NULL,'up'),
(6,8,3,NULL,'down'),(7,1,4,NULL,'up'), (8,2,4,NULL,'up'), (9,3,4,NULL,'up'), (10,5,4,NULL,'up'),
(11,2,NULL,1,'up'), (12,4,NULL,1,'up'),(13,6,NULL,2,'up'),(14,7,NULL,2,'up'), (15,8,NULL,4,'up'),
(16,9,NULL,4,'up'), (17,10,NULL,4,'up'),(18,1,8,NULL,'up'),(19,2,8,NULL,'up'), (20,3,8,NULL,'up'),
(21,5,10,NULL,'up'),(22,6,10,NULL,'up'),(23,7,10,NULL,'up'),(24,8,10,NULL,'up'),(25,9,10,NULL,'up'),
(26,1,NULL,7,'up'), (27,2,NULL,7,'up'), (28,3,NULL,7,'up'), (29,4,NULL,7,'up'), (30,5,NULL,7,'up');
SET IDENTITY_INSERT [dbo].[Votes] OFF;
GO

-- =============================================
-- 10. COLLECTIONS & BOOKMARKS (30 Records)
-- =============================================
SET IDENTITY_INSERT [dbo].[Collections] ON;
INSERT INTO [dbo].[Collections] ([collection_id], [user_id], [Name])
VALUES 
(1, 2, 'Java Tips'), (2, 4, 'Frontend Tricks'), (3, 6, 'DevOps Mastery');
SET IDENTITY_INSERT [dbo].[Collections] OFF;
GO

INSERT INTO [dbo].[Bookmarks] ([user_id], [question_id], [collection_id])
VALUES 
(2,1,1), (2,18,1), (2,21,1), 
(4,4,2), (4,8,2), (4,19,2), (4,20,2), (4,28,2), (4,29,2),
(6,5,3), (6,13,3), (6,16,3), (6,22,3), (6,25,3),
(1,2,NULL), (1,7,NULL), (1,10,NULL), (1,12,NULL), (1,26,NULL),
(3,1,NULL), (3,4,NULL), (3,8,NULL), (3,10,NULL), (3,11,NULL),
(5,2,NULL), (5,9,NULL), (5,21,NULL), (7,3,NULL), (7,14,NULL), (7,23,NULL);
GO

-- =============================================
-- 11. BADGES & PRIVILEGES
-- =============================================
SET IDENTITY_INSERT [dbo].[Badges] ON;
INSERT INTO [dbo].[Badges] ([badge_id], [name], [type], [description], [required_reputation])
VALUES 
(1, 'Problem Solver', 'gold', 'Provided an accepted answer scoring 10+.', 0),
(2, 'Curious Mind', 'bronze', 'Asked a well-received question.', 0),
(3, 'Community Builder', 'silver', 'Actively participated in voting.', 0),
(4, 'Great Answer', 'gold', 'Answer score of 20 or more.', 0),
(5, 'Popular Question', 'silver', 'Question viewed over 500 times.', 0);
SET IDENTITY_INSERT [dbo].[Badges] OFF;
GO

INSERT INTO [dbo].[User_Badges] ([user_id], [badge_id]) VALUES 
(1,3), (2,1), (3,2), (4,4), (4,5), (6,1), (10,4), (10,5), (5,1), (4,1);
GO

SET IDENTITY_INSERT [dbo].[Privileges] ON;
INSERT INTO [dbo].[Privileges] ([privilege_id], [name], [description], [required_reputation])
VALUES 
(1, 'Vote Up', 'Ability to upvote questions and answers.', 15),
(2, 'Leave Comments', 'Ability to leave comments on posts.', 50),
(3, 'Vote Down', 'Ability to downvote posts.', 125),
(4, 'Create Tags', 'Ability to create new tags.', 1500),
(5, 'Edit Posts', 'Ability to edit other users'' posts.', 2000),
(6, 'Close Questions', 'Ability to vote to close questions.', 3000),
(7, 'Moderator Tools', 'Access to site moderation tools.', 10000);
SET IDENTITY_INSERT [dbo].[Privileges] OFF;
GO

PRINT '--- DEMO DATA INSERTION COMPLETELY FINISHED ---';
GO

USE [devquery]
GO

PRINT '--- BỔ SUNG DATA CHO 12 BẢNG CÒN LẠI ---';

-- =============================================
-- 15. ANSWER_BOOKMARKS (Lưu câu trả lời hay)
-- =============================================
INSERT INTO [dbo].[Answer_Bookmarks] ([user_id], [answer_id])
VALUES 
(2, 4), (2, 7), (2, 21),
(4, 1), (4, 17),
(6, 2), (6, 13),
(7, 26), (8, 20), (10, 8);
GO

-- =============================================
-- 16. QUESTION_VIEWS (Lượt xem)
-- =============================================
INSERT INTO [dbo].[Question_Views] ([question_id], [viewer_ip], [user_id])
VALUES 
(1, '192.168.1.5', 2), (1, '10.0.0.12', NULL), (1, '172.16.0.4', 4),
(2, '192.168.1.9', 9), (2, '10.0.0.45', NULL),
(4, '172.16.0.8', 8), (4, '192.168.1.15', 5),
(10, '10.0.0.99', 6), (10, '172.16.0.22', NULL), (10, '192.168.1.50', 3);
GO

-- =============================================
-- 17. REPUTATION_HISTORY (Lịch sử cày điểm)
-- =============================================
INSERT INTO [dbo].[Reputation_History] ([user_id], [delta], [reason], [event_type], [related_post_type], [related_post_id], [actor_user_id])
VALUES 
(2, 10, 'Answer upvoted', 'upvote', 'answer', 1, 4),
(2, 15, 'Answer accepted', 'accept', 'answer', 1, 3),
(9, 10, 'Answer upvoted', 'upvote', 'answer', 2, 6),
(4, 5, 'Question upvoted', 'upvote', 'question', 4, 1),
(6, -2, 'Question downvoted', 'downvote', 'question', 5, 8);
GO

-- =============================================
-- 18. SYSTEM_RULES (Nội quy diễn đàn)
-- =============================================
INSERT INTO [dbo].[System_Rules] ([title], [content], [created_by])
VALUES 
('Be Nice and Respectful', 'DevQuery is a professional community. Harassment, hate speech, and spam will result in immediate bans.', 1),
('No Plagiarism', 'Always credit original sources when copying code snippets. Do not claim others work as your own.', 1),
('Keep Questions Clear', 'Include code, error logs, and what you have tried. Help us help you.', 1);
GO

-- =============================================
-- 19. NOTIFICATIONS (Thông báo cho User)
-- =============================================
INSERT INTO [dbo].[Notifications] ([user_id], [type], [content], [is_read])
VALUES 
(3, 'answer', 'User tech_guru99 answered your question: How to fix NullPointerException in Java?', 0),
(5, 'bounty', 'Your bounty on question #2 is expiring soon.', 0),
(2, 'badge', 'Congratulations! You earned the gold badge: Problem Solver.', 1),
(4, 'mention', 'User admin_hiep mentioned you in a comment.', 0);
GO

-- =============================================
-- 20. REPORTS (Báo cáo vi phạm)
-- =============================================
INSERT INTO [dbo].[Reports] ([reporter_id], [target_type], [target_id], [reason], [note], [status])
VALUES 
(3, 'answer', 5, 'Not an answer', 'This is just another question posted as an answer.', 'open'),
(6, 'question', 28, 'Spam or irrelevant', 'This looks like AI generated spam text.', 'resolved'),
(9, 'answer', 15, 'Rude or abusive', 'The author is insulting other users in the comments.', 'open');
GO

-- =============================================
-- 21. MODERATOR_ACTIONS (Nhật ký xử lý của Admin)
-- =============================================
INSERT INTO [dbo].[Moderator_Actions] ([moderator_id], [action_type], [target_type], [target_id], [description])
VALUES 
(1, 'close_question', 'question', 28, 'Closed due to spam reports.'),
(1, 'delete_comment', 'comment', 15, 'Removed toxic comment.'),
(1, 'edit_tags', 'question', 10, 'Added the git tag for better visibility.');
GO

-- =============================================
-- 22. TAGFOLLOW (User theo dõi Tag)
-- =============================================
INSERT INTO [dbo].[TagFollow] ([user_id], [tag_id])
VALUES 
(2, 1), (2, 2), (2, 12), -- tech_guru99 theo dõi Java, Spring Boot, SQL
(4, 4), (4, 6), (4, 23), -- frontend_ninja theo dõi JS, React, CSS
(6, 16), (6, 17), (6, 21), -- devops_master theo dõi Docker, K8s, Linux
(7, 3), (7, 13); -- data_geek theo dõi Python, MySQL
GO

-- =============================================
-- 23. USERFOLLOW (User theo dõi nhau)
-- =============================================
INSERT INTO [dbo].[UserFollow] ([follower_id], [following_id])
VALUES 
(3, 2), -- newbie follows tech_guru
(4, 1), -- frontend_ninja follows admin
(5, 6), -- backend follows devops
(8, 4), -- mobile follows frontend
(10, 2), (10, 9); -- fullstack follows guru and cloud
GO

-- =============================================
-- 24. BLOGS (Bài viết Blog)
-- =============================================
SET IDENTITY_INSERT [dbo].[Blogs] ON;
INSERT INTO [dbo].[Blogs] ([blog_id], [title], [content], [thumbnail_url], [author_id], [view_count], [comment_count])
VALUES 
(1, 'The Future of Java in 2026', 'Java 25 is bringing massive improvements to Project Loom and Valhalla. Let us deep dive into what this means for backend developers...', 'assets/img/blog-java.jpg', 2, 1500, 2),
(2, 'Mastering CSS Grid Layouts', 'CSS Grid is the most powerful layout system available in CSS. It is a 2-dimensional system, meaning it can handle both columns and rows...', 'assets/img/blog-css.jpg', 4, 3200, 1),
(3, 'Why we migrated to Kubernetes', 'Moving from traditional VMs to Kubernetes was a huge undertaking for our startup. Here are the lessons we learned...', 'assets/img/blog-k8s.jpg', 6, 850, 0);
SET IDENTITY_INSERT [dbo].[Blogs] OFF;
GO

-- =============================================
-- 25. BLOGCOMMENTS (Bình luận trong Blog)
-- =============================================
SET IDENTITY_INSERT [dbo].[BlogComments] ON;
INSERT INTO [dbo].[BlogComments] ([comment_id], [blog_id], [user_id], [parent_id], [content])
VALUES 
(1, 1, 5, NULL, 'Great article! Project Loom completely changed how we handle threading.'),
(2, 1, 2, 1, 'Absolutely. The throughput gains are insane without having to write reactive code.'),
(3, 2, 8, NULL, 'Do you prefer Grid over Flexbox for mobile navbars?');
SET IDENTITY_INSERT [dbo].[BlogComments] OFF;
GO

-- =============================================
-- 26. POST_EDIT_HISTORY (Lịch sử chỉnh sửa bài viết)
-- =============================================
INSERT INTO [dbo].[Post_Edit_History] ([post_type], [post_id], [title], [body], [editor_id])
VALUES 
('question', 1, 'How to fix NullPointerException in Java?', 'My app keeps crashing with a NPE. How do I trace it? I added some code below.', 3),
('answer', 4, NULL, 'Use `justify-content: center; align-items: center;` on the flex container. Make sure the container has a defined height like 100vh.', 4),
('question', 10, 'Git merge vs rebase (Practical differences)', 'What is the practical difference between merging and rebasing a branch? Which one is safer?', 1);
GO

PRINT '--- HOÀN TẤT THÊM DATA CHO 12 BẢNG ---';
GO

select * from Users