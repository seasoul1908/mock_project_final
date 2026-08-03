const http = require('http');
const url = require('url');

const PORT = 8080;

// Generate 36 tag items for test dataset across 3 pages (12 items per page)
const allTags = Array.from({ length: 36 }, (_, i) => {
  const id = i + 1;
  const names = [
    'java', 'python', 'javascript', 'spring-boot', 'react', 'node.js',
    'docker', 'sql', 'typescript', 'html', 'css', 'git',
    'angular', 'vue.js', 'c++', 'c#', 'go', 'rust',
    'kotlin', 'swift', 'mongodb', 'postgresql', 'mysql', 'redis',
    'aws', 'kubernetes', 'graphql', 'rest-api', 'linux', 'bash',
    'spring-security', 'jpa', 'hibernate', 'maven', 'gradle', 'playwright'
  ];
  return {
    id: id,
    tagName: names[i] || `tag-${id}`,
    description: `Description for ${names[i] || `tag-${id}`}`,
    questionCount: (i + 1) * 3,
    followerCount: 10 + i,
    isFollowed: false
  };
});

// Follow state cache per tag ID
let tagFollowState = {};

const server = http.createServer((req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;
  const query = parsedUrl.query;

  // Endpoint: POST /api/test/reset
  if (pathname === '/api/test/reset') {
    tagFollowState = {};
    res.writeHead(200, { 'Content-Type': 'application/json' });
    return res.end(JSON.stringify({ status: 'reset' }));
  }

  // Endpoint: GET /api/tags
  if (pathname === '/api/tags' && req.method === 'GET') {
    const page = parseInt(query.page || '1', 10);
    const size = parseInt(query.size || query.limit || '12', 10);
    const totalItems = allTags.length;
    const totalPages = Math.ceil(totalItems / size);
    
    const startIndex = (page - 1) * size;
    const paginatedData = allTags.slice(startIndex, startIndex + size).map(tag => ({
      ...tag,
      isFollowed: tagFollowState[tag.id] ?? false,
      followerCount: (tagFollowState[tag.id] ?? false) ? tag.followerCount + 1 : tag.followerCount
    }));

    res.writeHead(200, { 'Content-Type': 'application/json' });
    return res.end(JSON.stringify({
      data: paginatedData,
      currentPage: page,
      totalPages: totalPages,
      totalItems: totalItems
    }));
  }

  // Endpoint: POST /api/tags/{id}/follow or /api/tags/follow/{id}
  if (req.method === 'POST' && (pathname.match(/^\/api\/tags\/\d+\/follow$/) || pathname.match(/^\/api\/tags\/follow\/\d+$/))) {
    const match = pathname.match(/\d+/);
    const tagId = match ? parseInt(match[0], 10) : 1;
    const action = query.action;

    let isFollowed;
    if (action === 'follow') {
      isFollowed = true;
    } else if (action === 'unfollow') {
      isFollowed = false;
    } else {
      isFollowed = !(tagFollowState[tagId] ?? false);
    }
    tagFollowState[tagId] = isFollowed;

    res.writeHead(200, { 'Content-Type': 'application/json' });
    return res.end(JSON.stringify({
      status: 'success',
      isFollowed: isFollowed,
      message: isFollowed ? 'Tag followed successfully' : 'Tag unfollowed successfully'
    }));
  }

  // Route: /tags or /
  if (pathname === '/tags' || pathname === '/') {
    const page = parseInt(query.page || '1', 10);
    const size = 12;
    const totalItems = allTags.length;
    const totalPages = Math.ceil(totalItems / size);

    const startIndex = (page - 1) * size;
    const pageTags = allTags.slice(startIndex, startIndex + size);

    // Build Tag Cards HTML
    const tagCardsHtml = pageTags.map(tag => {
      const isFollowed = tagFollowState[tag.id] ?? false;
      const count = isFollowed ? tag.followerCount + 1 : tag.followerCount;
      const btnClass = isFollowed ? 'followed' : 'not-followed';
      const iconClass = isFollowed ? 'fa-eye-slash' : 'fa-eye';
      const btnText = isFollowed ? 'Following' : 'Follow';

      return `
        <div class="tag-card">
          <div class="tag-card-header">
            <a href="/tags/${tag.id}" class="tag-badge">${tag.tagName}</a>
            <button type="button" 
                    class="btn-watch tag-follow-btn ${btnClass}" 
                    data-tag-id="${tag.id}"
                    data-is-followed="${isFollowed}">
              <i class="fa-solid ${iconClass}"></i>
              <span class="follow-text">${btnText}</span>
            </button>
          </div>
          <p class="tag-desc">${tag.description}</p>
          <div class="tag-stats">
            <span>${tag.questionCount}</span> questions
            &nbsp;·&nbsp;
            <span class="follower-count-val">${count}</span> followers
          </div>
        </div>
      `;
    }).join('');

    // Build Pagination HTML
    let paginationHtml = '';
    if (totalPages > 0) {
      // Prev Button
      if (page <= 1) {
        paginationHtml += '<span class="disabled">Prev</span>';
      } else {
        paginationHtml += `<a href="/tags?page=${page - 1}">Prev</a>`;
      }

      // Page Numbers
      for (let p = 1; p <= totalPages; p++) {
        if (p === page) {
          paginationHtml += `<span class="current">${p}</span>`;
        } else {
          paginationHtml += `<a href="/tags?page=${p}">${p}</a>`;
        }
      }

      // Next Button
      if (page >= totalPages) {
        paginationHtml += '<span class="disabled">Next</span>';
      } else {
        paginationHtml += `<a href="/tags?page=${page + 1}">Next</a>`;
      }
    }

    const html = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Tags - DevQuery</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Arial, sans-serif; font-size: 13px; background-color: #fff; color: #0c0d0e; }
        .container-custom { max-width: 1264px; margin: 56px auto 0; display: flex; align-items: flex-start; }
        .main-content { flex-grow: 1; padding: 24px; min-width: 0; }
        .tags-header h1 { font-size: 27px; font-weight: 400; margin-bottom: 8px; color: #0c0d0e; }
        .tags-header p { color: #3b4045; line-height: 1.6; max-width: 640px; margin-bottom: 16px; }
        .tags-controls { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin-bottom: 12px; }
        .search-box { position: relative; width: 300px; }
        .search-box input { width: 100%; padding: 8px 12px; border: 1px solid #babfc4; border-radius: 4px; font-size: 13px; }
        .sort-buttons { display: flex; border: 1px solid #9fa6ad; border-radius: 3px; overflow: hidden; }
        .sort-buttons a { padding: 8px 12px; font-size: 13px; color: #6a737c; text-decoration: none; border-right: 1px solid #9fa6ad; }
        .sort-buttons a.active { background-color: #e3e6e8; font-weight: 500; color: #3b4045; }
        .tags-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
        .tag-card { background-color: #fff; border: 1px solid #e3e6e8; border-radius: 4px; padding: 12px; display: flex; flex-direction: column; gap: 8px; }
        .tag-badge { display: inline-block; background-color: #e1ecf4; color: #39739d; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 500; text-decoration: none; }
        .tag-card-header { display: flex; justify-content: space-between; align-items: center; }
        .tag-desc { font-size: 12px; color: #3b4045; line-height: 1.5; flex: 1; }
        .tag-stats { font-size: 11px; color: #838c95; margin-top: 5px; }
        .pagination { display: flex; gap: 4px; margin-top: 24px; justify-content: center; flex-wrap: wrap; }
        .pagination a, .pagination span { padding: 6px 12px; border: 1px solid #d6d9dc; border-radius: 3px; font-size: 13px; text-decoration: none; color: #0074cc; background: #fff; }
        .pagination span.current { background: #f48024; color: #fff; border-color: #f48024; font-weight: 600; }
        .pagination span.disabled, .pagination a.disabled { color: #9fa6ad; background: #f8f9f9; border-color: #d6d9dc; cursor: not-allowed; opacity: 0.6; pointer-events: none; }
        .btn-watch { padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 500; cursor: pointer; border: 1px solid; display: inline-flex; align-items: center; gap: 4px; text-decoration: none; }
        .btn-watch.not-followed { background: #0074cc; color: #fff; border-color: #0074cc; }
        .btn-watch.followed { background: #fff; color: #6a737c; border-color: #9fa6ad; }
    </style>
</head>
<body>
    <div class="container-custom">
        <main class="main-content">
            <div class="tags-header">
                <h1>Tags</h1>
                <p>A tag is a keyword or label that categorizes your question with other, similar questions.</p>
            </div>
            <form method="get" action="/tags">
                <div class="tags-controls">
                    <div class="search-box">
                        <input type="text" id="searchInput" name="search" placeholder="Filter by tag name" />
                    </div>
                    <div class="sort-buttons">
                        <a href="/tags?sort=popular">Popular</a>
                        <a href="/tags?sort=name" class="active">Name</a>
                        <a href="/tags?sort=newest">New</a>
                    </div>
                </div>
            </form>

            <div class="tags-grid">
                ${tagCardsHtml}
            </div>

            <div class="pagination">
                ${paginationHtml}
            </div>
        </main>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function () {
            document.addEventListener('click', function (e) {
                const btn = e.target.closest('.tag-follow-btn, .btn-watch');
                if (!btn) return;

                const tagId = btn.getAttribute('data-tag-id');
                if (!tagId) return;

                e.preventDefault();

                const isCurrentlyFollowed = btn.classList.contains('followed');
                const nextAction = isCurrentlyFollowed ? 'unfollow' : 'follow';

                fetch('/api/tags/' + tagId + '/follow?action=' + nextAction, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' }
                })
                .then(response => response.json())
                .then(data => {
                    if (!data || data.status !== 'success') return;
                    const isFollowed = data.isFollowed;

                    if (isFollowed) {
                        btn.classList.remove('not-followed');
                        btn.classList.add('followed');
                    } else {
                        btn.classList.remove('followed');
                        btn.classList.add('not-followed');
                    }
                    btn.setAttribute('data-is-followed', isFollowed);

                    const textSpan = btn.querySelector('.follow-text');
                    if (textSpan) {
                        textSpan.textContent = isFollowed ? 'Following' : 'Follow';
                    } else {
                        btn.textContent = isFollowed ? 'Following' : 'Follow';
                    }

                    const icon = btn.querySelector('i');
                    if (icon) {
                        icon.className = isFollowed ? 'fa-solid fa-eye-slash' : 'fa-solid fa-eye';
                    }

                    const card = btn.closest('.tag-card, .main-content, .action-bar');
                    if (card) {
                        const followerSpan = card.querySelector('.follower-count-val');
                        if (followerSpan) {
                            let currentVal = parseInt(followerSpan.textContent, 10) || 0;
                            followerSpan.textContent = isFollowed ? currentVal + 1 : Math.max(0, currentVal - 1);
                        }
                    }
                })
                .catch(err => {
                    console.error('Error toggling tag follow state:', err);
                });
            });
        });
    </script>
</body>
</html>`;

    res.writeHead(200, { 'Content-Type': 'text/html' });
    return res.end(html);
  }

  // Default 404
  res.writeHead(404, { 'Content-Type': 'text/plain' });
  res.end('Not Found');
});

server.listen(PORT, () => {
  console.log(`E2E Test Web Server running at http://localhost:${PORT}`);
});
