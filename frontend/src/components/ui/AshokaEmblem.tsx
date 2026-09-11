import React from 'react';
import { cn } from '@/lib/utils';

export type AshokaEmblemSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl';
export type AshokaEmblemVariant = 'navy' | 'light' | 'slate';

interface AshokaEmblemProps extends React.ImgHTMLAttributes<HTMLImageElement> {
  size?: AshokaEmblemSize;
  variant?: AshokaEmblemVariant;
  className?: string;
  alt?: string;
}

const sizeMap: Record<AshokaEmblemSize, { height: number; width: number; className: string }> = {
  xs: { height: 20, width: 13, className: 'h-5 w-auto' },
  sm: { height: 24, width: 15, className: 'h-6 w-auto' },
  md: { height: 30, width: 19, className: 'h-[30px] w-auto' },
  lg: { height: 38, width: 24, className: 'h-[38px] w-auto' },
  xl: { height: 48, width: 30, className: 'h-12 w-auto' },
};

const variantMap: Record<AshokaEmblemVariant, string> = {
  navy: '/emblem-navy.svg',
  light: '/emblem-light.svg',
  slate: '/emblem-slate.svg',
};

/**
 * AshokaEmblem
 * Renders the State Emblem of India (Lion Capital of Ashoka)
 * in an institutional monochrome format suitable for e-Governance portals.
 */
export const AshokaEmblem: React.FC<AshokaEmblemProps> = ({
  size = 'sm',
  variant = 'navy',
  className,
  alt = 'State Emblem of India',
  ...props
}) => {
  const config = sizeMap[size];
  const src = variantMap[variant];

  return (
    <img
      src={src}
      alt={alt}
      width={config.width}
      height={config.height}
      className={cn(
        'shrink-0 select-none object-contain transition-opacity',
        config.className,
        className
      )}
      loading="eager"
      decoding="async"
      draggable={false}
      {...props}
    />
  );
};
