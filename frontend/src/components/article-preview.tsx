import { Link } from "react-router-dom";
import type { Article } from "@/api/articles";
import FavoriteButton from "@/components/favorite-button";
import { DEFAULT_AVATAR } from "@/constants";
import { formatArticleDate } from "@/utils/date";
import styles from "./article-preview.module.css";

interface ArticlePreviewProps {
  article: Article;
}

export default function ArticlePreview({ article }: ArticlePreviewProps) {
  return (
    <article className={styles.articlePreview}>
      <div className={styles.articleMeta}>
        <Link to={`/profile/${article.author.username}`}>
          <img
            className={styles.authorImage}
            src={article.author.image || DEFAULT_AVATAR}
            alt=""
          />
        </Link>
        <div className={styles.authorInfo}>
          <Link
            to={`/profile/${article.author.username}`}
            className={styles.authorName}
          >
            {article.author.username}
          </Link>
          <time className={styles.articleDate} dateTime={article.createdAt}>
            {formatArticleDate(article.createdAt)}
          </time>
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
            <ul className={styles.previewTagList} aria-label="Article tags">
              {article.tagList.map((tag) => (
                <li key={tag} className={styles.previewTag}>
                  {tag}
                </li>
              ))}
            </ul>
          )}
        </div>
      </Link>
    </article>
  );
}
