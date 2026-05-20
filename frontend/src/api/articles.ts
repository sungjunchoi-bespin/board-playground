import apiClient from "./client";

export interface Article {
  slug: string;
  title: string;
  description: string;
  body: string;
  tagList: string[];
  createdAt: string;
  updatedAt: string;
  favorited: boolean;
  favoritesCount: number;
  author: {
    username: string;
    bio: string | null;
    image: string | null;
    following: boolean;
  };
}

interface ArticleResponse {
  article: Article;
}

export interface CreateArticleFields {
  title: string;
  description: string;
  body: string;
  tagList: string[];
}

export interface UpdateArticleFields {
  title?: string;
  description?: string;
  body?: string;
}

export async function createArticleApi(
  fields: CreateArticleFields,
): Promise<Article> {
  const { data } = await apiClient.post<ArticleResponse>("/articles", {
    article: fields,
  });
  return data.article;
}

export async function getArticleApi(slug: string): Promise<Article> {
  const { data } = await apiClient.get<ArticleResponse>(`/articles/${slug}`);
  return data.article;
}

export async function updateArticleApi(
  slug: string,
  fields: UpdateArticleFields,
): Promise<Article> {
  const { data } = await apiClient.put<ArticleResponse>(`/articles/${slug}`, {
    article: fields,
  });
  return data.article;
}

export async function deleteArticleApi(slug: string): Promise<void> {
  await apiClient.delete(`/articles/${slug}`);
}

interface ArticlesListResponse {
  articles: Article[];
  articlesCount: number;
}

export interface ListArticlesParams {
  tag?: string;
  author?: string;
  favorited?: string;
  limit?: number;
  offset?: number;
}

export async function listArticlesApi(
  params: ListArticlesParams = {},
): Promise<ArticlesListResponse> {
  const { data } = await apiClient.get<ArticlesListResponse>("/articles", {
    params,
  });
  return data;
}

export async function feedArticlesApi(
  limit = 10,
  offset = 0,
): Promise<ArticlesListResponse> {
  const { data } = await apiClient.get<ArticlesListResponse>(
    "/articles/feed",
    { params: { limit, offset } },
  );
  return data;
}

export async function favoriteArticleApi(slug: string): Promise<Article> {
  const { data } = await apiClient.post<ArticleResponse>(
    `/articles/${slug}/favorite`,
  );
  return data.article;
}

export async function unfavoriteArticleApi(slug: string): Promise<Article> {
  const { data } = await apiClient.delete<ArticleResponse>(
    `/articles/${slug}/favorite`,
  );
  return data.article;
}
