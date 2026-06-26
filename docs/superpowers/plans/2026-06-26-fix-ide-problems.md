# Fix IDE Problems & Warnings Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Clean up the IDE-reported warnings and false-positive errors.

**Architecture:** Refactor Thymeleaf inline JS, verify Java null checks, and add `@SuppressWarnings("null")` for JDT warnings.

**Tech Stack:** Java, HTML, CSS

---

### Task 1: Fix templates and CSS

**Files:**
- Modify: `src/main/resources/templates/User/blogDetail.html:315`
- Modify: `src/main/resources/templates/User/blogHome.html:78`

- [ ] **Step 1: Fix blogDetail.html**
  Update line 315 in [blogDetail.html](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/resources/templates/User/blogDetail.html):
  ```javascript
  const blogId = /*[[${blog.blogId}]]*/ 0;
  ```

- [ ] **Step 2: Fix blogHome.html**
  Add standard CSS `line-clamp` on line 78 of [blogHome.html](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/resources/templates/User/blogHome.html):
  ```css
              -webkit-line-clamp: 3;
              line-clamp: 3;
  ```

---

### Task 2: Fix Java Null Warning and Suppress Null Safety warnings

**Files:**
- Modify: `src/main/java/com/example/demo/controller/TagController.java:73`
- Modify: `src/main/java/com/example/demo/service/BlogServiceImpl.java`
- Modify: `src/main/java/com/example/demo/service/BlogCommentServiceImpl.java`

- [ ] **Step 1: Update TagController.java**
  Replace line 73 in [TagController.java](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/java/com/example/demo/controller/TagController.java):
  ```java
  boolean isFollowed = user != null && tagService.isFollowing(user.getUserId(), id);
  ```

- [ ] **Step 2: Add @SuppressWarnings("null") to BlogServiceImpl.java**
  Add annotations on sorting and fetching methods:
  ```java
      @Override
      @SuppressWarnings("null")
      public Page<Blog> getBlogsByPage(int page, String search, String sort) {
  ```
  and:
  ```java
      @Override
      @SuppressWarnings("null")
      public Blog getBlogById(int id) {
  ```

- [ ] **Step 3: Add @SuppressWarnings("null") to BlogCommentServiceImpl.java**
  Add annotation to `getCommentTreeByBlogId` method:
  ```java
      @Override
      @SuppressWarnings("null")
      public List<BlogCommentDTO> getCommentTreeByBlogId(int blogId) {
  ```

- [ ] **Step 4: Verify and Commit**
  Run: `$env:JAVA_HOME="C:\Program Files\Java\jdk-17"; .\mvnw clean compile`
  Expected: App compiles with zero compilation errors.
