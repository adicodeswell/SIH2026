import React from 'react';
import { Outlet } from 'react-router-dom';
import { Header } from './Header';
import { Footer } from './Footer';

export const OfficerLayout: React.FC = () => {
  const officerNav = [
    { label: 'Dashboard', href: '/officer' },
    { label: 'Review Queue', href: '/officer/reviews' },
  ];

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
      <Header portalTitle="Officer Review Console" navItems={officerNav} />
      <main className="flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
};
