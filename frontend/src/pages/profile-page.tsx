import { useState, useEffect } from "react";
import { useParams, useLocation, Link } from "react-router-dom";
import { useAuth } from "@/hooks/use-auth";
import { getProfileApi, followUserApi, unfollowUserApi } from "@/api/profiles";
import type { Profile } from "@/api/profiles";
import { listArticlesApi, type Article } from "@/api/articles";
import FavoriteButton from "@/components/favorite-button";
import styles from "./profile-page.module.css";

const ARTICLES_PER_PAGE = 10;
const DEFAULT_IMAGE = "https://api.realworld.io/images/smiley-cyrus.jpeg";

type TabType = "my" | "favorited";

function ProfilePage() {
  const { username } = useParams<{ username: string }>();
  const location = useLocation();
  const { user, isAuthenticated } = useAuth();

  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);
  const [articles, setArticles] = useState<Article[]>([]);
  const [articlesCount, setArticlesCount] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [articlesLoading, setArticlesLoading] = useState(true);
  const [followLoading, setFollowLoading] = useState(false);

  const isFavoritesRoute = location.pathname.endsWith("/favorites");
  const activeTab: TabType = isFavoritesRoute ? "favorited" : "my";
  const isOwnProfile = user?.username === username;

  // Load profile
  useEffect(() => {
    if (!username) return;
    setLoading(true);
    getProfileApi(username)
      .then(setProfile)
      .catch(() => setProfile(null))
      .finally(() => setLoading(false));
  }, [username]);

  // Load articles on tab/page change
  useEffect(() => {
    if (!username) return;
    setArticlesLoading(true);
    const offset = (currentPage - 1) * ARTICLES_PER_PAGE;
    const params =
      activeTab === "favorited"
        ? { favorited: username, limit: ARTICLES_PER_PAGE, offset }
        : { author: username, limit: ARTICLES_PER_PAGE, offset };

    listArticlesApi(params)
      .then((res) => {
        setArticles(res.articles);
        setArticlesCount(res.articlesCount);
      })
      .catch(() => {
        setArticles([]);
        setArticlesCount(0);
      })
      .finally(() => setArticlesLoading(false));
  }, [username, activeTab, currentPage]);

  // Reset page on tab change
  useEffect(() => {
    setCurrentPage(1);
  }, [activeTab]);

  async function handleFollowToggle() {
    if (!username || !profile || followLoading) return;
    setFollowLoading(true);
    try {
      const updated = profile.following
        ? await unfollowUserApi(username)
        : await followUserApi(username);
      setProfile(updated);
    } catch {
      // ignore
    } finally {
      setFollowLoading(false);
    }
  }

  if (loading) {
    return (
      <div className="profile-page">
        <div className="container page">
          <p>Loading profile...</p>
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="profile-page">
        <div className="container page">
          <p>User not found.</p>
        </div>
      </div>
    );
  }

  const totalPages = Math.ceil(articlesCount / ARTICLES_PER_PAGE);

  return (
    <div className="profile-page">
      {/* User Banner */}
      <div className={styles.userBanner}>
        <div className="container">
          <img
            className={styles.userImage}
            src={profile.image || DEFAULT_IMAGE}
            alt={profile.username}
          />
          <h4 className={styles.username}>{profile.username}</h4>
          {profile.bio && <p className={styles.userBio}>{profile.bio}</p>}
          {isOwnProfile ? (
            <Link to="/settings" className={styles.editSettingsBtn}>
              ⚙ Edit Profile Settings
            </Link>
          ) : (
            isAuthenticated && (
              <button
                className={
                  profile.following ? styles.unfollowBtn : styles.followBtn
                }
                onClick={handleFollowToggle}
                disabled={followLoading}
              >
                {profile.following
                  ? `✓ Unfollow ${profile.username}`
                  : `+ Follow ${profile.username}`}
              </button>
            )
          )}
        </div>
      </div>

      <div className="container page">
        <div className="row">
          <div className="col-md-10 offset-md-1">
            {/* Feed Tabs */}
            <div className={styles.feedToggle}>
              <ul>
                <li>
                  <Link
                    to={`/profile/${username}`}
                    className={
                      activeTab === "my"
                        ? styles.tabItemActive
                        : styles.tabItem
                    }
                  >
                    My Articles
                  </Link>
                </li>
                <li>
                  <Link
                    to={`/profile/${username}/favorites`}
                    className={
                      activeTab === "favorited"
                        ? styles.tabItemActive
                        : styles.tabItem
                    }
                  >
                    Favorited Articles
                  </Link>
                </li>
              </ul>
            </div>

            {/* Articles */}
            {articlesLoading ? (
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
            src={article.author.image || DEFAULT_IMAGE}
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
        <FavoriteButton
          slug={article.slug}
          favorited={article.favorited}
          favoritesCount={article.favoritesCount}
        />
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

export default ProfilePage;
