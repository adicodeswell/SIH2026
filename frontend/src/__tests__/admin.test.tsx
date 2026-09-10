import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { InteroperabilityDashboard } from '../pages/admin/InteroperabilityDashboard';
import { applicationApi } from '../lib/api';

vi.mock('../lib/api', () => ({
  applicationApi: {
    get: vi.fn(),
  },
}));

describe('Interoperability Dashboard', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    vi.clearAllMocks();
  });

  const renderDashboard = () => {
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/admin/interoperability']}>
          <Routes>
            <Route path="/admin/interoperability" element={<InteroperabilityDashboard />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );
  };

  it('renders loading and then services successfully', async () => {
    vi.mocked(applicationApi.get).mockResolvedValue({
      data: [
        {
          serviceCode: 'EDU-01',
          serviceName: 'Scholarship App',
          departmentCode: 'EDU',
          departmentName: 'Education',
          active: true,
        },
      ],
    });

    renderDashboard();

    expect(screen.getByText('Platform Operations & Interoperability')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('Scholarship App')).toBeInTheDocument();
      expect(screen.getByText('EDU-01')).toBeInTheDocument();
    });

    // Check that limitations are documented
    expect(screen.getByText('Backend API Limitations')).toBeInTheDocument();
    expect(screen.getByText('Data Unavailable')).toBeInTheDocument();
    expect(screen.getByText('Not Implementable')).toBeInTheDocument();
  });

  it('renders empty state for services', async () => {
    vi.mocked(applicationApi.get).mockResolvedValue({ data: [] });

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText('No services found.')).toBeInTheDocument();
    });
  });

  it('renders error state for services', async () => {
    vi.mocked(applicationApi.get).mockRejectedValue(new Error('Network Error'));

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText('Failed to load service catalog.')).toBeInTheDocument();
    });
  });
});
