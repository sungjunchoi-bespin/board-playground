import apiClient from "./client";

export interface Profile {
  username: string;
  bio: string | null;
  image: string | null;
  following: boolean;
}

interface ProfileResponse {
  profile: Profile;
}

export async function getProfileApi(username: string): Promise<Profile> {
  const { data } = await apiClient.get<ProfileResponse>(
    `/profiles/${username}`,
  );
  return data.profile;
}

export async function followUserApi(username: string): Promise<Profile> {
  const { data } = await apiClient.post<ProfileResponse>(
    `/profiles/${username}/follow`,
  );
  return data.profile;
}

export async function unfollowUserApi(username: string): Promise<Profile> {
  const { data } = await apiClient.delete<ProfileResponse>(
    `/profiles/${username}/follow`,
  );
  return data.profile;
}
