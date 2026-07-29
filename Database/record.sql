USE [devquery]
GO

-- =============================================
-- 1. TẠO DỮ LIỆU USERS (~15 users)
-- Password: "123456789" (SHA256 hash)
-- =============================================
INSERT INTO [dbo].[Users] ([username], [email], [password_hash], [role], [Reputation]) VALUES
('admin_master', 'admin@devquery.com', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'admin', 1000),
('hoang_coder', 'hoang@gmail.com', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'member', 150),
('thuy_java', 'thuy.nguyen@fpt.vn', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'moderator', 500),
('tuan_sql', 'tuanit@yahoo.com', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'member', 45),
('david_lee', 'david.lee@us.com', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'member', 10),
('alice_wonder', 'alice@gmail.com', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'member', 230),
('bob_builder', 'bob.code@outlook.com', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'member', 5),
('charlie_react', 'charlie@fe.com', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'member', 80),
('nam_spring', 'nam.java@viettel.com', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'member', 300),
('lan_tester', 'lan.qa@cmc.vn', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'member', 20),
('hung_mobile', 'hung.android@samsung.com', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'member', 110),
('mai_design', 'mai.uiux@vng.com.vn', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'member', 60),
('newbie_coder', 'student1@fpt.edu.vn', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'member', 0),
('pro_backend', 'senior@nashtech.com', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'member', 890),
('bot_auto', 'bot@devquery.system', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c979c0fde7dae', 'member', 0);
GO

-- =============================================
-- 2. TẠO USER PROFILE (15 profiles)
-- =============================================
INSERT INTO [dbo].[User_Profile] ([user_id], [location], [bio], [website]) VALUES
(1, N'Hà Nội', N'Administrator of System', NULL),
(2, N'TP.HCM', N'Fullstack Developer yêu thích Spring Boot', NULL),
(3, N'Đà Nẵng', N'Senior Java Developer', NULL),
(4, N'Hà Nội', N'Data Analyst & SQL Expert', NULL),
(5, N'New York', N'Freelancer', NULL),
(6, N'California', N'ReactJS Enthusiast', NULL),
(7, N'London', N'Learning to code', NULL),
(8, N'Singapore', N'Frontend Ninja', NULL),
(9, N'Hà Nội', N'Backend Architect', NULL),
(10, N'TP.HCM', N'Automation Tester', NULL),
(11, N'Bắc Ninh', N'Android & Kotlin', NULL),
(12, N'Hà Nội', N'Thích màu hồng và code HTML', NULL),
(13, N'Cần Thơ', N'Sinh viên năm 2', NULL),
(14, N'Remote', N'10 năm kinh nghiệm System Design', NULL),
(15, N'Server', N'I am a robot', NULL);
GO

-- =============================================
-- 3. TẠO TAGS (~12 tags)
-- =============================================
INSERT INTO [dbo].[Tags] ([tag_name], [description]) VALUES
('java', N'Java là một ngôn ngữ lập trình hướng đối tượng, đa nền tảng phổ biến nhất thế giới. Nó tuân theo nguyên tắc "viết một lần, chạy mọi nơi" (WORA). Java được sử dụng rộng rãi trong phát triển ứng dụng doanh nghiệp, web backend và ứng dụng di động.'),
('spring-boot', N'Spring Boot là framework giúp xây dựng các ứng dụng Java (dựa trên Spring) một cách nhanh chóng. Nó loại bỏ các cấu hình XML phức tạp, cung cấp các server nhúng như Tomcat để bạn có thể chạy ứng dụng ngay lập tức.'),
('sql-server', N'SQL Server là hệ quản trị cơ sở dữ liệu quan hệ (RDBMS) do Microsoft phát triển. Nó hỗ trợ xử lý giao dịch, ứng dụng phân tích kinh doanh (BI) và có tính bảo mật, hiệu năng cực kỳ cao.'),
('reactjs', N'React là một thư viện JavaScript mã nguồn mở dùng để xây dựng giao diện người dùng (UI). Được duy trì bởi Meta (Facebook), nó dựa trên kiến trúc component giúp tái sử dụng mã hiệu quả thông qua Virtual DOM.'),
('javascript', N'JavaScript (JS) là ngôn ngữ lập trình kịch bản chủ yếu được sử dụng để tạo ra các trang web tương tác. Cùng với HTML và CSS, nó là một trong ba công nghệ cốt lõi của Web. Hiện nay JS có thể chạy ở cả phía server thông qua Node.js.'),
('html-css', N'HTML cấu trúc nội dung trang web, còn CSS định dạng kiểu dáng cho nội dung đó. Đây là hai công nghệ nền tảng không thể thiếu để xây dựng bất kỳ giao diện web tĩnh hay động nào trên trình duyệt.'),
('python', N'Python là ngôn ngữ lập trình bậc cao, đa mục đích với cú pháp cực kỳ dễ đọc. Nó đang thống trị trong các lĩnh vực Trí tuệ nhân tạo (AI), Khoa học dữ liệu (Data Science) và kịch bản tự động hóa.'),
('docker', N'Docker là nền tảng phần mềm cho phép xây dựng, kiểm thử và triển khai ứng dụng dưới dạng các container. Các container này chứa sẵn mọi thư viện cần thiết, đảm bảo code chạy đồng nhất trên mọi môi trường.'),
('android', N'Android là hệ điều hành mã nguồn mở dựa trên nhân Linux, được thiết kế cho các thiết bị di động màn hình cảm ứng. Nó cung cấp một framework mạnh mẽ để các lập trình viên Java và Kotlin xây dựng ứng dụng.'),
('git', N'Git là hệ thống quản lý phiên bản phân tán (VCS) phổ biến nhất hiện nay. Nó giúp các nhóm lập trình viên theo dõi sự thay đổi của mã nguồn, rẽ nhánh (branch) và hợp nhất (merge) code một cách an toàn.'),
('algorithm', N'Thuật toán (Algorithm) là một tập hợp các hướng dẫn từng bước để giải quyết một bài toán cụ thể. Việc nắm vững thuật toán và cấu trúc dữ liệu là cốt lõi để viết ra code tối ưu về cả bộ nhớ lẫn thời gian chạy.'),
('c#', N'C# là một ngôn ngữ lập trình hướng đối tượng mạnh mẽ do Microsoft phát triển trong hệ sinh thái .NET. Nó có cú pháp khá giống Java và được ứng dụng mạnh trong việc làm game (Unity), ứng dụng Windows và web.');
GO

-- =============================================
-- 4. TẠO BADGES (5 badges)
-- =============================================
INSERT INTO [dbo].[Badges] ([name], [type], [description]) VALUES
('First Question', 'bronze', N'Đặt câu hỏi đầu tiên.'),
('Good Answer', 'silver', N'Câu trả lời đạt 10 vote.'),
('Famous Question', 'gold', N'Câu hỏi có 1000 lượt xem.'),
('Bug Hunter', 'silver', N'Tìm ra lỗi bảo mật.'),
('Helper', 'bronze', N'Trả lời 5 câu hỏi.');
GO

-- =============================================
-- 5. TẠO CÂU HỎI - QUESTIONS (~20 câu hỏi)
-- =============================================
INSERT INTO [dbo].[Questions] ([user_id], [title], [body], [code_snippet], [view_count], [Score]) VALUES
(13, N'Lỗi NullPointerException trong Java là gì?', N'Mình mới học Java và hay gặp lỗi này khi chạy chương trình. Ai giải thích giúp mình với?', N'String s = null; System.out.println(s.length());', 105, 5),
(2, N'Làm sao để kết nối SQL Server với Spring Boot?', N'Mình cấu hình file application.properties nhưng vẫn báo lỗi connection refused.', N'spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=test', 340, 12),
(6, N'React useEffect chạy vô hạn loop', N'Tại sao component của mình bị render lại liên tục?', N'useEffect(() => { setCount(count + 1); });', 560, 8),
(12, N'Canh giữa div trong CSS', N'Cách nào nhanh nhất để căn giữa 1 thẻ div trong thẻ cha?', N'.parent { ? }', 1200, 25),
(4, N'Sự khác nhau giữa INNER JOIN và LEFT JOIN', N'Mình vẫn chưa phân biệt rõ 2 cái này khi query dữ liệu.', NULL, 89, 3),
(7, N'Hỏi về lộ trình học Frontend năm 2026', N'Nên học React hay Vue hay Angular ạ?', NULL, 450, 10),
(11, N'Android Studio bị chậm trên máy RAM 8GB', N'Có cách nào tối ưu IDE không mọi người?', NULL, 210, 2),
(9, N'Microservices pattern saga là gì?', N'Mình đang tìm hiểu về distributed transaction.', NULL, 67, 15),
(2, N'Cách fix lỗi CORS khi gọi API', N'Browser chặn request từ frontend gọi sang backend khác port.', NULL, 800, 20),
(13, N'Biến static trong Java dùng để làm gì?', N'Tại sao hàm main phải là static?', NULL, 90, 1),
(5, N'Python list comprehension example', N'How to filter even numbers?', N'[x for x in range(10)]', 150, 6),
(3, N'Interface vs Abstract Class', N'Khi nào nên dùng cái nào trong thiết kế hệ thống?', NULL, 300, 18),
(10, N'Selenium không tìm thấy element', N'Mình dùng xpath nhưng lúc chạy được lúc không.', NULL, 120, 4),
(14, N'Tối ưu câu truy vấn SQL 1 triệu bản ghi', N'Dùng index như thế nào cho hiệu quả?', N'SELECT * FROM LargeTable WHERE status = 1', 900, 30),
(8, N'Redux Toolkit vs Context API', N'Dự án nhỏ thì nên dùng cái nào?', NULL, 400, 9),
(13, N'Làm sao push code lên Github?', N'Mình bị lỗi conflict khi pull về.', NULL, 50, 0),
(2, N'Deploy Spring Boot lên Docker', N'Xin file Dockerfile mẫu ạ.', NULL, 230, 7),
(4, N'Stored Procedure trong SQL', N'Có nên dùng logic trong DB không?', NULL, 180, 5),
(6, N'NextJS 14 Server Actions', N'Tính năng này có thay thế được API Route không?', NULL, 350, 11),
(9, N'Kafka vs RabbitMQ', N'So sánh ưu nhược điểm của 2 message queue này.', NULL, 500, 22);
GO

-- =============================================
-- 6. GẮN TAG CHO CÂU HỎI - QUESTION_TAGS (~30 bản ghi)
-- =============================================
INSERT INTO [dbo].[Question_Tags] ([question_id], [tag_id]) VALUES
(1, 1), (2, 2), (2, 3), (3, 4), (3, 5),
(4, 6), (5, 3), (6, 4), (6, 5), (6, 6),
(7, 9), (7, 1), (8, 1), (8, 2), (9, 4), (9, 2),
(10, 1), (11, 7), (12, 1), (13, 1),
(14, 3), (15, 4), (16, 10), (17, 2), (17, 8),
(18, 3), (19, 4), (19, 5), (20, 1);
GO

-- =============================================
-- 7. TẠO CÂU TRẢ LỜI - ANSWERS (~30 câu trả lời)
-- =============================================
INSERT INTO [dbo].[Answers] ([question_id], [user_id], [body], [code_snippet], [is_accepted], [Score]) VALUES
(1, 3, N'Bạn đang gọi phương thức trên một đối tượng null. Hãy kiểm tra s != null trước.', N'if (s != null) { ... }', 1, 10),
(1, 14, N'Đây là lỗi phổ biến, bạn nên dùng Optional trong Java 8 trở lên.', NULL, 0, 2),
(2, 9, N'Bạn kiểm tra xem SQL Server đã bật TCP/IP trong Configuration Manager chưa nhé.', NULL, 1, 15),
(3, 8, N'Bạn cần thêm dependency array vào useEffect.', N'useEffect(() => { ... }, []); // Thêm mảng rỗng', 1, 20),
(4, 12, N'Dùng Flexbox là nhanh nhất.', N'display: flex; justify-content: center; align-items: center;', 1, 50),
(4, 7, N'Dùng margin: 0 auto; nếu có width.', NULL, 0, 5),
(5, 4, N'INNER JOIN chỉ lấy phần chung, LEFT JOIN lấy hết bảng bên trái.', NULL, 1, 8),
(9, 14, N'Cấu hình @CrossOrigin ở Controller Spring Boot hoặc cài Proxy ở React.', NULL, 1, 12),
(14, 1, N'Đánh Index vào cột status, và tránh Select *.', NULL, 1, 25),
(16, 2, N'Dùng git stash để lưu code tạm, rồi pull, sau đó git stash pop.', NULL, 0, 3),
(16, 3, N'Giải quyết conflict bằng tay trong VS Code rồi commit lại.', NULL, 1, 4),
(10, 3, N'Static thuộc về lớp chứ không thuộc về đối tượng (instance).', NULL, 1, 6),
(12, 14, N'Interface cho hành động (Can do), Abstract cho bản chất (Is a).', NULL, 1, 15),
(7, 11, N'Nâng RAM lên 16GB đi bạn, 8GB không đủ đâu :D', NULL, 0, 10),
(17, 9, N'Tạo file Dockerfile như sau:', N'FROM openjdk:17-jdk-alpine\nCOPY target/*.jar app.jar\nENTRYPOINT ["java","-jar","/app.jar"]', 1, 9),
(6, 8, N'React vẫn đang là vua, job nhiều, nên học React trước.', NULL, 0, 12),
(8, 9, N'Saga pattern dùng để quản lý transaction qua nhiều service bằng chuỗi các local transaction.', NULL, 1, 7),
(20, 14, N'Kafka cho throughput cao (streaming), RabbitMQ cho complex routing.', NULL, 1, 11),
(19, 6, N'Server Actions rất mạnh, nhưng API Route vẫn cần cho Webhook hoặc Mobile app gọi vào.', NULL, 0, 5),
(11, 2, N'Dùng list comprehension nhanh hơn vòng lặp for thông thường.', NULL, 1, 3);
GO

-- =============================================
-- 8. TẠO COMMENTS (~25 comments)
-- =============================================
INSERT INTO [dbo].[Comments] ([user_id], [question_id], [answer_id], [body]) VALUES
(13, 1, 1, N'Cảm ơn bạn, mình sửa được rồi!'),
(2, 2, NULL, N'Bạn chụp ảnh lỗi lên được không?'),
(3, 2, 3, N'Chuẩn luôn, mình hay quên bật cái TCP/IP này.'),
(12, 4, 5, N'Cách này giờ là standard rồi.'),
(7, 4, NULL, N'CSS khó quá :('),
(5, 7, NULL, N'Mua máy mới là giải pháp tốt nhất :))'),
(13, 16, NULL, N'Mình mới học Git, sợ mất code quá.'),
(14, 14, 9, N'Select * hại performance lắm.'),
(8, 6, NULL, N'Học chắc JS trước khi học Framework nhé.'),
(11, 7, 14, N'Đúng là Android Studio ngốn RAM kinh khủng.'),
(1, 1, NULL, N'Nhớ format code khi đăng bài nhé bạn.'),
(9, 20, 18, N'Bài so sánh rất chi tiết.'),
(6, 3, 4, N'Quên cái dependency array là treo trình duyệt luôn.'),
(10, 13, NULL, N'Mình dùng Java + Selenium cũng hay bị.'),
(4, 5, 7, N'Giải thích ngắn gọn dễ hiểu.'),
(13, 10, NULL, N'Thầy mình bắt dùng mà chưa hiểu lắm.'),
(2, 17, NULL, N'Docker hay nhưng hơi khó cấu hình ban đầu.'),
(3, 12, 13, N'Vote +1 cho câu trả lời chất lượng.'),
(14, 9, NULL, N'CORS là nỗi đau của mọi dev frontend.'),
(5, 11, 20, N'Python cú pháp gọn thật.');
GO

-- =============================================
-- 9. TẠO VOTES (~30 votes)
-- =============================================
-- Giả lập vote cho câu hỏi và câu trả lời
INSERT INTO [dbo].[Votes] ([user_id], [question_id], [answer_id], [vote_type]) VALUES
(2, 1, NULL, 'up'), (3, 1, NULL, 'up'), (4, 1, 1, 'up'), (5, 1, 2, 'down'),
(6, 2, NULL, 'up'), (7, 2, 3, 'up'), (1, 2, 3, 'up'),
(8, 3, NULL, 'up'), (9, 3, 4, 'up'), (10, 3, 4, 'up'),
(11, 4, NULL, 'up'), (14, 4, 5, 'up'), (1, 4, 5, 'up'), (2, 4, 5, 'up'),
(3, 14, NULL, 'up'), (4, 14, 9, 'up'), (5, 14, 9, 'up'),
(6, 16, NULL, 'down'), (7, 16, 11, 'up'),
(8, 17, NULL, 'up'), (9, 17, 15, 'up'),
(10, 20, NULL, 'up'), (11, 20, 18, 'up'),
(12, 12, NULL, 'up'), (13, 12, 13, 'up'),
(14, 9, NULL, 'up'), (1, 9, 8, 'up'),
(2, 6, NULL, 'up'), (3, 6, 16, 'up'),
(4, 7, NULL, 'down'), (5, 7, 14, 'up');
GO

-- =============================================
-- 10. TẠO BỘ SƯU TẬP & BOOKMARKS
-- =============================================
INSERT INTO [dbo].[Collections] ([user_id], [Name]) VALUES
(2, N'Lỗi Java thường gặp'),
(6, N'React Best Practices'),
(14, N'System Design Interview');

INSERT INTO [dbo].[Bookmarks] ([user_id], [question_id], [collection_id]) VALUES
(2, 1, 1),
(6, 3, 2),
(14, 20, 3),
(14, 8, 3),
(9, 14, NULL),
(13, 16, NULL),
(3, 2, NULL),
(8, 6, NULL);
GO

-- =============================================
-- 11. CẤP BADGE CHO USER (USER_BADGES)
-- =============================================
INSERT INTO [dbo].[User_Badges] ([user_id], [badge_id]) VALUES
(13, 1), 
(12, 2),
(14, 2),
(4, 1),
(14, 5), 
(3, 5);
GO

-- =============================================
-- 12. SYSTEM RULES & NOTIFICATIONS
-- =============================================
INSERT INTO [dbo].[System_Rules] ([title], [content], [created_by]) VALUES
(N'Quy định đặt câu hỏi', N'Câu hỏi phải rõ ràng, có code minh họa, không spam.', 1),
(N'Văn hóa ứng xử', N'Tôn trọng người khác, không chửi bới, toxic.', 1);

INSERT INTO [dbo].[Notifications] ([user_id], [type], [content], [is_read]) VALUES
(13, 'answer', N'Thuy_java đã trả lời câu hỏi của bạn.', 0),
(2, 'comment', N'Có bình luận mới trong bài viết SQL của bạn.', 1),
(12, 'badge', N'Chúc mừng! Bạn nhận được huy hiệu Good Answer.', 0),
(14, 'system', N'Bảo trì hệ thống vào 12h đêm nay.', 0);
GO

-- Cập nhật mốc điểm cho Huy hiệu Đồng
UPDATE [dbo].[Badges] SET [required_reputation] = 15 WHERE [badge_id] = 1;
UPDATE [dbo].[Badges] SET [required_reputation] = 30 WHERE [badge_id] = 6;
UPDATE [dbo].[Badges] SET [required_reputation] = 100 WHERE [badge_id] = 5;

-- Cập nhật mốc điểm cho Huy hiệu Bạc
UPDATE [dbo].[Badges] SET [required_reputation] = 250 WHERE [badge_id] = 7;
UPDATE [dbo].[Badges] SET [required_reputation] = 500 WHERE [badge_id] = 2;
UPDATE [dbo].[Badges] SET [required_reputation] = 1000 WHERE [badge_id] = 4;

-- Cập nhật mốc điểm cho Huy hiệu Vàng
UPDATE [dbo].[Badges] SET [required_reputation] = 2500 WHERE [badge_id] = 3;
GO

select * from Badges

INSERT INTO [dbo].[Privileges] ([name], [description], [required_reputation]) VALUES
('Create posts', 'Ask a question or contribute an answer.', 1),
('Vote up', 'Indicate when questions and answers are useful.', 15),
('Comment', 'Leave comments on other people''s posts.', 50),
('Vote down', 'Indicate when questions and answers are not useful.', 125),
('Create tags', 'Add new tags to the system.', 250),
('Edit posts', 'Edit other people''s questions and answers.', 500),
('Moderator', 'Access moderation tools and delete posts.', 2000);
GO

-- ========================================================
-- BƠM DỮ LIỆU ẢO CHO TÀI KHOẢN CỦA BẠN (USER_ID = 16)
-- ========================================================

-- Tạo Profile cho tài khoản số 16 (Tránh lỗi thiếu thông tin trang cá nhân)
INSERT INTO [dbo].[User_Profile] ([user_id], [location], [bio], [website]) 
VALUES (16, N'Hà Nội', N'Tài khoản đăng nhập bằng Google', 'https://github.com/MaiThanh_1282');
GO

-- 1. Cập nhật điểm uy tín (Ví dụ: 1550 điểm)
UPDATE [dbo].[Users] 
SET [Reputation] = 1550 
WHERE [user_id] = 16;

-- 2. Bơm 2 Câu hỏi do chính bạn (ID = 16) đặt ra
INSERT INTO [dbo].[Questions] ([user_id], [title], [body], [view_count], [Score]) 
VALUES 
(16, N'Làm sao để code trang Profile chuẩn MVC?', N'Mình đang làm UI cho trang Profile của DevQuery mà chưa biết thiết kế DAO sao cho chuẩn.', 1250, 15),
(16, N'Lỗi gạch đỏ chữ Connection trong Java', N'Mọi người cho mình hỏi fix lỗi này như thế nào với?', 340, 5);

-- 3. Bơm 3 Câu trả lời do bạn (ID = 16) đi giải đáp cho người khác
INSERT INTO [dbo].[Answers] ([question_id], [user_id], [body], [is_accepted], [Score]) 
VALUES 
(1, 16, N'Lỗi NullPointerException này là do biến chưa được khởi tạo. Bạn check lại kỹ nhé.', 1, 10),
(2, 16, N'Bạn thử dùng JDBC chuẩn bằng hàm getConnection() xem sao.', 0, 2),
(3, 16, N'Lỗi vô hạn loop này thường do quên truyền dependency array vào useEffect trong React.', 1, 25);
Select * from Users
-- 4. Cấp phát 5 Danh hiệu (Badges) cho bạn
INSERT INTO [dbo].[User_Badges] ([user_id], [badge_id]) 
VALUES 
(16, 3), -- Tặng 1 huy hiệu Vàng (Famous Question)
(16, 2), -- Tặng 1 huy hiệu Bạc (Good Answer)
(16, 4), -- Tặng 1 huy hiệu Bạc (Bug Hunter)
(16, 1), -- Tặng 1 huy hiệu Đồng (First Question)
(16, 5); -- Tặng 1 huy hiệu Đồng (Helper)
GO

INSERT INTO [dbo].[Questions] ([user_id], [title], [body], [created_at], [Score]) 
VALUES (16, N'Java OOP là gì?', N'Nội dung test', '2025-11-10 10:00:00', 20);
DECLARE @Q1 BIGINT = SCOPE_IDENTITY();

INSERT INTO [dbo].[Questions] ([user_id], [title], [body], [created_at], [Score]) 
VALUES (16, N'Hỏi về React Hook', N'Nội dung test', '2026-01-05 09:00:00', 30);
DECLARE @Q2 BIGINT = SCOPE_IDENTITY();

INSERT INTO [dbo].[Answers] ([question_id], [user_id], [body], [created_at], [Score]) 
VALUES (1, 16, N'Test Answer tháng 12', '2025-12-15 14:00:00', 15);

INSERT INTO [dbo].[Answers] ([question_id], [user_id], [body], [created_at], [Score]) 
VALUES (2, 16, N'Test Answer tháng 2', '2026-02-20 16:00:00', 5);

-- B. Gắn Tags cho các câu hỏi của User 16
INSERT INTO [dbo].[Question_Tags] ([question_id], [tag_id]) VALUES 
(@Q1, 1), (@Q2, 4), (@Q2, 5);

INSERT INTO [dbo].[Question_Tags] ([question_id], [tag_id])
SELECT question_id, 1 FROM [dbo].[Questions] WHERE user_id = 16 AND title LIKE N'%Java%';

INSERT INTO [dbo].[Question_Tags] ([question_id], [tag_id])
SELECT question_id, 2 FROM [dbo].[Questions] WHERE user_id = 16 AND title LIKE N'%MVC%'; 
GO

-- ========================================================
-- Tạo 15 bản ghi cho mỗi tab
-- ========================================================
DECLARE @Counter INT = 1;
DECLARE @NewQuestionID BIGINT;
DECLARE @NewAnswerID BIGINT;

WHILE @Counter <= 15
BEGIN
    INSERT INTO [dbo].[Questions] ([user_id], [title], [body], [created_at], [Score], [view_count])
    VALUES (16, CONCAT(N'[Test Phân Trang] Câu hỏi số ', @Counter), N'Nội dung để test UI phân trang...', DATEADD(DAY, -@Counter, GETDATE()), @Counter * 2, @Counter * 10);
    
    SET @NewQuestionID = SCOPE_IDENTITY();

    INSERT INTO [dbo].[Answers] ([question_id], [user_id], [body], [created_at], [Score], [is_accepted])
    VALUES (@NewQuestionID, 16, CONCAT(N'[Test Phân Trang] Câu trả lời số ', @Counter), DATEADD(HOUR, -@Counter, GETDATE()), @Counter, @Counter % 2);
    
    SET @NewAnswerID = SCOPE_IDENTITY();

    INSERT INTO [dbo].[Bookmarks] ([user_id], [question_id], [created_at])
    VALUES (16, @NewQuestionID, DATEADD(MINUTE, -@Counter, GETDATE()));

    IF @Counter % 2 = 0
        INSERT INTO [dbo].[Comments] ([user_id], [question_id], [answer_id], [body], [created_at])
        VALUES (16, @NewQuestionID, NULL, CONCAT(N'Comment test trên Question số ', @Counter), DATEADD(SECOND, -@Counter, GETDATE()));
    ELSE
        INSERT INTO [dbo].[Comments] ([user_id], [question_id], [answer_id], [body], [created_at])
        VALUES (16, NULL, @NewAnswerID, CONCAT(N'Comment test trên Answer số ', @Counter), DATEADD(SECOND, -@Counter, GETDATE()));

    IF @Counter % 2 = 0
        INSERT INTO [dbo].[Votes] ([user_id], [question_id], [answer_id], [vote_type], [created_at])
        VALUES (16, @NewQuestionID, NULL, 'up', DATEADD(MILLISECOND, -@Counter * 10, GETDATE()));
    ELSE
        INSERT INTO [dbo].[Votes] ([user_id], [question_id], [answer_id], [vote_type], [created_at])
        VALUES (16, NULL, @NewAnswerID, 'down', DATEADD(MILLISECOND, -@Counter * 10, GETDATE()));

    SET @Counter = @Counter + 1;
END
GO

-- ========================================================
-- BỔ SUNG DỮ LIỆU CHO 10 BẢNG MỞ RỘNG 
-- ========================================================

-- 1. ANSWER_BOOKMARKS (Lưu nháp câu trả lời)
INSERT INTO [dbo].[Answer_Bookmarks] ([user_id], [answer_id]) VALUES
(16, 1), (16, 2), (2, 4), (3, 5);
GO

-- 2. QUESTION_VIEWS (Lịch sử view chi tiết)
INSERT INTO [dbo].[Question_Views] ([question_id], [viewer_ip], [user_id]) VALUES
(1, '192.168.1.100', 16), (2, '192.168.1.101', 2), (3, '127.0.0.1', 16);
GO

-- 3. REPUTATION_HISTORY (Lịch sử biến động điểm)
INSERT INTO [dbo].[Reputation_History] ([user_id], [delta], [reason], [event_type], [related_post_type], [related_post_id], [actor_user_id]) VALUES
(16, 10, N'Upvote câu hỏi', 'upvote', 'question', 1, 2),
(16, 15, N'Câu trả lời được chấp nhận', 'accept_answer', 'answer', 1, 3);
GO

-- 4. REPORTS (Báo cáo vi phạm)
INSERT INTO [dbo].[Reports] ([reporter_id], [target_type], [target_id], [reason]) VALUES
(16, 'question', 4, N'Câu hỏi này bị trùng lặp với bài viết khác'),
(2, 'answer', 2, N'Ngôn từ chưa phù hợp');
GO

-- 5. MODERATOR_ACTIONS (Lịch sử quản trị)
INSERT INTO [dbo].[Moderator_Actions] ([moderator_id], [action_type], [target_type], [target_id], [description]) VALUES
(1, 'delete', 'question', 5, N'Xóa bài do vi phạm quy tắc cộng đồng'),
(3, 'edit', 'answer', 3, N'Sửa lại format code cho dễ đọc');
GO

-- 6. TAGFOLLOW (Theo dõi Tag)
INSERT INTO [dbo].[TagFollow] ([user_id], [tag_id]) VALUES
(16, 1), (16, 4), (16, 5), (2, 2);
GO

-- 7. USERFOLLOW (Theo dõi User)
INSERT INTO [dbo].[UserFollow] ([follower_id], [following_id]) VALUES
(16, 2), (16, 3), (2, 16), (4, 16);
GO

-- 8. BLOGS (Bài viết Blog)
INSERT INTO [dbo].[Blogs] ([title], [content], [author_id]) VALUES
(N'Lộ trình học React cập nhật 2026', N'Nội dung chi tiết bài viết hướng dẫn học React...', 16),
(N'Tối ưu hóa Database SQL Server', N'Chia sẻ kinh nghiệm đánh Index...', 4);
GO

-- 9. BLOGCOMMENTS (Bình luận Blog)
INSERT INTO [dbo].[BlogComments] ([blog_id], [user_id], [content]) VALUES
(1, 2, N'Bài viết rất chi tiết, cảm ơn bạn!'),
(2, 16, N'Mình sẽ áp dụng ngay kiến thức này vào DevQuery.');
GO

-- 10. POST_EDIT_HISTORY (Lịch sử chỉnh sửa bài viết)
INSERT INTO [dbo].[Post_Edit_History] ([post_type], [post_id], [title], [body], [editor_id]) VALUES
('question', 1, N'Lỗi NullPointerException trong Java là gì?', N'Đã update thêm phần code bị lỗi để mọi người dễ nhìn', 16),
('answer', 1, NULL, N'Đã fix lỗi chính tả trong câu trả lời', 3);
GO
