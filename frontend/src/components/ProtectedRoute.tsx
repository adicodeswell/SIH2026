import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { PageLoader } from './feedback/Loading';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: string[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, allowedRoles }) => {
  const { isAuthenticated, isInitialized, hasRole } = useAuth();
  const location = useLocation();

  if (!isInitialized) {
    return <PageLoader message="Verifying security credentials..." subtext="Checking Keycloak session token" />;
  }

  if (!isAuthenticated) {
    // Preserve intended destination in state
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // If route requires specific roles, check if the user has AT LEAST ONE
  if (allowedRoles && allowedRoles.length > 0) {
    const hasRequiredRole = allowedRoles.some((role) => hasRole(role));
    if (!hasRequiredRole) {
      // Role-aware fallback redirection
      if (hasRole('OFFICER')) return <Navigate to="/officer" replace />;
      if (hasRole('CITIZEN')) return <Navigate to="/citizen" replace />;
      return <Navigate to="/" replace />;
    }
  }

  return <>{children}</>;
};
