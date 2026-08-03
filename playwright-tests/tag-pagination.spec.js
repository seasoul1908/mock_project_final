const { test, expect } = require('@playwright/test');

test.describe('Tag Pagination & Dynamic Follow Button E2E Test Suite', () => {

  test.beforeEach(async ({ page }) => {
    await page.request.post('/api/test/reset').catch(() => {});
  });

  test('Test 1 (Pagination Navigation): Page 1 -> Page 2 -> Page 3 navigation', async ({ page }) => {
    // 1. Navigate to Page 1
    await page.goto('/tags');
    await expect(page).toHaveURL(/\/tags/);

    // Verify Page 1 state
    const currentSpanPage1 = page.locator('.pagination span.current');
    await expect(currentSpanPage1).toHaveText('1');

    const prevButtonPage1 = page.locator('.pagination span.disabled').first();
    await expect(prevButtonPage1).toContainText('Prev');

    const page1FirstTag = page.locator('.tag-card .tag-badge').first();
    await expect(page1FirstTag).toHaveText('java');

    // 2. Navigate to Page 2 by clicking page 2 link
    const page2Link = page.locator('.pagination a', { hasText: '2' });
    await page2Link.click();
    await expect(page).toHaveURL(/page=2/);

    // Verify Page 2 state
    const currentSpanPage2 = page.locator('.pagination span.current');
    await expect(currentSpanPage2).toHaveText('2');

    const prevButtonPage2 = page.locator('.pagination a', { hasText: 'Prev' });
    await expect(prevButtonPage2).toBeVisible();

    const nextButtonPage2 = page.locator('.pagination a', { hasText: 'Next' });
    await expect(nextButtonPage2).toBeVisible();

    const page2FirstTag = page.locator('.tag-card .tag-badge').first();
    await expect(page2FirstTag).toHaveText('angular');

    // 3. Navigate to Page 3 by clicking page 3 link
    const page3Link = page.locator('.pagination a', { hasText: '3' });
    await page3Link.click();
    await expect(page).toHaveURL(/page=3/);

    // Verify Page 3 state
    const currentSpanPage3 = page.locator('.pagination span.current');
    await expect(currentSpanPage3).toHaveText('3');

    const nextButtonPage3 = page.locator('.pagination span.disabled');
    await expect(nextButtonPage3).toContainText('Next');

    const page3FirstTag = page.locator('.tag-card .tag-badge').first();
    await expect(page3FirstTag).toHaveText('aws');
  });

  test('Test 2 (Dynamic Follow Button State): Toggle Follow / Unfollow state', async ({ page }) => {
    await page.goto('/tags');

    const firstCard = page.locator('.tag-card').first();
    const followBtn = firstCard.locator('.btn-watch.tag-follow-btn');
    const followText = followBtn.locator('.follow-text');
    const followerCountSpan = firstCard.locator('.follower-count-val');

    // Initial state check: has class not-followed, does NOT have standalone class followed
    await expect(followBtn).toHaveClass(/(^|\s)not-followed(\s|$)/);
    await expect(followBtn).not.toHaveClass(/(^|\s)followed(\s|$)/);
    await expect(followText).toHaveText('Follow');

    const initialCountText = await followerCountSpan.textContent();
    const initialCount = parseInt(initialCountText.trim(), 10);

    // Click Follow -> Toggle to Followed state
    const followResponsePromise = page.waitForResponse(response =>
      response.url().includes('/api/tags/') && response.url().includes('/follow') && response.status() === 200
    );
    await followBtn.click();
    await followResponsePromise;

    // Verify updated state: has class followed, does NOT have class not-followed
    await expect(followBtn).toHaveClass(/(^|\s)followed(\s|$)/);
    await expect(followBtn).not.toHaveClass(/(^|\s)not-followed(\s|$)/);
    await expect(followText).toHaveText('Following');

    const updatedCountText = await followerCountSpan.textContent();
    const updatedCount = parseInt(updatedCountText.trim(), 10);
    expect(updatedCount).toBe(initialCount + 1);

    // Click again -> Toggle back to Unfollowed state
    const unfollowResponsePromise = page.waitForResponse(response =>
      response.url().includes('/api/tags/') && response.url().includes('/follow') && response.status() === 200
    );
    await followBtn.click();
    await unfollowResponsePromise;

    // Verify reverted state
    await expect(followBtn).toHaveClass(/(^|\s)not-followed(\s|$)/);
    await expect(followBtn).not.toHaveClass(/(^|\s)followed(\s|$)/);
    await expect(followText).toHaveText('Follow');

    const revertedCountText = await followerCountSpan.textContent();
    const revertedCount = parseInt(revertedCountText.trim(), 10);
    expect(revertedCount).toBe(initialCount);
  });

  test('Test 3 (Network & Console Error Integrity): Verify HTTP 200/201 status and 0 JS errors', async ({ page }) => {
    const consoleErrors = [];
    const monitoredResponses = [];

    // Listen for console errors
    page.on('console', msg => {
      if (msg.type() === 'error') {
        consoleErrors.push(msg.text());
      }
    });

    // Listen for network responses
    page.on('response', response => {
      const url = response.url();
      if (url.includes('/tags') || url.includes('/api/tags')) {
        monitoredResponses.push({
          url: url,
          status: response.status()
        });
      }
    });

    // Perform page navigation
    await page.goto('/tags?page=1');
    await page.goto('/tags?page=2');

    // Trigger API action
    const followBtn = page.locator('.btn-watch.tag-follow-btn').first();
    await followBtn.click();
    await page.waitForTimeout(300);

    // Fetch API endpoint directly to test GET /api/tags
    const apiResponse = await page.request.get('/api/tags?page=1&size=12');
    expect(apiResponse.status()).toBe(200);

    const json = await apiResponse.json();
    expect(json.currentPage).toBe(1);
    expect(json.totalPages).toBeGreaterThanOrEqual(1);
    expect(Array.isArray(json.data)).toBe(true);

    // Assert monitored response statuses are 200 or 201
    expect(monitoredResponses.length).toBeGreaterThan(0);
    for (const res of monitoredResponses) {
      expect([200, 201]).toContain(res.status);
    }

    // Assert 0 JS console errors
    expect(consoleErrors.length).toBe(0);
  });

  test('Test 4 (Mock / Standalone E2E verification): Full user journey passes reliably', async ({ page }) => {
    await page.goto('/tags');

    // Verify initial load
    const tagsHeader = page.locator('.tags-header h1');
    await expect(tagsHeader).toHaveText('Tags');

    // Navigate to page 2
    await page.click('.pagination a:has-text("2")');
    await expect(page).toHaveURL(/page=2/);

    // Follow item on page 2
    const firstFollowBtnOnPage2 = page.locator('.tag-card').first().locator('.btn-watch.tag-follow-btn');
    await expect(firstFollowBtnOnPage2).toHaveClass(/(^|\s)not-followed(\s|$)/);
    await firstFollowBtnOnPage2.click();
    await expect(firstFollowBtnOnPage2).toHaveClass(/(^|\s)followed(\s|$)/);

    // Navigate back to page 1
    await page.click('.pagination a:has-text("Prev")');
    await expect(page).toHaveURL(/tags\?page=1|tags$/);
    const activePage = page.locator('.pagination span.current');
    await expect(activePage).toHaveText('1');
  });

});
