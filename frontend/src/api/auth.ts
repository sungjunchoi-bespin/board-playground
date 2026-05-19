import apiClient from "./client";
import type { User } from "@/hooks/use-auth";

interface AuthResponse {
  user: User;
}

export async function loginApi(
  email: string,
  password: string,
): Promise<User> {
  const { data } = await apiClient.post<AuthResponse>("/users/login", {
    user: { email, password },
  });
  return data.user;
}

export async function registerApi(
  username: string,
  email: string,
  password: string,
): Promise<User> {
  const { data } = await apiClient.post<AuthResponse>("/users", {
    user: { username, email, password },
  });
  return data.user;
}

export async function getCurrentUserApi(): Promise<User> {
  const { data } = await apiClient.get<AuthResponse>("/user");
  return data.user;
}

export interface UpdateUserFields {
  email?: string;
  username?: string;
  password?: string;
  bio?: string;
  image?: string;
}

export async function updateUserApi(
  fields: UpdateUserFields,
): Promise<User> {
  const { data } = await apiClient.put<AuthResponse>("/user", {
    user: fields,
  });
  return data.user;
}
