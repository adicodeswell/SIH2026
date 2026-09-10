import React from 'react';
import { Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface LoadingSpinnerProps extends React.HTMLAttributes<HTMLDivElement> {
  size?: 'sm' | 'md' | 'lg';
  label?: string;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = 'md',
  label = 'Loading...',
  className,
  ...props
}) => {
  const sizeMap = {
    sm: 'w-4 h-4',
    md: 'w-6 h-6',
    lg: 'w-8 h-8',
  };

  return (
    <div
      role="status"
      aria-label={label}
      className={cn('inline-flex items-center gap-2 text-primary', className)}
      {...props}
    >
      <Loader2 className={cn('animate-spin text-primary', sizeMap[size])} />
      <span className="sr-only">{label}</span>
    </div>
  );
};

export interface PageLoaderProps {
  message?: string;
  subtext?: string;
}

export const PageLoader: React.FC<PageLoaderProps> = ({
  message = 'Loading platform resources...',
  subtext = 'Government of Maharashtra Digital Portal',
}) => {
  return (
    <div
      role="status"
      aria-live="polite"
      className="min-h-[50vh] flex flex-col items-center justify-center p-8 space-y-4"
    >
      <div className="w-12 h-12 rounded-full border-3 border-primary/20 border-t-primary animate-spin" />
      <div className="text-center space-y-1">
        <p className="text-base font-semibold text-slate-900">{message}</p>
        <p className="text-xs text-slate-500">{subtext}</p>
      </div>
      <span className="sr-only">{message}</span>
    </div>
  );
};

export interface SkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  className?: string;
}

export const Skeleton: React.FC<SkeletonProps> = ({ className, ...props }) => {
  return (
    <div
      data-slot="skeleton"
      className={cn('animate-pulse rounded-md bg-slate-200/80', className)}
      {...props}
    />
  );
};

export interface LoadingOverlayProps {
  active: boolean;
  message?: string;
  children: React.ReactNode;
}

export const LoadingOverlay: React.FC<LoadingOverlayProps> = ({
  active,
  message = 'Processing secure request...',
  children,
}) => {
  return (
    <div className="relative">
      {children}
      {active && (
        <div
          role="status"
          aria-live="assertive"
          className="absolute inset-0 z-40 bg-white/75 backdrop-blur-[1px] flex flex-col items-center justify-center p-6 space-y-3"
        >
          <Loader2 className="w-8 h-8 text-primary animate-spin" />
          <p className="text-sm font-medium text-slate-800">{message}</p>
        </div>
      )}
    </div>
  );
};
