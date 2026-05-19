import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
} from "react";
export interface User {
  email: string;
  token: string;
  username: string;
  bio: string | null;
  image: string | null;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (user: User) => void;
  logout: () => void;
}

const TOKEN_KEY = "conduit_token";
const USER_KEY = "conduit_user";

export const AuthContext = createContext<AuthContextType>({
  user: null,
  isAuthenticated: false,
  login: () => {},
  logout: () => {},
});

export function useAuth(): AuthContextType {
  return useContext(AuthContext);
}

export function useAuthProvider(): {
  value: AuthContextType;
  Provider: typeof AuthContext.Provider;
} {
  const [user, setUser] = useState<User | null>(() => {
    const stored = localStorage.getItem(USER_KEY);
    if (stored) {
      try {
        return JSON.parse(stored) as User;
      } catch {
        return null;
      }
    }
    return null;
  });

  useEffect(() => {
    if (user) {
      localStorage.setItem(TOKEN_KEY, user.token);
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    } else {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    }
  }, [user]);

  const login = useCallback((u: User) => setUser(u), []);
  const logout = useCallback(() => setUser(null), []);

  return {
    value: {
      user,
      isAuthenticated: user !== null,
      login,
      logout,
    },
    Provider: AuthContext.Provider,
  };
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}
