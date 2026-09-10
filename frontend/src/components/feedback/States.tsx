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
  title = 'Resource Not Located',
  description = 'The requested government resource or scheme could not be found or may have been transitioned.',
  actionHref = '/',
  actionText = 'Return to Portal Gateway',
}) => {
  return (
    <div className="min-h-[60vh] flex flex-col items-center justify-center text-center p-6 space-y-4 max-w-md mx-auto">
      <div className="w-14 h-14 rounded-sm bg-slate-100 flex items-center justify-center border border-slate-200">
        <FileQuestion className="w-7 h-7 text-slate-600" />
      </div>
      <div className="space-y-1.5">
        <h1 className="text-xl font-bold tracking-tight text-slate-900">{title}</h1>
        <p className="text-xs text-slate-600 leading-relaxed">{description}</p>
      </div>
      <div className="pt-2">
        <Link to={actionHref}>
          <Button className="font-bold text-xs h-9 gap-1.5 bg-[#0B1F3A] hover:bg-[#102A43] text-white">
            <Home className="w-3.5 h-3.5" />
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
        'flex flex-col items-center justify-center text-center p-8 bg-white border border-dashed border-slate-300 rounded-md space-y-3',
        className
      )}
    >
      <div className="w-10 h-10 rounded-sm bg-slate-50 flex items-center justify-center border border-slate-200 text-slate-500">
        {icon || <Inbox className="w-5 h-5" />}
      </div>
      <div className="max-w-sm space-y-1">
        <h3 className="text-sm font-bold text-slate-900">{title}</h3>
        <p className="text-xs text-slate-500 leading-normal">{description}</p>
      </div>
      {action && (
        <Button variant="outline" size="xs" onClick={action.onClick} className="text-xs font-semibold h-8 mt-1">
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
  title = 'Service Communication Interruption',
  message = 'An unexpected error occurred while communicating with the government service endpoint.',
  onRetry,
  className,
}) => {
  return (
    <div
      role="alert"
      className={cn(
        'flex flex-col items-center justify-center text-center p-6 bg-red-50/60 border border-red-200 rounded-md space-y-3 max-w-lg mx-auto',
        className
      )}
    >
      <div className="w-10 h-10 rounded-sm bg-red-100 flex items-center justify-center text-red-700">
        <AlertTriangle className="w-5 h-5" />
      </div>
      <div className="space-y-1">
        <h3 className="text-sm font-bold text-red-950">{title}</h3>
        <p className="text-xs text-red-800 leading-normal">{message}</p>
      </div>
      {onRetry && (
        <Button
          variant="outline"
          size="xs"
          onClick={onRetry}
          className="border-red-300 bg-white text-red-900 hover:bg-red-50 gap-1.5 h-8 font-semibold text-xs mt-1"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          Retry Connection
        </Button>
      )}
    </div>
  );
};
