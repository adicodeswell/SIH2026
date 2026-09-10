import React, { createContext, useState, useEffect, useContext, useRef } from 'react';
import keycloak from '../lib/auth';

export interface AuthContextType {
  isAuthenticated: boolean;
  isInitialized: boolean;
  token: string | undefined;
  user: {
    username?: string;
    email?: string;
    name?: string;
    roles: string[];
  } | null;
  login: () => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

function extractUserFromToken(token?: string) {
  if (!token) return null;
  try {
    const parts = token.split('.');
    if (parts.length < 2) return null;
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    const parsed = JSON.parse(jsonPayload);
    const realmRoles = parsed.realm_access?.roles || [];
    return {
      username: parsed.preferred_username || parsed.sub,
      email: parsed.email,
      name: parsed.name || parsed.preferred_username,
      roles: Array.isArray(realmRoles) ? realmRoles : [],
    };
  } catch {
    return null;
  }
}

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isInitialized, setIsInitialized] = useState(false);
  const [token, setToken] = useState<string | undefined>(undefined);
  const isRun = useRef(false);

  useEffect(() => {
    // Prevent React StrictMode from initializing Keycloak twice
    if (isRun.current) return;
    isRun.current = true;

    keycloak.init({ checkLoginIframe: false })
      .then((authenticated) => {
        setIsAuthenticated(authenticated);
        setToken(keycloak.token);
        setIsInitialized(true);
      })
      .catch((err) => {
        console.error("Keycloak init failed:", err);
        setIsInitialized(true); // Don't hang the UI if Keycloak is down
      });

    // Refresh token periodically to avoid expiration during active session
    keycloak.onTokenExpired = () => {
      keycloak.updateToken(30).then((refreshed) => {
        if (refreshed) {
          setToken(keycloak.token);
        }
      }).catch(() => {
        keycloak.logout();
      });
    };
  }, []);

  const login = () => keycloak.login();
  const logout = () => keycloak.logout();
  const hasRole = (role: string) => keycloak.hasRealmRole(role);

  const user = extractUserFromToken(token);

  return (
    <AuthContext.Provider value={{ isAuthenticated, isInitialized, token, user, login, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};
