# TODO - View Post Detail Enhancement

## Step 1: Repo audit for missing backend support
- Locate existing repository/service/controller for: Report, deletion/moderation, any revision/edit history, bounty awarding, trending.
- Identify current DB schema for needed new tables/columns.

## Step 2: Answer sorting (Feature 7)
- [ ] Add sort query param support in `ViewPostDetailController` + `QuestionDetailService`.
- [ ] Ensure sorting is applied server-side (votes/newest/oldest).
- [ ] Add dropdown UI in `viewpostdetail.html`.


## Step 3: Comment replies (Feature 8)
- Extend comment fetching to build threaded structure using `parent_comment_id`.
- Add reply UI under each top-level question comment (Depth=2: comment -> replies).
- Add endpoint `POST /comments/reply` for question comments.


## Step 4: Share question (Feature 9)
- Add Share button + copy link UI using base URL + existing question id.

## Step 5: Bounty UI & awarding hook (Feature 12)
- Add bounty badge + “Add bounty” form.
- Award bounty when an answer is accepted (toggle accept/unaccept).

## Step 6: Edit history & edit content (Features 10 & 11)
- Check if question/answer edit endpoints exist; if not, implement.
- Add `QuestionRevision` persistence + history endpoint.
- Update UI to show edit + timeline.

## Step 7: Trending (Feature 13)
- Implement trending query and sidebar section on detail page.

## Step 8: Report / Suggest deletion / Delete (Features 14-16)
- Implement user submission endpoints for reports.
- Implement admin review queue (if missing).
- Implement permission-gated soft-delete endpoints.
- Add UI buttons + confirm prompts.

## Step 9: Final integration verification (Feature 17)
- Ensure all UI actions work from the single detail page.
- Verify gating for authenticated vs non-authenticated, and admin/owner permissions.

