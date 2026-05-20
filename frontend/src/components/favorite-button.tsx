import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/hooks/use-auth";
import {
  favoriteArticleApi,
  unfavoriteArticleApi,
} from "@/api/articles";
import styles from "./favorite-button.module.css";

interface FavoriteButtonProps {
  slug: string;
  favorited: boolean;
  favoritesCount: number;
  size?: "sm" | "lg";
  onToggled?: (favorited: boolean, count: number) => void;
}

export default function FavoriteButton({
  slug,
  favorited: initialFavorited,
  favoritesCount: initialCount,
  size = "sm",
  onToggled,
}: FavoriteButtonProps) {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [favorited, setFavorited] = useState(initialFavorited);
  const [count, setCount] = useState(initialCount);
  const [loading, setLoading] = useState(false);

  async function handleClick() {
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }
    if (loading) return;

    // Optimistic update
    const prevFavorited = favorited;
    const prevCount = count;
    const newFavorited = !favorited;
    const newCount = newFavorited ? count + 1 : count - 1;

    setFavorited(newFavorited);
    setCount(newCount);
    setLoading(true);

    try {
      const article = newFavorited
        ? await favoriteArticleApi(slug)
        : await unfavoriteArticleApi(slug);
      setFavorited(article.favorited);
      setCount(article.favoritesCount);
      onToggled?.(article.favorited, article.favoritesCount);
    } catch {
      // Rollback
      setFavorited(prevFavorited);
      setCount(prevCount);
    } finally {
      setLoading(false);
    }
  }

  let className: string;
  if (size === "lg") {
    className = favorited ? styles.favoriteBtnLgActive : styles.favoriteBtnLg;
  } else {
    className = favorited ? styles.favoriteBtnActive : styles.favoriteBtn;
  }

  return (
    <button className={className} onClick={handleClick} disabled={loading}>
      {favorited ? "♥" : "♡"}{" "}
      {size === "lg"
        ? `${favorited ? "Unfavorite" : "Favorite"} Article (${count})`
        : count}
    </button>
  );
}
