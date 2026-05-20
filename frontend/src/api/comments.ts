import apiClient from "./client";

export interface Comment {
  id: number;
  createdAt: string;
  updatedAt: string;
  body: string;
  author: {
    username: string;
    bio: string;
    image: string;
    following: boolean;
  };
}

interface CommentsResponse {
  comments: Comment[];
}

interface CommentResponse {
  comment: Comment;
}

export async function listCommentsApi(slug: string): Promise<Comment[]> {
  const { data } = await apiClient.get<CommentsResponse>(
    `/articles/${slug}/comments`,
  );
  return data.comments;
}

export async function addCommentApi(
  slug: string,
  body: string,
): Promise<Comment> {
  const { data } = await apiClient.post<CommentResponse>(
    `/articles/${slug}/comments`,
    { comment: { body } },
  );
  return data.comment;
}

export async function deleteCommentApi(
  slug: string,
  id: number,
): Promise<void> {
  await apiClient.delete(`/articles/${slug}/comments/${id}`);
}
