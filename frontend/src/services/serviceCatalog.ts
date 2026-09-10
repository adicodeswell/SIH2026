import { applicationApi, workflowApi } from '@/lib/api';
import type { ServiceResponse, ApplicationResponse, CreateApplicationRequest, ConsentRequest, ConsentResponse } from '@/types/service';

export const serviceCatalogApi = {
  getServices: async (): Promise<ServiceResponse[]> => {
    const response = await applicationApi.get<ServiceResponse[]>('/api/v1/services');
    return response.data;
  },

  getServiceById: async (serviceCode: string): Promise<ServiceResponse> => {
    const response = await applicationApi.get<ServiceResponse>(`/api/v1/services/${serviceCode}`);
    return response.data;
  },

  createApplication: async (payload: CreateApplicationRequest): Promise<ApplicationResponse> => {
    const response = await applicationApi.post<ApplicationResponse>('/api/v1/applications', payload);
    return response.data;
  },

  
  submitApplication: async (applicationNumber: string): Promise<ApplicationResponse> => {
    const response = await applicationApi.post<ApplicationResponse>(`/api/v1/applications/${applicationNumber}/submit`);
    return response.data;
  },

  grantConsent: async (payload: ConsentRequest): Promise<ConsentResponse> => {
    const response = await workflowApi.post<ConsentResponse>('/api/v1/consents', payload);
    return response.data;
  },
};
