import axios from 'axios';
import keycloak from './auth';

// By leaving baseURL empty, Vite's proxy (in vite.config.ts) will seamlessly 
// intercept requests starting with /api and forward them to the correct microservice!
// This completely bypasses all CORS errors.
export const applicationApi = axios.create();
export const interoperabilityApi = axios.create();
export const workflowApi = axios.create();
export const officerApi = axios.create();

// Automatically refresh and attach the Keycloak JWT Bearer token to every single request
const attachToken = async (config: any) => {
  if (keycloak.token) {
    try {
      // If the token is going to expire within 30 seconds, refresh it BEFORE making the API call!
      await keycloak.updateToken(30);
      config.headers.Authorization = `Bearer ${keycloak.token}`;
    } catch (error) {
      console.error("Failed to refresh Keycloak token", error);
      keycloak.login(); // Force re-login if refresh fails
    }
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
