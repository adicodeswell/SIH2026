export interface OfficerReviewTaskResponse {
  taskId: string;
  taskName: string;
  applicationId: string;
  processInstanceId: string;
  citizenId: string;
  serviceCode: string;
  createTime: string;
  candidateGroup: string;
  assignee: string | null;
  status: string;
}

export interface OfficerDecisionRequest {
  decision: 'APPROVE' | 'REJECT';
  reason?: string;
}

export interface OfficerDecisionResponse {
  taskId: string;
  applicationId: string;
  decision: string;
  officerId: string;
  reason?: string;
  timestamp: string;
  status: string;
}
