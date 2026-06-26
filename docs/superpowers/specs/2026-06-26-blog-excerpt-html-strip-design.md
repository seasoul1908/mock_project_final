# Design: Strip HTML Tags from Blog Preview Excerpt

This design document outlines the changes to remove HTML tags (such as `<p>`, `<h5>`, etc.) from the blog preview snippet on the blog homepage list.

## Proposed Changes

### 1. Backend: [Blog.java](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/java/com/example/demo/entity/Blog.java)
We will add a helper method `getExcerptText()` to clean the `content` field without mutating the database-mapped field:
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

### 2. Frontend: [blogHome.html](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/resources/templates/User/blogHome.html)
We will update the Thymeleaf expression for the excerpt snippet at line 180 to use `excerptText` instead of `content`:
```html
<p class="blog-excerpt" th:text="${blog.excerptText}">Excerpt content...</p>
```

## Verification Plan
1. Re-compile and run.
2. Navigate to `/blog` and verify the third blog post no longer shows `<p>` tags or other HTML tags in the preview snippet.
