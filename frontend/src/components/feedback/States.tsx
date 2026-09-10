import React from 'react';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { FileQuestion, AlertTriangle, Inbox, RefreshCw, Home } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface NotFoundProps {
  title?: string;
  description?: string;
  actionHref?: string;
  actionText?: string;
}

export const NotFound: React.FC<NotFoundProps> = ({
  title = 'Page not found',
  description = "The requested resource could not be found or may have moved to a different department section.",
  actionHref = '/',
  actionText = 'Return to Portal Home',
}) => {
  return (
    <div className="min-h-[60vh] flex flex-col items-center justify-center text-center p-6 space-y-5 max-w-md mx-auto">
      <div className="w-16 h-16 rounded-2xl bg-slate-100 flex items-center justify-center border border-slate-200">
        <FileQuestion className="w-8 h-8 text-slate-600" />
      </div>
      <div className="space-y-2">
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">{title}</h1>
        <p className="text-sm text-slate-600 leading-relaxed">{description}</p>
      </div>
      <div className="flex gap-3">
        <Link to={actionHref}>
          <Button className="font-medium gap-2">
            <Home className="w-4 h-4" />
            {actionText}
          </Button>
        </Link>
      </div>
    </div>
  );
};

export interface EmptyStateProps {
  icon?: React.ReactNode;
  title: string;
  description: string;
  action?: {
    label: string;
    onClick: () => void;
  };
  className?: string;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  icon,
  title,
  description,
  action,
  className,
}) => {
  return (
    <div
      className={cn(
        'flex flex-col items-center justify-center text-center p-8 bg-white border border-dashed border-slate-200 rounded-xl space-y-4',
        className
      )}
    >
      <div className="w-12 h-12 rounded-xl bg-slate-50 flex items-center justify-center border border-slate-200 text-slate-500">
        {icon || <Inbox className="w-6 h-6" />}
      </div>
      <div className="max-w-sm space-y-1">
        <h3 className="text-base font-semibold text-slate-900">{title}</h3>
        <p className="text-sm text-slate-500 leading-normal">{description}</p>
      </div>
      {action && (
        <Button variant="outline" size="sm" onClick={action.onClick}>
          {action.label}
        </Button>
      )}
    </div>
  );
};

export interface ErrorStateProps {
  title?: string;
  message?: string;
  onRetry?: () => void;
  className?: string;
}

export const ErrorState: React.FC<ErrorStateProps> = ({
  title = 'Service Unavailable',
  message = 'An unexpected error occurred while communicating with the service.',
  onRetry,
  className,
}) => {
  return (
    <div
      role="alert"
      className={cn(
        'flex flex-col items-center justify-center text-center p-8 bg-red-50/50 border border-red-200 rounded-xl space-y-4 max-w-lg mx-auto',
        className
      )}
    >
      <div className="w-12 h-12 rounded-xl bg-red-100 flex items-center justify-center text-red-700">
        <AlertTriangle className="w-6 h-6" />
      </div>
      <div className="space-y-1">
        <h3 className="text-base font-semibold text-red-950">{title}</h3>
        <p className="text-sm text-red-800 leading-normal">{message}</p>
      </div>
      {onRetry && (
        <Button
          variant="outline"
          size="sm"
          onClick={onRetry}
          className="border-red-200 bg-white text-red-900 hover:bg-red-50 gap-2"
        >
          <RefreshCw className="w-4 h-4" />
          Try Again
        </Button>
      )}
    </div>
  );
};
