import { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { marked } from "marked";
import { useAuth } from "@/hooks/use-auth";
import { getArticleApi, deleteArticleApi } from "@/api/articles";
import type { Article } from "@/api/articles";
import FavoriteButton from "@/components/favorite-button";
import {
  listCommentsApi,
  addCommentApi,
  deleteCommentApi,
  type Comment,
} from "@/api/comments";
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
            {isAuthor ? (
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
            ) : (
              <div className={styles.actions}>
                <FavoriteButton
                  slug={article.slug}
                  favorited={article.favorited}
                  favoritesCount={article.favoritesCount}
                  size="lg"
                  onToggled={(fav, cnt) =>
                    setArticle((prev) =>
                      prev ? { ...prev, favorited: fav, favoritesCount: cnt } : prev,
                    )
                  }
                />
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
        <CommentSection slug={slug!} />
      </div>
    </div>
  );
}

const DEFAULT_IMAGE = "https://api.realworld.io/images/smiley-cyrus.jpeg";

function CommentSection({ slug }: { slug: string }) {
  const { user, isAuthenticated } = useAuth();
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentBody, setCommentBody] = useState("");
  const [posting, setPosting] = useState(false);

  useEffect(() => {
    listCommentsApi(slug).then(setComments).catch(() => {});
  }, [slug]);

  async function handlePostComment(e: React.FormEvent) {
    e.preventDefault();
    if (!commentBody.trim() || posting) return;
    setPosting(true);
    try {
      const newComment = await addCommentApi(slug, commentBody);
      setComments((prev) => [newComment, ...prev]);
      setCommentBody("");
    } catch {
      // ignore
    } finally {
      setPosting(false);
    }
  }

  async function handleDeleteComment(id: number) {
    try {
      await deleteCommentApi(slug, id);
      setComments((prev) => prev.filter((c) => c.id !== id));
    } catch {
      // ignore
    }
  }

  return (
    <div className={styles.commentSection}>
      {isAuthenticated ? (
        <form className={styles.commentForm} onSubmit={handlePostComment}>
          <textarea
            className={styles.commentTextarea}
            placeholder="Write a comment..."
            value={commentBody}
            onChange={(e) => setCommentBody(e.target.value)}
            rows={3}
          />
          <div className={styles.commentFormFooter}>
            <img
              className={styles.commentFormImage}
              src={user?.image || DEFAULT_IMAGE}
              alt={user?.username || ""}
            />
            <button
              className={styles.postCommentBtn}
              type="submit"
              disabled={posting || !commentBody.trim()}
            >
              Post Comment
            </button>
          </div>
        </form>
      ) : (
        <p className={styles.signInPrompt}>
          <Link to="/login">Sign in</Link> or{" "}
          <Link to="/register">sign up</Link> to add comments on this article.
        </p>
      )}

      {comments.map((comment) => (
        <CommentCard
          key={comment.id}
          comment={comment}
          currentUsername={user?.username}
          onDelete={handleDeleteComment}
        />
      ))}
    </div>
  );
}

function CommentCard({
  comment,
  currentUsername,
  onDelete,
}: {
  comment: Comment;
  currentUsername?: string;
  onDelete: (id: number) => void;
}) {
  const formattedDate = new Date(comment.createdAt).toLocaleDateString(
    "en-US",
    { year: "numeric", month: "long", day: "numeric" },
  );
  const isOwn = currentUsername === comment.author.username;

  return (
    <div className={styles.commentCard}>
      <div className={styles.commentBody}>{comment.body}</div>
      <div className={styles.commentCardFooter}>
        <Link to={`/profile/${comment.author.username}`}>
          <img
            className={styles.commentAuthorImage}
            src={comment.author.image || DEFAULT_IMAGE}
            alt={comment.author.username}
          />
        </Link>
        <Link
          to={`/profile/${comment.author.username}`}
          className={styles.commentAuthorName}
        >
          {comment.author.username}
        </Link>
        <span className={styles.commentDate}>{formattedDate}</span>
        {isOwn && (
          <button
            className={styles.commentDeleteBtn}
            onClick={() => onDelete(comment.id)}
            title="Delete comment"
          >
            🗑
          </button>
        )}
      </div>
    </div>
  );
}

export default ArticlePage;
