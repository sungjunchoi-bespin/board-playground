import { test, expect, type Page } from "@playwright/test";

const BASE = "http://localhost:5174";
const API = "http://localhost:8080/api";

let userCounter = 0;
function uniqueUser() {
  userCounter++;
  const ts = Date.now();
  return {
    username: `favuser${userCounter}${ts}`,
    email: `favuser${userCounter}${ts}@test.com`,
    password: "password123",
  };
}

async function registerApi(user: {
  username: string;
  email: string;
  password: string;
}) {
  const res = await fetch(`${API}/users`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ user }),
  });
  const data = await res.json();
  return data.user.token as string;
}

async function createArticleApi(token: string, title: string) {
  const res = await fetch(`${API}/articles`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Token ${token}`,
    },
    body: JSON.stringify({
      article: {
        title,
        description: `Description for ${title}`,
        body: `Body for ${title}`,
        tagList: [],
      },
    }),
  });
  const data = await res.json();
  return data.article.slug as string;
}

async function loginInBrowser(
  page: Page,
  email: string,
  token: string,
  username: string,
) {
  await page.goto(`${BASE}/`);
  await page.evaluate(
    ({ token, user }) => {
      localStorage.setItem("conduit_token", token);
      localStorage.setItem("conduit_user", JSON.stringify(user));
    },
    {
      token,
      user: { email, token, username, bio: null, image: null },
    },
  );
}

test.describe("Favorite Button", () => {
  test("should show favorite button on article preview in home feed", async ({
    page,
  }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    await createArticleApi(token, `Fav Home ${Date.now()}`);

    await page.goto(`${BASE}/`);
    // Wait for articles to load
    await expect(page.locator('[class*="favoriteBtn"]').first()).toBeVisible({
      timeout: 10000,
    });
  });

  test("should show favorite button on article page for non-author", async ({
    page,
  }) => {
    const author = uniqueUser();
    const authorToken = await registerApi(author);
    const slug = await createArticleApi(
      authorToken,
      `Fav Article ${Date.now()}`,
    );

    const viewer = uniqueUser();
    const viewerToken = await registerApi(viewer);
    await loginInBrowser(page, viewer.email, viewerToken, viewer.username);

    await page.goto(`${BASE}/#/article/${slug}`);
    await page.reload();

    await expect(
      page.getByText(/Favorite Article \(\d+\)/),
    ).toBeVisible({ timeout: 10000 });
  });

  test("should not show favorite button for article author", async ({
    page,
  }) => {
    const author = uniqueUser();
    const authorToken = await registerApi(author);
    const slug = await createArticleApi(
      authorToken,
      `Own Article ${Date.now()}`,
    );

    await loginInBrowser(page, author.email, authorToken, author.username);
    await page.goto(`${BASE}/#/article/${slug}`);
    await page.reload();

    // Author should see edit/delete, not favorite
    await expect(page.getByText("Edit Article")).toBeVisible({
      timeout: 10000,
    });
    await expect(
      page.getByText(/Favorite Article/),
    ).not.toBeVisible();
  });

  test("should toggle favorite on article preview (optimistic update)", async ({
    page,
  }) => {
    const author = uniqueUser();
    const authorToken = await registerApi(author);
    await createArticleApi(authorToken, `Toggle Fav ${Date.now()}`);

    const viewer = uniqueUser();
    const viewerToken = await registerApi(viewer);
    await loginInBrowser(page, viewer.email, viewerToken, viewer.username);

    await page.goto(`${BASE}/`);
    await page.reload();

    // Authenticated user sees "Your Feed" by default — switch to "Global Feed"
    await page.getByText("Global Feed").click();

    // Find the first favorite button
    const favBtn = page.locator('[class*="favoriteBtn"]').first();
    await expect(favBtn).toBeVisible({ timeout: 10000 });

    // Initial count should be 0
    await expect(favBtn).toContainText("0");

    // Click to favorite
    await favBtn.click();

    // Should show count 1 (optimistic)
    await expect(favBtn).toContainText("1", { timeout: 5000 });

    // Click to unfavorite
    await favBtn.click();

    // Should show count 0 again
    await expect(favBtn).toContainText("0", { timeout: 5000 });
  });

  test("should toggle favorite on article page", async ({ page }) => {
    const author = uniqueUser();
    const authorToken = await registerApi(author);
    const slug = await createArticleApi(
      authorToken,
      `Page Fav ${Date.now()}`,
    );

    const viewer = uniqueUser();
    const viewerToken = await registerApi(viewer);
    await loginInBrowser(page, viewer.email, viewerToken, viewer.username);

    await page.goto(`${BASE}/#/article/${slug}`);
    await page.reload();

    const favBtn = page.getByText(/Favorite Article \(\d+\)/);
    await expect(favBtn).toBeVisible({ timeout: 10000 });
    await expect(favBtn).toContainText("(0)");

    // Click to favorite
    await favBtn.click();

    // Should change to "Unfavorite Article (1)"
    await expect(
      page.getByText(/Unfavorite Article \(1\)/),
    ).toBeVisible({ timeout: 5000 });

    // Click to unfavorite
    await page.getByText(/Unfavorite Article/).click();

    // Should change back
    await expect(
      page.getByText(/Favorite Article \(0\)/),
    ).toBeVisible({ timeout: 5000 });
  });

  test("should redirect to login when unauthenticated user clicks favorite", async ({
    page,
  }) => {
    const author = uniqueUser();
    const authorToken = await registerApi(author);
    await createArticleApi(authorToken, `NoAuth Fav ${Date.now()}`);

    await page.goto(`${BASE}/`);

    const favBtn = page.locator('[class*="favoriteBtn"]').first();
    await expect(favBtn).toBeVisible({ timeout: 10000 });

    await favBtn.click();

    // Should navigate to login page
    await expect(page).toHaveURL(/\/#\/login/, { timeout: 5000 });
  });

  test("should show active style when article is favorited", async ({
    page,
  }) => {
    const author = uniqueUser();
    const authorToken = await registerApi(author);
    const slug = await createArticleApi(
      authorToken,
      `Style Fav ${Date.now()}`,
    );

    const viewer = uniqueUser();
    const viewerToken = await registerApi(viewer);

    // Favorite via API first
    await fetch(`${API}/articles/${slug}/favorite`, {
      method: "POST",
      headers: { Authorization: `Token ${viewerToken}` },
    });

    await loginInBrowser(page, viewer.email, viewerToken, viewer.username);
    await page.goto(`${BASE}/#/article/${slug}`);
    await page.reload();

    // Should show "Unfavorite" (already favorited)
    await expect(
      page.getByText(/Unfavorite Article \(1\)/),
    ).toBeVisible({ timeout: 10000 });
  });

  test("should show favorite button on profile page article list", async ({
    page,
  }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    await createArticleApi(token, `Profile Fav ${Date.now()}`);

    await loginInBrowser(page, user.email, token, user.username);
    await page.goto(`${BASE}/#/profile/${user.username}`);
    await page.reload();

    await expect(page.locator('[class*="favoriteBtn"]').first()).toBeVisible({
      timeout: 10000,
    });
  });
});
