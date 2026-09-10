import axios, { type AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import keycloak from './auth';
import { toast } from 'sonner';

/**
 * Normalized API error structure.
 * Never exposes raw stack traces or internal backend infrastructure details to the UI.
 */
export interface ApiError {
  status: number;
  code: string;
  message: string;
  timestamp: string;
  details?: unknown;
}

/**
 * Normalizes HTTP and network errors into standard user-friendly civic messages.
 */
export function normalizeApiError(error: AxiosError | any): ApiError {
  const timestamp = new Date().toISOString();

  if (!error.response) {
    // Network failure or backend unreachable
    return {
      status: 0,
      code: 'NETWORK_ERROR',
      message: 'Unable to connect to Ekikrit. Please check your connection and try again.',
      timestamp,
    };
  }

  const { status, data } = error.response as { status: number; data: any };

  switch (status) {
    case 401:
      return {
        status,
        code: 'UNAUTHORIZED',
        message: 'Your session has expired. Please sign in again.',
        timestamp,
      };
    case 403:
      return {
        status,
        code: 'FORBIDDEN',
        message: "You don't have permission to perform this action.",
        timestamp,
      };
    case 404:
      return {
        status,
        code: 'NOT_FOUND',
        message: 'The requested resource could not be found.',
        timestamp,
      };
    case 409:
      return {
        status,
        code: 'CONFLICT',
        message: 'This action conflicts with the current application state.',
        timestamp,
      };
    case 500:
    case 502:
    case 503:
    case 504:
      return {
        status,
        code: 'SERVER_ERROR',
        message: 'Something went wrong. Please try again.',
        timestamp,
      };
    default: {
      // Safe fallback - check if backend sent a sanitized message
      const sanitizedMessage =
        typeof data?.message === 'string' &&
        !data.message.includes('Exception') &&
        !data.message.includes('org.camunda') &&
        !data.message.includes('org.springframework')
          ? data.message
          : 'An unexpected error occurred. Please try again.';
      return {
        status,
        code: data?.code || `HTTP_${status}`,
        message: sanitizedMessage,
        timestamp,
      };
    }
  }
}

// Global Axios Instances matching microservices through Vite proxy
export const applicationApi = axios.create();
export const workflowApi = axios.create();
export const officerApi = axios.create();

// Attach Keycloak JWT Bearer token to requests
const attachToken = async (config: InternalAxiosRequestConfig) => {
  if (keycloak && keycloak.token) {
    try {
      if (typeof keycloak.updateToken === 'function') {
        await keycloak.updateToken(30);
      }
      if (keycloak.token) {
        config.headers.Authorization = `Bearer ${keycloak.token}`;
      }
    } catch (error) {
      console.error('Failed to refresh Keycloak token', error);
      if (typeof keycloak.login === 'function') {
        keycloak.login();
      }
    }
  }
  return config;
};

// Response interceptor with global error normalization & toast notification
const handleResponseSuccess = (response: AxiosResponse) => response;

const handleResponseError = (error: AxiosError) => {
  const normalized = normalizeApiError(error);

  if (normalized.status === 401) {
    toast.error('Session Expired', {
      description: normalized.message,
    });
    if (keycloak && typeof keycloak.login === 'function') {
      keycloak.login();
    }
  } else if (normalized.status === 403) {
    toast.error('Access Denied', {
      description: normalized.message,
    });
  }

  return Promise.reject(normalized);
};

// Attach interceptors
[applicationApi, workflowApi, officerApi].forEach((client) => {
  client.interceptors.request.use(attachToken);
  client.interceptors.response.use(handleResponseSuccess, handleResponseError);
});

