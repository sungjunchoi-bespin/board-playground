import apiClient from "./client";

interface TagsResponse {
  tags: string[];
}

export async function getTagsApi(): Promise<string[]> {
  const { data } = await apiClient.get<TagsResponse>("/tags");
  return data.tags;
}
