import { useAuth } from '../context/AuthContext';
import { Navigate } from 'react-router-dom';

export default function Login() {
  const { isAuthenticated, isInitialized, login } = useAuth();

  if (!isInitialized) {
    return <div className="text-center p-8">Loading...</div>;
  }

  // If they are already authenticated, don't show the login screen again!
  if (isAuthenticated) {
    return <Navigate to="/citizen" replace />;
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-xl shadow-md w-full max-w-md text-center border border-gray-200">
        <h2 className="text-2xl font-bold mb-2 text-gray-800">GovSecure Authentication</h2>
        <p className="text-sm text-gray-500 mb-8">Centralized Identity & Access Management</p>
        <button 
          onClick={login}
          className="w-full px-4 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-semibold transition shadow-sm"
        >
          Login via Keycloak
        </button>
      </div>
    </div>
  );
}
