import React from 'react';
import { ShieldCheck } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-slate-900 text-slate-400 border-t border-slate-800 text-xs py-8 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <ShieldCheck className="w-5 h-5 text-emerald-400" />
            <div>
              <p className="font-semibold text-slate-200">
                Ekikrit — Digital Interoperability & DPDP Consent Platform
              </p>
              <p className="text-slate-400">
                Designed in compliance with Government of Maharashtra digital standards & DPDP Act 2023.
              </p>
            </div>
          </div>
          <div className="text-slate-400 text-center md:text-right space-y-1">
            <p>Cryptographic Consent • Zero-Document Verification • Camunda BPMN</p>
            <p className="text-slate-400">© 2026 Government of Maharashtra. All rights reserved.</p>
          </div>
        </div>
      </div>
    </footer>
  );
};
