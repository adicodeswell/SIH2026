import React from 'react';

interface EkikritLogoProps {
  variant?: 'light' | 'dark';
  size?: 'sm' | 'md' | 'lg';
  showSubtitle?: boolean;
}

export const EkikritLogo: React.FC<EkikritLogoProps> = ({
  variant = 'dark',
  size = 'md',
  showSubtitle = true,
}) => {
  const isDark = variant === 'dark'; // dark means navy background (white text)

  const sizeStyles = {
    sm: {
      emblem: 'w-7 h-7',
      title: 'text-sm',
      sub: 'text-[9px]',
    },
    md: {
      emblem: 'w-9 h-9',
      title: 'text-base',
      sub: 'text-[10px]',
    },
    lg: {
      emblem: 'w-12 h-12',
      title: 'text-xl',
      sub: 'text-xs',
    },
  };

  return (
    <div className="flex items-center gap-3 select-none">
      {/* Institutional Visual Mark / Crest */}
      <div
        className={`${sizeStyles[size].emblem} shrink-0 rounded-sm flex items-center justify-center relative shadow-xs overflow-hidden border ${
          isDark
            ? 'bg-[#102A43] border-[#243B53] text-[#F97316]'
            : 'bg-[#0B1F3A] border-[#102A43] text-[#F97316]'
        }`}
        aria-hidden="true"
      >
        {/* Subtle geometric government crest vector */}
        <svg
          viewBox="0 0 40 40"
          className="w-full h-full p-1"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          {/* Outer circle */}
          <circle cx="20" cy="20" r="17" stroke="#F97316" strokeWidth="1.5" strokeOpacity="0.85" />
          {/* Inner circle */}
          <circle cx="20" cy="20" r="13" stroke="#FFFFFF" strokeWidth="1" strokeOpacity="0.7" />
          {/* Interlocking geometric hub (symbolizing interoperability) */}
          <polygon
            points="20,7 31,14 31,26 20,33 9,26 9,14"
            stroke="#138808"
            strokeWidth="1.2"
            fill="#0B1F3A"
          />
          {/* Central Chakra Hub */}
          <circle cx="20" cy="20" r="3.5" fill="#F97316" />
          <circle cx="20" cy="20" r="1.5" fill="#FFFFFF" />
          {/* Cardinal spokes */}
          <line x1="20" y1="9" x2="20" y2="16.5" stroke="#FFFFFF" strokeWidth="1" />
          <line x1="20" y1="23.5" x2="20" y2="31" stroke="#FFFFFF" strokeWidth="1" />
          <line x1="9" y1="20" x2="16.5" y2="20" stroke="#FFFFFF" strokeWidth="1" />
          <line x1="23.5" y1="20" x2="31" y2="20" stroke="#FFFFFF" strokeWidth="1" />
        </svg>
      </div>

      {/* Brand Typography */}
      <div className="flex flex-col leading-tight">
        <div className="flex items-center gap-1.5">
          <span
            className={`font-black tracking-wider uppercase font-sans ${sizeStyles[size].title} ${
              isDark ? 'text-white' : 'text-[#0B1F3A]'
            }`}
          >
            EKIKRIT
          </span>
          <span
            className={`font-semibold tracking-normal text-xs px-1 py-0.2 rounded-xs ${
              isDark ? 'bg-[#102A43] text-amber-300' : 'bg-slate-100 text-[#0B1F3A]'
            }`}
          >
            एकीकृत
          </span>
        </div>
        {showSubtitle && (
          <span
            className={`font-medium tracking-tight truncate ${sizeStyles[size].sub} ${
              isDark ? 'text-slate-300' : 'text-slate-500'
            }`}
          >
            Government Interoperability &amp; Workflow Platform
          </span>
        )}
      </div>
    </div>
  );
};
