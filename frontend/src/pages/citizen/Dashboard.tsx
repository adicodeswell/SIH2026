import { useState, useEffect, useMemo } from 'react';
import { applicationServiceApi } from '@/services/applicationService';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '@/context/AuthContext';
import { applicationApi } from '@/lib/api';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Breadcrumb } from '@/components/ui/Breadcrumb';
import { ServiceCard } from '@/components/citizen/ServiceCard';
import {
  Loader2,
  ArrowRight,
  ShieldCheck,
  FileText,
  Clock,
  CheckCircle2,
  Building2,
  FolderOpen,
  ChevronRight,
} from 'lucide-react';
import { format } from 'date-fns';

export default function CitizenDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const username = user?.username || 'CITIZEN';
  const displayName = user?.name || username;

  const { data: myApps, isLoading: loadingApps } = useQuery({
    queryKey: ['citizen-applications'],
    queryFn: () => applicationServiceApi.getMyApplications(),
  });

  const [schemes, setSchemes] = useState<any[]>([]);
  const [loadingSchemes, setLoadingSchemes] = useState(true);

  // Fetch schemes on mount
  useEffect(() => {
    const fetchSchemes = async () => {
      try {
        const response = await applicationApi.get('/api/v1/services');
        setSchemes(response.data);
      } catch {
        toast.error('Failed to load schemes catalog');
      } finally {
        setLoadingSchemes(false);
      }
    };
    fetchSchemes();
  }, []);

  // Compute metrics strictly from real data
  const metrics = useMemo(() => {
    if (!myApps) {
      return { total: 0, pending: 0, approved: 0, rejected: 0 };
    }
    const total = myApps.length;
    const pending = myApps.filter(
      (a) =>
        a.status.includes('PENDING') ||
        a.status === 'SUBMITTED' ||
        a.status === 'IN_PROGRESS' ||
        a.status === 'DRAFT'
    ).length;
    const approved = myApps.filter((a) => a.status === 'APPROVED' || a.status === 'COMPLETED').length;
    const rejected = myApps.filter(
      (a) => a.status === 'REJECTED' || a.status === 'FAILED' || a.status === 'CONSENT_DENIED'
    ).length;
    return { total, pending, approved, rejected };
  }, [myApps]);

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'DRAFT':
        return <Badge variant="secondary">Draft</Badge>;
      case 'SUBMITTED':
        return <Badge variant="info">Submitted</Badge>;
      case 'PENDING_OFFICER_REVIEW':
      case 'PENDING_REVIEW':
        return <Badge variant="pending">Under Review</Badge>;
      case 'APPROVED':
      case 'COMPLETED':
        return <Badge variant="active">Approved</Badge>;
      case 'REJECTED':
      case 'FAILED':
      case 'CONSENT_DENIED':
        return <Badge variant="failed">Rejected</Badge>;
      default:
        return <Badge variant="outline">{status.replace(/_/g, ' ')}</Badge>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Breadcrumb Navigation */}
      <Breadcrumb
        items={[
          { label: 'Citizen Services', href: '/citizen' },
          { label: 'Dashboard' },
        ]}
      />

      {/* Official Welcome & Executive Banner */}
      <div className="bg-white border border-slate-200 rounded-md p-6 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="bg-[#0B1F3A] text-white text-[10px] font-bold uppercase px-2 py-0.5 rounded-xs tracking-wider">
              Citizen Gateway
            </span>
            <span className="text-xs text-slate-500 font-medium">
              Citizen Reference: <strong className="font-mono text-slate-800">{username}</strong>
            </span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Welcome, {displayName}
          </h1>
          <p className="text-xs sm:text-sm text-slate-600 max-w-2xl leading-relaxed">
            Access certified state schemes and welfare assistance. Verified personal and academic
            records are pulled directly from government repositories with DPDP cryptographic authorization.
          </p>
        </div>

        <div className="flex items-center gap-2.5 shrink-0">
          <Link to="/citizen/services">
            <Button className="font-semibold text-xs h-9 px-4 gap-2">
              <Building2 className="w-3.5 h-3.5" />
              Apply for New Scheme
            </Button>
          </Link>
        </div>
      </div>

      {/* Real-Data KPI / Statistic Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Card 1: Total Applications */}
        <Card className="p-4 border-slate-200 bg-white">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Total Applications
            </span>
            <div className="w-7 h-7 rounded-sm bg-blue-50 text-[#0B1F3A] flex items-center justify-center">
              <FileText className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-black text-slate-900 font-mono">
              {loadingApps ? <Loader2 className="w-5 h-5 animate-spin text-slate-400" /> : metrics.total}
            </span>
            <span className="text-[11px] text-slate-500">filed schemes</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-2 border-t border-slate-100 pt-1.5">
            Registered under citizen record
          </p>
        </Card>

        {/* Card 2: Pending Officer Review */}
        <Card className="p-4 border-slate-200 bg-white">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              In Review / Pending
            </span>
            <div className="w-7 h-7 rounded-sm bg-amber-50 text-amber-700 flex items-center justify-center">
              <Clock className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-black text-amber-700 font-mono">
              {loadingApps ? <Loader2 className="w-5 h-5 animate-spin text-slate-400" /> : metrics.pending}
            </span>
            <span className="text-[11px] text-slate-500">active dossiers</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-2 border-t border-slate-100 pt-1.5">
            Awaiting verification or officer decision
          </p>
        </Card>

        {/* Card 3: Approved / Completed */}
        <Card className="p-4 border-slate-200 bg-white">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Sanctioned / Approved
            </span>
            <div className="w-7 h-7 rounded-sm bg-emerald-50 text-emerald-700 flex items-center justify-center">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-black text-emerald-700 font-mono">
              {loadingApps ? <Loader2 className="w-5 h-5 animate-spin text-slate-400" /> : metrics.approved}
            </span>
            <span className="text-[11px] text-slate-500">approved benefits</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-2 border-t border-slate-100 pt-1.5">
            Successfully concluded workflows
          </p>
        </Card>

        {/* Card 4: Catalog Services */}
        <Card className="p-4 border-slate-200 bg-white">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              State Services
            </span>
            <div className="w-7 h-7 rounded-sm bg-slate-100 text-slate-700 flex items-center justify-center">
              <Building2 className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-black text-slate-900 font-mono">
              {loadingSchemes ? <Loader2 className="w-5 h-5 animate-spin text-slate-400" /> : schemes.length}
            </span>
            <span className="text-[11px] text-slate-500">active schemes</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-2 border-t border-slate-100 pt-1.5">
            Zero-document onboarding enabled
          </p>
        </Card>
      </div>

      {/* Section: My Applications Tracker (Dense Administrative Style) */}
      <Card className="bg-white border-slate-200 shadow-xs">
        <CardHeader className="border-b border-slate-100 pb-3 flex flex-row items-center justify-between">
          <div>
            <CardTitle className="text-base flex items-center gap-2">
              <FolderOpen className="w-4 h-4 text-[#0B1F3A]" />
              <span>Recent Application Dossiers</span>
            </CardTitle>
            <CardDescription>
              Chronological status of citizen schemes submitted through the Ekikrit interoperability gateway
            </CardDescription>
          </div>
          <Link to="/citizen/applications">
            <Button variant="outline" size="sm" className="gap-1.5 text-xs font-semibold">
              View All Applications <ArrowRight className="w-3.5 h-3.5" />
            </Button>
          </Link>
        </CardHeader>
        <CardContent className="p-0">
          {loadingApps ? (
            <div className="p-10 text-center text-slate-500">
              <Loader2 className="w-6 h-6 animate-spin mx-auto text-[#0B1F3A] mb-2" />
              <p className="text-xs font-medium">Fetching application records...</p>
            </div>
          ) : !myApps || myApps.length === 0 ? (
            <div className="p-10 text-center bg-slate-50/50">
              <FileText className="w-10 h-10 text-slate-300 mx-auto mb-2" />
              <h4 className="text-sm font-bold text-slate-800">No applications on record</h4>
              <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
                You haven't submitted any scheme applications yet. Browse the state service directory to apply with zero document uploads.
              </p>
              <div className="mt-4">
                <Link to="/citizen/services">
                  <Button size="sm" className="font-medium text-xs">
                    Browse State Schemes
                  </Button>
                </Link>
              </div>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 text-[11px] font-semibold uppercase tracking-wider">
                    <th className="px-4 py-3">Reference Number</th>
                    <th className="px-4 py-3">Scheme / Service</th>
                    <th className="px-4 py-3">Department</th>
                    <th className="px-4 py-3">Submission Date</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-xs">
                  {myApps.slice(0, 5).map((app) => (
                    <tr
                      key={app.applicationNumber}
                      onClick={() => navigate(`/citizen/applications/${app.applicationNumber}`)}
                      className="hover:bg-slate-50 transition-colors cursor-pointer"
                    >
                      <td className="px-4 py-3 font-mono font-bold text-slate-900">
                        {app.applicationNumber}
                      </td>
                      <td className="px-4 py-3 font-semibold text-slate-800">
                        {app.serviceName}
                        <div className="text-[10px] font-mono text-slate-400 mt-0.5">
                          Code: {app.serviceCode}
                        </div>
                      </td>
                      <td className="px-4 py-3 text-slate-600">
                        {app.departmentName || app.departmentCode}
                      </td>
                      <td className="px-4 py-3 text-slate-500 whitespace-nowrap">
                        {app.submittedAt ? format(new Date(app.submittedAt), 'dd MMM yyyy') : 'Draft'}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap">
                        {getStatusBadge(app.status)}
                      </td>
                      <td className="px-4 py-3 text-right">
                        <Button
                          variant="outline"
                          size="xs"
                          className="font-medium gap-1 text-[11px] hover:bg-[#0B1F3A] hover:text-white"
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(`/citizen/applications/${app.applicationNumber}`);
                          }}
                        >
                          View <ChevronRight className="w-3 h-3" />
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Featured Government Schemes Directory */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <Building2 className="w-4 h-4 text-[#0B1F3A]" />
              <span>Available Government Schemes</span>
            </h2>
            <p className="text-xs text-slate-500">
              Schemes enabled for automated inter-departmental verification
            </p>
          </div>
          <Link to="/citizen/services" className="text-xs font-semibold text-[#0B1F3A] hover:underline flex items-center gap-1">
            Browse Full Directory <ChevronRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {loadingSchemes ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-44 bg-white border border-slate-200 rounded-md p-4 animate-pulse" />
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {schemes.slice(0, 3).map((scheme: any) => (
              <ServiceCard key={scheme.serviceCode} service={scheme} />
            ))}
          </div>
        )}
      </div>

      {/* DPDP Legal & Institutional Trust Notice */}
      <div className="bg-white border border-slate-200 rounded-md p-5 shadow-xs flex flex-col sm:flex-row items-start gap-4 text-xs text-slate-700">
        <div className="w-10 h-10 rounded-sm bg-amber-50 border border-amber-200 flex items-center justify-center shrink-0">
          <ShieldCheck className="w-5 h-5 text-amber-700" />
        </div>
        <div className="space-y-1">
          <h4 className="font-bold text-slate-900 text-sm">
            Digital Personal Data Protection (DPDP) Act 2023 Framework
          </h4>
          <p className="text-slate-600 leading-relaxed">
            All records retrieved during scheme eligibility evaluation are queried directly from authoritative state databases
            (Revenue, Education Board, Employment Exchange) strictly following your explicit cryptographic consent.
            Physical scans or paper photocopies are never required on Ekikrit.
          </p>
        </div>
      </div>
    </div>
  );
}
