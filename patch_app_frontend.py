import re

file = 'frontend/src/__tests__/applications.test.tsx'
with open(file, 'r') as f:
    content = f.read()

tests_to_add = """
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
"""

content = content.replace("});\n", tests_to_add + "\n});\n")

with open(file, 'w') as f:
    f.write(content)

