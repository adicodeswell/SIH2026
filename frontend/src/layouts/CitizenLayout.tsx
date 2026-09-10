import React from 'react';
import { Outlet } from 'react-router-dom';
import { Header } from './Header';
import { Footer } from './Footer';

export const CitizenLayout: React.FC = () => {
  const citizenNav = [
    { label: 'Home', href: '/citizen' },
    { label: 'Services', href: '/citizen/services' },
    { label: 'My Applications', href: '/citizen/applications' },
    { label: 'My Consents', href: '/citizen/consents' },
  ];

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
      <Header portalTitle="Citizen Services Portal" navItems={citizenNav} />
      <main className="flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
};
