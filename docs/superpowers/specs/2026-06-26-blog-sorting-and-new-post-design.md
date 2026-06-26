# Design: Blog Secondary Sorting & Mock Blog Seeding

This design document outlines changes to implement secondary sorting for blog posts and seeds a third blog post in the database.

## Proposed Changes

### 1. Backend Logic: Secondary Sorting
In [BlogServiceImpl.java](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/java/com/example/demo/service/BlogServiceImpl.java), we will modify the sorting definitions so that when `createdAt` values are identical, `blogId` acts as the tie-breaker:
- **newest**: `Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "blogId"))`
- **oldest**: `Sort.by(Sort.Direction.ASC, "createdAt").and(Sort.by(Sort.Direction.ASC, "blogId"))`

### 2. Database Seeding: New Blog Post
We will insert a new blog post into the `Blogs` table via SQL command:
- **Title**: "Cẩm nang Git & GitHub toàn tập cho lập trình viên"
- **Content**: "<p>Git và GitHub là hai công cụ không thể thiếu đối với mọi lập trình viên hiện đại...</p>"
- **Thumbnail URL**: "https://placehold.co/800x400/eeeeee/999999?text=Git+and+GitHub"
- **Author ID**: 1
- **Status**: 1 (Published)
- **View Count**: 15
- **Comment Count**: 0
- **Created At**: Current system date

## Verification Plan

### Manual Verification
1. Re-compile and run the application.
2. Navigate to `/blog` and select sorting "Newest" vs "Oldest".
3. Verify that the order of the blogs is reversed correctly.
4. Verify that the third blog post is displayed in the list.
