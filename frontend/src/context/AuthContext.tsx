import React, { createContext, useState, useEffect, useContext, useRef } from 'react';
import keycloak from '../lib/auth';

interface AuthContextType {
  isAuthenticated: boolean;
  isInitialized: boolean;
  token: string | undefined;
  login: () => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isInitialized, setIsInitialized] = useState(false);
  const [token, setToken] = useState<string | undefined>(undefined);
  const isRun = useRef(false);

  useEffect(() => {
    // Prevent React StrictMode from initializing Keycloak twice
    if (isRun.current) return;
    isRun.current = true;

    // Removing onLoad: 'check-sso' entirely prevents any automatic redirects when you visit the homepage.
    // It will only redirect when you explicitly click the "Login" button.
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

  return (
    <AuthContext.Provider value={{ isAuthenticated, isInitialized, token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};
