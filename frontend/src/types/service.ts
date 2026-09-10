export interface ServiceResponse {
  serviceCode: string;
  serviceName: string;
  description: string;
  active: boolean;
  departmentCode: string;
  departmentName: string;
}

export interface CreateApplicationRequest {
  citizenId: string;
  serviceCode: string;
}

export type ApplicationStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'IN_PROGRESS'
  | 'PENDING_VERIFICATION'
  | 'PENDING_REVIEW'
  | 'PENDING_OFFICER_REVIEW'
  | 'APPROVED'
  | 'REJECTED'
  | 'CONSENT_DENIED'
  | 'FAILED'
  | 'CANCELLED'
  | 'COMPLETED';

export interface ApplicationResponse {
  applicationNumber: string;
  status: ApplicationStatus;
  citizenId: string;
  serviceCode: string;
  submittedAt: string;
  verificationData?: unknown;
}

export interface ConsentRequest {
  applicationId: string;
  serviceCode: string;
  dataScope: string;
  purpose: string;
  requestingDepartmentId: string;
}

export interface ConsentResponse {
  id: string;
  citizenId: string;
  dataScope: string;
  purpose: string;
  requestingDepartmentId: string;
  status: string;
  grantedAt: string;
  revokedAt?: string | null;
}

export interface TimelineEventResponse {
  eventType: string;
  description: string;
  occurredAt: string;
}
