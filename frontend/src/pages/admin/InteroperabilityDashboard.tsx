import { useQuery } from '@tanstack/react-query';
import { applicationApi } from '@/lib/api';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { AlertCircle, Activity, Server, FileText, Clock, Network, AlertTriangle } from 'lucide-react';
import { Loader2 } from 'lucide-react';

interface ServiceResponse {
  serviceCode: string;
  serviceName: string;
  description: string;
  active: boolean;
  departmentCode: string;
  departmentName: string;
}

export function InteroperabilityDashboard() {
  const { data: services, isLoading, error } = useQuery<ServiceResponse[]>({
    queryKey: ['adminServices'],
    queryFn: async () => {
      const response = await applicationApi.get('/api/v1/services');
      return response.data;
    },
  });

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Platform Operations & Interoperability</h1>
        <p className="text-sm text-slate-500 mt-1">
          Monitor connected government systems and operational processing state.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="bg-slate-50 border-slate-200">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-slate-500 flex items-center">
              <Network className="w-4 h-4 mr-2" /> Connected Services
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-slate-900">
              {isLoading ? <Loader2 className="w-5 h-5 animate-spin" /> : services?.length || 0}
            </div>
            <p className="text-xs text-slate-500 mt-1">Active integration schemas</p>
          </CardContent>
        </Card>
        
        <Card className="bg-slate-50 border-slate-200">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-slate-500 flex items-center">
              <Activity className="w-4 h-4 mr-2" /> Connector Status
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Badge variant="outline" className="bg-slate-200/50 text-slate-600 border-slate-300">
              Data Unavailable
            </Badge>
            <p className="text-xs text-slate-500 mt-2">Metrics not exposed by backend</p>
          </CardContent>
        </Card>

        <Card className="bg-slate-50 border-slate-200">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-slate-500 flex items-center">
              <FileText className="w-4 h-4 mr-2" /> Audit Logs
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Badge variant="outline" className="bg-slate-200/50 text-slate-600 border-slate-300">
              API Unavailable
            </Badge>
            <p className="text-xs text-slate-500 mt-2">No audit endpoint exposed</p>
          </CardContent>
        </Card>

        <Card className="bg-slate-50 border-slate-200">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-slate-500 flex items-center">
              <Clock className="w-4 h-4 mr-2" /> SLA Compliance
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Badge variant="outline" className="bg-slate-200/50 text-slate-600 border-slate-300">
              Not Implementable
            </Badge>
            <p className="text-xs text-slate-500 mt-2">Lacking processing timestamps</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        
        {/* Department & Service Integration Catalog */}
        <Card>
          <CardHeader className="border-b border-slate-100 pb-4">
            <CardTitle className="text-lg flex items-center">
              <Server className="w-5 h-5 text-primary mr-2" />
              Department & Service Catalog
            </CardTitle>
            <CardDescription>Master data of onboarded government services</CardDescription>
          </CardHeader>
          <CardContent className="pt-6">
            {isLoading ? (
              <div className="flex justify-center p-8">
                <Loader2 className="w-6 h-6 animate-spin text-primary" />
              </div>
            ) : error ? (
              <div className="bg-red-50 text-red-800 p-4 rounded-lg flex items-start gap-3">
                <AlertCircle className="w-5 h-5 shrink-0" />
                <p className="text-sm">Failed to load service catalog.</p>
              </div>
            ) : !services || services.length === 0 ? (
              <div className="text-center p-8 text-slate-500">
                No services found.
              </div>
            ) : (
              <div className="space-y-4">
                {services.map(service => (
                  <div key={service.serviceCode} className="p-4 border border-slate-200 rounded-lg bg-white flex justify-between items-start">
                    <div>
                      <h4 className="font-bold text-sm text-slate-900">{service.serviceName}</h4>
                      <p className="text-xs text-slate-500 mt-1">{service.departmentName} ({service.departmentCode})</p>
                      <p className="text-xs font-mono bg-slate-100 text-slate-600 px-2 py-0.5 rounded mt-2 inline-block">
                        {service.serviceCode}
                      </p>
                    </div>
                    <div>
                      <Badge className={service.active ? 'bg-emerald-100 text-emerald-800 hover:bg-emerald-100' : 'bg-slate-100 text-slate-600 hover:bg-slate-100'}>
                        {service.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Backend Limitations Notice */}
        <div className="space-y-6">
          <Card className="border-amber-200 bg-amber-50">
            <CardHeader className="pb-2">
              <CardTitle className="text-amber-800 flex items-center text-base">
                <AlertTriangle className="w-5 h-5 mr-2" />
                Backend API Limitations
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-sm text-amber-900/80 space-y-3">
                <p>
                  This dashboard strictly adheres to the frozen backend contract. The following operational capabilities are not currently exposed by the backend:
                </p>
                <ul className="list-disc pl-5 space-y-2">
                  <li><strong>Interoperability Status:</strong> No endpoints exist to monitor connector health (e.g., Education System, Skills System) or successful/failed verification attempts.</li>
                  <li><strong>Audit Logs:</strong> No <code>AuditController</code> or audit query API is available.</li>
                  <li><strong>Operational Metrics:</strong> Application processing counts, workflow analytics, and approval rates are not exposed via any domain API.</li>
                  <li><strong>SLA Monitoring:</strong> SLA targets and processing durations cannot be calculated due to missing measurement metrics in the API responses.</li>
                </ul>
                <p className="font-semibold pt-2">
                  No fake data has been generated to populate this dashboard.
                </p>
              </div>
            </CardContent>
          </Card>
        </div>

      </div>
    </div>
  );
}
