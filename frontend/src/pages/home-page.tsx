import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "@/hooks/use-auth";
import {
  listArticlesApi,
  feedArticlesApi,
  type Article,
} from "@/api/articles";
import { getTagsApi } from "@/api/tags";
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

  // Load tags once
  useEffect(() => {
    getTagsApi().then(setTags).catch(() => {});
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
            <div className={styles.feedToggle}>
              <ul>
                {isAuthenticated && (
                  <li>
                    <button
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
                <li>
                  <button
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
                  <li>
                    <button className={styles.tabItemActive}>
                      # {selectedTag}
                    </button>
                  </li>
                )}
              </ul>
            </div>

            {/* Articles */}
            {loading ? (
              <p className={styles.loadingMessage}>Loading articles...</p>
            ) : articles.length === 0 ? (
              <p className={styles.loadingMessage}>
                No articles are here... yet.
              </p>
            ) : (
              articles.map((article) => (
                <ArticlePreview key={article.slug} article={article} />
              ))
            )}

            {/* Pagination */}
            {totalPages > 1 && (
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
                      >
                        {page}
                      </button>
                    </li>
                  ),
                )}
              </ul>
            )}
          </div>

          <div className="col-md-3">
            <TagSidebar tags={tags} onTagClick={handleTagClick} />
          </div>
        </div>
      </div>
    </div>
  );
}

function ArticlePreview({ article }: { article: Article }) {
  const formattedDate = new Date(article.createdAt).toLocaleDateString(
    "en-US",
    { year: "numeric", month: "long", day: "numeric" },
  );

  return (
    <div className={styles.articlePreview}>
      <div className={styles.articleMeta}>
        <Link to={`/profile/${article.author.username}`}>
          <img
            className={styles.authorImage}
            src={
              article.author.image ||
              "https://api.realworld.io/images/smiley-cyrus.jpeg"
            }
            alt={article.author.username}
          />
        </Link>
        <div className={styles.authorInfo}>
          <Link
            to={`/profile/${article.author.username}`}
            className={styles.authorName}
          >
            {article.author.username}
          </Link>
          <span className={styles.articleDate}>{formattedDate}</span>
        </div>
        <span className={styles.favCount}>
          ♥ {article.favoritesCount}
        </span>
      </div>
      <Link to={`/article/${article.slug}`} className={styles.previewLink}>
        <h2 className={styles.previewTitle}>{article.title}</h2>
        <p className={styles.previewDescription}>{article.description}</p>
        <div className={styles.readMore}>
          <span className={styles.readMoreText}>Read more...</span>
          {article.tagList.length > 0 && (
            <ul className={styles.previewTagList}>
              {article.tagList.map((tag) => (
                <li key={tag} className={styles.previewTag}>
                  {tag}
                </li>
              ))}
            </ul>
          )}
        </div>
      </Link>
    </div>
  );
}

function TagSidebar({
  tags,
  onTagClick,
}: {
  tags: string[];
  onTagClick: (tag: string) => void;
}) {
  return (
    <div className={styles.sidebar}>
      <p className={styles.sidebarTitle}>Popular Tags</p>
      {tags.length === 0 ? (
        <p>Loading tags...</p>
      ) : (
        <div className={styles.tagListSidebar}>
          {tags.map((tag) => (
            <button
              key={tag}
              className={styles.sidebarTag}
              onClick={() => onTagClick(tag)}
            >
              {tag}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

export default HomePage;
