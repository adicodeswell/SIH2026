import { useAuth } from '../context/AuthContext';
import { Navigate, useLocation } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { PageLoader } from '@/components/feedback/Loading';
import { EkikritLogo } from '@/components/ui/EkikritLogo';
import { AshokaEmblem } from '@/components/ui/AshokaEmblem';
import { KeyRound, CheckCircle2, Lock } from 'lucide-react';

export default function Login() {
  const { isAuthenticated, isInitialized, login, hasRole } = useAuth();
  const location = useLocation();

  if (!isInitialized) {
    return (
      <PageLoader
        message="Connecting to GovSecure IAM Server..."
        subtext="Initiating OpenID Connect cryptographic handshake with Keycloak"
      />
    );
  }

  // If already authenticated, redirect to role destination or return URL
  if (isAuthenticated) {
    const origin = (location.state as any)?.from?.pathname;
    if (origin && origin !== '/login') {
      return <Navigate to={origin} replace />;
    }
    if (hasRole('OFFICER')) {
      return <Navigate to="/officer" replace />;
    }
    return <Navigate to="/citizen" replace />;
  }

  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center py-10 px-4">
      {/* Centered Minimal Institutional Ashoka Emblem above Authentication Card */}
      <div className="flex flex-col items-center justify-center mb-5 text-center select-none" aria-hidden="true">
        <AshokaEmblem size="lg" variant="navy" className="h-10 mb-2 opacity-90" alt="State Emblem of India" />
        <span className="text-[10px] uppercase font-bold tracking-widest text-slate-500">
          Government Digital Services
        </span>
        <span className="text-[11px] text-slate-400 font-medium">
          Unified e-Governance Authentication Gateway
        </span>
      </div>

      <Card className="w-full max-w-md border-slate-200 shadow-xs bg-white rounded-md overflow-hidden">
        {/* Card Header with Navy styling */}
        <CardHeader className="text-center space-y-2 pb-5 bg-slate-50/70 border-b border-slate-100">
          <div className="flex justify-center mb-1">
            <EkikritLogo variant="light" size="sm" showSubtitle={false} />
          </div>
          <CardTitle className="text-lg font-bold text-slate-900 tracking-tight">
            GovSecure Identity &amp; Access Management
          </CardTitle>
          <CardDescription className="text-xs text-slate-500">
            e-Governance Single Sign-On • Keycloak IAM (DPDP Compliant)
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-5 p-6">
          <div className="space-y-2.5 text-xs text-slate-700 bg-slate-50 p-3.5 rounded-sm border border-slate-200">
            <p className="font-bold text-slate-900">Supported Authentication Profiles:</p>
            <div className="flex items-start gap-2">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 shrink-0 mt-0.5" />
              <span>
                <strong className="text-slate-800">Citizen Profile:</strong> Discover schemes, grant DPDP cryptographic consent &amp; track applications.
              </span>
            </div>
            <div className="flex items-start gap-2">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 shrink-0 mt-0.5" />
              <span>
                <strong className="text-slate-800">Review Officer Profile:</strong> Review verified records &amp; execute BPMN human-task statutory sanctions.
              </span>
            </div>
          </div>

          <Button
            onClick={login}
            size="lg"
            className="w-full h-11 font-bold text-xs bg-[#0B1F3A] hover:bg-[#102A43] text-white gap-2 shadow-xs"
          >
            <KeyRound className="w-4 h-4 text-amber-400" />
            Authenticate via Keycloak SSO
          </Button>

          <div className="flex items-center justify-center gap-1.5 text-[11px] text-slate-400 text-center">
            <Lock className="w-3 h-3 text-slate-400" />
            <span>Protected under DPDP Act 2023. Encrypted OAuth2 Bearer Tokens.</span>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
