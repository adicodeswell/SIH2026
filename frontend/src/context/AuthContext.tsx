import React, { createContext, useState, useEffect, useContext } from 'react';
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

  useEffect(() => {
    keycloak.init({ onLoad: 'check-sso', checkLoginIframe: false })
      .then((authenticated) => {
        setIsAuthenticated(authenticated);
        setToken(keycloak.token);
        setIsInitialized(true);
      })
      .catch(console.error);

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
