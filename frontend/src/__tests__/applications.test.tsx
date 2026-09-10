import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CitizenApplicationsPage } from '../pages/citizen/ApplicationsPage';
import { CitizenApplicationDetailsPage } from '../pages/citizen/ApplicationDetailsPage';
import { applicationServiceApi } from '../services/applicationService';

vi.mock('../services/applicationService', () => ({
  applicationServiceApi: {
    getApplicationById: vi.fn(),
    getApplicationActivity: vi.fn(),
  },
}));

const mockApplication = {
  applicationNumber: 'MH-2024-TEST',
  status: 'SUBMITTED',
  citizenId: 'MH1001',
  serviceCode: 'TEST_SERVICE',
  createdAt: '2024-03-01T09:00:00Z',
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
    expect(screen.getByText(/Track by Reference Number/i)).toBeInTheDocument();
    
    const input = screen.getByPlaceholderText(/e.g., MH-2024-1234/i);
    await userEvent.type(input, 'MH-2024-1234');
    
    const button = screen.getByRole('button', { name: /Track/i });
    await userEvent.click(button);
    
    await waitFor(() => {
      expect(screen.getByText('Details Page')).toBeInTheDocument();
    });
  });

  it('renders application details successfully', async () => {
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(mockApplication as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(mockTimeline as any);
    
    renderDetails();
    
    await screen.findByText(/MH-2024-TEST/i);
    expect(screen.getAllByText(/TEST_SERVICE/i)[0]).toBeInTheDocument();
    expect(screen.getByText(/Application submitted/i)).toBeInTheDocument();
  });

  it('handles application not found error', async () => {
    vi.mocked(applicationServiceApi.getApplicationById).mockRejectedValue(new Error('Not found'));
    
    renderDetails('INVALID');
    
    await waitFor(() => {
      expect(screen.getByText(/We could not find the requested application/i)).toBeInTheDocument();
    });
  });
});
