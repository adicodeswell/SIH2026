import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import Home from './pages/Home';
import Login from './pages/Login';
import CitizenDashboard from './pages/citizen/Dashboard';
import OfficerDashboard from './pages/officer/Dashboard';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          
          {/* Protected Routes */}
          <Route path="/citizen" element={
            <ProtectedRoute allowedRoles={['CITIZEN']}>
              <CitizenDashboard />
            </ProtectedRoute>
          } />
          
          <Route path="/officer" element={
            <ProtectedRoute allowedRoles={['OFFICER']}>
              <OfficerDashboard />
            </ProtectedRoute>
          } />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
