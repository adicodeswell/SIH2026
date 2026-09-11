import re

with open('frontend/src/__tests__/officer.test.tsx', 'r') as f:
    content = f.read()

new_test = """
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
"""

# Insert before the first `it(` in `describe('Review Task Page'`
target = "it('renders review details successfully', async () => {"
content = content.replace(target, new_test + "\n    " + target)

with open('frontend/src/__tests__/officer.test.tsx', 'w') as f:
    f.write(content)

