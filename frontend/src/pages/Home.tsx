import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { ShieldCheck, UserCheck, ArrowRight, Database, Lock } from 'lucide-react';

export default function Home() {
  return (
    <div className="space-y-12 py-4">
      {/* Hero Section */}
      <section className="text-center max-w-3xl mx-auto space-y-4">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-accent text-accent-foreground text-xs font-semibold uppercase tracking-wider">
          Government of Maharashtra Interoperability Platform
        </div>
        <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight text-slate-900 leading-tight">
          Ekikrit Unified Citizen Service Portal
        </h1>
        <p className="text-lg text-slate-600 leading-relaxed">
          A zero-document digital governance framework powered by cryptographic DPDP consent, automated state registries verification, and Camunda BPMN workflow orchestration.
        </p>
        <div className="flex flex-wrap items-center justify-center gap-4 pt-4">
          <Link to="/citizen">
            <Button size="lg" className="h-11 px-6 font-semibold gap-2 shadow-sm">
              Citizen Portal <ArrowRight className="w-4 h-4" />
            </Button>
          </Link>
          <Link to="/officer">
            <Button size="lg" variant="outline" className="h-11 px-6 font-semibold gap-2 border-slate-300">
              Officer Review Console
            </Button>
          </Link>
        </div>
      </section>

      {/* Feature Pillar Grid */}
      <section className="grid grid-cols-1 md:grid-cols-3 gap-6 pt-6">
        <Card className="border-slate-200 shadow-sm bg-white hover:border-slate-300 transition-colors">
          <CardHeader>
            <div className="w-10 h-10 rounded-lg bg-blue-50 text-blue-700 flex items-center justify-center mb-2">
              <Lock className="w-5 h-5" />
            </div>
            <CardTitle className="text-base">DPDP Consent Security</CardTitle>
            <CardDescription>Explicit Cryptographic Permissions</CardDescription>
          </CardHeader>
          <CardContent className="text-sm text-slate-600 leading-relaxed">
            Data is retrieved from government databases only after the citizen explicitly grants cryptographically verifiable consent with defined purpose and data scope.
          </CardContent>
        </Card>

        <Card className="border-slate-200 shadow-sm bg-white hover:border-slate-300 transition-colors">
          <CardHeader>
            <div className="w-10 h-10 rounded-lg bg-emerald-50 text-emerald-700 flex items-center justify-center mb-2">
              <Database className="w-5 h-5" />
            </div>
            <CardTitle className="text-base">Zero-Document Interoperability</CardTitle>
            <CardDescription>Direct Department Verification</CardDescription>
          </CardHeader>
          <CardContent className="text-sm text-slate-600 leading-relaxed">
            Citizens no longer need to upload physical scans. Ekikrit queries canonical source registries (Education, Revenue, Employment) directly and safely.
          </CardContent>
        </Card>

        <Card className="border-slate-200 shadow-sm bg-white hover:border-slate-300 transition-colors">
          <CardHeader>
            <div className="w-10 h-10 rounded-lg bg-amber-50 text-amber-700 flex items-center justify-center mb-2">
              <UserCheck className="w-5 h-5" />
            </div>
            <CardTitle className="text-base">Auditable Officer Console</CardTitle>
            <CardDescription>BPMN Human-Task Review</CardDescription>
          </CardHeader>
          <CardContent className="text-sm text-slate-600 leading-relaxed">
            Department officers evaluate verified profile data side-by-side with complete auditability, executing approval or rejection with mandatory rationales.
          </CardContent>
        </Card>
      </section>

      {/* Trust & Architecture Notice */}
      <section className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm flex flex-col sm:flex-row items-center gap-4 text-left">
        <div className="w-12 h-12 rounded-xl bg-slate-100 flex items-center justify-center shrink-0">
          <ShieldCheck className="w-6 h-6 text-emerald-600" />
        </div>
        <div>
          <h4 className="text-sm font-semibold text-slate-900">National & State Standards Compliant</h4>
          <p className="text-xs text-slate-500 mt-1">
            Built using standard OpenID Connect (Keycloak), RESTful application service layers, and state department microservice connectors without exposing internal databases or workflow engines.
          </p>
        </div>
      </section>
    </div>
  );
}
