import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { AuthContext, type AuthContextType } from '../context/AuthContext';
import { NotFound } from '../components/feedback/States';

const mockCitizenContext: AuthContextType = {
  isAuthenticated: true,
  isInitialized: true,
  token: 'mock-token',
  user: {
    username: 'CITIZEN_USER',
    name: 'Citizen Jane',
    roles: ['CITIZEN'],
  },
  login: vi.fn(),
  logout: vi.fn(),
  hasRole: (role: string) => role === 'CITIZEN',
};

const mockOfficerContext: AuthContextType = {
  isAuthenticated: true,
  isInitialized: true,
  token: 'mock-token',
  user: {
    username: 'OFFICER_USER',
    name: 'Officer John',
    roles: ['OFFICER'],
  },
  login: vi.fn(),
  logout: vi.fn(),
  hasRole: (role: string) => role === 'OFFICER',
};

const mockUnauthenticatedContext: AuthContextType = {
  isAuthenticated: false,
  isInitialized: true,
  token: undefined,
  user: null,
  login: vi.fn(),
  logout: vi.fn(),
  hasRole: () => false,
};

const mockInitializingContext: AuthContextType = {
  isAuthenticated: false,
  isInitialized: false,
  token: undefined,
  user: null,
  login: vi.fn(),
  logout: vi.fn(),
  hasRole: () => false,
};

describe('Frontend Routing & Route Protection', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders loading state when authentication is initializing', () => {
    render(
      <AuthContext.Provider value={mockInitializingContext}>
        <MemoryRouter initialEntries={['/citizen']}>
          <Routes>
            <Route
              path="/citizen"
              element={
                <ProtectedRoute allowedRoles={['CITIZEN']}>
                  <div>Citizen Content</div>
                </ProtectedRoute>
              }
            />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>
    );

    const loadingElements = screen.getAllByText(/Verifying security credentials/i);
    expect(loadingElements.length).toBeGreaterThan(0);
    expect(screen.getByText(/Checking Keycloak session token/i)).toBeInTheDocument();
  });

  it('redirects unauthenticated user trying to access citizen route to /login', () => {
    render(
      <AuthContext.Provider value={mockUnauthenticatedContext}>
        <MemoryRouter initialEntries={['/citizen']}>
          <Routes>
            <Route
              path="/citizen"
              element={
                <ProtectedRoute allowedRoles={['CITIZEN']}>
                  <div>Citizen Content</div>
                </ProtectedRoute>
              }
            />
            <Route path="/login" element={<div>Login Page Mock</div>} />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>
    );

    expect(screen.getByText('Login Page Mock')).toBeInTheDocument();
    expect(screen.queryByText('Citizen Content')).not.toBeInTheDocument();
  });

  it('redirects unauthenticated user trying to access officer route to /login', () => {
    render(
      <AuthContext.Provider value={mockUnauthenticatedContext}>
        <MemoryRouter initialEntries={['/officer']}>
          <Routes>
            <Route
              path="/officer"
              element={
                <ProtectedRoute allowedRoles={['OFFICER']}>
                  <div>Officer Console Content</div>
                </ProtectedRoute>
              }
            />
            <Route path="/login" element={<div>Login Page Mock</div>} />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>
    );

    expect(screen.getByText('Login Page Mock')).toBeInTheDocument();
    expect(screen.queryByText('Officer Console Content')).not.toBeInTheDocument();
  });

  it('allows authenticated citizen to access citizen route', () => {
    render(
      <AuthContext.Provider value={mockCitizenContext}>
        <MemoryRouter initialEntries={['/citizen']}>
          <Routes>
            <Route
              path="/citizen"
              element={
                <ProtectedRoute allowedRoles={['CITIZEN']}>
                  <div>Citizen Dashboard View</div>
                </ProtectedRoute>
              }
            />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>
    );

    expect(screen.getByText('Citizen Dashboard View')).toBeInTheDocument();
  });

  it('allows authenticated officer to access officer route', () => {
    render(
      <AuthContext.Provider value={mockOfficerContext}>
        <MemoryRouter initialEntries={['/officer']}>
          <Routes>
            <Route
              path="/officer"
              element={
                <ProtectedRoute allowedRoles={['OFFICER']}>
                  <div>Officer Review Queue View</div>
                </ProtectedRoute>
              }
            />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>
    );

    expect(screen.getByText('Officer Review Queue View')).toBeInTheDocument();
  });

  it('prevents citizen from accessing officer route and redirects to /citizen', () => {
    render(
      <AuthContext.Provider value={mockCitizenContext}>
        <MemoryRouter initialEntries={['/officer']}>
          <Routes>
            <Route
              path="/officer"
              element={
                <ProtectedRoute allowedRoles={['OFFICER']}>
                  <div>Officer Restricted Content</div>
                </ProtectedRoute>
              }
            />
            <Route path="/citizen" element={<div>Citizen Redirect Landing</div>} />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>
    );

    expect(screen.getByText('Citizen Redirect Landing')).toBeInTheDocument();
    expect(screen.queryByText('Officer Restricted Content')).not.toBeInTheDocument();
  });

  it('renders official 404 page for unknown routes', () => {
    render(
      <MemoryRouter initialEntries={['/unknown-department-scheme']}>
        <Routes>
          <Route path="/" element={<div>Home</div>} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Page not found')).toBeInTheDocument();
    expect(screen.getByText(/Return to Portal Home/i)).toBeInTheDocument();
  });
});
