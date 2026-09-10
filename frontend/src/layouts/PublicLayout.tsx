import React from 'react';
import { Outlet } from 'react-router-dom';
import { Header } from './Header';
import { Footer } from './Footer';

export const PublicLayout: React.FC = () => {
  const publicNav = [
    { label: 'Home', href: '/' },
    { label: 'Citizen Portal', href: '/citizen' },
    { label: 'Officer Console', href: '/officer' },
  ];

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
      <Header portalTitle="State Interoperability Gateway" navItems={publicNav} />
      <main className="flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
};
