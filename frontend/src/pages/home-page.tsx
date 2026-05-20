import { useState, useEffect } from "react";
import { useAuth } from "@/hooks/use-auth";
import {
  listArticlesApi,
  feedArticlesApi,
  type Article,
} from "@/api/articles";
import { getTagsApi } from "@/api/tags";
import ArticlePreview from "@/components/article-preview";
import LoadingState from "@/components/state/loading-state";
import EmptyState from "@/components/state/empty-state";
import styles from "./home-page.module.css";

const ARTICLES_PER_PAGE = 10;

type FeedTab = "your" | "global" | "tag";

function HomePage() {
  const { isAuthenticated } = useAuth();
  const [activeTab, setActiveTab] = useState<FeedTab>(
    isAuthenticated ? "your" : "global",
  );
  const [selectedTag, setSelectedTag] = useState("");
  const [articles, setArticles] = useState<Article[]>([]);
  const [articlesCount, setArticlesCount] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [tags, setTags] = useState<string[]>([]);
  const [tagsLoading, setTagsLoading] = useState(true);

  // Load tags once
  useEffect(() => {
    setTagsLoading(true);
    getTagsApi()
      .then(setTags)
      .catch(() => setTags([]))
      .finally(() => setTagsLoading(false));
  }, []);

  // Reset to appropriate tab when auth state changes
  useEffect(() => {
    if (!isAuthenticated && activeTab === "your") {
      setActiveTab("global");
    }
  }, [isAuthenticated, activeTab]);

  // Fetch articles when tab/page/tag changes
  useEffect(() => {
    setLoading(true);
    const offset = (currentPage - 1) * ARTICLES_PER_PAGE;

    let request: Promise<{ articles: Article[]; articlesCount: number }>;

    if (activeTab === "your") {
      request = feedArticlesApi(ARTICLES_PER_PAGE, offset);
    } else if (activeTab === "tag" && selectedTag) {
      request = listArticlesApi({
        tag: selectedTag,
        limit: ARTICLES_PER_PAGE,
        offset,
      });
    } else {
      request = listArticlesApi({ limit: ARTICLES_PER_PAGE, offset });
    }

    request
      .then((res) => {
        setArticles(res.articles);
        setArticlesCount(res.articlesCount);
      })
      .catch(() => {
        setArticles([]);
        setArticlesCount(0);
      })
      .finally(() => setLoading(false));
  }, [activeTab, currentPage, selectedTag]);

  function handleTagClick(tag: string) {
    setSelectedTag(tag);
    setActiveTab("tag");
    setCurrentPage(1);
  }

  function handleTabClick(tab: FeedTab) {
    setActiveTab(tab);
    setCurrentPage(1);
    if (tab !== "tag") setSelectedTag("");
  }

  const totalPages = Math.ceil(articlesCount / ARTICLES_PER_PAGE);

  return (
    <div className="home-page">
      {!isAuthenticated && (
        <div className={styles.banner}>
          <div className="container">
            <h1 className={styles.bannerTitle}>conduit</h1>
            <p className={styles.bannerSubtitle}>
              A place to share your knowledge.
            </p>
          </div>
        </div>
      )}

      <div className="container page">
        <div className="row">
          <div className="col-md-9">
            {/* Feed Tabs */}
            <nav className={styles.feedToggle} aria-label="Article feeds">
              <ul role="tablist">
                {isAuthenticated && (
                  <li role="presentation">
                    <button
                      role="tab"
                      aria-selected={activeTab === "your"}
                      className={
                        activeTab === "your"
                          ? styles.tabItemActive
                          : styles.tabItem
                      }
                      onClick={() => handleTabClick("your")}
                    >
                      Your Feed
                    </button>
                  </li>
                )}
                <li role="presentation">
                  <button
                    role="tab"
                    aria-selected={activeTab === "global"}
                    className={
                      activeTab === "global"
                        ? styles.tabItemActive
                        : styles.tabItem
                    }
                    onClick={() => handleTabClick("global")}
                  >
                    Global Feed
                  </button>
                </li>
                {activeTab === "tag" && selectedTag && (
                  <li role="presentation">
                    <button
                      role="tab"
                      aria-selected="true"
                      className={styles.tabItemActive}
                    >
                      # {selectedTag}
                    </button>
                  </li>
                )}
              </ul>
            </nav>

            {/* Articles */}
            {loading ? (
              <LoadingState label="Loading articles..." />
            ) : articles.length === 0 ? (
              <EmptyState
                icon="📰"
                title="No articles are here... yet."
                hint={
                  activeTab === "your"
                    ? "Follow other users to see their articles, or write your own."
                    : "Be the first to share an article."
                }
              />
            ) : (
              articles.map((article) => (
                <ArticlePreview key={article.slug} article={article} />
              ))
            )}

            {/* Pagination */}
            {totalPages > 1 && (
              <nav aria-label="Article pages">
                <ul className={styles.pagination}>
                  {Array.from({ length: totalPages }, (_, i) => i + 1).map(
                    (page) => (
                      <li
                        key={page}
                        className={`${styles.pageItem} ${page === currentPage ? styles.pageItemActive : ""}`}
                      >
                        <button
                          className={styles.pageLink}
                          onClick={() => setCurrentPage(page)}
                          aria-current={
                            page === currentPage ? "page" : undefined
                          }
                          aria-label={`Go to page ${page}`}
                        >
                          {page}
                        </button>
                      </li>
                    ),
                  )}
                </ul>
              </nav>
            )}
          </div>

          <div className="col-md-3">
            <TagSidebar
              tags={tags}
              loading={tagsLoading}
              onTagClick={handleTagClick}
            />
          </div>
        </div>
      </div>
    </div>
  );
}

function TagSidebar({
  tags,
  loading,
  onTagClick,
}: {
  tags: string[];
  loading: boolean;
  onTagClick: (tag: string) => void;
}) {
  return (
    <aside className={styles.sidebar} aria-labelledby="popular-tags-title">
      <p id="popular-tags-title" className={styles.sidebarTitle}>
        Popular Tags
      </p>
      {loading ? (
        <LoadingState label="Loading tags..." size="sm" />
      ) : tags.length === 0 ? (
        <p className={styles.sidebarEmpty}>No tags yet.</p>
      ) : (
        <div className={styles.tagListSidebar}>
          {tags.map((tag) => (
            <button
              key={tag}
              className={styles.sidebarTag}
              onClick={() => onTagClick(tag)}
              aria-label={`Filter by ${tag} tag`}
            >
              {tag}
            </button>
          ))}
        </div>
      )}
    </aside>
  );
}

export default HomePage;
