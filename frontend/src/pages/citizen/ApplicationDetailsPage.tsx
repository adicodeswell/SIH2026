import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { applicationServiceApi } from '@/services/applicationService';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Breadcrumb } from '@/components/ui/Breadcrumb';
import { WorkflowProcessVisualizer } from '@/components/ui/WorkflowProcessVisualizer';
import {
  Loader2,
  ArrowLeft,
  Clock,
  FileText,
  XCircle,
  CheckCircle2,
  AlertCircle,
  Building2,
  ShieldCheck,
} from 'lucide-react';
import { format } from 'date-fns';

const getStatusBadge = (status: string) => {
  switch (status) {
    case 'SUBMITTED':
      return <Badge variant="info">Submitted</Badge>;
    case 'APPROVED':
      return <Badge variant="active">Approved</Badge>;
    case 'REJECTED':
    case 'FAILED':
    case 'CONSENT_DENIED':
      return <Badge variant="failed">Rejected</Badge>;
    case 'PENDING_OFFICER_REVIEW':
    case 'PENDING_REVIEW':
      return <Badge variant="pending">Under Review</Badge>;
    case 'DRAFT':
      return <Badge variant="secondary">Draft</Badge>;
    default:
      return <Badge variant="outline">{status.replace(/_/g, ' ')}</Badge>;
  }
};

const getCategoryIcon = (category: string) => {
  switch (category) {
    case 'CONSENT':
      return <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />;
    case 'APPLICATION':
      return <FileText className="w-3.5 h-3.5 text-blue-600" />;
    case 'WORKFLOW':
      return <Clock className="w-3.5 h-3.5 text-indigo-600" />;
    case 'OFFICER_REVIEW':
      return <AlertCircle className="w-3.5 h-3.5 text-amber-600" />;
    case 'SYSTEM':
      return <ShieldCheck className="w-3.5 h-3.5 text-slate-600" />;
    default:
      return <Clock className="w-3.5 h-3.5 text-slate-500" />;
  }
};

export function CitizenApplicationDetailsPage() {
  const { id } = useParams<{ id: string }>();

  const { data: application, isLoading: loadingApp, error: appError } = useQuery({
    queryKey: ['citizen-application', id],
    queryFn: () => applicationServiceApi.getApplicationById(id!),
    enabled: !!id,
  });

  const { data: activity, isLoading: loadingActivity } = useQuery({
    queryKey: ['citizen-application-activity', id],
    queryFn: () => applicationServiceApi.getApplicationActivity(id!),
    enabled: !!id,
  });

  if (loadingApp) {
    return (
      <div className="flex flex-col items-center justify-center p-16 space-y-3 bg-white border border-slate-200 rounded-md">
        <Loader2 className="w-8 h-8 animate-spin text-[#0B1F3A]" />
        <p className="text-xs font-semibold text-slate-700">Retrieving official application dossier...</p>
      </div>
    );
  }

  if (appError || !application) {
    return (
      <div className="space-y-4">
        <Link to="/citizen/applications" className="inline-flex items-center text-xs text-[#0B1F3A] hover:underline font-semibold">
          <ArrowLeft className="w-3.5 h-3.5 mr-1" /> Back to My Applications
        </Link>
        <div className="bg-red-50 border border-red-200 text-red-900 p-6 rounded-md flex items-start gap-3">
          <XCircle className="h-6 w-6 mt-0.5 text-red-600 shrink-0" />
          <div className="space-y-1">
            <h4 className="font-bold text-sm">Application Dossier Not Found</h4>
            <p className="text-xs text-red-800">
              The requested reference number "{id}" could not be located or you lack permissions to inspect it.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Breadcrumbs */}
      <Breadcrumb
        items={[
          { label: 'Citizen Services', href: '/citizen' },
          { label: 'My Applications', href: '/citizen/applications' },
          { label: application.applicationNumber },
        ]}
      />

      {/* Top Dossier Title Banner */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-md border border-slate-200 shadow-xs">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="bg-[#0B1F3A] text-white text-[10px] font-bold uppercase px-2 py-0.5 rounded-xs tracking-wider">
              Application Dossier
            </span>
            <span className="text-xs text-slate-500 font-mono">
              Ref: <strong className="text-slate-900">{application.applicationNumber}</strong>
            </span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            {application.serviceName || application.serviceCode}
          </h1>
          <p className="text-xs text-slate-500 flex items-center gap-1">
            <Building2 className="w-3.5 h-3.5 text-slate-400" />
            <span>Department: {application.departmentCode || 'State Administration'}</span>
          </p>
        </div>

        <div className="flex flex-col sm:items-end">
          <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider mb-1">
            Current Status
          </span>
          {getStatusBadge(application.status)}
        </div>
      </div>

      {/* Real-Time Workflow Process Flow Pipeline */}
      <WorkflowProcessVisualizer currentStatus={application.status} />

      {/* Two-Column Detail View: Parameters on Left, Activity Timeline on Right */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Left 2 Cols: Details & Verification Data */}
        <div className="md:col-span-2 space-y-6">
          <Card className="rounded-md border-slate-200 shadow-xs">
            <CardHeader className="border-b border-slate-100 bg-slate-50/70 pb-3">
              <CardTitle className="flex items-center gap-2 text-sm font-bold text-slate-900">
                <FileText className="w-4 h-4 text-[#0B1F3A]" />
                <span>Application Dossier Metadata</span>
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-4 space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-y-4 gap-x-4 text-xs">
                <div>
                  <span className="text-slate-500 font-medium">Service Scheme Code</span>
                  <p className="font-mono mt-1 font-bold text-slate-900">{application.serviceCode}</p>
                </div>
                <div>
                  <span className="text-slate-500 font-medium">Applicant Citizen ID</span>
                  <p className="font-mono mt-1 font-bold text-slate-900">{application.citizenId}</p>
                </div>
                <div>
                  <span className="text-slate-500 font-medium">Creation Timestamp</span>
                  <p className="mt-1 font-semibold text-slate-800">{format(new Date(application.createdAt), 'PPP p')}</p>
                </div>
                <div>
                  <span className="text-slate-500 font-medium">Filing Timestamp</span>
                  <p className="mt-1 font-semibold text-slate-800">
                    {application.submittedAt ? format(new Date(application.submittedAt), 'PPP p') : 'Not Submitted'}
                  </p>
                </div>
              </div>

              {/* Verified Registry Summary */}
              {!!application.verificationData && (
                <div className="border-t border-slate-100 pt-4 mt-4 space-y-2">
                  <h4 className="text-xs font-bold text-slate-900 flex items-center gap-1.5">
                    <ShieldCheck className="w-4 h-4 text-emerald-600" />
                    <span>Interoperability Verification Summary</span>
                  </h4>
                  <div className="bg-slate-50 border border-slate-200 rounded-sm p-3 text-xs space-y-1.5">
                    <div className="flex items-center gap-2 text-slate-700">
                      <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600 shrink-0" />
                      <span>Canonical citizen identity verified from state database</span>
                    </div>
                    <div className="flex items-center gap-2 text-slate-700">
                      <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600 shrink-0" />
                      <span>Academic &amp; credential parameters validated against Education Board</span>
                    </div>
                    <div className="flex items-center gap-2 text-slate-700">
                      <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600 shrink-0" />
                      <span>Zero paper document photocopies required</span>
                    </div>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Right Col: Chronological Activity History */}
        <div className="space-y-4">
          <Card className="h-fit rounded-md border-slate-200 shadow-xs">
            <CardHeader className="bg-slate-50/70 border-b border-slate-100 pb-3">
              <CardTitle className="flex items-center gap-2 text-sm font-bold text-slate-900">
                <Clock className="w-4 h-4 text-[#0B1F3A]" />
                <span>Activity History</span>
              </CardTitle>
              <CardDescription className="text-[11px]">
                Chronological audit events recorded for this application.
              </CardDescription>
            </CardHeader>
            <CardContent className="pt-4">
              {loadingActivity ? (
                <div className="flex justify-center p-6">
                  <Loader2 className="w-5 h-5 animate-spin text-[#0B1F3A]" />
                </div>
              ) : activity && activity.length > 0 ? (
                <div className="space-y-4 relative before:absolute before:inset-0 before:ml-2 before:-translate-x-px before:h-full before:w-0.5 before:bg-slate-200">
                  {activity.map((event, index) => {
                    if (event.type === 'ACTIVITY_PARTIALLY_UNAVAILABLE') {
                      return (
                        <div key={`err-${index}`} className="relative flex items-center">
                          <div className="bg-red-50 text-red-700 text-[11px] p-2 rounded border border-red-200 w-full ml-6">
                            <AlertCircle className="inline h-3 w-3 mr-1" /> {event.description}
                          </div>
                        </div>
                      );
                    }

                    return (
                      <div key={event.id || index} className="relative flex items-start">
                        <div className="flex items-center justify-center w-4.5 h-4.5 rounded-full border border-white bg-white shrink-0 z-10 shadow-xs mt-0.5">
                          {getCategoryIcon(event.category)}
                        </div>
                        <div className="ml-3 flex-1 text-xs">
                          <h4 className="font-bold text-slate-900 text-xs">{event.title}</h4>
                          <time className="text-[10px] text-slate-400 block font-mono">
                            {format(new Date(event.occurredAt), 'dd MMM yyyy, p')}
                          </time>
                          <p className="text-[11px] text-slate-600 mt-0.5">{event.description}</p>
                          {event.status && (
                            <div className="mt-1">
                              {getStatusBadge(event.status)}
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className="text-center py-6 text-slate-400 text-xs">
                  <Clock className="w-6 h-6 mx-auto mb-1.5 text-slate-300" />
                  <p>No activity history logged yet.</p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
