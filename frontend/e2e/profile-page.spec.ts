import { test, expect, type Page } from "@playwright/test";

const BASE = "http://localhost:5174";
const API = "http://localhost:8080/api";

let userCounter = 0;
function uniqueUser() {
  userCounter++;
  const ts = Date.now();
  return {
    username: `profuser${userCounter}${ts}`,
    email: `profuser${userCounter}${ts}@test.com`,
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

async function createArticleApi(
  token: string,
  title: string,
  tags: string[] = [],
) {
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
        tagList: tags,
      },
    }),
  });
  const data = await res.json();
  return data.article.slug as string;
}

async function favoriteArticleApi(token: string, slug: string) {
  await fetch(`${API}/articles/${slug}/favorite`, {
    method: "POST",
    headers: { Authorization: `Token ${token}` },
  });
}

async function followUserApi(token: string, username: string) {
  await fetch(`${API}/profiles/${username}/follow`, {
    method: "POST",
    headers: { Authorization: `Token ${token}` },
  });
}

async function loginInBrowser(
  page: Page,
  email: string,
  token: string,
  username: string,
) {
  // Navigate first to have a valid origin for localStorage
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

test.describe("Profile Page", () => {
  test("should show profile banner with username and image", async ({
    page,
  }) => {
    const user = uniqueUser();
    await registerApi(user);

    await page.goto(`${BASE}/#/profile/${user.username}`);
    await expect(page.locator("h4")).toContainText(user.username);
    await expect(page.locator("img").first()).toBeVisible();
  });

  test("should show 'My Articles' tab with user's articles", async ({
    page,
  }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    await createArticleApi(token, `Profile Article One ${Date.now()}`);
    await createArticleApi(token, `Profile Article Two ${Date.now()}`);

    await page.goto(`${BASE}/#/profile/${user.username}`);
    await expect(page.getByText("My Articles")).toBeVisible();

    // Wait for articles to load
    await expect(
      page.locator('[class*="articlePreview"]').first(),
    ).toBeVisible({ timeout: 10000 });
    const articles = page.locator('[class*="articlePreview"]');
    await expect(articles).toHaveCount(2);
  });

  test("should show 'Favorited Articles' tab with favorited articles", async ({
    page,
  }) => {
    const author = uniqueUser();
    const authorToken = await registerApi(author);
    const slug = await createArticleApi(
      authorToken,
      `Fav Target ${Date.now()}`,
    );

    const viewer = uniqueUser();
    const viewerToken = await registerApi(viewer);
    await favoriteArticleApi(viewerToken, slug);

    await page.goto(`${BASE}/#/profile/${viewer.username}/favorites`);
    await expect(page.getByText("Favorited Articles")).toBeVisible();

    await expect(
      page.locator('[class*="articlePreview"]').first(),
    ).toBeVisible({ timeout: 10000 });
    const articles = page.locator('[class*="articlePreview"]');
    await expect(articles).toHaveCount(1);
  });

  test("should show 'Edit Profile Settings' button on own profile", async ({
    page,
  }) => {
    const user = uniqueUser();
    const token = await registerApi(user);

    await loginInBrowser(page, user.email, token, user.username);
    await page.goto(`${BASE}/#/profile/${user.username}`);
    await page.reload();

    await expect(page.getByText("Edit Profile Settings")).toBeVisible({
      timeout: 10000,
    });
  });

  test("should show Follow/Unfollow button on other user's profile", async ({
    page,
  }) => {
    const other = uniqueUser();
    await registerApi(other);

    const viewer = uniqueUser();
    const viewerToken = await registerApi(viewer);

    await loginInBrowser(page, viewer.email, viewerToken, viewer.username);
    await page.goto(`${BASE}/#/profile/${other.username}`);
    await page.reload();

    const followBtn = page.getByText(`Follow ${other.username}`, {
      exact: false,
    });
    await expect(followBtn).toBeVisible({ timeout: 10000 });
  });

  test("should toggle Follow/Unfollow on click", async ({ page }) => {
    const other = uniqueUser();
    await registerApi(other);

    const viewer = uniqueUser();
    const viewerToken = await registerApi(viewer);

    await loginInBrowser(page, viewer.email, viewerToken, viewer.username);
    await page.goto(`${BASE}/#/profile/${other.username}`);
    await page.reload();

    // Click Follow
    const followBtn = page.getByText(`Follow ${other.username}`, {
      exact: false,
    });
    await expect(followBtn).toBeVisible({ timeout: 10000 });
    await followBtn.click();

    // Should now show Unfollow
    await expect(
      page.getByText(`Unfollow ${other.username}`, { exact: false }),
    ).toBeVisible({ timeout: 10000 });

    // Click Unfollow
    await page
      .getByText(`Unfollow ${other.username}`, { exact: false })
      .click();

    // Should show Follow again
    await expect(
      page.getByText(`Follow ${other.username}`, { exact: false }),
    ).toBeVisible({ timeout: 10000 });
  });

  test("should not show Follow button when not authenticated", async ({
    page,
  }) => {
    const user = uniqueUser();
    await registerApi(user);

    await page.goto(`${BASE}/#/profile/${user.username}`);
    await expect(page.locator("h4")).toContainText(user.username);

    // No follow button for unauthenticated user
    await expect(
      page.getByText("Follow", { exact: false }),
    ).not.toBeVisible();
    await expect(
      page.getByText("Edit Profile Settings"),
    ).not.toBeVisible();
  });

  test("should switch between My Articles and Favorited Articles tabs", async ({
    page,
  }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    await createArticleApi(token, `My Own Article ${Date.now()}`);

    // Create an article by another user and favorite it
    const other = uniqueUser();
    const otherToken = await registerApi(other);
    const slug = await createArticleApi(
      otherToken,
      `Other Fav Article ${Date.now()}`,
    );
    await favoriteArticleApi(token, slug);

    await page.goto(`${BASE}/#/profile/${user.username}`);

    // My Articles tab should show 1 article
    await expect(
      page.locator('[class*="articlePreview"]').first(),
    ).toBeVisible({ timeout: 10000 });

    const myArticles = page.locator('[class*="articlePreview"]');
    await expect(myArticles).toHaveCount(1);

    // Click Favorited Articles tab
    await page.getByText("Favorited Articles").click();
    await page.waitForURL(/\/favorites/);

    await expect(
      page.locator('[class*="articlePreview"]').first(),
    ).toBeVisible({ timeout: 10000 });
    const favArticles = page.locator('[class*="articlePreview"]');
    await expect(favArticles).toHaveCount(1);
  });

  test("should show empty message when no articles", async ({ page }) => {
    const user = uniqueUser();
    await registerApi(user);

    await page.goto(`${BASE}/#/profile/${user.username}`);
    await expect(
      page.getByText("No articles are here... yet."),
    ).toBeVisible({ timeout: 10000 });
  });

  test("should show User not found for nonexistent profile", async ({
    page,
  }) => {
    await page.goto(`${BASE}/#/profile/nonexistent_user_xyz`);
    await expect(page.getByText("User not found.")).toBeVisible({
      timeout: 10000,
    });
  });

  test("should navigate to article from profile", async ({ page }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    await createArticleApi(token, `Clickable Article ${Date.now()}`);

    await page.goto(`${BASE}/#/profile/${user.username}`);
    await expect(
      page.locator('[class*="articlePreview"]').first(),
    ).toBeVisible({ timeout: 10000 });

    // Click on the article preview title/link
    await page.locator('[class*="previewLink"]').first().click();
    await expect(page).toHaveURL(/article/);
  });
});
