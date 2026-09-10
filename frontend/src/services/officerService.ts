import { officerApi } from '@/lib/api';
import type { OfficerReviewTaskResponse, OfficerDecisionRequest, OfficerDecisionResponse } from '@/types/officer';

export const officerServiceApi = {
  getPendingReviews: async (): Promise<OfficerReviewTaskResponse[]> => {
    const response = await officerApi.get<OfficerReviewTaskResponse[]>('/api/v1/officer/reviews');
    return response.data;
  },

  getReviewTask: async (taskId: string): Promise<OfficerReviewTaskResponse> => {
    const response = await officerApi.get<OfficerReviewTaskResponse>(`/api/v1/officer/reviews/${taskId}`);
    return response.data;
  },

  claimTask: async (taskId: string): Promise<OfficerReviewTaskResponse> => {
    const response = await officerApi.post<OfficerReviewTaskResponse>(`/api/v1/officer/reviews/${taskId}/claim`);
    return response.data;
  },

  unclaimTask: async (taskId: string): Promise<OfficerReviewTaskResponse> => {
    const response = await officerApi.post<OfficerReviewTaskResponse>(`/api/v1/officer/reviews/${taskId}/unclaim`);
    return response.data;
  },

  submitDecision: async (taskId: string, payload: OfficerDecisionRequest): Promise<OfficerDecisionResponse> => {
    const response = await officerApi.post<OfficerDecisionResponse>(`/api/v1/officer/reviews/${taskId}/decision`, payload);
    return response.data;
  }
};
