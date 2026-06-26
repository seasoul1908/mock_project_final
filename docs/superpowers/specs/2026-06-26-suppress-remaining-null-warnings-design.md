# Design: Suppress Remaining Null Warnings

This design document outlines the suppression of remaining null-type safety and potential null pointer access warnings across the rest of the Java backend files.

## Proposed Changes

### Java Source Code

#### [MODIFY] [AdminRestController.java](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/java/com/example/demo/controller/AdminRestController.java)
Add `@SuppressWarnings("null")` class-level annotation:
```java
@RestController
@RequestMapping("/api/admin")
@SuppressWarnings("null")
public class AdminRestController {
```

#### [MODIFY] [QuestionRecommendationService.java](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/java/com/example/demo/service/QuestionRecommendationService.java)
Add `@SuppressWarnings("null")` class-level annotation:
```java
@Service
@SuppressWarnings("null")
public class QuestionRecommendationService {
```

#### [MODIFY] [QuestionServiceImpl.java](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/java/com/example/demo/service/QuestionServiceImpl.java)
Add `@SuppressWarnings("null")` class-level annotation:
```java
@Service
@SuppressWarnings("null")
public class QuestionServiceImpl implements QuestionService {
```

## Verification Plan
1. Rebuild project to ensure compilation passes successfully.
2. Confirm the warnings are cleared from the IDE list.
