import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/button';
import { LogOut, Menu, X, Shield, ChevronRight } from 'lucide-react';
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
  portalTitle = 'Government of Maharashtra',
  navItems = [],
}) => {
  const { isAuthenticated, user, logout } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const location = useLocation();

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-40">
      {/* Tricolor National/State Accent Bar */}
      <div className="h-1 w-full flex">
        <div className="flex-1 bg-[#FF9933]" />
        <div className="flex-1 bg-white border-y border-slate-200" />
        <div className="flex-1 bg-[#138808]" />
      </div>

      {/* Main Bar */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo & Portal Branding */}
          <div className="flex items-center gap-4">
            <Link to="/" className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-primary text-primary-foreground flex items-center justify-center font-bold text-lg shadow-sm">
                <Shield className="w-5 h-5 text-accent-foreground" />
              </div>
              <div className="flex flex-col">
                <span className="text-base font-bold text-slate-900 tracking-tight leading-tight">
                  Ekikrit <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">एकीकृत</span>
                </span>
                <span className="text-xs text-slate-500 font-medium">
                  {portalTitle}
                </span>
              </div>
            </Link>

            {/* Desktop Navigation */}
            {navItems.length > 0 && (
              <nav className="hidden md:flex items-center gap-1 ml-8" aria-label="Portal Navigation">
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
                        'px-3 py-2 rounded-md text-sm font-medium transition-colors',
                        isActive
                          ? 'bg-slate-100 text-primary font-semibold'
                          : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                      )}
                    >
                      {item.label}
                    </Link>
                  );
                })}
              </nav>
            )}
          </div>

          {/* User Profile / Authentication controls */}
          <div className="hidden md:flex items-center gap-3">
            {isAuthenticated ? (
              <div className="flex items-center gap-3">
                <div className="text-right">
                  <div className="text-sm font-semibold text-slate-900">
                    {user?.name || user?.username || 'Authenticated User'}
                  </div>
                  <div className="text-xs text-slate-500 uppercase tracking-wider">
                    {user?.roles?.includes('OFFICER')
                      ? 'Review Officer'
                      : user?.roles?.includes('CITIZEN')
                      ? 'Citizen'
                      : 'Authenticated'}
                  </div>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={logout}
                  className="gap-2 text-slate-700 hover:text-red-700 hover:border-red-200"
                >
                  <LogOut className="w-4 h-4" />
                  Sign Out
                </Button>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link to="/login">
                  <Button size="sm" className="font-medium">
                    Sign In
                  </Button>
                </Link>
              </div>
            )}
          </div>

          {/* Mobile Menu Toggle */}
          <div className="flex md:hidden items-center gap-2">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 rounded-lg text-slate-600 hover:text-slate-900 hover:bg-slate-100"
              aria-label="Toggle navigation menu"
              aria-expanded={mobileMenuOpen}
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t border-slate-200 bg-white px-4 pt-3 pb-5 space-y-3">
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
                      'flex items-center justify-between px-3 py-2 rounded-md text-base font-medium',
                      isActive
                        ? 'bg-slate-100 text-primary font-semibold'
                        : 'text-slate-700 hover:bg-slate-50'
                    )}
                  >
                    <span>{item.label}</span>
                    <ChevronRight className="w-4 h-4 text-slate-400" />
                  </Link>
                );
              })}
            </nav>
          )}

          <div className="pt-3 border-t border-slate-100">
            {isAuthenticated ? (
              <div className="space-y-3">
                <div className="px-3">
                  <p className="text-sm font-semibold text-slate-900">
                    {user?.name || user?.username}
                  </p>
                  <p className="text-xs text-slate-500">
                    {user?.roles?.includes('OFFICER') ? 'Review Officer' : 'Citizen'}
                  </p>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={logout}
                  className="w-full justify-center gap-2 text-red-700 hover:bg-red-50"
                >
                  <LogOut className="w-4 h-4" />
                  Sign Out
                </Button>
              </div>
            ) : (
              <Link to="/login" onClick={() => setMobileMenuOpen(false)}>
                <Button size="sm" className="w-full justify-center">
                  Sign In
                </Button>
              </Link>
            )}
          </div>
        </div>
      )}
    </header>
  );
};
