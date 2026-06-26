# Fix Blog Detail White Screen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the SpelEvaluationException (EL1007E) on the blog detail page.

**Architecture:** Guard the recursive `commentNode` Thymeleaf fragment from being executed during root sequential flow parsing.

**Tech Stack:** Java, Spring Boot, Thymeleaf

## Global Constraints
- None.

---

### Task 1: Guard commentNode Fragment

**Files:**
- Modify: `src/main/resources/templates/User/blogDetail.html:165`

- [ ] **Step 1: Add th:if check to fragment**
  Update [blogDetail.html](file:///c:/Users/Admin/Downloads/mock_project_final/src/main/resources/templates/User/blogDetail.html) at line 165:
  ```html
  <div th:fragment="commentNode(node, depth)" th:if="${node != null}" class="d-flex mb-3 mt-2">
  ```

- [ ] **Step 2: Restart server and verify**
  Run: `$env:JAVA_HOME="C:\Program Files\Java\jdk-17"; .\mvnw spring-boot:run`
  Expected: App starts, navigating to `http://localhost:8080/blog/detail?id=2` loads successfully.
