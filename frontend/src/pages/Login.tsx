import { useAuth } from '../context/AuthContext';
import { Navigate, useLocation } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { PageLoader } from '@/components/feedback/Loading';
import { Shield, KeyRound, CheckCircle2 } from 'lucide-react';

export default function Login() {
  const { isAuthenticated, isInitialized, login, hasRole } = useAuth();
  const location = useLocation();

  if (!isInitialized) {
    return <PageLoader message="Initializing authentication..." subtext="Connecting to Keycloak IAM Server" />;
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
    <div className="min-h-[70vh] flex items-center justify-center py-10 px-4">
      <Card className="w-full max-w-md border-slate-200 shadow-sm bg-white">
        <CardHeader className="text-center space-y-2 pb-6">
          <div className="w-12 h-12 bg-primary/10 text-primary rounded-xl mx-auto flex items-center justify-center mb-1">
            <Shield className="w-6 h-6 text-primary" />
          </div>
          <CardTitle className="text-xl font-bold text-slate-900 tracking-tight">
            GovSecure Identity Access Management
          </CardTitle>
          <CardDescription className="text-sm text-slate-500">
            Government of Maharashtra Single Sign-On (Keycloak)
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="space-y-2 text-xs text-slate-600 bg-slate-50 p-4 rounded-lg border border-slate-200">
            <p className="font-semibold text-slate-800">Supported Authentication Roles:</p>
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
              <span><strong>Citizen:</strong> Access scheme discovery, digital consent & applications</span>
            </div>
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
              <span><strong>Officer:</strong> Review applications with verified records & BPMN human tasks</span>
            </div>
          </div>

          <Button
            onClick={login}
            size="lg"
            className="w-full h-11 font-semibold gap-2 shadow-sm text-base"
          >
            <KeyRound className="w-4 h-4" />
            Sign in via Keycloak SSO
          </Button>

          <p className="text-center text-xs text-slate-400">
            Protected under DPDP Act 2023. Session tokens are encrypted and periodically refreshed.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
