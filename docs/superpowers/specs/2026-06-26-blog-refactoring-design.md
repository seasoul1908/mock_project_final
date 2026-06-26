# Design Spec - Refactor Blog System to Spring Boot

This document describes the design for refactoring the Blog Homepage, Blog Details, and Blog Comments features from the legacy Java Servlet/JSP project (`SWP`) to the current Spring Boot/Thymeleaf project (`mock_project_final`).

## User Review Required

> [!IMPORTANT]
> The Blog Comment tree requires a parent-child relationship. We will represent this using a self-referential relationship on the `BlogComment` entity or via a flat query parsed into a tree structure in Java, matching the original `BlogCommentDAO` logic to prevent database schema modification. We choose to keep the database schema unchanged and replicate the flat SQL structure with a hierarchical mapping helper in the service layer.

---

## Proposed Changes

### Data Model & Repositories

#### [NEW] [BlogComment.java](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/java/com/example/demo/entity/BlogComment.java)
- A JPA Entity mapped to the `BlogComments` table.
- Fields:
  - `commentId` (Primary Key, Identity)
  - `blogId` (Integer)
  - `userId` (Long)
  - `parentId` (Integer, nullable)
  - `content` (String, NVARCHAR(MAX))
  - `createdAt` (TemporalType.TIMESTAMP)

#### [NEW] [BlogCommentRepository.java](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/java/com/example/demo/repository/BlogCommentRepository.java)
- Extends `JpaRepository<BlogComment, Integer>`.
- Custom queries to retrieve comments for a blog and find children.

---

### Services

#### [NEW] [BlogService.java](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/java/com/example/demo/service/BlogService.java) & [BlogServiceImpl.java](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/java/com/example/demo/service/BlogServiceImpl.java)
- Encapsulates blog-related business logic:
  - `getBlogsByPage(int page, String search, String sort)` (using `Pageable` and custom sorting).
  - `getBlogById(int id)` (handling `status = 1` check and updating view count).
  - `increaseViewCount(int id)`.

#### [NEW] [BlogCommentService.java](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/java/com/example/demo/service/BlogCommentService.java) & [BlogCommentServiceImpl.java](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/java/com/example/demo/service/BlogCommentServiceImpl.java)
- Encapsulates comment-related business logic:
  - `getCommentTreeByBlogId(int blogId)` (retrieves flat list, maps to recursive `BlogCommentDTO` tree).
  - `addComment(BlogComment comment)`.
  - `updateComment(int commentId, long userId, String content)`.
  - `deleteComment(int commentId, long userId)` (recursively deletes children to maintain referential integrity without DB cascades).

---

### DTOs

#### [NEW] [BlogCommentDTO.java](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/java/com/example/demo/dto/BlogCommentDTO.java)
- Holds fields for display: `commentId`, `blogId`, `userId`, `parentId`, `content`, `createdAt`, `username`, `userAvatar`, and `List<BlogCommentDTO> replies`.

---

### Controllers

#### [NEW] [BlogController.java](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/java/com/example/demo/controller/BlogController.java)
- Handles user-facing Blog requests:
  - `GET /blog` - Displays the Blog Homepage with searching, sorting, and paging.
  - `GET /blog/detail` - Increments view count, fetches blog details and comment tree, and displays the detail page.
  - `POST /blog/comment` - Handles adding, editing, and deleting comments, then redirecting back to the detail page.

---

### Views (Thymeleaf Templates)

#### [NEW] [blogHome.html](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/resources/templates/User/blogHome.html)
- Adapted from `blogHome.jsp`.
- Uses Thymeleaf layout tags, standard navbar, and sidebar.

#### [NEW] [blogDetail.html](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/resources/templates/User/blogDetail.html)
- Adapted from `blogDetail.jsp`.
- Implements the recursive comment template fragment to display deep replies.

---

## Verification Plan

### Automated Tests
- Build verification via Maven: `.\mvnw clean compile` to ensure zero compilation or entity-mapping errors.

### Manual Verification
1. Access `/blog` to see the blog post grid, search by keyword, and test sorting options.
2. Click on a blog post to go to `/blog/detail?id=X`, check if views increment, and ensure comments render correctly.
3. Post a new comment, reply to an existing comment, edit a comment, and delete a comment.
