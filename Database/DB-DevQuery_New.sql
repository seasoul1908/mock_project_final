USE [master]
GO

ALTER DATABASE [devquery] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
GO
DROP DATABASE [devquery];
GO

CREATE DATABASE [devquery]
GO

USE [devquery]
GO

-- =============================================
-- 1. USERS
-- =============================================
CREATE TABLE [dbo].[Users](
    [user_id]       [bigint]       IDENTITY(1,1) PRIMARY KEY,
    [username]      [nvarchar](50) NOT NULL UNIQUE,
    [email]         [varchar](120) NOT NULL UNIQUE,
    [password_hash] [varchar](255) NOT NULL,
    [role]          [varchar](20)  DEFAULT 'member',   
    [status]        [varchar](20)  DEFAULT 'active',   
    [created_at]    [datetime]     DEFAULT GETDATE(),
    [updated_at]    [datetime]     DEFAULT GETDATE(),
    [Reputation]    [int]          DEFAULT 0,
    [provider]      [varchar](20)  DEFAULT 'local',
    [provider_id]   [varchar](150) NULL
)
GO

-- =============================================
-- 2. USER_PROFILE
-- =============================================
CREATE TABLE [dbo].[User_Profile](
    [profile_id] [bigint]        IDENTITY(1,1) PRIMARY KEY,
    [user_id]    [bigint]        NOT NULL UNIQUE,
    [bio]        [nvarchar](max) NULL,
    [avatar_url] [varchar](255)  NULL,
    [location]   [nvarchar](100) NULL,
    [website]    [varchar](255)  NULL,
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[Users]([user_id]) ON DELETE CASCADE
)
GO

-- =============================================
-- 3. QUESTIONS
-- =============================================
CREATE TABLE [dbo].[Questions](
    [question_id]       [bigint]        IDENTITY(1,1) PRIMARY KEY,
    [user_id]           [bigint]        NOT NULL,
    [title]             [nvarchar](255) NOT NULL,
    [body]              [nvarchar](max) NOT NULL,
    [code_snippet]      [nvarchar](max) NULL,
    [view_count]        [int]           DEFAULT 0,
    [is_closed]         [bit]           NOT NULL DEFAULT 0,
    [closed_by]         [bigint]        NULL,
    [closed_reason]     [nvarchar](255) NULL,
    [closed_at]         [datetime]      NULL,
    [is_deleted]        [bit]           NOT NULL DEFAULT 0,
    [deleted_at]        [datetime]      NULL,
    [deleted_by]        [bigint]        NULL,
    [bounty_amount]     [int]           NOT NULL DEFAULT 0,
    [bounty_awarder_id] [bigint]        NULL,
    [bounty_started_at] [datetime]      NULL,
    [bounty_expires_at] [datetime]      NULL,
    [created_at]        [datetime]      DEFAULT GETDATE(),
    [updated_at]        [datetime]      DEFAULT GETDATE(),
    [Score]             [int]           DEFAULT 0,
    CONSTRAINT [FK_Questions_User]      FOREIGN KEY ([user_id])    REFERENCES [dbo].[Users]([user_id]),
    CONSTRAINT [FK_Questions_ClosedBy]  FOREIGN KEY ([closed_by])  REFERENCES [dbo].[Users]([user_id]),
    CONSTRAINT [FK_Questions_DeletedBy] FOREIGN KEY ([deleted_by]) REFERENCES [dbo].[Users]([user_id])
)
GO

CREATE INDEX [IX_Questions_is_deleted]
    ON [dbo].[Questions]([is_deleted])
GO

CREATE INDEX [IX_Questions_Bounty_Active]
    ON [dbo].[Questions]([bounty_expires_at], [bounty_amount])
    WHERE [bounty_amount] > 0
GO

-- =============================================
-- 4. ANSWERS
-- =============================================
CREATE TABLE [dbo].[Answers](
    [answer_id]    [bigint]        IDENTITY(1,1) PRIMARY KEY,
    [question_id]  [bigint]        NOT NULL,
    [user_id]      [bigint]        NOT NULL,
    [body]         [nvarchar](max) NOT NULL,
    [code_snippet] [nvarchar](max) NULL,
    [is_edited]    [bit]           DEFAULT 0,
    [is_accepted]  [bit]           DEFAULT 0,
    [created_at]   [datetime]      DEFAULT GETDATE(),
    [updated_at]   [datetime]      DEFAULT GETDATE(),
    [Score]        [int]           DEFAULT 0,
    FOREIGN KEY ([question_id]) REFERENCES [dbo].[Questions]([question_id]) ON DELETE CASCADE,
    FOREIGN KEY ([user_id])     REFERENCES [dbo].[Users]([user_id])
)
GO

-- Add accepted_answer_id after Answers exists
ALTER TABLE [dbo].[Questions]
    ADD [accepted_answer_id] [bigint] NULL
GO
ALTER TABLE [dbo].[Questions]
    ADD CONSTRAINT [FK_Questions_AcceptedAnswer]
        FOREIGN KEY ([accepted_answer_id]) REFERENCES [dbo].[Answers]([answer_id]) ON DELETE NO ACTION
GO

-- =============================================
-- 5. COMMENTS
-- =============================================
CREATE TABLE [dbo].[Comments](
    [comment_id]        [bigint]        IDENTITY(1,1) PRIMARY KEY,
    [user_id]           [bigint]        NOT NULL,
    [question_id]       [bigint]        NULL,
    [answer_id]         [bigint]        NULL,
    [parent_comment_id] [bigint]        NULL,
    [body]              [nvarchar](max) NOT NULL,
    [created_at]        [datetime]      DEFAULT GETDATE(),
    FOREIGN KEY ([user_id])     REFERENCES [dbo].[Users]([user_id]),
    FOREIGN KEY ([question_id]) REFERENCES [dbo].[Questions]([question_id]) ON DELETE CASCADE,
    FOREIGN KEY ([answer_id])   REFERENCES [dbo].[Answers]([answer_id]),
    CONSTRAINT [FK_Comments_ParentComment]
        FOREIGN KEY ([parent_comment_id]) REFERENCES [dbo].[Comments]([comment_id])
)
GO

CREATE INDEX [IX_Comments_ParentComment]
    ON [dbo].[Comments]([parent_comment_id])
GO

-- =============================================
-- 6. VOTES
-- =============================================
CREATE TABLE [dbo].[Votes](
    [vote_id]     [bigint]      IDENTITY(1,1) PRIMARY KEY,
    [user_id]     [bigint]      NOT NULL,
    [question_id] [bigint]      NULL,
    [answer_id]   [bigint]      NULL,
    [vote_type]   [varchar](10) NOT NULL, 
    [created_at]  [datetime]    DEFAULT GETDATE(),
    CONSTRAINT [UQ_Votes_user_question_answer] UNIQUE ([user_id], [question_id], [answer_id]),
    FOREIGN KEY ([user_id])     REFERENCES [dbo].[Users]([user_id]),
    FOREIGN KEY ([question_id]) REFERENCES [dbo].[Questions]([question_id]) ON DELETE CASCADE,
    FOREIGN KEY ([answer_id])   REFERENCES [dbo].[Answers]([answer_id])
)
GO

-- =============================================
-- 7. TAGS
-- =============================================
CREATE TABLE [dbo].[Tags](
    [tag_id]      [bigint]        IDENTITY(1,1) PRIMARY KEY,
    [tag_name]    [varchar](50)   NOT NULL UNIQUE,
    [description] [nvarchar](max) NULL,
    [IsActive]    [bit]           DEFAULT 1
)
GO

-- =============================================
-- 8. QUESTION_TAGS
-- =============================================
CREATE TABLE [dbo].[Question_Tags](
    [question_id] [bigint] NOT NULL,
    [tag_id]      [bigint] NOT NULL,
    PRIMARY KEY ([question_id], [tag_id]),
    FOREIGN KEY ([question_id]) REFERENCES [dbo].[Questions]([question_id]) ON DELETE CASCADE,
    FOREIGN KEY ([tag_id])      REFERENCES [dbo].[Tags]([tag_id])           ON DELETE CASCADE
)
GO

-- =============================================
-- 9. COLLECTIONS
-- =============================================
CREATE TABLE [dbo].[Collections](
    [collection_id] [int]           IDENTITY(1,1) PRIMARY KEY,
    [user_id]       [bigint]        NOT NULL,
    [Name]          [nvarchar](100) NOT NULL,
    [CreatedAt]     [datetime2](7)  DEFAULT GETDATE(),
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[Users]([user_id]) ON DELETE CASCADE
)
GO

-- =============================================
-- 10. BOOKMARKS
-- =============================================
CREATE TABLE [dbo].[Bookmarks](
    [user_id]       [bigint]  NOT NULL,
    [question_id]   [bigint]  NOT NULL,
    [created_at]    [datetime] DEFAULT GETDATE(),
    [collection_id] [int]     NULL,
    PRIMARY KEY ([user_id], [question_id]),
    FOREIGN KEY ([user_id])       REFERENCES [dbo].[Users]([user_id]),
    FOREIGN KEY ([question_id])   REFERENCES [dbo].[Questions]([question_id]) ON DELETE CASCADE,
    FOREIGN KEY ([collection_id]) REFERENCES [dbo].[Collections]([collection_id])
)
GO

-- =============================================
-- 11. ANSWER_BOOKMARKS
-- =============================================
CREATE TABLE [dbo].[Answer_Bookmarks](
    [user_id]    [bigint]  NOT NULL,
    [answer_id]  [bigint]  NOT NULL,
    [created_at] [datetime] DEFAULT GETDATE(),
    PRIMARY KEY ([user_id], [answer_id]),
    FOREIGN KEY ([user_id])   REFERENCES [dbo].[Users]([user_id]),
    FOREIGN KEY ([answer_id]) REFERENCES [dbo].[Answers]([answer_id]) ON DELETE CASCADE
)
GO

-- =============================================
-- 12. BADGES
-- =============================================
CREATE TABLE [dbo].[Badges](
    [badge_id]            [bigint]        IDENTITY(1,1) PRIMARY KEY,
    [name]                [nvarchar](50)  NOT NULL UNIQUE,
    [type]                [varchar](10)   NOT NULL,  
    [description]         [nvarchar](max) NULL,
    [required_reputation] [int]           DEFAULT 0
)
GO

-- =============================================
-- 13. USER_BADGES
-- =============================================
CREATE TABLE [dbo].[User_Badges](
    [user_id]    [bigint]  NOT NULL,
    [badge_id]   [bigint]  NOT NULL,
    [created_at] [datetime] DEFAULT GETDATE(),
    PRIMARY KEY ([user_id], [badge_id]),
    FOREIGN KEY ([user_id])  REFERENCES [dbo].[Users]([user_id])   ON DELETE CASCADE,
    FOREIGN KEY ([badge_id]) REFERENCES [dbo].[Badges]([badge_id]) ON DELETE CASCADE
)
GO

-- =============================================
-- 14. QUESTION_VIEWS
-- =============================================
CREATE TABLE [dbo].[Question_Views](
    [id]          [bigint]      IDENTITY(1,1) PRIMARY KEY,
    [question_id] [bigint]      NOT NULL,
    [viewer_ip]   [varchar](50) NULL,
    [viewed_at]   [datetime]    DEFAULT GETDATE(),
    [user_id]     [bigint]      NULL,
    FOREIGN KEY ([question_id]) REFERENCES [dbo].[Questions]([question_id]) ON DELETE CASCADE,
    FOREIGN KEY ([user_id])     REFERENCES [dbo].[Users]([user_id])
)
GO

-- =============================================
-- 15. REPUTATION_HISTORY  (delta-based schema)
-- =============================================
CREATE TABLE [dbo].[Reputation_History](
    [history_id]        [bigint]        IDENTITY(1,1) NOT NULL,
    [user_id]           [bigint]        NOT NULL,
    [delta]             [int]           NOT NULL,
    [reason]            [nvarchar](255) NULL,
    [event_type]        [varchar](50)   NULL,
    [related_post_type] [varchar](20)   NULL,
    [related_post_id]   [bigint]        NULL,
    [actor_user_id]     [bigint]        NULL,
    [created_at]        [datetime]      NOT NULL
        CONSTRAINT [DF_Reputation_History_created_at] DEFAULT GETDATE(),
    CONSTRAINT [PK_Reputation_History]       PRIMARY KEY CLUSTERED ([history_id] ASC),
    CONSTRAINT [FK_Reputation_History_user]  FOREIGN KEY ([user_id])       REFERENCES [dbo].[Users]([user_id]),
    CONSTRAINT [FK_Reputation_History_actor] FOREIGN KEY ([actor_user_id]) REFERENCES [dbo].[Users]([user_id])
)
GO

CREATE INDEX [IX_Reputation_History_User_Created]
    ON [dbo].[Reputation_History]([user_id], [created_at] DESC)
GO

-- =============================================
-- 16. PRIVILEGES
-- =============================================
CREATE TABLE [dbo].[Privileges](
    [privilege_id]        [int]           IDENTITY(1,1) PRIMARY KEY,
    [name]                [nvarchar](100) NOT NULL,
    [description]         [nvarchar](max) NOT NULL,
    [required_reputation] [int]           NOT NULL
)
GO

-- =============================================
-- 17. SYSTEM_RULES
-- =============================================
CREATE TABLE [dbo].[System_Rules](
    [rule_id]    [bigint]        IDENTITY(1,1) PRIMARY KEY,
    [title]      [nvarchar](255) NOT NULL,
    [content]    [nvarchar](max) NOT NULL,
    [created_at] [datetime]      DEFAULT GETDATE(),
    [updated_at] [datetime]      DEFAULT GETDATE(),
    [created_by] [bigint]        NOT NULL,
    [updated_by] [bigint]        NULL,
    FOREIGN KEY ([created_by]) REFERENCES [dbo].[Users]([user_id])
)
GO

-- =============================================
-- 18. NOTIFICATIONS
-- =============================================
CREATE TABLE [dbo].[Notifications](
    [notification_id] [bigint]        IDENTITY(1,1) PRIMARY KEY,
    [user_id]         [bigint]        NOT NULL,
    [type]            [varchar](20)   NOT NULL,
    [content]         [nvarchar](max) NOT NULL,
    [is_read]         [bit]           DEFAULT 0,
    [created_at]      [datetime]      DEFAULT GETDATE(),
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[Users]([user_id])
)
GO

-- =============================================
-- 19. REPORTS
-- =============================================
CREATE TABLE [dbo].[Reports](
    [report_id]   [bigint]        IDENTITY(1,1) PRIMARY KEY,
    [reporter_id] [bigint]        NOT NULL,
    [target_type] [varchar](20)   NOT NULL,
    [target_id]   [bigint]        NOT NULL,
    [reason]      [nvarchar](max) NOT NULL,
    [note]        [nvarchar](500) NULL,
    [status]      [varchar](20)   DEFAULT 'open',
    [created_at]  [datetime]      DEFAULT GETDATE(),
    CONSTRAINT [CHK_Reports_TargetType] CHECK ([target_type] IN ('question', 'answer')),
    FOREIGN KEY ([reporter_id]) REFERENCES [dbo].[Users]([user_id])
)
GO

CREATE INDEX [IX_Reports_Target]    ON [dbo].[Reports]([target_type], [target_id])
CREATE INDEX [IX_Reports_Reporter]  ON [dbo].[Reports]([reporter_id])
CREATE INDEX [IX_Reports_CreatedAt] ON [dbo].[Reports]([created_at])
GO

-- =============================================
-- 20. MODERATOR_ACTIONS
-- =============================================
CREATE TABLE [dbo].[Moderator_Actions](
    [action_id]    [bigint]        IDENTITY(1,1) PRIMARY KEY,
    [moderator_id] [bigint]        NOT NULL,
    [action_type]  [varchar](30)   NOT NULL,
    [target_type]  [varchar](20)   NOT NULL,
    [target_id]    [bigint]        NOT NULL,
    [description]  [nvarchar](max) NULL,
    [created_at]   [datetime]      DEFAULT GETDATE(),
    FOREIGN KEY ([moderator_id]) REFERENCES [dbo].[Users]([user_id])
)
GO

-- =============================================
-- 21. TAGFOLLOW
-- =============================================
CREATE TABLE [dbo].[TagFollow](
    [id]          [bigint]  IDENTITY(1,1) PRIMARY KEY,
    [user_id]     [bigint]  NOT NULL,
    [tag_id]      [bigint]  NOT NULL,
    [followed_at] [datetime] DEFAULT GETDATE(),
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[Users]([user_id]),
    FOREIGN KEY ([tag_id])  REFERENCES [dbo].[Tags]([tag_id])
)
GO

-- =============================================
-- 22. USERFOLLOW
-- =============================================
CREATE TABLE [dbo].[UserFollow](
    [follower_id]  [bigint]       NOT NULL,
    [following_id] [bigint]       NOT NULL,
    [followed_at]  [datetime2](7) DEFAULT GETDATE(),
    PRIMARY KEY ([follower_id], [following_id]),
    FOREIGN KEY ([follower_id])  REFERENCES [dbo].[Users]([user_id]),
    FOREIGN KEY ([following_id]) REFERENCES [dbo].[Users]([user_id])
)
GO

-- =============================================
-- 23. BLOGS
-- =============================================
CREATE TABLE [dbo].[Blogs](
    [blog_id]       [int]           IDENTITY(1,1) PRIMARY KEY,
    [title]         [nvarchar](max) NOT NULL,
    [content]       [nvarchar](max) NOT NULL,
    [thumbnail_url] [varchar](max)  NULL,
    [author_id]     [bigint]        NOT NULL,
    [created_at]    [datetime]      DEFAULT GETDATE(),
    [updated_at]    [datetime]      DEFAULT GETDATE(),
    [view_count]    [int]           DEFAULT 0,
    [comment_count] [int]           DEFAULT 0,
    [status]        [int]           DEFAULT 1,
    FOREIGN KEY ([author_id]) REFERENCES [dbo].[Users]([user_id])
)
GO

-- =============================================
-- 24. BLOGCOMMENTS
-- =============================================
CREATE TABLE [dbo].[BlogComments](
    [comment_id] [int]           IDENTITY(1,1) PRIMARY KEY,
    [blog_id]    [int]           NOT NULL,
    [user_id]    [bigint]        NOT NULL,
    [parent_id]  [int]           NULL,
    [content]    [nvarchar](max) NOT NULL,
    [created_at] [datetime]      DEFAULT GETDATE(),
    FOREIGN KEY ([blog_id])   REFERENCES [dbo].[Blogs]([blog_id]) ON DELETE CASCADE,
    FOREIGN KEY ([user_id])   REFERENCES [dbo].[Users]([user_id]),
    FOREIGN KEY ([parent_id]) REFERENCES [dbo].[BlogComments]([comment_id])
)
GO

-- =============================================
-- 25. POST_EDIT_HISTORY
-- =============================================
CREATE TABLE [dbo].[Post_Edit_History](
    [history_id]   [bigint]         IDENTITY(1,1) PRIMARY KEY,
    [post_type]    [varchar](20)    NOT NULL,   
    [post_id]      [bigint]         NOT NULL,
    [title]        [nvarchar](255)  NULL,
    [body]         [nvarchar](max)  NOT NULL,
    [code_snippet] [nvarchar](max)  NULL,
    [tags]         [nvarchar](1000) NULL,
    [editor_id]    [bigint]         NOT NULL,
    [edited_at]    [datetime]       NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY ([editor_id]) REFERENCES [dbo].[Users]([user_id])
)
GO

CREATE INDEX [IX_Post_Edit_History_Post]
    ON [dbo].[Post_Edit_History]([post_type], [post_id], [edited_at] DESC)
GO

CREATE TABLE user_preferences (
    user_id BIGINT PRIMARY KEY,
    theme VARCHAR(20) DEFAULT 'light',
    high_contrast BIT DEFAULT 0,
    new_editor BIT DEFAULT 1,
    keyboard_shortcuts BIT DEFAULT 0,
    left_navigation BIT DEFAULT 1,
    hot_network_questions BIT DEFAULT 1,
    staging_ground BIT DEFAULT 1,
    tag_hover_guidance BIT DEFAULT 0,
    experiments BIT DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) -- (Thay 'users' bằng tên bảng user thực tế của ông)
);
Go

-- =============================================
-- 26. FEEDBACKS
-- =============================================
CREATE TABLE [dbo].[Feedbacks](
    [feedback_id] [bigint] IDENTITY(1,1) PRIMARY KEY,
    [user_id]     [bigint] NOT NULL,
    [name]        [nvarchar](100) NOT NULL,
    [email]       [varchar](120) NOT NULL,
    [message]     [nvarchar](max) NOT NULL,
    [created_at]  [datetime] DEFAULT GETDATE(),
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[Users]([user_id]) ON DELETE CASCADE
)
GO

ALTER TABLE Users
ADD accepted_terms BIT NOT NULL DEFAULT 0;

