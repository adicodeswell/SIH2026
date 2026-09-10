import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CitizenApplicationsPage } from '../pages/citizen/ApplicationsPage';
import { CitizenApplicationDetailsPage } from '../pages/citizen/ApplicationDetailsPage';
import { applicationServiceApi } from '../services/applicationService';

vi.mock('../services/applicationService', () => ({
  applicationServiceApi: {
    getApplicationById: vi.fn(),
    getApplicationTimeline: vi.fn(),
  },
}));

const mockApplication = {
  applicationNumber: 'MH-2024-TEST',
  status: 'SUBMITTED',
  citizenId: 'MH1001',
  serviceCode: 'TEST_SERVICE',
  submittedAt: '2024-03-01T10:00:00Z',
};

const mockTimeline = [
  { eventType: 'SUBMITTED', description: 'Application submitted', occurredAt: '2024-03-01T10:00:00Z' },
];

describe('Citizen Applications', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    vi.clearAllMocks();
  });

  const renderTracker = () => {
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/citizen/applications']}>
          <Routes>
            <Route path="/citizen/applications" element={<CitizenApplicationsPage />} />
            <Route path="/citizen/applications/:id" element={<div>Details Page</div>} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );
  };

  const renderDetails = (id = 'MH-2024-TEST') => {
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={[`/citizen/applications/${id}`]}>
          <Routes>
            <Route path="/citizen/applications/:id" element={<CitizenApplicationDetailsPage />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );
  };

  it('renders tracker page and navigates on search', async () => {
    renderTracker();
    expect(screen.getByText(/Track Application/i)).toBeInTheDocument();
    
    const input = screen.getByPlaceholderText(/Enter Reference Number/i);
    fireEvent.change(input, { target: { value: 'MH-2024-1234' } });
    
    const button = screen.getByRole('button', { name: /Track/i });
    fireEvent.click(button);
    
    await waitFor(() => {
      expect(screen.getByText('Details Page')).toBeInTheDocument();
    });
  });

  it('renders application details successfully', async () => {
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(mockApplication as any);
    vi.mocked(applicationServiceApi.getApplicationTimeline).mockResolvedValue(mockTimeline);
    
    renderDetails();
    
    await waitFor(() => {
      expect(screen.getByText('Application Details')).toBeInTheDocument();
      expect(screen.getByText('MH-2024-TEST')).toBeInTheDocument();
      expect(screen.getByText('TEST_SERVICE')).toBeInTheDocument();
      expect(screen.getByText('Application submitted')).toBeInTheDocument();
    });
  });

  it('handles application not found error', async () => {
    vi.mocked(applicationServiceApi.getApplicationById).mockRejectedValue(new Error('Not found'));
    
    renderDetails('INVALID');
    
    await waitFor(() => {
      expect(screen.getByText(/Could not find application with reference number/i)).toBeInTheDocument();
    });
  });
});
