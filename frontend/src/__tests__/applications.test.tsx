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
    queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } 
  it('renders APPROVED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'APPROVED' };
    const activity = [
      { id: '1', title: 'Application Approved', description: 'Everything was fine', status: 'APPROVED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Approved/i);
    expect(screen.getByText(/Everything was fine/i)).toBeInTheDocument();
    
    // Status Badge check (assuming 'Approved' text is in a badge)
    expect(screen.getAllByText('Approved')[0]).toBeInTheDocument();
  });

  it('renders REJECTED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'REJECTED' };
    const activity = [
      { id: '2', title: 'Application Rejected', description: 'Applicant does not satisfy conditions', status: 'REJECTED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Rejected/i);
    expect(screen.getByText(/Applicant does not satisfy conditions/i)).toBeInTheDocument();
    
    // Status Badge check
    expect(screen.getAllByText('Rejected')[0]).toBeInTheDocument();
  });

});
    vi.clearAllMocks();
  
  it('renders APPROVED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'APPROVED' };
    const activity = [
      { id: '1', title: 'Application Approved', description: 'Everything was fine', status: 'APPROVED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Approved/i);
    expect(screen.getByText(/Everything was fine/i)).toBeInTheDocument();
    
    // Status Badge check (assuming 'Approved' text is in a badge)
    expect(screen.getAllByText('Approved')[0]).toBeInTheDocument();
  });

  it('renders REJECTED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'REJECTED' };
    const activity = [
      { id: '2', title: 'Application Rejected', description: 'Applicant does not satisfy conditions', status: 'REJECTED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Rejected/i);
    expect(screen.getByText(/Applicant does not satisfy conditions/i)).toBeInTheDocument();
    
    // Status Badge check
    expect(screen.getAllByText('Rejected')[0]).toBeInTheDocument();
  });

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
    
    const button = screen.getByRole('button', { name: /Track/i 
  it('renders APPROVED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'APPROVED' };
    const activity = [
      { id: '1', title: 'Application Approved', description: 'Everything was fine', status: 'APPROVED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Approved/i);
    expect(screen.getByText(/Everything was fine/i)).toBeInTheDocument();
    
    // Status Badge check (assuming 'Approved' text is in a badge)
    expect(screen.getAllByText('Approved')[0]).toBeInTheDocument();
  });

  it('renders REJECTED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'REJECTED' };
    const activity = [
      { id: '2', title: 'Application Rejected', description: 'Applicant does not satisfy conditions', status: 'REJECTED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Rejected/i);
    expect(screen.getByText(/Applicant does not satisfy conditions/i)).toBeInTheDocument();
    
    // Status Badge check
    expect(screen.getAllByText('Rejected')[0]).toBeInTheDocument();
  });

});
    await userEvent.click(button);
    
    await waitFor(() => {
      expect(screen.getByText('Details Page')).toBeInTheDocument();
    
  it('renders APPROVED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'APPROVED' };
    const activity = [
      { id: '1', title: 'Application Approved', description: 'Everything was fine', status: 'APPROVED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Approved/i);
    expect(screen.getByText(/Everything was fine/i)).toBeInTheDocument();
    
    // Status Badge check (assuming 'Approved' text is in a badge)
    expect(screen.getAllByText('Approved')[0]).toBeInTheDocument();
  });

  it('renders REJECTED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'REJECTED' };
    const activity = [
      { id: '2', title: 'Application Rejected', description: 'Applicant does not satisfy conditions', status: 'REJECTED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Rejected/i);
    expect(screen.getByText(/Applicant does not satisfy conditions/i)).toBeInTheDocument();
    
    // Status Badge check
    expect(screen.getAllByText('Rejected')[0]).toBeInTheDocument();
  });

});
  
  it('renders APPROVED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'APPROVED' };
    const activity = [
      { id: '1', title: 'Application Approved', description: 'Everything was fine', status: 'APPROVED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Approved/i);
    expect(screen.getByText(/Everything was fine/i)).toBeInTheDocument();
    
    // Status Badge check (assuming 'Approved' text is in a badge)
    expect(screen.getAllByText('Approved')[0]).toBeInTheDocument();
  });

  it('renders REJECTED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'REJECTED' };
    const activity = [
      { id: '2', title: 'Application Rejected', description: 'Applicant does not satisfy conditions', status: 'REJECTED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Rejected/i);
    expect(screen.getByText(/Applicant does not satisfy conditions/i)).toBeInTheDocument();
    
    // Status Badge check
    expect(screen.getAllByText('Rejected')[0]).toBeInTheDocument();
  });

});

  it('renders application details successfully', async () => {
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(mockApplication as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(mockTimeline as any);
    
    renderDetails();
    
    await screen.findByText(/MH-2024-TEST/i);
    expect(screen.getAllByText(/TEST_SERVICE/i)[0]).toBeInTheDocument();
    expect(screen.getByText(/Application submitted/i)).toBeInTheDocument();
  
  it('renders APPROVED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'APPROVED' };
    const activity = [
      { id: '1', title: 'Application Approved', description: 'Everything was fine', status: 'APPROVED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Approved/i);
    expect(screen.getByText(/Everything was fine/i)).toBeInTheDocument();
    
    // Status Badge check (assuming 'Approved' text is in a badge)
    expect(screen.getAllByText('Approved')[0]).toBeInTheDocument();
  });

  it('renders REJECTED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'REJECTED' };
    const activity = [
      { id: '2', title: 'Application Rejected', description: 'Applicant does not satisfy conditions', status: 'REJECTED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Rejected/i);
    expect(screen.getByText(/Applicant does not satisfy conditions/i)).toBeInTheDocument();
    
    // Status Badge check
    expect(screen.getAllByText('Rejected')[0]).toBeInTheDocument();
  });

});

  it('handles application not found error', async () => {
    vi.mocked(applicationServiceApi.getApplicationById).mockRejectedValue(new Error('Not found'));
    
    renderDetails('INVALID');
    
    await waitFor(() => {
      expect(screen.getByText(/We could not find the requested application/i)).toBeInTheDocument();
    
  it('renders APPROVED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'APPROVED' };
    const activity = [
      { id: '1', title: 'Application Approved', description: 'Everything was fine', status: 'APPROVED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Approved/i);
    expect(screen.getByText(/Everything was fine/i)).toBeInTheDocument();
    
    // Status Badge check (assuming 'Approved' text is in a badge)
    expect(screen.getAllByText('Approved')[0]).toBeInTheDocument();
  });

  it('renders REJECTED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'REJECTED' };
    const activity = [
      { id: '2', title: 'Application Rejected', description: 'Applicant does not satisfy conditions', status: 'REJECTED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Rejected/i);
    expect(screen.getByText(/Applicant does not satisfy conditions/i)).toBeInTheDocument();
    
    // Status Badge check
    expect(screen.getAllByText('Rejected')[0]).toBeInTheDocument();
  });

});
  
  it('renders APPROVED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'APPROVED' };
    const activity = [
      { id: '1', title: 'Application Approved', description: 'Everything was fine', status: 'APPROVED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Approved/i);
    expect(screen.getByText(/Everything was fine/i)).toBeInTheDocument();
    
    // Status Badge check (assuming 'Approved' text is in a badge)
    expect(screen.getAllByText('Approved')[0]).toBeInTheDocument();
  });

  it('renders REJECTED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'REJECTED' };
    const activity = [
      { id: '2', title: 'Application Rejected', description: 'Applicant does not satisfy conditions', status: 'REJECTED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Rejected/i);
    expect(screen.getByText(/Applicant does not satisfy conditions/i)).toBeInTheDocument();
    
    // Status Badge check
    expect(screen.getAllByText('Rejected')[0]).toBeInTheDocument();
  });

});

  it('renders APPROVED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'APPROVED' };
    const activity = [
      { id: '1', title: 'Application Approved', description: 'Everything was fine', status: 'APPROVED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Approved/i);
    expect(screen.getByText(/Everything was fine/i)).toBeInTheDocument();
    
    // Status Badge check (assuming 'Approved' text is in a badge)
    expect(screen.getAllByText('Approved')[0]).toBeInTheDocument();
  });

  it('renders REJECTED activity with reason correctly', async () => {
    const app = { ...mockApplication, status: 'REJECTED' };
    const activity = [
      { id: '2', title: 'Application Rejected', description: 'Applicant does not satisfy conditions', status: 'REJECTED', category: 'OFFICER_REVIEW', occurredAt: '2024-03-01T12:00:00Z' }
    ];
    vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue(app as any);
    vi.mocked(applicationServiceApi.getApplicationActivity).mockResolvedValue(activity as any);
    
    renderDetails();
    
    await screen.findByText(/Application Rejected/i);
    expect(screen.getByText(/Applicant does not satisfy conditions/i)).toBeInTheDocument();
    
    // Status Badge check
    expect(screen.getAllByText('Rejected')[0]).toBeInTheDocument();
  });

});
