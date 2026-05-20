import { useState, useEffect } from "react";
import { useParams, useLocation, Link } from "react-router-dom";
import { useAuth } from "@/hooks/use-auth";
import { getProfileApi, followUserApi, unfollowUserApi } from "@/api/profiles";
import type { Profile } from "@/api/profiles";
import { listArticlesApi, type Article } from "@/api/articles";
import ArticlePreview from "@/components/article-preview";
import LoadingState from "@/components/state/loading-state";
import EmptyState from "@/components/state/empty-state";
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
          <LoadingState label="Loading profile..." size="lg" />
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="profile-page">
        <div className="container page">
          <EmptyState
            icon="👤"
            title="User not found."
            hint="The profile may not exist or the username is misspelled."
          />
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
            <nav className={styles.feedToggle} aria-label="Profile article views">
              <ul>
                <li>
                  <Link
                    to={`/profile/${username}`}
                    aria-current={activeTab === "my" ? "page" : undefined}
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
                    aria-current={
                      activeTab === "favorited" ? "page" : undefined
                    }
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
            </nav>

            {/* Articles */}
            {articlesLoading ? (
              <LoadingState label="Loading articles..." />
            ) : articles.length === 0 ? (
              <EmptyState
                icon="📰"
                title="No articles are here... yet."
                hint={
                  activeTab === "favorited"
                    ? `${profile.username} hasn't favorited any articles yet.`
                    : `${profile.username} hasn't written any articles yet.`
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
        </div>
      </div>
    </div>
  );
}

export default ProfilePage;
