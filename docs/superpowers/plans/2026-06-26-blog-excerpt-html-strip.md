# Blog Excerpt HTML Strip Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Strip HTML tags from blog preview snippets.

**Architecture:** Add transient/cleaner getter `getExcerptText()` to `Blog` entity, and refer to it in `blogHome.html`.

**Tech Stack:** Java, Thymeleaf

## Global Constraints
- None.

---

### Task 1: Add getExcerptText to Blog Entity

**Files:**
- Modify: `src/main/java/com/example/demo/entity/Blog.java`

- [ ] **Step 1: Edit Blog.java**
  Add the helper method `getExcerptText` inside the `Blog` class:
  ```java
      public String getExcerptText() {
          if (content == null) return "";
          String noHtml = content.replaceAll("<[^>]*>", "");
          noHtml = noHtml.replace("&nbsp;", " ")
                         .replace("&amp;", "&")
                         .replace("&lt;", "<")
                         .replace("&gt;", ">")
                         .replace("&quot;", "\"");
          return noHtml.trim();
      }
  ```

- [ ] **Step 2: Commit backend change**
  ```bash
  git add src/main/java/com/example/demo/entity/Blog.java
  git commit -m "feat: add HTML stripping excerpt getter to Blog entity"
  ```

---

### Task 2: Update blogHome.html Excerpt Binding

**Files:**
- Modify: `src/main/resources/templates/User/blogHome.html`

- [ ] **Step 1: Edit blogHome.html**
  Update line 180 of [blogHome.html](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/resources/templates/User/blogHome.html):
  ```html
  <p class="blog-excerpt" th:text="${blog.excerptText}">Excerpt content...</p>
  ```

- [ ] **Step 2: Recompile and Verify**
  Verify that the blog homepage list displays clean preview snippets without HTML tags.
