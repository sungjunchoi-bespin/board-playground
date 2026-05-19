import axios from "axios";
import { getToken } from "@/hooks/use-auth";

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "/api",
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Token ${token}`;
  }
  return config;
});

export default apiClient;
