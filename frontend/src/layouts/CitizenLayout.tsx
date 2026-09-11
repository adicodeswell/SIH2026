import React from 'react';
import { Outlet } from 'react-router-dom';
import { Header } from './Header';
import { Footer } from './Footer';

export const CitizenLayout: React.FC = () => {
  const citizenNav = [
    { label: 'Dashboard', href: '/citizen' },
    { label: 'All Schemes', href: '/citizen/services' },
    { label: 'My Applications', href: '/citizen/applications' },
    { label: 'Data Consents', href: '/citizen/consents' },
  ];

  return (
    <div className="min-h-screen flex flex-col bg-[#F4F6F8] text-slate-900">
      <Header portalTitle="Citizen Services Portal" navItems={citizenNav} />
      <main id="main-content" className="flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8 focus:outline-none">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
};
