import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/button';
import { EkikritLogo } from '@/components/ui/EkikritLogo';
import { AshokaEmblem } from '@/components/ui/AshokaEmblem';
import {
  LogOut,
  Menu,
  X,
  ChevronRight,
  UserCheck,
  Globe,
  SunMoon,
  Info,
  CheckCircle2,
} from 'lucide-react';
import { cn } from '@/lib/utils';

interface NavItem {
  label: string;
  href: string;
}

interface HeaderProps {
  portalTitle?: string;
  navItems?: NavItem[];
}

export const Header: React.FC<HeaderProps> = ({
  portalTitle = 'Government Interoperability Platform',
  navItems = [],
}) => {
  const { isAuthenticated, user, logout } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [currentZoom, setCurrentZoom] = useState<'sm' | 'base' | 'lg'>('base');
  const [isHighContrast, setIsHighContrast] = useState(false);
  const [currentLang, setCurrentLang] = useState('English');
  const location = useLocation();

  const handleZoomChange = (zoom: 'sm' | 'base' | 'lg') => {
    document.documentElement.classList.remove('zoom-sm', 'zoom-base', 'zoom-lg');
    document.documentElement.classList.add(`zoom-${zoom}`);
    setCurrentZoom(zoom);
  };

  const toggleContrast = () => {
    document.documentElement.classList.toggle('high-contrast');
    setIsHighContrast((prev) => !prev);
  };

  useEffect(() => {
    // Reset zoom classes on unmount
    return () => {
      document.documentElement.classList.remove('zoom-sm', 'zoom-base', 'zoom-lg', 'high-contrast');
    };
  }, []);

  return (
    <>
      {/* Screen Reader Skip Link */}
      <a href="#main-content" className="skip-to-main">
        Skip to main content
      </a>

      {/* 1. National Tricolor Strip */}
      <div className="h-1 w-full flex select-none" aria-hidden="true">
        <div className="flex-1 bg-[#FF9933]" />
        <div className="flex-1 bg-white" />
        <div className="flex-1 bg-[#138808]" />
      </div>

      {/* 2. Top Identity & Accessibility Bar */}
      <div className="bg-slate-100 border-b border-slate-200 text-xs text-slate-700 select-none">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-8 flex items-center justify-between">
          {/* Institutional Identity Area */}
          <div className="flex items-center gap-2 sm:gap-2.5">
            <AshokaEmblem size="xs" variant="navy" className="h-5" alt="State Emblem of India" />
            <div className="h-3.5 w-px bg-slate-300" />
            <div className="flex items-center gap-2">
              <span className="font-bold text-slate-800 tracking-tight text-[11px] sm:text-xs">
                GOVERNMENT DIGITAL SERVICES
              </span>
              <span className="text-slate-300 hidden sm:inline">•</span>
              <span className="text-slate-500 hidden sm:inline text-[11px]">
                e-Governance &amp; Interoperability Platform
              </span>
            </div>
          </div>

          {/* Accessibility & Language Controls */}
          <div className="flex items-center gap-3">
            {/* Text Zoom Controls */}
            <div className="flex items-center gap-1 border border-slate-300 rounded bg-white px-1.5 py-0.5" title="Font Size">
              <button
                type="button"
                onClick={() => handleZoomChange('sm')}
                className={cn(
                  'px-1 font-bold text-[10px] hover:text-[#0B1F3A]',
                  currentZoom === 'sm' ? 'text-[#F97316] underline font-extrabold' : 'text-slate-600'
                )}
                aria-label="Decrease font size"
              >
                A-
              </button>
              <span className="text-slate-300 text-[10px]">|</span>
              <button
                type="button"
                onClick={() => handleZoomChange('base')}
                className={cn(
                  'px-1 font-bold text-[11px] hover:text-[#0B1F3A]',
                  currentZoom === 'base' ? 'text-[#F97316] underline font-extrabold' : 'text-slate-600'
                )}
                aria-label="Normal font size"
              >
                A
              </button>
              <span className="text-slate-300 text-[10px]">|</span>
              <button
                type="button"
                onClick={() => handleZoomChange('lg')}
                className={cn(
                  'px-1 font-bold text-[12px] hover:text-[#0B1F3A]',
                  currentZoom === 'lg' ? 'text-[#F97316] underline font-extrabold' : 'text-slate-600'
                )}
                aria-label="Increase font size"
              >
                A+
              </button>
            </div>

            {/* High Contrast Toggle */}
            <button
              type="button"
              onClick={toggleContrast}
              className={cn(
                'hidden md:flex items-center gap-1 px-2 py-0.5 rounded border text-[11px] font-medium transition-colors',
                isHighContrast
                  ? 'bg-slate-900 text-white border-slate-900'
                  : 'bg-white text-slate-700 border-slate-300 hover:bg-slate-50'
              )}
              title="Toggle High Contrast Mode"
              aria-pressed={isHighContrast}
            >
              <SunMoon className="w-3 h-3 text-amber-500" />
              <span>{isHighContrast ? 'Standard' : 'Contrast'}</span>
            </button>

            {/* Language Switcher */}
            <div className="flex items-center gap-1 bg-white border border-slate-300 rounded px-1.5 py-0.5">
              <Globe className="w-3 h-3 text-slate-500" />
              <select
                value={currentLang}
                onChange={(e) => setCurrentLang(e.target.value)}
                className="bg-transparent text-[11px] font-medium text-slate-700 focus:outline-none cursor-pointer"
                aria-label="Select Language"
              >
                <option value="English">English</option>
                <option value="Hindi">हिंदी</option>
                <option value="Marathi">मराठी</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* 3. Main Institutional Dark Navy Application Header */}
      <header className="bg-[#0B1F3A] text-white border-b border-[#1A365D] sticky top-0 z-40 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-18">
            {/* Logo & Platform Identity */}
            <div className="flex items-center gap-4 sm:gap-6">
              <Link
                to="/"
                className="flex items-center gap-3 focus:outline-none focus:ring-2 focus:ring-[#F97316] rounded py-1 group"
                aria-label="EKIKRIT - Government Interoperability Platform"
              >
                {/* Subtle Monochrome Ashoka Emblem */}
                <div className="hidden sm:flex items-center gap-3">
                  <AshokaEmblem
                    size="sm"
                    variant="light"
                    className="h-7 opacity-85 group-hover:opacity-100 transition-opacity"
                    alt="State Emblem of India"
                  />
                  <div className="h-6 w-px bg-slate-700/80" />
                </div>

                <EkikritLogo variant="dark" size="md" />
              </Link>

              {/* Portal Section Divider */}
              <div className="hidden xl:block h-8 w-px bg-slate-700/60" />
              <span className="hidden xl:block text-xs font-semibold text-slate-300 tracking-tight">
                {portalTitle}
              </span>
            </div>

            {/* Desktop Navigation */}
            {navItems.length > 0 && (
              <nav className="hidden lg:flex items-center gap-1" aria-label="Portal Navigation">
                {navItems.map((item) => {
                  const isActive =
                    item.href === '/'
                      ? location.pathname === '/'
                      : location.pathname.startsWith(item.href);
                  return (
                    <Link
                      key={item.href}
                      to={item.href}
                      className={cn(
                        'px-3.5 py-1.5 rounded-sm text-xs font-semibold tracking-wide uppercase transition-colors',
                        isActive
                          ? 'bg-[#102A43] text-white border-b-2 border-[#F97316] shadow-xs'
                          : 'text-slate-300 hover:text-white hover:bg-[#102A43]/60'
                      )}
                    >
                      {item.label}
                    </Link>
                  );
                })}
              </nav>
            )}

            {/* Right: Operational Status & User Profile */}
            <div className="hidden md:flex items-center gap-4">
              {/* System Health Status Indicator */}
              <div
                className="flex items-center gap-1.5 bg-[#102A43] px-2.5 py-1 rounded-sm border border-slate-700/80 text-[11px] text-slate-300"
                title="Workflow & Interoperability Gateway Operational"
              >
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                <span className="font-semibold text-white">System:</span>
                <span className="text-emerald-400 font-medium">Operational</span>
              </div>

              {/* Authentication & User Pill */}
              {isAuthenticated ? (
                <div className="flex items-center gap-3 pl-2 border-l border-slate-700/80">
                  <div className="text-right">
                    <div className="text-xs font-bold text-white flex items-center justify-end gap-1.5">
                      <UserCheck className="w-3.5 h-3.5 text-emerald-400" />
                      <span>{user?.name || user?.username?.toUpperCase() || 'Gov User'}</span>
                    </div>
                    <div className="text-[10px] text-amber-400 font-medium tracking-wider uppercase">
                      {user?.roles?.includes('OFFICER')
                        ? 'Review Officer'
                        : user?.roles?.includes('CITIZEN')
                        ? 'Verified Citizen'
                        : 'Administrator'}
                    </div>
                  </div>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={logout}
                    className="h-8 text-xs font-semibold bg-transparent text-slate-200 border-slate-600 hover:bg-red-950/40 hover:text-red-300 hover:border-red-400 gap-1.5"
                  >
                    <LogOut className="w-3.5 h-3.5" />
                    Sign Out
                  </Button>
                </div>
              ) : (
                <Link to="/login">
                  <Button
                    size="sm"
                    className="h-8 text-xs font-bold bg-[#F97316] text-white hover:bg-[#EA580C] px-3.5"
                  >
                    Sign In
                  </Button>
                </Link>
              )}
            </div>

            {/* Mobile Menu Toggle */}
            <div className="flex lg:hidden items-center gap-2">
              <button
                onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                className="p-2 rounded-md text-slate-300 hover:text-white hover:bg-[#102A43] focus:outline-none"
                aria-label="Toggle navigation menu"
                aria-expanded={mobileMenuOpen}
              >
                {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
              </button>
            </div>
          </div>
        </div>

        {/* Mobile Navigation Drawer */}
        {mobileMenuOpen && (
          <div className="lg:hidden border-t border-[#1E3A5F] bg-[#0B1F3A] px-4 pt-3 pb-5 space-y-3">
            {navItems.length > 0 && (
              <nav className="space-y-1" aria-label="Mobile Navigation">
                {navItems.map((item) => {
                  const isActive =
                    item.href === '/'
                      ? location.pathname === '/'
                      : location.pathname.startsWith(item.href);
                  return (
                    <Link
                      key={item.href}
                      to={item.href}
                      onClick={() => setMobileMenuOpen(false)}
                      className={cn(
                        'flex items-center justify-between px-3 py-2 rounded-sm text-sm font-semibold tracking-wide uppercase',
                        isActive
                          ? 'bg-[#102A43] text-white border-l-4 border-[#F97316]'
                          : 'text-slate-300 hover:bg-[#102A43]/60'
                      )}
                    >
                      <span>{item.label}</span>
                      <ChevronRight className="w-4 h-4 text-slate-400" />
                    </Link>
                  );
                })}
              </nav>
            )}

            <div className="pt-3 border-t border-slate-700/60 space-y-3">
              <div className="flex items-center justify-between px-3 text-xs text-slate-300">
                <span>System Status:</span>
                <span className="text-emerald-400 font-bold flex items-center gap-1">
                  <CheckCircle2 className="w-3.5 h-3.5" /> Operational
                </span>
              </div>

              {isAuthenticated ? (
                <div className="space-y-3">
                  <div className="px-3 py-2 bg-[#102A43] rounded-sm border border-slate-700 text-xs">
                    <p className="font-bold text-white">
                      {user?.name || user?.username?.toUpperCase()}
                    </p>
                    <p className="text-[10px] text-amber-400 uppercase tracking-wider mt-0.5">
                      {user?.roles?.includes('OFFICER')
                        ? 'Review Officer'
                        : user?.roles?.includes('CITIZEN')
                        ? 'Verified Citizen'
                        : 'Administrator'}
                    </p>
                  </div>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={logout}
                    className="w-full justify-center gap-2 text-red-300 border-red-500/40 bg-red-950/20 hover:bg-red-950/40"
                  >
                    <LogOut className="w-4 h-4" />
                    Sign Out
                  </Button>
                </div>
              ) : (
                <Link to="/login" onClick={() => setMobileMenuOpen(false)}>
                  <Button size="sm" className="w-full justify-center bg-[#F97316] hover:bg-[#EA580C]">
                    Sign In
                  </Button>
                </Link>
              )}
            </div>
          </div>
        )}
      </header>

      {/* 4. Official Notice / Announcement Bar */}
      <div className="bg-amber-50 border-b border-amber-200/90 py-1.5 px-4 text-xs text-amber-950 shadow-2xs">
        <div className="max-w-7xl mx-auto flex items-center justify-between gap-3">
          <div className="flex items-center gap-2 overflow-hidden">
            <span className="shrink-0 bg-[#F97316] text-white font-extrabold text-[10px] px-1.5 py-0.5 rounded-xs tracking-wider uppercase">
              Notice
            </span>
            <p className="truncate font-medium text-amber-900">
              DPDP Act 2023 Cryptographic Consent Gateway Active — Zero physical document uploads required for state services.
            </p>
          </div>
          <div className="hidden sm:flex items-center gap-1.5 shrink-0 text-amber-800 text-[11px] font-semibold">
            <Info className="w-3.5 h-3.5 text-[#F97316]" />
            <span>Secure TLS 1.3 Certified</span>
          </div>
        </div>
      </div>
    </>
  );
};
