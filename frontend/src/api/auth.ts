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
