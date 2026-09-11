import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { EkikritLogo } from '@/components/ui/EkikritLogo';
import {
  ShieldCheck,
  UserCheck,
  ArrowRight,
  Database,
  Lock,
  Building2,
  Clock,
} from 'lucide-react';

export default function Home() {
  return (
    <div className="space-y-10 py-2">
      {/* Hero Section */}
      <section className="text-center max-w-4xl mx-auto space-y-5 pt-2">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-xs bg-slate-100 border border-slate-300 text-slate-800 text-xs font-bold uppercase tracking-wider">
          <span className="w-2 h-2 rounded-full bg-[#F97316]" />
          <span>National Interoperability &amp; Digital Governance Framework</span>
        </div>

        <div className="space-y-2">
          <div className="flex justify-center mb-2">
            <EkikritLogo variant="light" size="lg" showSubtitle={false} />
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-slate-900 leading-tight">
            EKIKRIT Citizen Interoperability Platform
          </h1>
          <p className="text-sm sm:text-base text-slate-600 max-w-2xl mx-auto leading-relaxed">
            A zero-document digital governance gateway enabling automated state welfare delivery.
            Powered by DPDP Act 2023 cryptographic consent, canonical state registry lookups, and Camunda BPMN workflow orchestration.
          </p>
        </div>

        {/* Portal Entry Quick Launchpads */}
        <div className="flex flex-wrap items-center justify-center gap-3 pt-2">
          <Link to="/citizen">
            <Button size="lg" className="h-10 px-5 text-xs font-bold bg-[#0B1F3A] hover:bg-[#102A43] text-white gap-2 shadow-xs">
              <UserCheck className="w-4 h-4 text-emerald-400" />
              Citizen Services Portal <ArrowRight className="w-3.5 h-3.5" />
            </Button>
          </Link>
          <Link to="/officer">
            <Button size="lg" variant="outline" className="h-10 px-5 text-xs font-bold border-slate-300 text-slate-800 hover:bg-slate-50 gap-2">
              <Building2 className="w-4 h-4 text-[#0B1F3A]" />
              Officer Review Console
            </Button>
          </Link>
          <Link to="/citizen/services">
            <Button size="lg" variant="secondary" className="h-10 px-5 text-xs font-semibold gap-1.5">
              Browse Schemes Directory
            </Button>
          </Link>
        </div>
      </section>

      {/* Feature Pillar Grid */}
      <section className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <Card className="border-slate-200 shadow-xs bg-white rounded-md">
          <CardHeader className="pb-3 border-b border-slate-100 bg-slate-50/70">
            <div className="w-9 h-9 rounded-sm bg-blue-50 text-[#0B1F3A] flex items-center justify-center mb-1 border border-blue-200/60">
              <Lock className="w-4 h-4 text-[#0B1F3A]" />
            </div>
            <CardTitle className="text-sm font-bold text-slate-900">DPDP Act 2023 Consent</CardTitle>
            <CardDescription className="text-xs">Explicit Cryptographic Mandates</CardDescription>
          </CardHeader>
          <CardContent className="pt-4 text-xs text-slate-600 leading-relaxed">
            Data is retrieved from government repositories only after the citizen explicitly grants cryptographically verifiable authorization with defined purpose and bounded data scope.
          </CardContent>
        </Card>

        <Card className="border-slate-200 shadow-xs bg-white rounded-md">
          <CardHeader className="pb-3 border-b border-slate-100 bg-slate-50/70">
            <div className="w-9 h-9 rounded-sm bg-emerald-50 text-emerald-700 flex items-center justify-center mb-1 border border-emerald-200/60">
              <Database className="w-4 h-4 text-emerald-700" />
            </div>
            <CardTitle className="text-sm font-bold text-slate-900">Zero-Document Verification</CardTitle>
            <CardDescription className="text-xs">Direct Department Connectors</CardDescription>
          </CardHeader>
          <CardContent className="pt-4 text-xs text-slate-600 leading-relaxed">
            Citizens no longer upload physical scans or paper documents. Ekikrit queries authoritative state source registries (Education Board, Revenue, Employment) directly and securely.
          </CardContent>
        </Card>

        <Card className="border-slate-200 shadow-xs bg-white rounded-md">
          <CardHeader className="pb-3 border-b border-slate-100 bg-slate-50/70">
            <div className="w-9 h-9 rounded-sm bg-amber-50 text-amber-700 flex items-center justify-center mb-1 border border-amber-200/60">
              <Clock className="w-4 h-4 text-amber-700" />
            </div>
            <CardTitle className="text-sm font-bold text-slate-900">Auditable Officer Console</CardTitle>
            <CardDescription className="text-xs">BPMN Human-Task Review</CardDescription>
          </CardHeader>
          <CardContent className="pt-4 text-xs text-slate-600 leading-relaxed">
            Department review officers evaluate verified profile data side-by-side with complete audit logs, executing approvals or rejections with mandatory statutory rationales.
          </CardContent>
        </Card>
      </section>

      {/* Trust & Architecture Notice */}
      <section className="bg-white border border-slate-200 rounded-md p-6 shadow-xs flex flex-col sm:flex-row items-center gap-4 text-left">
        <div className="w-11 h-11 rounded-sm bg-slate-100 flex items-center justify-center shrink-0 border border-slate-200">
          <ShieldCheck className="w-6 h-6 text-emerald-700" />
        </div>
        <div className="space-y-1">
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-900">
            National &amp; State Digital Standards Compliant
          </h4>
          <p className="text-xs text-slate-500 leading-relaxed">
            Built following standard OpenID Connect (Keycloak IAM), RESTful application microservices, and state department data connectors without exposing raw databases or proprietary workflow engines.
          </p>
        </div>
      </section>
    </div>
  );
}
