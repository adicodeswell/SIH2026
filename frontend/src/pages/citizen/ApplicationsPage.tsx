import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { applicationServiceApi } from '@/services/applicationService';
import { Button } from '@/components/ui/button';
import { Card, CardContent, } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Search, ChevronRight, RefreshCw, AlertCircle, FileText } from 'lucide-react';
import { format } from 'date-fns';

export function CitizenApplicationsPage() {
  const [searchId, setSearchId] = useState('');
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

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'DRAFT':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-100 text-slate-800">Draft</span>;
      case 'SUBMITTED':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">Submitted</span>;
      case 'PENDING_OFFICER_REVIEW':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800">Pending Review</span>;
      case 'APPROVED':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-100 text-emerald-800">Approved</span>;
      case 'REJECTED':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">Rejected</span>;
      case 'FAILED':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">Failed</span>;
      default:
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-100 text-slate-800">{status}</span>;
    }
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">My Applications</h1>
          <p className="text-sm text-slate-500 mt-1">
            View and track all government schemes you have applied for.
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={() => refetch()} disabled={isLoading}>
          <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      {isLoading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <Card key={i} className="animate-pulse">
              <CardContent className="h-24"></CardContent>
            </Card>
          ))}
        </div>
      ) : isError ? (
        <div className="bg-red-50 border border-red-200 text-red-800 p-6 rounded-lg flex flex-col items-center justify-center text-center">
          <AlertCircle className="h-10 w-10 text-red-500 mb-4" />
          <h3 className="text-lg font-semibold">Unable to load your applications</h3>
          <p className="text-sm mt-2 mb-4">There was an error communicating with the server.</p>
          <Button onClick={() => refetch()} variant="outline" className="border-red-300 text-red-700 hover:bg-red-100">
            Try Again
          </Button>
        </div>
      ) : applications && applications.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {applications.map((app) => (
            <Card key={app.applicationNumber} className="hover:border-primary/50 transition-colors cursor-pointer" onClick={() => navigate(`/citizen/applications/${app.applicationNumber}`)}>
              <CardContent className="p-5 flex flex-col h-full">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h3 className="font-semibold text-lg text-slate-900 line-clamp-1" title={app.serviceName}>{app.serviceName}</h3>
                    <p className="text-sm text-slate-500 font-mono mt-1">{app.applicationNumber}</p>
                  </div>
                  <div>
                    {getStatusBadge(app.status)}
                  </div>
                </div>
                
                <div className="mt-auto space-y-2 pt-4 border-t border-slate-100 text-sm text-slate-600">
                  <div className="flex justify-between">
                    <span>Department:</span>
                    <span className="font-medium text-slate-900 line-clamp-1 text-right ml-4" title={app.departmentName}>{app.departmentName}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Submitted:</span>
                    <span className="font-medium text-slate-900">
                      {app.submittedAt ? format(new Date(app.submittedAt), 'dd MMM yyyy') : 'Not Submitted'}
                    </span>
                  </div>
                </div>
                
                <div className="mt-4 flex items-center text-primary font-medium text-sm hover:underline">
                  View Details <ChevronRight className="h-4 w-4 ml-1" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : (
        <div className="bg-white border border-slate-200 p-12 rounded-lg flex flex-col items-center justify-center text-center">
          <div className="bg-slate-100 p-4 rounded-full mb-4">
            <FileText className="h-10 w-10 text-slate-400" />
          </div>
          <h3 className="text-xl font-semibold text-slate-900">No applications yet</h3>
          <p className="text-slate-500 mt-2 mb-6 max-w-md">
            You haven't applied for any government schemes yet. Browse available services to get started.
          </p>
          <Button onClick={() => navigate('/citizen/services')}>
            Browse Services
          </Button>
        </div>
      )}

      <div className="mt-12 pt-8 border-t border-slate-200">
        <h3 className="text-lg font-medium text-slate-900 mb-4">Track by Reference Number</h3>
        <form onSubmit={handleSearch} className="flex gap-4 max-w-md">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
            <Input
              placeholder="e.g., MH-2024-1234"
              className="pl-9"
              value={searchId}
              onChange={(e) => setSearchId(e.target.value)}
              required
            />
          </div>
          <Button type="submit" variant="secondary" disabled={!searchId.trim()}>
            Track
          </Button>
        </form>
      </div>
    </div>
  );
}
