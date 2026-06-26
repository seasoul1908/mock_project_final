# Design: Fix Blog Detail Blank Screen / SpelEvaluationException

This design documents the fix for the intermittent/frequent blank screen issue on the blog detail page (`/blog/detail?id=X`) caused by a Thymeleaf rendering exception.

## Problem Description
When accessing the blog detail page, Thymeleaf renders the page sequentially. It encounters the recursive comment fragment definition (`commentNode`) at the bottom of [blogDetail.html](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/resources/templates/User/blogDetail.html). Because this definition is in the root flow of the page, Thymeleaf attempts to evaluate its inner attributes (specifically `${node.userAvatar}`) during sequential template parsing.
Since `node` is only defined during explicit fragment invocation (`th:replace="~{:: commentNode(...)}"`) and is `null`/undefined in the page's root evaluation context, Thymeleaf throws a `SpelEvaluationException: EL1007E: Property or field 'userAvatar' cannot be found on null` and fails to render the page (resulting in a blank page or 500 error).

## Proposed Changes

### [blogDetail.html](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/resources/templates/User/blogDetail.html)
Guard the `commentNode` fragment definition from being evaluated in the root sequential flow of the page by adding `th:if="${node != null}"` directly to the fragment container tag.

```html
<!-- Recursive Node Fragment Definition -->
<div th:fragment="commentNode(node, depth)" th:if="${node != null}" class="d-flex mb-3 mt-2">
```

## Verification Plan

### Manual Verification
1. Re-compile and run the application.
2. Navigate to `http://localhost:8080/blog/detail?id=2` and `http://localhost:8080/blog/detail?id=3`.
3. Verify that the blog details load correctly, the comments render properly, and no white page is displayed.
