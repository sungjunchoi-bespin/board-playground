import { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { marked } from "marked";
import { useAuth } from "@/hooks/use-auth";
import { getArticleApi, deleteArticleApi } from "@/api/articles";
import type { Article } from "@/api/articles";
import styles from "./article-page.module.css";

function ArticlePage() {
  const { slug } = useParams<{ slug: string }>();
  const [article, setArticle] = useState<Article | null>(null);
  const [loading, setLoading] = useState(true);
  const [deleting, setDeleting] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (slug) {
      getArticleApi(slug)
        .then(setArticle)
        .finally(() => setLoading(false));
    }
  }, [slug]);

  async function handleDelete() {
    if (!slug || !confirm("Are you sure you want to delete this article?"))
      return;
    setDeleting(true);
    try {
      await deleteArticleApi(slug);
      navigate("/");
    } catch {
      setDeleting(false);
    }
  }

  if (loading) {
    return (
      <div className="article-page">
        <div className="container page">
          <p>Loading article...</p>
        </div>
      </div>
    );
  }

  if (!article) {
    return (
      <div className="article-page">
        <div className="container page">
          <p>Article not found.</p>
        </div>
      </div>
    );
  }

  const isAuthor = user?.username === article.author.username;
  const formattedDate = new Date(article.createdAt).toLocaleDateString(
    "en-US",
    {
      year: "numeric",
      month: "long",
      day: "numeric",
    },
  );
  const bodyHtml = marked.parse(article.body) as string;

  return (
    <div className="article-page">
      <div className={styles.banner}>
        <div className="container">
          <h1 className={styles.bannerTitle}>{article.title}</h1>
          <div className={styles.articleMeta}>
            <Link to={`/@${article.author.username}`}>
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
                to={`/@${article.author.username}`}
                className={styles.authorName}
              >
                {article.author.username}
              </Link>
              <span className={styles.date}>{formattedDate}</span>
            </div>
            {isAuthor && (
              <div className={styles.actions}>
                <Link
                  to={`/editor/${article.slug}`}
                  className={`btn btn-sm btn-outline-secondary ${styles.editBtn}`}
                >
                  <i className="ion-edit" /> Edit Article
                </Link>
                <button
                  className={`btn btn-sm btn-outline-danger ${styles.deleteBtn}`}
                  onClick={handleDelete}
                  disabled={deleting}
                >
                  <i className="ion-trash-a" /> Delete Article
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="container page">
        <div className={styles.articleContent}>
          <div
            className={styles.articleBody}
            dangerouslySetInnerHTML={{ __html: bodyHtml }}
          />
          <div className={styles.tagList}>
            {article.tagList.map((tag) => (
              <span key={tag} className={styles.tag}>
                {tag}
              </span>
            ))}
          </div>
        </div>
        <hr />
      </div>
    </div>
  );
}

export default ArticlePage;
