import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import OfficerDashboard from '../pages/officer/Dashboard';
import { OfficerReviewTaskPage } from '../pages/officer/ReviewTaskPage';
import { officerServiceApi } from '../services/officerService';
import { applicationServiceApi } from '../services/applicationService';

vi.mock('../services/officerService', () => ({
  officerServiceApi: {
    getPendingReviews: vi.fn(),
    getReviewTask: vi.fn(),
    claimTask: vi.fn(),
    unclaimTask: vi.fn(),
    submitDecision: vi.fn(),
  },
}));

vi.mock('../services/applicationService', () => ({
  applicationServiceApi: {
    getApplicationById: vi.fn(),
    getApplicationTimeline: vi.fn(),
  },
}));

// Mock AuthContext
vi.mock('../context/AuthContext', () => ({
  useAuth: () => ({
    user: { username: 'officer1' },
    isAuthenticated: true,
  }),
}));

const mockTasks = [
  {
    taskId: 'task-1',
    taskName: 'Review Application',
    applicationId: 'MH-100',
    processInstanceId: 'pi-1',
    citizenId: 'c-1',
    serviceCode: 'TEST',
    createTime: '2024-03-01T10:00:00Z',
    candidateGroup: 'OFFICER',
    assignee: null,
    status: 'PENDING_OFFICER_REVIEW',
  },
];

describe('Officer Workflow', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    vi.clearAllMocks();
  });

  const renderDashboard = () => {
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/officer']}>
          <Routes>
            <Route path="/officer" element={<OfficerDashboard />} />
            <Route path="/officer/reviews/:taskId" element={<div>Details Page</div>} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );
  };

  const renderDetails = (taskId = 'task-1') => {
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={[`/officer/reviews/${taskId}`]}>
          <Routes>
            <Route path="/officer/reviews/:taskId" element={<OfficerReviewTaskPage />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );
  };

  describe('Officer Dashboard', () => {
    it('renders empty state', async () => {
      vi.mocked(officerServiceApi.getPendingReviews).mockResolvedValue([]);
      renderDashboard();
      
      await waitFor(() => {
        expect(screen.getByText('Inbox Clear')).toBeInTheDocument();
      });
    });

    it('renders tasks and navigates', async () => {
      vi.mocked(officerServiceApi.getPendingReviews).mockResolvedValue(mockTasks);
      renderDashboard();
      
      await waitFor(() => {
        expect(screen.getByText('MH-100')).toBeInTheDocument();
      });
      
      fireEvent.click(screen.getByText(/Review File/i));
      
      await waitFor(() => {
        expect(screen.getByText('Details Page')).toBeInTheDocument();
      });
    });
  });

  describe('Review Task Page', () => {
    beforeEach(() => {
      vi.mocked(officerServiceApi.getReviewTask).mockResolvedValue(mockTasks[0]);
      vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue({
        applicationNumber: 'MH-100',
        status: 'PENDING_OFFICER_REVIEW',
        citizenId: 'c-1',
        serviceCode: 'TEST',
        submittedAt: '2024-03-01',
        verificationData: { verified: true }
      } as any);
    });

    
    it('displays task verification data when application verification data is missing', async () => {
      vi.mocked(officerServiceApi.getReviewTask).mockResolvedValue({
        ...mockTasks[0],
        verificationData: { overallStatus: "VERIFIED_FROM_TASK" }
      });
      vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue({
        applicationNumber: 'MH-100',
        status: 'PENDING_OFFICER_REVIEW',
        citizenId: 'c-1',
        serviceCode: 'TEST',
        submittedAt: '2024-03-01',
      } as any);

      renderDetails();
      
      await waitFor(() => {
        expect(screen.getByText(/VERIFIED_FROM_TASK/i)).toBeInTheDocument();
      });
    });

    it('displays no verification payload message when both are missing', async () => {
      vi.mocked(officerServiceApi.getReviewTask).mockResolvedValue({
        ...mockTasks[0]
      });
      vi.mocked(applicationServiceApi.getApplicationById).mockResolvedValue({
        applicationNumber: 'MH-100',
        status: 'PENDING_OFFICER_REVIEW',
        citizenId: 'c-1',
        serviceCode: 'TEST',
        submittedAt: '2024-03-01',
      } as any);

      renderDetails();
      
      await waitFor(() => {
        expect(screen.getByText(/No verification payload available/i)).toBeInTheDocument();
      });
    });

    it('renders review details successfully', async () => {
      renderDetails();
      
      await waitFor(() => {
        expect(screen.getByText(/Dossier Review: MH-100/i)).toBeInTheDocument();
        expect(screen.getByText(/Authoritative State System Verified/i)).toBeInTheDocument();
      });
    });

    it('claims task successfully', async () => {
      renderDetails();
      
      await waitFor(() => {
        expect(screen.getByRole('button', { name: /Claim File for Review/i })).toBeInTheDocument();
      });
      
      vi.mocked(officerServiceApi.claimTask).mockResolvedValue({ ...mockTasks[0], assignee: 'officer1' });
      
      fireEvent.click(screen.getByRole('button', { name: /Claim File for Review/i }));
      
      await waitFor(() => {
        expect(officerServiceApi.claimTask).toHaveBeenCalledWith('task-1');
      });
    });

    it('submits approve decision', async () => {
      // Simulate task already claimed by me
      vi.mocked(officerServiceApi.getReviewTask).mockResolvedValue({ ...mockTasks[0], assignee: 'officer1' });
      
      renderDetails();
      
      await waitFor(() => {
        expect(screen.getByText(/Approve/i)).toBeInTheDocument();
      });
      
      fireEvent.click(screen.getByText(/Approve/i));
      
      await waitFor(() => {
        expect(screen.getByRole('button', { name: /Submit Statutory Resolution/i })).not.toBeDisabled();
      });
      
      vi.mocked(officerServiceApi.submitDecision).mockResolvedValue({} as any);
      
      fireEvent.click(screen.getByRole('button', { name: /Submit Statutory Resolution/i }));
      
      await waitFor(() => {
        expect(officerServiceApi.submitDecision).toHaveBeenCalledWith('task-1', { decision: 'APPROVE', reason: '' });
      });
    });
  });
});
