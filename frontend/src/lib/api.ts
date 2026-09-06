import axios from 'axios';
import keycloak from './auth';

// Create base Axios instances for your microservices
export const applicationApi = axios.create({
  baseURL: 'http://localhost:8081',
});

export const interoperabilityApi = axios.create({
  baseURL: 'http://localhost:8082',
});

export const workflowApi = axios.create({
  baseURL: 'http://localhost:8083',
});

// Automatically attach the Keycloak JWT Bearer token to every single request
const attachToken = async (config: any) => {
  if (keycloak.token) {
    config.headers.Authorization = `Bearer ${keycloak.token}`;
  }
  return config;
};

applicationApi.interceptors.request.use(attachToken);
interoperabilityApi.interceptors.request.use(attachToken);
workflowApi.interceptors.request.use(attachToken);
