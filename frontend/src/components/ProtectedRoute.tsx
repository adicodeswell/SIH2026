import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: string[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, allowedRoles }) => {
  const { isAuthenticated, isInitialized, hasRole } = useAuth();

  if (!isInitialized) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-xl text-blue-600 font-semibold animate-pulse">Initializing Security...</div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // If the route requires specific roles, check if the user has AT LEAST ONE of them
  if (allowedRoles && allowedRoles.length > 0) {
    const hasRequiredRole = allowedRoles.some(role => hasRole(role));
    if (!hasRequiredRole) {
      // If they are logged in but lack the role, send them to their own dashboard
      if (hasRole('OFFICER')) return <Navigate to="/officer" replace />;
      return <Navigate to="/citizen" replace />;
    }
  }

  return <>{children}</>;
};
