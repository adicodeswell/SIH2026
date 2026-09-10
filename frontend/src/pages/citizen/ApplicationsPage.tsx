import { useState, useMemo } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { applicationServiceApi } from '@/services/applicationService';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Breadcrumb } from '@/components/ui/Breadcrumb';
import { Input } from '@/components/ui/input';
import {
  Search,
  ChevronRight,
  RefreshCw,
  AlertCircle,
  FileText,
} from 'lucide-react';
import { format } from 'date-fns';

export function CitizenApplicationsPage() {
  const [searchId, setSearchId] = useState('');
  const [filterText, setFilterText] = useState('');
  const navigate = useNavigate();

  const { data: applications, isLoading, isError, refetch } = useQuery({
    queryKey: ['citizen-applications'],
    queryFn: () => applicationServiceApi.getMyApplications(),
  });

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchId.trim()) {
      navigate(`/citizen/applications/${searchId.trim()}`);
    }
  };

  const filteredApps = useMemo(() => {
    if (!applications) return [];
    if (!filterText.trim()) return applications;
    const lower = filterText.toLowerCase();
    return applications.filter(
      (app) =>
        app.applicationNumber.toLowerCase().includes(lower) ||
        app.serviceName.toLowerCase().includes(lower) ||
        app.serviceCode.toLowerCase().includes(lower) ||
        (app.departmentName && app.departmentName.toLowerCase().includes(lower))
    );
  }, [applications, filterText]);

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
          { label: 'My Applications' },
        ]}
      />

      {/* Page Header */}
      <div className="bg-white border border-slate-200 rounded-md p-6 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="bg-[#0B1F3A] text-white text-[10px] font-bold uppercase px-2 py-0.5 rounded-xs tracking-wider">
              Citizen Ledger
            </span>
            <span className="text-xs text-slate-500 font-medium">
              Registered Dossiers: {applications?.length || 0}
            </span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            My Scheme Applications
          </h1>
          <p className="text-xs sm:text-sm text-slate-600 mt-1 max-w-2xl leading-relaxed">
            Real-time tracking ledger for all government applications filed under your authenticated citizen profile.
          </p>
        </div>

        <div className="flex items-center gap-2 shrink-0">
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            disabled={isLoading}
            className="text-xs font-semibold h-9 gap-1.5"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${isLoading ? 'animate-spin' : ''}`} />
            Refresh Queue
          </Button>
          <Link to="/citizen/services">
            <Button className="text-xs font-bold h-9 bg-[#0B1F3A] hover:bg-[#102A43] text-white">
              Apply for Scheme
            </Button>
          </Link>
        </div>
      </div>

      {/* Quick Search & Filter Controls */}
      <div className="bg-white border border-slate-200 rounded-md p-4 shadow-xs flex flex-col sm:flex-row gap-3 items-center justify-between">
        {/* Table in-memory search */}
        <div className="w-full sm:w-80 relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-slate-400" />
          <Input
            placeholder="Filter by title, reference, department..."
            className="pl-9 text-xs h-9"
            value={filterText}
            onChange={(e) => setFilterText(e.target.value)}
          />
        </div>

        {/* Direct Reference Tracking form */}
        <form onSubmit={handleSearch} className="w-full sm:w-auto flex gap-2 items-center">
          <Input
            placeholder="Track Reference e.g. MH-2026-..."
            className="text-xs h-9 w-full sm:w-60 font-mono"
            value={searchId}
            onChange={(e) => setSearchId(e.target.value)}
          />
          <Button
            type="submit"
            variant="secondary"
            disabled={!searchId.trim()}
            className="text-xs font-semibold h-9 shrink-0"
          >
            Track
          </Button>
        </form>
      </div>

      {/* Main Ledger Content */}
      {isLoading ? (
        <div className="p-12 text-center bg-white border border-slate-200 rounded-md">
          <RefreshCw className="w-6 h-6 animate-spin mx-auto text-[#0B1F3A] mb-2" />
          <p className="text-xs font-semibold text-slate-700">Loading citizen application dossier records...</p>
        </div>
      ) : isError ? (
        <div className="bg-red-50 border border-red-200 text-red-900 p-6 rounded-md flex flex-col items-center justify-center text-center space-y-2">
          <AlertCircle className="h-8 w-8 text-red-600" />
          <h3 className="text-sm font-bold">Unable to load application ledger</h3>
          <p className="text-xs text-red-700 max-w-md">
            Communication failure with the application service. Please verify network connectivity and retry.
          </p>
          <Button onClick={() => refetch()} variant="outline" size="sm" className="border-red-300 text-red-800 bg-white mt-2">
            Try Again
          </Button>
        </div>
      ) : filteredApps && filteredApps.length > 0 ? (
        <div className="bg-white border border-slate-200 rounded-md overflow-hidden shadow-xs">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 text-[11px] font-semibold uppercase tracking-wider">
                  <th className="px-4 py-3">Reference Number</th>
                  <th className="px-4 py-3">Scheme Name</th>
                  <th className="px-4 py-3">Department</th>
                  <th className="px-4 py-3">Filing Date</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {filteredApps.map((app) => (
                  <tr
                    key={app.applicationNumber}
                    onClick={() => navigate(`/citizen/applications/${app.applicationNumber}`)}
                    className="hover:bg-slate-50 transition-colors cursor-pointer even:bg-slate-50/40"
                  >
                    <td className="px-4 py-3 font-mono font-bold text-slate-900 whitespace-nowrap">
                      {app.applicationNumber}
                    </td>
                    <td className="px-4 py-3 font-semibold text-slate-900">
                      <div className="line-clamp-1">{app.serviceName}</div>
                      <div className="text-[10px] font-mono text-slate-500 font-normal">
                        Code: {app.serviceCode}
                      </div>
                    </td>
                    <td className="px-4 py-3 text-slate-600">
                      <div className="line-clamp-1">{app.departmentName || app.departmentCode}</div>
                    </td>
                    <td className="px-4 py-3 text-slate-500 whitespace-nowrap font-medium">
                      {app.submittedAt ? format(new Date(app.submittedAt), 'dd MMM yyyy, p') : 'Not Submitted'}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap">
                      {getStatusBadge(app.status)}
                    </td>
                    <td className="px-4 py-3 text-right whitespace-nowrap">
                      <Button
                        size="xs"
                        variant="outline"
                        className="font-semibold text-[11px] gap-1 hover:bg-[#0B1F3A] hover:text-white"
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/citizen/applications/${app.applicationNumber}`);
                        }}
                      >
                        View Dossier <ChevronRight className="w-3 h-3" />
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="bg-white border border-slate-200 p-12 rounded-md flex flex-col items-center justify-center text-center">
          <div className="bg-slate-100 p-3 rounded-full mb-3">
            <FileText className="h-8 w-8 text-slate-400" />
          </div>
          <h3 className="text-base font-bold text-slate-900">
            {filterText ? 'No matching applications found' : 'No applications filed on record'}
          </h3>
          <p className="text-xs text-slate-500 mt-1 mb-5 max-w-sm">
            {filterText
              ? `No application matched your filter "${filterText}". Try searching for another keyword.`
              : 'You have not submitted any government welfare applications yet. Browse available schemes to apply.'}
          </p>
          {filterText ? (
            <Button size="sm" variant="outline" onClick={() => setFilterText('')}>
              Clear Filter
            </Button>
          ) : (
            <Button size="sm" className="bg-[#0B1F3A] text-white" onClick={() => navigate('/citizen/services')}>
              Browse Available Schemes
            </Button>
          )}
        </div>
      )}
    </div>
  );
}
