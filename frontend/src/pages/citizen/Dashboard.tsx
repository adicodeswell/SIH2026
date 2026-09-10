import { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { applicationApi } from '@/lib/api';
import { Link } from 'react-router-dom';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
  Loader2,
  ArrowRight,
  Lock,
  LayoutGrid,
  Building2,
} from 'lucide-react';

export default function CitizenDashboard() {
  const { user } = useAuth();
  const username = user?.username || 'CITIZEN';

  const [schemes, setSchemes] = useState<any[]>([]);
  const [loadingSchemes, setLoadingSchemes] = useState(true);

  // Fetch schemes on mount
  useEffect(() => {
    const fetchSchemes = async () => {
      try {
        const response = await applicationApi.get('/api/v1/services');
        setSchemes(response.data);
      } catch {
        toast.error('Failed to load schemes');
      } finally {
        setLoadingSchemes(false);
      }
    };
    fetchSchemes();
  }, []);

  return (
    <div className="space-y-8">
      {/* Welcome Banner */}
      <div className="bg-white p-6 sm:p-8 rounded-xl border border-slate-200 shadow-sm flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Citizen Services Dashboard
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Logged in as <span className="font-semibold text-primary">{username}</span>. Apply for state welfare
            schemes with zero physical document uploads.
          </p>
        </div>
        <Link to="/citizen/services">
          <Button className="gap-2 font-medium">
            <LayoutGrid className="w-4 h-4" /> Browse All Services
          </Button>
        </Link>
      </div>

      {/* Featured Schemes Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <Building2 className="w-5 h-5 text-primary" /> Available Government Schemes
          </h2>
          <Link to="/citizen/services" className="text-xs font-medium text-primary hover:underline flex items-center gap-1">
            View directory <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {loadingSchemes ? (
          <div className="flex justify-center p-12 bg-white rounded-xl border border-slate-200">
            <Loader2 className="w-8 h-8 animate-spin text-primary" />
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {schemes.slice(0, 3).map((scheme: any) => (
              <Card key={scheme.serviceCode} className="border-slate-200 shadow-sm flex flex-col justify-between bg-white">
                <div>
                  <CardHeader className="pb-3 border-b border-slate-100 bg-slate-50/50">
                    <div className="flex items-start justify-between gap-2">
                      <div>
                        <CardTitle className="text-base font-semibold text-slate-900 leading-snug">
                          {scheme.serviceName}
                        </CardTitle>
                        <p className="text-xs text-slate-500 mt-0.5">{scheme.departmentName}</p>
                      </div>
                      <Badge variant="outline" className="bg-blue-50 text-blue-800 border-blue-200 font-mono text-xs">
                        {scheme.serviceCode}
                      </Badge>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-4 pb-4">
                    <p className="text-sm text-slate-600 line-clamp-3">{scheme.description}</p>
                  </CardContent>
                </div>
                <div className="p-4 pt-0 border-t border-slate-100 bg-white">
                  <Link to={`/citizen/services/${scheme.serviceCode}`} className="w-full">
                    <Button className="w-full justify-center gap-2 text-sm font-medium">
                      Apply Online <ArrowRight className="w-4 h-4" />
                    </Button>
                  </Link>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>

      {/* Citizen Guidance Notice */}
      <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm flex flex-col sm:flex-row items-start gap-4 text-xs text-slate-600">
        <Lock className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
        <div className="space-y-1">
          <p className="font-semibold text-slate-800 text-sm">DPDP Act 2023 Digital Consent Framework</p>
          <p className="leading-relaxed">
            All personal records retrieved during your scheme evaluation are queried directly from authoritative state
            registries following your cryptographic consent. No raw documents or paper photocopies are ever required.
          </p>
        </div>
      </div>
    </div>
  );
}
