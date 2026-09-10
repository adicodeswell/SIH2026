import { applicationApi } from '@/lib/api';
import type { ApplicationResponse, TimelineEventResponse, CitizenApplicationSummaryResponse, CitizenApplicationActivityResponse } from '@/types/service';

export const applicationServiceApi = {
  getMyApplications: async (): Promise<CitizenApplicationSummaryResponse[]> => {
    const response = await applicationApi.get<CitizenApplicationSummaryResponse[]>('/api/v1/applications/me');
    return response.data;
  },

  getApplicationById: async (id: string): Promise<ApplicationResponse> => {
    const response = await applicationApi.get<ApplicationResponse>(`/api/v1/applications/${id}`);
    return response.data;
  },

  getApplicationActivity: async (id: string): Promise<CitizenApplicationActivityResponse[]> => {
    const response = await applicationApi.get<CitizenApplicationActivityResponse[]>(`/api/v1/applications/${id}/activity`);
    return response.data;
  },

  getApplicationTimeline: async (id: string): Promise<TimelineEventResponse[]> => {
    const response = await applicationApi.get<TimelineEventResponse[]>(`/api/v1/applications/${id}/timeline`);
    return response.data;
  },
};
