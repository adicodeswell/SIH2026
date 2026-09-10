import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ServicesPage } from '../pages/citizen/ServicesPage';
import { ServiceDetailPage } from '../pages/citizen/ServiceDetailPage';
import { serviceCatalogApi } from '../services/serviceCatalog';
import { AuthContext, type AuthContextType } from '../context/AuthContext';
import { notify } from '../lib/toast';

vi.mock('../services/serviceCatalog', () => ({
  serviceCatalogApi: {
    getServices: vi.fn(),
    getServiceById: vi.fn(),
    createApplication: vi.fn(),
    grantConsent: vi.fn(),
    submitApplication: vi.fn(),
  },
}));

vi.mock('../lib/toast', () => ({
  notify: {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
    warning: vi.fn(),
    loading: vi.fn(),
    dismiss: vi.fn(),
  },
}));

const mockCitizenContext: AuthContextType = {
  isAuthenticated: true,
  isInitialized: true,
  token: 'mock-token',
  user: {
    username: 'MH1001',
    name: 'Rahul Patil',
    roles: ['CITIZEN'],
  },
  login: vi.fn(),
  logout: vi.fn(),
  hasRole: (role: string) => role === 'CITIZEN',
};

const mockServices = [
  {
    serviceCode: 'SKILL_BENEFIT',
    serviceName: 'Pradhan Mantri Kaushal Vikas Yojana',
    description: 'Provides industry-relevant, short-term skill training and certification.',
    active: true,
    departmentCode: 'DEPT-SKILLS',
    departmentName: 'Labor and Employment Ministry',
  },
  {
    serviceCode: 'SCHOLARSHIP',
    serviceName: 'NSP Post-Matric Scholarship Scheme',
    description: 'Financial assistance to students studying at post-secondary stage.',
    active: true,
    departmentCode: 'DEPT_EDU',
    departmentName: 'Department of Higher Education',
  },
];

function createTestQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });
}

describe('Citizen Service Discovery & Application Creation (Phase 2)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Service Discovery (/citizen/services)', () => {
    it('displays loading skeleton while fetching services', () => {
      vi.mocked(serviceCatalogApi.getServices).mockReturnValue(new Promise(() => {})); // Never resolves

      render(
        <QueryClientProvider client={createTestQueryClient()}>
          <AuthContext.Provider value={mockCitizenContext}>
            <MemoryRouter initialEntries={['/citizen/services']}>
              <ServicesPage />
            </MemoryRouter>
          </AuthContext.Provider>
        </QueryClientProvider>
      );

      expect(screen.getByTestId('services-loading')).toBeInTheDocument();
    });

    it('renders list of government schemes when loaded successfully', async () => {
      vi.mocked(serviceCatalogApi.getServices).mockResolvedValue(mockServices);

      render(
        <QueryClientProvider client={createTestQueryClient()}>
          <AuthContext.Provider value={mockCitizenContext}>
            <MemoryRouter initialEntries={['/citizen/services']}>
              <ServicesPage />
            </MemoryRouter>
          </AuthContext.Provider>
        </QueryClientProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('Pradhan Mantri Kaushal Vikas Yojana')).toBeInTheDocument();
        expect(screen.getByText('NSP Post-Matric Scholarship Scheme')).toBeInTheDocument();
      });

      expect(screen.getByText('Labor and Employment Ministry')).toBeInTheDocument();
      expect(screen.getByText('Department of Higher Education')).toBeInTheDocument();
    });

    it('renders empty state when no services are returned', async () => {
      vi.mocked(serviceCatalogApi.getServices).mockResolvedValue([]);

      render(
        <QueryClientProvider client={createTestQueryClient()}>
          <AuthContext.Provider value={mockCitizenContext}>
            <MemoryRouter initialEntries={['/citizen/services']}>
              <ServicesPage />
            </MemoryRouter>
          </AuthContext.Provider>
        </QueryClientProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('No government schemes available')).toBeInTheDocument();
      });
    });

    it('renders error state and allows retry on API failure', async () => {
      vi.mocked(serviceCatalogApi.getServices).mockRejectedValueOnce({
        status: 500,
        message: 'Application service unavailable',
      });

      render(
        <QueryClientProvider client={createTestQueryClient()}>
          <AuthContext.Provider value={mockCitizenContext}>
            <MemoryRouter initialEntries={['/citizen/services']}>
              <ServicesPage />
            </MemoryRouter>
          </AuthContext.Provider>
        </QueryClientProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('Unable to load government schemes')).toBeInTheDocument();
      });

      // Try retry
      vi.mocked(serviceCatalogApi.getServices).mockResolvedValueOnce(mockServices);
      const retryBtn = screen.getByRole('button', { name: /Try Again/i });
      await userEvent.click(retryBtn);

      await waitFor(() => {
        expect(screen.getByText('Pradhan Mantri Kaushal Vikas Yojana')).toBeInTheDocument();
      });
    });

    it('filters services based on search query', async () => {
      vi.mocked(serviceCatalogApi.getServices).mockResolvedValue(mockServices);

      render(
        <QueryClientProvider client={createTestQueryClient()}>
          <AuthContext.Provider value={mockCitizenContext}>
            <MemoryRouter initialEntries={['/citizen/services']}>
              <ServicesPage />
            </MemoryRouter>
          </AuthContext.Provider>
        </QueryClientProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('Pradhan Mantri Kaushal Vikas Yojana')).toBeInTheDocument();
      });

      const searchInput = screen.getByPlaceholderText(/Search schemes or departments/i);
      await userEvent.type(searchInput, 'Scholarship');

      expect(screen.getByText('NSP Post-Matric Scholarship Scheme')).toBeInTheDocument();
      expect(screen.queryByText('Pradhan Mantri Kaushal Vikas Yojana')).not.toBeInTheDocument();
    });
  });

  describe('Service Detail & Application Form (/citizen/services/:serviceId)', () => {
    it('renders pre-application overview of the selected service', async () => {
      vi.mocked(serviceCatalogApi.getServiceById).mockResolvedValue(mockServices[0]);

      render(
        <QueryClientProvider client={createTestQueryClient()}>
          <AuthContext.Provider value={mockCitizenContext}>
            <MemoryRouter initialEntries={['/citizen/services/SKILL_BENEFIT']}>
              <Routes>
                <Route path="/citizen/services/:serviceId" element={<ServiceDetailPage />} />
              </Routes>
            </MemoryRouter>
          </AuthContext.Provider>
        </QueryClientProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('Pradhan Mantri Kaushal Vikas Yojana')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Apply for this Service/i })).toBeInTheDocument();
      });
    });

    it('validates required fields and consent checkbox before submission', async () => {
      vi.mocked(serviceCatalogApi.getServiceById).mockResolvedValue(mockServices[0]);

      render(
        <QueryClientProvider client={createTestQueryClient()}>
          <AuthContext.Provider value={mockCitizenContext}>
            <MemoryRouter initialEntries={['/citizen/services/SKILL_BENEFIT']}>
              <Routes>
                <Route path="/citizen/services/:serviceId" element={<ServiceDetailPage />} />
              </Routes>
            </MemoryRouter>
          </AuthContext.Provider>
        </QueryClientProvider>
      );

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /Apply for this Service/i })).toBeInTheDocument();
      });

      // Open application form
      await userEvent.click(screen.getByRole('button', { name: /Apply for this Service/i }));

      const submitBtn = screen.getByRole('button', { name: /Submit Application/i });

      // Clear citizen ID to test required field validation
      const citizenIdInput = screen.getByLabelText(/Citizen Identifier/i);
      await userEvent.clear(citizenIdInput);

      await userEvent.click(submitBtn);

      expect(screen.getByText('Citizen ID must not be blank')).toBeInTheDocument();
      expect(screen.getByText(/You must grant explicit consent/i)).toBeInTheDocument();

      expect(serviceCatalogApi.createApplication).not.toHaveBeenCalled();
    });

    it('submits application successfully with explicit DPDP consent and displays reference', async () => {
      vi.mocked(serviceCatalogApi.getServiceById).mockResolvedValue(mockServices[0]);
      vi.mocked(serviceCatalogApi.grantConsent).mockResolvedValue({
        status: 'SUCCESS',
        referenceId: 'CONSENT-123',
        consentId: 'C-123',
        expiresAt: '2026-01-01',
      });
      vi.mocked(serviceCatalogApi.submitApplication).mockResolvedValue({
        applicationNumber: 'MH-2026-SKILL-99',
        status: 'SUBMITTED',
        citizenId: 'MH1001',
        serviceCode: 'SKILL_BENEFIT',
        submittedAt: new Date().toISOString(),
      });
      vi.mocked(serviceCatalogApi.createApplication).mockResolvedValue({
        applicationNumber: 'MH-2026-SKILL-99',
        status: 'SUBMITTED',
        citizenId: 'MH1001',
        serviceCode: 'SKILL_BENEFIT',
        submittedAt: new Date().toISOString(),
        serviceName: 'Mock Service',
        departmentCode: 'DEPT-MOCK',
        createdAt: new Date().toISOString(),
        updatedAt: null,
      });

      render(
        <QueryClientProvider client={createTestQueryClient()}>
          <AuthContext.Provider value={mockCitizenContext}>
            <MemoryRouter initialEntries={['/citizen/services/SKILL_BENEFIT']}>
              <Routes>
                <Route path="/citizen/services/:serviceId" element={<ServiceDetailPage />} />
              </Routes>
            </MemoryRouter>
          </AuthContext.Provider>
        </QueryClientProvider>
      );

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /Apply for this Service/i })).toBeInTheDocument();
      });

      await userEvent.click(screen.getByRole('button', { name: /Apply for this Service/i }));

      // Check consent box
      const consentBox = screen.getByRole('checkbox');
      await userEvent.click(consentBox);

      // Click submit
      const submitBtn = screen.getByRole('button', { name: /Submit Application/i });
      await userEvent.click(submitBtn);

      await waitFor(() => {
        expect(serviceCatalogApi.grantConsent).toHaveBeenCalledWith({
          applicationId: 'MH-2026-SKILL-99',
          dataScope: 'education,employment,skills',
          purpose: 'verification',
          requestingDepartmentId: 'DEPT-SKILLS',
          serviceCode: 'SKILL_BENEFIT',
        });
        expect(serviceCatalogApi.createApplication).toHaveBeenCalledWith({
          citizenId: 'MH1001',
          serviceCode: 'SKILL_BENEFIT',
        });
      });

      // Expect success confirmation and reference
      await screen.findByText(/Application Submitted Successfully/i, {}, { timeout: 3000 });
      expect(screen.getByText('MH-2026-SKILL-99')).toBeInTheDocument();
      expect(screen.getByRole('link', { name: /View My Applications/i })).toBeInTheDocument();
    });

    it('handles 409 conflict gracefully during application submission', async () => {
      vi.mocked(serviceCatalogApi.getServiceById).mockResolvedValue(mockServices[0]);
      vi.mocked(serviceCatalogApi.grantConsent).mockResolvedValue({} as any);
      vi.mocked(serviceCatalogApi.createApplication).mockRejectedValue({
        status: 409,
        code: 'CONFLICT',
        message: 'This action conflicts with the current application state.',
      });

      render(
        <QueryClientProvider client={createTestQueryClient()}>
          <AuthContext.Provider value={mockCitizenContext}>
            <MemoryRouter initialEntries={['/citizen/services/SKILL_BENEFIT']}>
              <Routes>
                <Route path="/citizen/services/:serviceId" element={<ServiceDetailPage />} />
              </Routes>
            </MemoryRouter>
          </AuthContext.Provider>
        </QueryClientProvider>
      );

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /Apply for this Service/i })).toBeInTheDocument();
      });

      await userEvent.click(screen.getByRole('button', { name: /Apply for this Service/i }));
      await userEvent.click(screen.getByRole('checkbox'));
      await userEvent.click(screen.getByRole('button', { name: /Submit Application/i }));

      await waitFor(() => {
        expect(notify.error).toHaveBeenCalledWith(
          'Application Conflict',
          'An active application already exists for this scheme or state.'
        );
      });
    });
  });
});
