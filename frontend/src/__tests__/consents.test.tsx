import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CitizenConsentsPage } from '../pages/citizen/ConsentsPage';
import { consentServiceApi } from '../services/consentService';

vi.mock('../services/consentService', () => ({
  consentServiceApi: {
    getConsents: vi.fn(),
    revokeConsent: vi.fn(),
  },
}));

const mockConsents = [
  {
    id: 'c1',
    citizenId: 'MH1001',
    dataScope: 'education',
    purpose: 'Scholarship',
    requestingDepartmentId: 'DEPT-EDU',
    status: 'ACTIVE',
    grantedAt: '2024-03-01T10:00:00Z',
  },
];

describe('Citizen Consents', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    vi.clearAllMocks();
  });

  const renderConsents = () => {
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <CitizenConsentsPage />
        </MemoryRouter>
      </QueryClientProvider>
    );
  };

  it('renders loading state initially', () => {
    // Need a delay to see loading state usually, but mock is not resolved yet
    vi.mocked(consentServiceApi.getConsents).mockReturnValue(new Promise(() => {}));
    renderConsents();
    expect(screen.getByText(/Active & Historical Consents/i)).toBeInTheDocument();
  });

  it('renders empty state when no consents', async () => {
    vi.mocked(consentServiceApi.getConsents).mockResolvedValue([]);
    renderConsents();
    await waitFor(() => {
      expect(screen.getByText(/No Consents Found/i)).toBeInTheDocument();
    });
  });

  it('renders list of consents', async () => {
    vi.mocked(consentServiceApi.getConsents).mockResolvedValue(mockConsents);
    renderConsents();
    await waitFor(() => {
      expect(screen.getByText('Scholarship')).toBeInTheDocument();
      expect(screen.getByText(/DEPT-EDU/i)).toBeInTheDocument();
      expect(screen.getByText('ACTIVE')).toBeInTheDocument();
    });
  });

  it('calls revoke when revoke button clicked', async () => {
    vi.mocked(consentServiceApi.getConsents).mockResolvedValue(mockConsents);
    vi.mocked(consentServiceApi.revokeConsent).mockResolvedValue();
    vi.spyOn(window, "confirm").mockReturnValue(true);
    
    renderConsents();
    
    await waitFor(() => {
      expect(screen.getByText('Revoke')).toBeInTheDocument();
    });
    
    fireEvent.click(screen.getByText('Revoke'));
    
    expect(window.confirm).toHaveBeenCalled();
    await waitFor(() => expect(consentServiceApi.revokeConsent).toHaveBeenCalledWith('c1', expect.anything()));
  });
});
