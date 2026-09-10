import { applicationApi } from '@/lib/api';
import type { ApplicationResponse, TimelineEventResponse } from '@/types/service';

export const applicationServiceApi = {
  getApplicationById: async (id: string): Promise<ApplicationResponse> => {
    const response = await applicationApi.get<ApplicationResponse>(`/api/v1/applications/${id}`);
    return response.data;
  },

  getApplicationTimeline: async (id: string): Promise<TimelineEventResponse[]> => {
    const response = await applicationApi.get<TimelineEventResponse[]>(`/api/v1/applications/${id}/timeline`);
    return response.data;
  },
};
