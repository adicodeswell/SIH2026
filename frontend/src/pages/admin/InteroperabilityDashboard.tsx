import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { applicationApi } from '@/lib/api';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Breadcrumb } from '@/components/ui/Breadcrumb';
import {
  Server,
  Network,
  ShieldCheck,
  Building2,
  RefreshCw,
  Loader2,
  AlertCircle,
  FileCheck,
  Lock,
} from 'lucide-react';

interface ServiceResponse {
  serviceCode: string;
  serviceName: string;
  description: string;
  active: boolean;
  departmentCode: string;
  departmentName: string;
}

export function InteroperabilityDashboard() {
  const { data: services, isLoading, error, refetch } = useQuery<ServiceResponse[]>({
    queryKey: ['adminServices'],
    queryFn: async () => {
      const response = await applicationApi.get('/api/v1/services');
      return response.data;
    },
  });

  const { activeCount, uniqueDepts } = useMemo(() => {
    if (!services) return { activeCount: 0, uniqueDepts: [] };
    const active = services.filter((s) => s.active).length;
    const depts = new Set<string>();
    services.forEach((s) => {
      if (s.departmentName) depts.add(s.departmentName);
      else if (s.departmentCode) depts.add(s.departmentCode);
    });
    return { activeCount: active, uniqueDepts: Array.from(depts) };
  }, [services]);

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Breadcrumb Navigation */}
      <Breadcrumb
        items={[
          { label: 'Platform Administration', href: '/admin/interoperability' },
          { label: 'Platform Operations' },
        ]}
      />

      {/* Page Header */}
      <div className="bg-white border border-slate-200 rounded-md p-6 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="bg-[#0B1F3A] text-white text-[10px] font-bold uppercase px-2 py-0.5 rounded-xs tracking-wider">
              Platform Administration
            </span>
            <span className="text-xs text-slate-500 font-medium">
              State Interoperability Gateway
            </span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Platform Operations &amp; Integration Registry
          </h1>
          <p className="text-xs sm:text-sm text-slate-600 mt-1 max-w-2xl leading-relaxed">
            Master directory of onboarded state departments, registered service schemas, and active canonical connectors powering the Ekikrit zero-document ecosystem.
          </p>
        </div>

        <div className="flex items-center gap-2.5 shrink-0">
          <Button
            onClick={() => refetch()}
            variant="outline"
            size="sm"
            disabled={isLoading}
            className="text-xs font-semibold h-9 gap-1.5"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
            Refresh Directory
          </Button>
        </div>
      </div>

      {/* Real Data KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="p-4 border-slate-200 bg-white shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Registered Schemes
            </span>
            <div className="w-7 h-7 rounded-sm bg-blue-50 text-[#0B1F3A] flex items-center justify-center">
              <Network className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-black text-slate-900 font-mono">
              {isLoading ? <Loader2 className="w-5 h-5 animate-spin text-slate-400" /> : services?.length || 0}
            </span>
            <span className="text-[11px] text-slate-500">integrated services</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-2 border-t border-slate-100 pt-1.5">
            Active in state service catalog
          </p>
        </Card>

        <Card className="p-4 border-slate-200 bg-white shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Participating Departments
            </span>
            <div className="w-7 h-7 rounded-sm bg-emerald-50 text-emerald-700 flex items-center justify-center">
              <Building2 className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-black text-emerald-800 font-mono">
              {isLoading ? <Loader2 className="w-5 h-5 animate-spin text-slate-400" /> : uniqueDepts.length}
            </span>
            <span className="text-[11px] text-slate-500">line departments</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-2 border-t border-slate-100 pt-1.5">
            Connected via RESTful API bridges
          </p>
        </Card>

        <Card className="p-4 border-slate-200 bg-white shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Active Routing Schemas
            </span>
            <div className="w-7 h-7 rounded-sm bg-purple-50 text-purple-700 flex items-center justify-center">
              <Server className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-black text-purple-900 font-mono">
              {isLoading ? <Loader2 className="w-5 h-5 animate-spin text-slate-400" /> : activeCount}
            </span>
            <span className="text-[11px] text-slate-500">operational pipelines</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-2 border-t border-slate-100 pt-1.5">
            BPMN Camunda workflow enabled
          </p>
        </Card>
      </div>

      {/* Master Service Catalog Table */}
      <Card className="border-slate-200 shadow-xs bg-white rounded-md overflow-hidden">
        <CardHeader className="border-b border-slate-100 bg-slate-50/70 pb-3">
          <CardTitle className="text-sm font-bold text-slate-900 flex items-center gap-2">
            <Server className="w-4 h-4 text-[#0B1F3A]" />
            <span>Master Department &amp; Service Registry</span>
          </CardTitle>
          <CardDescription className="text-xs">
            Canonical service definitions registered on the state interoperability message bus
          </CardDescription>
        </CardHeader>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="p-12 text-center text-slate-500">
              <Loader2 className="w-6 h-6 animate-spin mx-auto text-[#0B1F3A] mb-2" />
              <p className="text-xs font-medium">Loading catalog schemas...</p>
            </div>
          ) : error ? (
            <div className="p-6 bg-red-50 text-red-800 flex items-start gap-3">
              <AlertCircle className="w-5 h-5 shrink-0 text-red-600 mt-0.5" />
              <p className="text-xs font-medium">Failed to communicate with master catalog service.</p>
            </div>
          ) : !services || services.length === 0 ? (
            <div className="p-10 text-center text-slate-500 text-xs">
              No services registered in the catalog yet.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 text-[11px] font-semibold uppercase tracking-wider">
                    <th className="px-4 py-3">Service Code</th>
                    <th className="px-4 py-3">Scheme Name</th>
                    <th className="px-4 py-3">Administering Department</th>
                    <th className="px-4 py-3">Description</th>
                    <th className="px-4 py-3">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-xs">
                  {services.map((service) => (
                    <tr key={service.serviceCode} className="hover:bg-slate-50 transition-colors even:bg-slate-50/40">
                      <td className="px-4 py-3 font-mono font-bold text-slate-900 whitespace-nowrap">
                        <Badge variant="service" className="text-[11px]">
                          {service.serviceCode}
                        </Badge>
                      </td>
                      <td className="px-4 py-3 font-bold text-slate-900 whitespace-nowrap">
                        {service.serviceName}
                      </td>
                      <td className="px-4 py-3 text-slate-700 whitespace-nowrap">
                        <div className="font-medium">{service.departmentName}</div>
                        <div className="text-[10px] font-mono text-slate-400 font-normal">
                          {service.departmentCode}
                        </div>
                      </td>
                      <td className="px-4 py-3 text-slate-600 max-w-md">
                        <div className="line-clamp-2">{service.description}</div>
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap">
                        <Badge variant={service.active ? 'active' : 'secondary'} className="text-[10px]">
                          {service.active ? 'Active' : 'Inactive'}
                        </Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Architecture & Security Standards Overview */}
      <div className="bg-white border border-slate-200 rounded-md p-6 shadow-xs space-y-4">
        <div className="flex items-center gap-2 border-b border-slate-100 pb-3">
          <ShieldCheck className="w-5 h-5 text-emerald-700" />
          <h3 className="text-sm font-bold text-slate-900">
            Government Interoperability Architecture Standards
          </h3>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs text-slate-600">
          <div className="space-y-1">
            <h4 className="font-bold text-slate-800 flex items-center gap-1.5">
              <Lock className="w-3.5 h-3.5 text-blue-700" />
              OpenID Connect / Keycloak
            </h4>
            <p className="leading-relaxed text-slate-500">
              Role-based authorization and cryptographic session management adhering to India Stack federated identity standards.
            </p>
          </div>
          <div className="space-y-1">
            <h4 className="font-bold text-slate-800 flex items-center gap-1.5">
              <Server className="w-3.5 h-3.5 text-emerald-700" />
              Statutory Consent Binding
            </h4>
            <p className="leading-relaxed text-slate-500">
              Granular purpose and scope permissions verified prior to invoking canonical education, revenue, or skills registries.
            </p>
          </div>
          <div className="space-y-1">
            <h4 className="font-bold text-slate-800 flex items-center gap-1.5">
              <FileCheck className="w-3.5 h-3.5 text-amber-700" />
              Zero-Document Pipeline
            </h4>
            <p className="leading-relaxed text-slate-500">
              Eliminates untrusted client document uploads in favor of authoritative state registry lookups with full auditability.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
