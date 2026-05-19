import axios from "axios";

export function parseApiErrors(err: unknown): string[] {
  if (axios.isAxiosError(err) && err.response?.data?.errors) {
    const errorObj = err.response.data.errors as Record<string, string[]>;
    return Object.entries(errorObj).flatMap(([field, messages]) =>
      messages.map((msg) => `${field} ${msg}`),
    );
  }
  if (err instanceof Error) {
    return [err.message];
  }
  return ["An unexpected error occurred"];
}
