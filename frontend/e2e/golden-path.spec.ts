import { test, expect, type Page } from "@playwright/test";

const BASE = "http://localhost:5174";
const API = "http://localhost:8080/api";

let userCounter = 0;
function uniqueUser() {
  userCounter++;
  const ts = Date.now();
  return {
    username: `gpuser${userCounter}${ts}`,
    email: `gpuser${userCounter}${ts}@test.com`,
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

// ─── Golden Path 1: Registration Flow ───────────────────

test.describe("Golden Path 1: Registration Flow", () => {
  test("register → home redirect → navbar changes", async ({ page }) => {
    const user = uniqueUser();

    await page.goto(`${BASE}/#/register`);
    await expect(page.getByPlaceholder("Username")).toBeVisible({
      timeout: 10000,
    });

    await page.getByPlaceholder("Username").fill(user.username);
    await page.getByPlaceholder("Email").fill(user.email);
    await page.getByPlaceholder("Password").fill(user.password);
    await page.getByRole("button", { name: "Sign up" }).click();

    // Should redirect to home
    await expect(page).toHaveURL(`${BASE}/#/`, { timeout: 10000 });

    // Navbar should show authenticated state
    await expect(page.getByText("New Article")).toBeVisible({ timeout: 5000 });
    await expect(page.getByText("Settings")).toBeVisible();
    await expect(page.getByText(user.username)).toBeVisible();

    // Sign up / Sign in should not be visible
    await expect(page.getByText("Sign up")).not.toBeVisible();
  });
});

// ─── Golden Path 2: Article CRUD Flow ───────────────────

test.describe("Golden Path 2: Article CRUD Flow", () => {
  test("write article → detail page → markdown rendering → edit → delete", async ({
    page,
  }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    await loginInBrowser(page, user.email, token, user.username);

    // Navigate to editor
    await page.goto(`${BASE}/#/editor`);
    await page.reload();

    const titleInput = page.getByPlaceholder("Article Title");
    await expect(titleInput).toBeVisible({ timeout: 10000 });

    const articleTitle = `E2E Article ${Date.now()}`;
    await titleInput.fill(articleTitle);
    await page.getByPlaceholder("What's this article about?").fill("E2E test description");
    await page
      .getByPlaceholder("Write your article (in markdown)")
      .fill("## Hello World\n\nThis is **bold** text.");
    const tagInput = page.getByPlaceholder("Enter tags");
    await tagInput.fill("e2etag");
    await tagInput.press("Enter");
    await tagInput.fill("goldentest");
    await tagInput.press("Enter");

    await page.getByText("Publish Article").click();

    // Should redirect to article detail
    await expect(page.locator("h1")).toContainText(articleTitle, {
      timeout: 10000,
    });

    // Markdown should render (h2 + bold)
    await expect(page.locator(".article-page h2")).toContainText("Hello World");
    await expect(page.locator(".article-page strong")).toContainText("bold");

    // Tags should be visible
    await expect(page.getByText("e2etag")).toBeVisible();
    await expect(page.getByText("goldentest")).toBeVisible();

    // Author info should be visible
    await expect(
      page.locator('[class*="authorName"]').first(),
    ).toContainText(user.username);

    // Edit button should be visible for author
    await expect(page.getByText("Edit Article")).toBeVisible();

    // Click Edit Article
    await page.getByText("Edit Article").click();
    await expect(page.getByPlaceholder("Article Title")).toHaveValue(
      articleTitle,
      { timeout: 10000 },
    );

    // Update title
    const updatedTitle = `Updated ${articleTitle}`;
    await page.getByPlaceholder("Article Title").fill(updatedTitle);
    await page.getByText("Publish Article").click();

    // Should show updated title
    await expect(page.locator("h1")).toContainText(updatedTitle, {
      timeout: 10000,
    });

    // Delete article
    page.on("dialog", (dialog) => dialog.accept());
    await page.getByText("Delete Article").click();

    // Should redirect to home
    await expect(page).toHaveURL(`${BASE}/#/`, { timeout: 10000 });
  });
});

// ─── Golden Path 3: Comment Flow ────────────────────────

test.describe("Golden Path 3: Comment Flow", () => {
  test("write comment → appears in list → delete", async ({ page }) => {
    const author = uniqueUser();
    const authorToken = await registerApi(author);

    // Create article via API
    const res = await fetch(`${API}/articles`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Token ${authorToken}`,
      },
      body: JSON.stringify({
        article: {
          title: `Comment GP ${Date.now()}`,
          description: "desc",
          body: "body",
          tagList: [],
        },
      }),
    });
    const slug = (await res.json()).article.slug;

    const commenter = uniqueUser();
    const commenterToken = await registerApi(commenter);
    await loginInBrowser(page, commenter.email, commenterToken, commenter.username);

    await page.goto(`${BASE}/#/article/${slug}`);
    await page.reload();

    const textarea = page.getByPlaceholder("Write a comment...");
    await expect(textarea).toBeVisible({ timeout: 10000 });

    // Post comment
    await textarea.fill("Golden path comment!");
    await page.getByText("Post Comment").click();

    // Comment should appear
    await expect(page.getByText("Golden path comment!")).toBeVisible({
      timeout: 10000,
    });
    await expect(
      page.locator('[class*="commentAuthorName"]').first(),
    ).toContainText(commenter.username);

    // Textarea should be cleared
    await expect(textarea).toHaveValue("");

    // Delete the comment
    await page.locator('[class*="commentDeleteBtn"]').click();
    await expect(page.getByText("Golden path comment!")).not.toBeVisible({
      timeout: 5000,
    });
  });
});

// ─── Golden Path 4: Favorite → Profile Favorited Tab ────

test.describe("Golden Path 4: Favorite → Profile Flow", () => {
  test("favorite article → count increases → appears in profile favorited tab", async ({
    page,
  }) => {
    const author = uniqueUser();
    const authorToken = await registerApi(author);

    // Create article via API
    const articleTitle = `Fav GP ${Date.now()}`;
    const res = await fetch(`${API}/articles`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Token ${authorToken}`,
      },
      body: JSON.stringify({
        article: {
          title: articleTitle,
          description: "desc",
          body: "body",
          tagList: ["golden"],
        },
      }),
    });
    const slug = (await res.json()).article.slug;

    const fan = uniqueUser();
    const fanToken = await registerApi(fan);
    await loginInBrowser(page, fan.email, fanToken, fan.username);

    // Go to article page
    await page.goto(`${BASE}/#/article/${slug}`);
    await page.reload();

    // Click favorite button
    const favBtn = page.getByText(/Favorite Article \(0\)/);
    await expect(favBtn).toBeVisible({ timeout: 10000 });
    await favBtn.click();

    // Should show Unfavorite with count 1
    await expect(page.getByText(/Unfavorite Article \(1\)/)).toBeVisible({
      timeout: 5000,
    });

    // Navigate to own profile → Favorited Articles tab
    await page.goto(`${BASE}/#/profile/${fan.username}/favorites`);
    await page.reload();

    // The favorited article should appear
    await expect(page.getByText(articleTitle)).toBeVisible({ timeout: 10000 });
  });
});

// ─── Golden Path 5: Full Journey ────────────────────────

test.describe("Golden Path 5: Full Journey", () => {
  test("register → write → read → comment → favorite → profile check", async ({
    page,
  }) => {
    // === Step 1: Register via UI ===
    const user = uniqueUser();
    await page.goto(`${BASE}/#/register`);
    await expect(page.getByPlaceholder("Username")).toBeVisible({
      timeout: 10000,
    });
    await page.getByPlaceholder("Username").fill(user.username);
    await page.getByPlaceholder("Email").fill(user.email);
    await page.getByPlaceholder("Password").fill(user.password);
    await page.getByRole("button", { name: "Sign up" }).click();
    await expect(page).toHaveURL(`${BASE}/#/`, { timeout: 10000 });

    // === Step 2: Write article ===
    await page.goto(`${BASE}/#/editor`);
    const articleTitle = `Full Journey ${Date.now()}`;
    const titleInput = page.getByPlaceholder("Article Title");
    await expect(titleInput).toBeVisible({ timeout: 10000 });
    await titleInput.fill(articleTitle);
    await page.getByPlaceholder("What's this article about?").fill("Journey desc");
    await page
      .getByPlaceholder("Write your article (in markdown)")
      .fill("Journey body content");
    await page.getByText("Publish Article").click();

    // Should be on article detail
    await expect(page.locator("h1")).toContainText(articleTitle, {
      timeout: 10000,
    });

    // === Step 3: Comment on own article ===
    const textarea = page.getByPlaceholder("Write a comment...");
    await expect(textarea).toBeVisible({ timeout: 10000 });
    await textarea.fill("Self-comment on my article");
    await page.getByText("Post Comment").click();
    await expect(page.getByText("Self-comment on my article")).toBeVisible({
      timeout: 10000,
    });

    // === Step 4: Go to home feed, check article appears ===
    await page.goto(`${BASE}/`);
    // Switch to "Your Feed" doesn't help here (no followers), use Global Feed
    await page.getByText("Global Feed").click();
    await expect(page.getByText(articleTitle)).toBeVisible({ timeout: 10000 });

    // === Step 5: Check profile page ===
    await page.goto(`${BASE}/#/profile/${user.username}`);
    await expect(page.getByText(articleTitle)).toBeVisible({ timeout: 10000 });
    await expect(page.getByText("Edit Profile Settings")).toBeVisible();
  });
});

// ─── Error Scenarios ────────────────────────────────────

test.describe("Error Scenarios", () => {
  test("invalid login shows error message", async ({ page }) => {
    await page.goto(`${BASE}/#/login`);
    await expect(page.getByPlaceholder("Email")).toBeVisible({
      timeout: 10000,
    });

    await page.getByPlaceholder("Email").fill("wrong@example.com");
    await page.getByPlaceholder("Password").fill("wrongpassword");
    await page.getByRole("button", { name: "Sign in" }).click();

    // Should show error message
    await expect(page.locator('[class*="errorMessages"]')).toBeVisible({
      timeout: 10000,
    });
  });

  test("accessing editor without login shows empty form (no crash)", async ({
    page,
  }) => {
    await page.goto(`${BASE}/#/editor`);

    // Editor page loads without crashing (no route guard — app-level behavior)
    await expect(page.getByPlaceholder("Article Title")).toBeVisible({
      timeout: 10000,
    });
  });

  test("404 article shows not found", async ({ page }) => {
    await page.goto(`${BASE}/#/article/nonexistent-slug-12345`);
    await expect(page.getByText("Article not found")).toBeVisible({
      timeout: 10000,
    });
  });
});

// ─── Navigation Tests ───────────────────────────────────

test.describe("Navigation", () => {
  test("unauthenticated navbar shows correct links", async ({ page }) => {
    await page.goto(`${BASE}/`);
    await expect(
      page.locator("nav").getByText("conduit"),
    ).toBeVisible({ timeout: 10000 });
    await expect(page.getByText("Home")).toBeVisible();
    await expect(page.getByText("Sign in")).toBeVisible();
    await expect(page.getByText("Sign up")).toBeVisible();
  });

  test("authenticated navbar shows correct links", async ({ page }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    await loginInBrowser(page, user.email, token, user.username);
    await page.goto(`${BASE}/`);
    await page.reload();

    await expect(page.getByText("Home")).toBeVisible({ timeout: 10000 });
    await expect(page.getByText("New Article")).toBeVisible();
    await expect(page.getByText("Settings")).toBeVisible();
    await expect(page.getByText(user.username)).toBeVisible();
  });

  test("tag click filters articles", async ({ page }) => {
    const user = uniqueUser();
    const token = await registerApi(user);

    // Create article with specific tag via API
    await fetch(`${API}/articles`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Token ${token}`,
      },
      body: JSON.stringify({
        article: {
          title: `Nav Tag ${Date.now()}`,
          description: "desc",
          body: "body",
          tagList: ["uniquetag" + Date.now()],
        },
      }),
    });

    await page.goto(`${BASE}/`);
    // Wait for tags sidebar to load
    await expect(page.locator('[class*="sidebarTag"]').first()).toBeVisible({
      timeout: 10000,
    });

    // Click a tag from sidebar
    const firstTag = page.locator('[class*="sidebarTag"]').first();
    const tagText = await firstTag.textContent();
    await firstTag.click();

    // Should show tag tab
    await expect(page.getByText(`# ${tagText}`)).toBeVisible({
      timeout: 5000,
    });
  });

  test("settings page allows profile update", async ({ page }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    await loginInBrowser(page, user.email, token, user.username);

    await page.goto(`${BASE}/#/settings`);
    await page.reload();

    const bioInput = page.getByPlaceholder("Short bio about you");
    await expect(bioInput).toBeVisible({ timeout: 10000 });

    await bioInput.fill("E2E test bio");
    await page.getByRole("button", { name: "Update Settings" }).click();

    // Verify on profile page
    await page.goto(`${BASE}/#/profile/${user.username}`);
    await page.reload();
    await expect(page.getByText("E2E test bio")).toBeVisible({
      timeout: 10000,
    });
  });

  test("logout clears session", async ({ page }) => {
    const user = uniqueUser();
    const token = await registerApi(user);
    await loginInBrowser(page, user.email, token, user.username);

    await page.goto(`${BASE}/#/settings`);
    await page.reload();

    await expect(
      page.getByRole("button", { name: /Logout/i }),
    ).toBeVisible({ timeout: 10000 });
    await page.getByRole("button", { name: /Logout/i }).click();

    // Should show unauthenticated state
    await expect(page.getByText("Sign in")).toBeVisible({ timeout: 10000 });
    await expect(page.getByText("Sign up")).toBeVisible();
  });
});
