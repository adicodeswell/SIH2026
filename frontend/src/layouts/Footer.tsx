import React from 'react';
import { Link } from 'react-router-dom';
import { EkikritLogo } from '@/components/ui/EkikritLogo';
import { AshokaEmblem } from '@/components/ui/AshokaEmblem';
import { ShieldCheck, Lock, Award, FileCheck } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-[#0B1F3A] text-slate-300 border-t border-[#1E3A5F] text-xs mt-auto">
      {/* Upper Footer: Multi-column Government Structure */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {/* Col 1: Platform Mandate */}
          <div className="space-y-3">
            <EkikritLogo variant="dark" size="sm" />
            <p className="text-slate-400 text-xs leading-relaxed">
              Ekikrit is an institutional digital interoperability framework engineered to facilitate zero-document,
              consent-driven citizen service delivery across government departments.
            </p>
            <div className="flex items-center gap-2 text-[11px] text-emerald-400 font-semibold">
              <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0" />
              <span>Compliant with DPDP Act 2023 &amp; GIGW 3.0</span>
            </div>
          </div>

          {/* Col 2: Navigation Shortcuts */}
          <div className="space-y-2">
            <h4 className="text-xs font-bold uppercase tracking-wider text-white border-b border-slate-700/60 pb-1.5">
              Portal Portfolios
            </h4>
            <ul className="space-y-1.5 text-xs text-slate-400">
              <li>
                <Link to="/citizen" className="hover:text-white transition-colors">
                  Citizen Services Portal
                </Link>
              </li>
              <li>
                <Link to="/citizen/services" className="hover:text-white transition-colors">
                  Verified Schemes Directory
                </Link>
              </li>
              <li>
                <Link to="/citizen/applications" className="hover:text-white transition-colors">
                  Application Tracking
                </Link>
              </li>
              <li>
                <Link to="/citizen/consents" className="hover:text-white transition-colors">
                  DPDP Consent Ledger
                </Link>
              </li>
              <li>
                <Link to="/officer" className="hover:text-white transition-colors">
                  Officer Review Console
                </Link>
              </li>
              <li>
                <Link to="/admin/interoperability" className="hover:text-white transition-colors">
                  Platform Administration
                </Link>
              </li>
            </ul>
          </div>

          {/* Col 3: Technical Security Standards */}
          <div className="space-y-2">
            <h4 className="text-xs font-bold uppercase tracking-wider text-white border-b border-slate-700/60 pb-1.5">
              Security Architecture
            </h4>
            <ul className="space-y-2 text-xs text-slate-400">
              <li className="flex items-start gap-2">
                <Lock className="w-3.5 h-3.5 text-amber-400 shrink-0 mt-0.5" />
                <span>Cryptographic Consent Tokens bound per citizen service request.</span>
              </li>
              <li className="flex items-start gap-2">
                <FileCheck className="w-3.5 h-3.5 text-blue-400 shrink-0 mt-0.5" />
                <span>Zero-Document Interoperability directly querying authoritative source registries.</span>
              </li>
              <li className="flex items-start gap-2">
                <Award className="w-3.5 h-3.5 text-emerald-400 shrink-0 mt-0.5" />
                <span>Strict Keycloak OpenID Connect Role-Based Access Control.</span>
              </li>
            </ul>
          </div>

          {/* Col 4: Official Compliance & Notice */}
          <div className="space-y-2">
            <h4 className="text-xs font-bold uppercase tracking-wider text-white border-b border-slate-700/60 pb-1.5">
              Official Disclaimer
            </h4>
            <p className="text-slate-400 text-xs leading-relaxed">
              Ekikrit operates exclusively as a secure data interchange and workflow gateway. Personal citizen records
              are never cached in intermediate stores and remain under citizen cryptographic ownership.
            </p>
            <div className="bg-[#102A43] p-3 rounded-sm border border-slate-700/80 text-[11px] flex items-center gap-3">
              <AshokaEmblem size="sm" variant="light" className="h-8 opacity-80 shrink-0" alt="State Emblem of India" />
              <div className="space-y-0.5">
                <div className="font-semibold text-slate-200">Government Digital Services</div>
                <div className="text-slate-400 text-[10px]">National e-Governance &amp; Interoperability Standards</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Lower Footer: Copyright & Legal Bar */}
      <div className="border-t border-[#1E3A5F] bg-[#071527] py-4">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-3 text-[11px] text-slate-400">
          <div>
            <span>© 2026 EKIKRIT. All rights reserved. Designed for Government Interoperability.</span>
          </div>
          <div className="flex items-center gap-4 text-slate-400">
            <span>Privacy Policy (DPDP)</span>
            <span>•</span>
            <span>Terms of Service</span>
            <span>•</span>
            <span>Hyperlinking Policy</span>
            <span>•</span>
            <span>Helpdesk</span>
          </div>
        </div>
      </div>

      {/* Tricolor Bottom Accent */}
      <div className="h-1 w-full flex select-none" aria-hidden="true">
        <div className="flex-1 bg-[#FF9933]" />
        <div className="flex-1 bg-white" />
        <div className="flex-1 bg-[#138808]" />
      </div>
    </footer>
  );
};
