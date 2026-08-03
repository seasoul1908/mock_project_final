# Project: mock_project_final

## Architecture
- Backend: Spring Boot Java (REST Controllers, Services, Repositories, JPA/Hibernate, Database)
- Frontend: HTML/JS/CSS templates (Thymeleaf + JS fetch/dynamic controls)
- E2E Tests: Playwright test suite in `playwright-tests` directory

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Exploration & Architecture | Explore backend endpoints, frontend templates/controllers, DB schema, and test setup | none | DONE |
| 2 | Backend Tag Pagination & Follow API | Update/implement Tag listing REST API (`page`, `limit`/`size`, `totalPages`, `totalItems`, `currentPage`, `data`) & Follow REST API | M1 | DONE |
| 3 | Frontend Tag Pagination & Dynamic Follow UI | Implement pagination UI controls & dynamic state/color toggle for Follow button | M2 | DONE |
| 4 | Automated E2E Testing & Coverage | Create & execute Playwright test suite verifying pagination, button states, 200 HTTP, 0 console errors | M3 | DONE |
| 5 | DevOps / Git Verification & Push | Verify 100% test pass, semantic commit, and push to remote branch `dphu` | M4 | DONE |

## Interface Contracts
### Tag REST Controller ↔ Frontend Client
- `GET /api/tags?page={page}&size={size}`
  - Returns JSON: `{ "data": [...], "currentPage": int, "totalPages": int, "totalItems": long }`
- `POST /api/tags/{id}/follow` (or `POST /api/tags/follow/{id}`)
  - Returns HTTP 200/201 JSON response: `{ "status": "success", "isFollowed": boolean, "message": string }`

## Code Layout
- `src/main/java/...`: Backend Java Controllers, Services, Entities, Repositories
- `src/main/resources/...`: Frontend templates/static files/properties
- `playwright-tests/...`: E2E Playwright test scripts
