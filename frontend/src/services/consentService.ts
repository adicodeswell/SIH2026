import { workflowApi } from '@/lib/api';
import type { ConsentResponse } from '@/types/service';

export const consentServiceApi = {
  getConsents: async (): Promise<ConsentResponse[]> => {
    const response = await workflowApi.get<ConsentResponse[]>('/api/v1/consents');
    return response.data;
  },

  revokeConsent: async (id: string): Promise<void> => {
    await workflowApi.post(`/api/v1/consents/${id}/revoke`);
  },
};
