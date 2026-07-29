# Design Specification: Fix Blog Save Failure

Fix the database constraints violation when saving a blog post from the admin dashboard.

## Background & Problem
When creating a blog post via the admin interface (`/admin/blogs`), the front-end sends a JSON payload with `title`, `content`, and `thumbnailUrl`. However, the backend fails to save due to:
1. `author_id` is defined as `NOT NULL` in the `Blogs` database schema, but is left null on blog creation because the controller does not resolve the authenticated user.
2. `thumbnail_url` is capped at 500 characters, which causes saving to fail if external image URLs (or base64 strings) exceed this length.

## Proposed Changes

### Java Code Changes

#### [MODIFY] [Blog.java](file:///c:/Users/Admin/Desktop/Summer2026_FSA/mock_project_final/src/main/java/com/example/demo/entity/Blog.java)
Modify the JPA column configuration for `thumbnailUrl` to allow unbounded length strings.
- Change `@Column(name = "thumbnail_url", length = 500)` to `@Column(name = "thumbnail_url", columnDefinition = "VARCHAR(MAX)")`.

#### [MODIFY] [AdminRestController.java](file:///c:/Users/Admin/Desktop/Summer2026_FSA/mock_project_final/src/main/java/com/example/demo/controller/AdminRestController.java)
- In the `saveBlog` method:
  - Add `Authentication auth` parameter.
  - Retrieve the current logged-in user using `userRepository.findByEmail(auth.getName())`.
  - Extract the User's ID and set it as the blog's `authorId`.

### Database Schema Updates
Execute a migration query on the active database connection:
```sql
ALTER TABLE Blogs ALTER COLUMN thumbnail_url VARCHAR(MAX) NULL;
```
Also update the schema initialization files `Database/DB-DevQuery_New.sql` to match.

## Verification Plan

### Automated/Manual Verification
1. Restart the Spring Boot project.
2. Navigate to the Admin Blogs dashboard (`/admin/blogs`).
3. Add a blog post with a long thumbnail URL (e.g. 500+ characters).
4. Verify the blog is saved successfully and the current admin user's ID is set as the `author_id` in the database.
