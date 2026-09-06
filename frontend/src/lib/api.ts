import axios from 'axios';
import keycloak from './auth';

// By leaving baseURL empty, Vite's proxy (in vite.config.ts) will seamlessly 
// intercept requests starting with /api and forward them to the correct microservice!
// This completely bypasses all CORS errors.
export const applicationApi = axios.create();
export const interoperabilityApi = axios.create();
export const workflowApi = axios.create();
export const officerApi = axios.create();

// Automatically attach the Keycloak JWT Bearer token to every single request
const attachToken = async (config: any) => {
  if (keycloak.token) {
    config.headers.Authorization = `Bearer ${keycloak.token}`;
  }
  return config;
};

applicationApi.interceptors.request.use(attachToken);
workflowApi.interceptors.request.use(attachToken);
officerApi.interceptors.request.use(attachToken);

// The Interoperability Service is an internal microservice that does not use Keycloak.
// It explicitly expects the hardcoded internal service token instead of the user's JWT!
interoperabilityApi.interceptors.request.use((config) => {
  config.headers.Authorization = 'Bearer dev-interop-token';
  return config;
});
