# Blog Secondary Sorting & New Post Seeding Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement secondary sorting logic for blogs and seed a third blog.

**Architecture:** Use Spring Data Sort to chain sorting fields in Java, and execute SQL INSERT on SQL Server.

**Tech Stack:** Java, Spring Boot, Spring Data JPA, SQL Server

## Global Constraints
- None.

---

### Task 1: Update Sort Logic in BlogServiceImpl

**Files:**
- Modify: `src/main/java/com/example/demo/service/BlogServiceImpl.java:30-34`

- [ ] **Step 1: Edit sorting definitions**
  Update sorting logic in `BlogServiceImpl` to include `blogId` as a secondary sorting field.
  ```java
  } else if ("oldest".equals(sort)) {
      sortOrder = Sort.by(Sort.Direction.ASC, "createdAt").and(Sort.by(Sort.Direction.ASC, "blogId"));
  } else {
      sortOrder = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "blogId"));
  }
  ```

- [ ] **Step 2: Commit sorting changes**
  ```bash
  git add src/main/java/com/example/demo/service/BlogServiceImpl.java
  git commit -m "feat: add blogId as secondary sorting field for blog list"
  ```

---

### Task 2: Seed Third Blog Post

**Files:**
- Database Query execution

- [ ] **Step 1: Execute SQL insert**
  Run SQL Server insert command to add the third blog post.
  ```sql
  INSERT INTO Blogs (title, content, thumbnail_url, author_id, status, view_count, comment_count, created_at, updated_at)
  VALUES (
      N'Cẩm nang Git & GitHub toàn tập cho lập trình viên',
      N'<p>Git và GitHub là công cụ quản lý mã nguồn phổ biến nhất thế giới hiện nay. Việc nắm vững Git giúp lập trình viên kiểm soát phiên bản code hiệu quả, làm việc nhóm mượt mà và đóng góp vào các dự án mã nguồn mở một cách dễ dàng.</p><h5>Tại sao nên dùng Git?</h5><ul><li>Ghi lại lịch sử thay đổi của dự án</li><li>Quay lại phiên bản cũ khi code bị lỗi</li><li>Hỗ trợ phân nhánh (branching) mạnh mẽ</li></ul>',
      'https://placehold.co/800x400/eeeeee/999999?text=Git+and+GitHub',
      1,
      1,
      15,
      0,
      GETDATE(),
      GETDATE()
  );
  ```

- [ ] **Step 2: Verify on UI**
  Verify the sorting order reversed successfully on the UI for Newest/Oldest and the third blog post is displayed.
