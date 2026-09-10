import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { ErrorBoundary } from './components/ErrorBoundary';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from './components/ui/sonner';

// Layouts
import { PublicLayout } from './layouts/PublicLayout';
import { CitizenLayout } from './layouts/CitizenLayout';
import { OfficerLayout } from './layouts/OfficerLayout';
import { AdminLayout } from './layouts/AdminLayout';

// Public Pages
import Home from './pages/Home';
import Login from './pages/Login';

// Citizen Pages
import CitizenDashboard from './pages/citizen/Dashboard';
import { ServicesPage } from './pages/citizen/ServicesPage';
import { ServiceDetailPage } from './pages/citizen/ServiceDetailPage';
import { CitizenApplicationsPage } from './pages/citizen/ApplicationsPage';
import { CitizenApplicationDetailsPage } from './pages/citizen/ApplicationDetailsPage';
import { CitizenConsentsPage } from './pages/citizen/ConsentsPage';

// Officer Pages
import OfficerDashboard from './pages/officer/Dashboard';
import { OfficerReviewTaskPage } from './pages/officer/ReviewTaskPage';
import { InteroperabilityDashboard } from './pages/admin/InteroperabilityDashboard';

// Fallback & 404
import { NotFound } from './components/feedback/States';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function App() {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <Router>
            <Routes>
              {/* Public Routes Shell */}
              <Route element={<PublicLayout />}>
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<Login />} />
                <Route path="*" element={<NotFound />} />
              </Route>

              {/* Citizen Portal Protected Shell */}
              <Route
                path="/citizen"
                element={
                  <ProtectedRoute allowedRoles={['CITIZEN']}>
                    <CitizenLayout />
                  </ProtectedRoute>
                }
              >
                <Route index element={<CitizenDashboard />} />
                <Route path="services" element={<ServicesPage />} />
                <Route path="services/:serviceId" element={<ServiceDetailPage />} />
                <Route path="applications" element={<CitizenApplicationsPage />} />
                <Route path="applications/:id" element={<CitizenApplicationDetailsPage />} />
                <Route path="consents" element={<CitizenConsentsPage />} />
                <Route path="*" element={<Navigate to="/citizen" replace />} />
              </Route>

              {/* Officer Console Protected Shell */}
              <Route
                path="/officer"
                element={
                  <ProtectedRoute allowedRoles={['OFFICER']}>
                    <OfficerLayout />
                  </ProtectedRoute>
                }
              >
                <Route index element={<OfficerDashboard />} />
                <Route path="reviews" element={<OfficerDashboard />} />
                <Route path="reviews/:taskId" element={<OfficerReviewTaskPage />} />
                <Route path="*" element={<Navigate to="/officer" replace />} />
              </Route>

              {/* Admin Portal Protected Shell */}
              <Route
                path="/admin"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <AdminLayout />
                  </ProtectedRoute>
                }
              >
                <Route index element={<Navigate to="/admin/interoperability" replace />} />
                <Route path="interoperability" element={<InteroperabilityDashboard />} />
                <Route path="*" element={<Navigate to="/admin/interoperability" replace />} />
              </Route>
            </Routes>
          </Router>
          <Toaster richColors position="top-right" />
        </AuthProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
}

export default App;
