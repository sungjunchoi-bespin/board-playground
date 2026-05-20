import { test, expect, type Page } from "@playwright/test";

const BASE = "http://localhost:5174";
const API = "http://localhost:8080/api";

let userCounter = 0;
function uniqueUser() {
  userCounter++;
  const ts = Date.now();
  return {
    username: `cmtuser${userCounter}${ts}`,
    email: `cmtuser${userCounter}${ts}@test.com`,
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

test.describe("Comment Section", () => {
  test("should show sign in prompt when not authenticated", async ({
    page,
  }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    const slug = await createArticleApi(token, `Comment Test ${Date.now()}`);

    await page.goto(`${BASE}/#/article/${slug}`);
    await expect(
      page.getByText("to add comments on this article"),
    ).toBeVisible({ timeout: 10000 });
    // The sign-in prompt should contain links
    await expect(
      page.locator('[class*="signInPrompt"]'),
    ).toBeVisible();
  });

  test("should show comment form when authenticated", async ({ page }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    const slug = await createArticleApi(token, `Comment Form ${Date.now()}`);

    await loginInBrowser(page, user.email, token, user.username);
    await page.goto(`${BASE}/#/article/${slug}`);
    await page.reload();

    await expect(page.getByPlaceholder("Write a comment...")).toBeVisible({
      timeout: 10000,
    });
    await expect(page.getByText("Post Comment")).toBeVisible();
  });

  test("should post a comment and show it in the list", async ({ page }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    const slug = await createArticleApi(token, `Add Comment ${Date.now()}`);

    await loginInBrowser(page, user.email, token, user.username);
    await page.goto(`${BASE}/#/article/${slug}`);
    await page.reload();

    const textarea = page.getByPlaceholder("Write a comment...");
    await expect(textarea).toBeVisible({ timeout: 10000 });

    await textarea.fill("This is my test comment!");
    await page.getByText("Post Comment").click();

    // Comment should appear in the list
    await expect(page.getByText("This is my test comment!")).toBeVisible({
      timeout: 10000,
    });
    // Author name should be visible in the comment card
    await expect(
      page.locator('[class*="commentAuthorName"]').first(),
    ).toContainText(user.username);
  });

  test("should clear textarea after posting", async ({ page }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    const slug = await createArticleApi(token, `Clear Textarea ${Date.now()}`);

    await loginInBrowser(page, user.email, token, user.username);
    await page.goto(`${BASE}/#/article/${slug}`);
    await page.reload();

    const textarea = page.getByPlaceholder("Write a comment...");
    await expect(textarea).toBeVisible({ timeout: 10000 });

    await textarea.fill("Comment to clear");
    await page.getByText("Post Comment").click();

    // Wait for the comment to appear
    await expect(page.getByText("Comment to clear")).toBeVisible({
      timeout: 10000,
    });

    // Textarea should be cleared
    await expect(textarea).toHaveValue("");
  });

  test("should show delete button only for own comments", async ({ page }) => {
    const author = uniqueUser();
    const authorToken = await registerApi(author);
    const slug = await createArticleApi(
      authorToken,
      `Delete Check ${Date.now()}`,
    );

    // Author posts a comment via API
    await fetch(`${API}/articles/${slug}/comments`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Token ${authorToken}`,
      },
      body: JSON.stringify({ comment: { body: "Author comment" } }),
    });

    // Another user views the article
    const viewer = uniqueUser();
    const viewerToken = await registerApi(viewer);

    await loginInBrowser(page, viewer.email, viewerToken, viewer.username);
    await page.goto(`${BASE}/#/article/${slug}`);
    await page.reload();

    await expect(page.getByText("Author comment")).toBeVisible({
      timeout: 10000,
    });

    // No delete button since it's not own comment
    await expect(page.locator('[class*="commentDeleteBtn"]')).toHaveCount(0);
  });

  test("should delete own comment", async ({ page }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    const slug = await createArticleApi(
      token,
      `Delete Comment ${Date.now()}`,
    );

    await loginInBrowser(page, user.email, token, user.username);
    await page.goto(`${BASE}/#/article/${slug}`);
    await page.reload();

    const textarea = page.getByPlaceholder("Write a comment...");
    await expect(textarea).toBeVisible({ timeout: 10000 });

    await textarea.fill("Comment to delete");
    await page.getByText("Post Comment").click();

    await expect(page.getByText("Comment to delete")).toBeVisible({
      timeout: 10000,
    });

    // Click delete button
    await page.locator('[class*="commentDeleteBtn"]').click();

    // Comment should be removed
    await expect(page.getByText("Comment to delete")).not.toBeVisible({
      timeout: 5000,
    });
  });

  test("should show multiple comments", async ({ page }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    const slug = await createArticleApi(
      token,
      `Multi Comment ${Date.now()}`,
    );

    // Create comments via API
    for (const body of ["First comment", "Second comment", "Third comment"]) {
      await fetch(`${API}/articles/${slug}/comments`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Token ${token}`,
        },
        body: JSON.stringify({ comment: { body } }),
      });
    }

    await page.goto(`${BASE}/#/article/${slug}`);
    await expect(page.getByText("First comment")).toBeVisible({
      timeout: 10000,
    });
    await expect(page.getByText("Second comment")).toBeVisible();
    await expect(page.getByText("Third comment")).toBeVisible();

    const commentCards = page.locator('[class*="commentCard"]');
    const count = await commentCards.count();
    expect(count).toBeGreaterThanOrEqual(3);
  });

  test("Post Comment button should be disabled when textarea is empty", async ({
    page,
  }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    const slug = await createArticleApi(
      token,
      `Disabled Btn ${Date.now()}`,
    );

    await loginInBrowser(page, user.email, token, user.username);
    await page.goto(`${BASE}/#/article/${slug}`);
    await page.reload();

    await expect(page.getByPlaceholder("Write a comment...")).toBeVisible({
      timeout: 10000,
    });

    const postBtn = page.getByText("Post Comment");
    await expect(postBtn).toBeDisabled();
  });
});
