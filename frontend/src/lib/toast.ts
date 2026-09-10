import { toast as sonnerToast } from 'sonner';

/**
 * Civic Design System Notification Convention
 * Standardized across Citizen and Officer portals.
 */
export const notify = {
  success: (message: string, description?: string) => {
    sonnerToast.success(message, { description });
  },
  info: (message: string, description?: string) => {
    sonnerToast.info(message, { description });
  },
  warning: (message: string, description?: string) => {
    sonnerToast.warning(message, { description });
  },
  error: (message: string, description?: string) => {
    sonnerToast.error(message, { description });
  },
  loading: (message: string, description?: string) => {
    return sonnerToast.loading(message, { description });
  },
  dismiss: (id?: string | number) => {
    sonnerToast.dismiss(id);
  },
};
