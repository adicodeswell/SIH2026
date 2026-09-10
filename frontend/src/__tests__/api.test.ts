import { describe, it, expect, vi, beforeEach } from 'vitest';
import { normalizeApiError, applicationApi } from '../lib/api';
import keycloak from '../lib/auth';

vi.mock('../lib/auth', () => ({
  default: {
    token: 'mock-jwt-token',
    updateToken: vi.fn().mockResolvedValue(true),
    login: vi.fn(),
    logout: vi.fn(),
    hasRealmRole: vi.fn(),
  },
}));

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
    info: vi.fn(),
    warning: vi.fn(),
    loading: vi.fn(),
    dismiss: vi.fn(),
  },
}));

describe('API Client Foundation & Error Normalization', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('normalizes network failure into a user-friendly civic error', () => {
    const networkError = {
      message: 'Network Error',
      isAxiosError: true,
    };
    const normalized = normalizeApiError(networkError);
    expect(normalized.status).toBe(0);
    expect(normalized.code).toBe('NETWORK_ERROR');
    expect(normalized.message).toBe('Unable to connect to Ekikrit. Please check your connection and try again.');
  });

  it('normalizes 401 Unauthorized into session expiration message', () => {
    const error401 = {
      response: {
        status: 401,
        data: { message: 'Unauthorized' },
      },
      isAxiosError: true,
    };
    const normalized = normalizeApiError(error401);
    expect(normalized.status).toBe(401);
    expect(normalized.code).toBe('UNAUTHORIZED');
    expect(normalized.message).toBe('Your session has expired. Please sign in again.');
  });

  it('normalizes 403 Forbidden into permissions message', () => {
    const error403 = {
      response: {
        status: 403,
        data: { message: 'Forbidden' },
      },
      isAxiosError: true,
    };
    const normalized = normalizeApiError(error403);
    expect(normalized.status).toBe(403);
    expect(normalized.code).toBe('FORBIDDEN');
    expect(normalized.message).toBe("You don't have permission to perform this action.");
  });

  it('normalizes 404 Not Found into resource missing message', () => {
    const error404 = {
      response: {
        status: 404,
        data: { message: 'Not found' },
      },
      isAxiosError: true,
    };
    const normalized = normalizeApiError(error404);
    expect(normalized.status).toBe(404);
    expect(normalized.code).toBe('NOT_FOUND');
    expect(normalized.message).toBe('The requested resource could not be found.');
  });

  it('normalizes 409 Conflict into state conflict message', () => {
    const error409 = {
      response: {
        status: 409,
        data: { message: 'Conflict' },
      },
      isAxiosError: true,
    };
    const normalized = normalizeApiError(error409);
    expect(normalized.status).toBe(409);
    expect(normalized.code).toBe('CONFLICT');
    expect(normalized.message).toBe('This action conflicts with the current application state.');
  });

  it('normalizes 500 Server Error and prevents internal stack exposure', () => {
    const error500 = {
      response: {
        status: 500,
        data: {
          message: 'org.camunda.bpm.engine.ProcessEngineException: Could not execute query',
          stackTrace: ['at org.camunda...', 'at org.springframework...'],
        },
      },
      isAxiosError: true,
    };
    const normalized = normalizeApiError(error500);
    expect(normalized.status).toBe(500);
    expect(normalized.code).toBe('SERVER_ERROR');
    expect(normalized.message).toBe('Something went wrong. Please try again.');
    // Ensure no internals leaked
    expect(normalized.message).not.toContain('camunda');
    expect(normalized.message).not.toContain('ProcessEngineException');
  });

  it('attaches authorization header with token to request config', async () => {
    // Interceptor test
    const config: any = { headers: {} };
    // Get request interceptor handler from applicationApi
    const interceptor = (applicationApi.interceptors.request as any).handlers[0].fulfilled;
    const modifiedConfig = await interceptor(config);
    expect(modifiedConfig.headers.Authorization).toBe('Bearer mock-jwt-token');
    expect(keycloak.updateToken).toHaveBeenCalledWith(30);
  });
});
