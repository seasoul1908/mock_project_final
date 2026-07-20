# Fix Blog Save Failure Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix blog post creation failures by resolving `author_id` dynamically from authentication context and removing length limits on `thumbnail_url`.

**Architecture:** Update database schema column `thumbnail_url` to `VARCHAR(MAX)`, update the Hibernate entity mapping to match, and retrieve the current user's email in `AdminRestController` to set `authorId` before persisting the blog.

**Tech Stack:** Spring Boot, Spring Data JPA, SQL Server, Java 17

## Global Constraints
- Target Java version: 17
- Naming conventions must match existing code.

---

### Task 1: Database Schema Migration

**Files:**
- Modify: `Database/DB-DevQuery_New.sql` (to reflect the updated schema definition)

**Interfaces:**
- Consumes: Existing DB table `Blogs`
- Produces: `Blogs.thumbnail_url` as `VARCHAR(MAX)`

- [ ] **Step 1: Execute ALTER TABLE on current database**

Run this SQL query using a DB tool or by running a command to alter the column type:
```sql
ALTER TABLE Blogs ALTER COLUMN thumbnail_url VARCHAR(MAX) NULL;
```
Verify the command runs without error.

- [ ] **Step 2: Update database initialization scripts**

Update line 390 of `Database/DB-DevQuery_New.sql` from:
```sql
    [thumbnail_url] [varchar](500)  NULL,
```
to:
```sql
    [thumbnail_url] [varchar](max)  NULL,
```

- [ ] **Step 3: Commit migration**

```bash
git add Database/DB-DevQuery_New.sql
git commit -m "migration: update thumbnail_url to varchar(max) in database schema"
```

---

### Task 2: Modify Blog Entity

**Files:**
- Modify: `src/main/java/com/example/demo/entity/Blog.java:21-22`

**Interfaces:**
- Consumes: None
- Produces: `Blog` entity with updated `thumbnailUrl` JPA column definition

- [ ] **Step 1: Update Blog.java JPA column annotation**

Modify `src/main/java/com/example/demo/entity/Blog.java` to change:
```java
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;
```
to:
```java
    @Column(name = "thumbnail_url", columnDefinition = "VARCHAR(MAX)")
    private String thumbnailUrl;
```

- [ ] **Step 2: Compile the application**

Run compilation to make sure there are no syntax errors:
```bash
.\mvnw.cmd compile
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/demo/entity/Blog.java
git commit -m "feat: update Blog entity thumbnailUrl column mapping to VARCHAR(MAX)"
```

---

### Task 3: Resolve author_id in AdminRestController

**Files:**
- Modify: `src/main/java/com/example/demo/controller/AdminRestController.java:53-58`

**Interfaces:**
- Consumes: `Authentication` and `UserRepository`
- Produces: Blog instance saved with a resolved `authorId`

- [ ] **Step 1: Update AdminRestController.java saveBlog method**

Modify the `/blogs` POST endpoint:
```java
    @PostMapping("/blogs")
    public ResponseEntity<Blog> saveBlog(@RequestBody Blog blog, Authentication auth) {
        if (blog.getCreatedAt() == null) blog.setCreatedAt(new Date());
        if (blog.getStatus() == null) blog.setStatus(1);
        
        Long userId = 1L; // Fallback default
        if (auth != null && auth.getName() != null) {
            userId = userRepository.findByEmail(auth.getName())
                    .map(u -> u.getUserId())
                    .orElse(1L);
        }
        blog.setAuthorId(userId);
        
        return ResponseEntity.ok(blogRepository.save(blog));
    }
```
Ensure `org.springframework.security.core.Authentication` is imported (already present in `AdminRestController.java`).

- [ ] **Step 2: Compile and run test build**

```bash
.\mvnw.cmd compile
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/demo/controller/AdminRestController.java
git commit -m "feat: resolve and set authorId from Authentication context when saving blog"
```
