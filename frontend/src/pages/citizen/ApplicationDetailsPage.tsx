import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { applicationServiceApi } from '@/services/applicationService';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Loader2, ArrowLeft, Clock, FileText, XCircle, CheckCircle2, AlertCircle } from 'lucide-react';
import { format } from 'date-fns';

const getStatusColor = (status: string) => {
  switch (status) {
    case 'SUBMITTED':
      return 'bg-blue-100 text-blue-800 border-blue-200';
    case 'APPROVED':
      return 'bg-emerald-100 text-emerald-800 border-emerald-200';
    case 'REJECTED':
    case 'FAILED':
    case 'CONSENT_DENIED':
      return 'bg-red-100 text-red-800 border-red-200';
    case 'PENDING_OFFICER_REVIEW':
      return 'bg-amber-100 text-amber-800 border-amber-200';
    case 'DRAFT':
      return 'bg-slate-100 text-slate-800 border-slate-200';
    default:
      return 'bg-slate-100 text-slate-800 border-slate-200';
  }
};

const getCategoryIcon = (category: string) => {
  switch (category) {
    case 'CONSENT':
      return <CheckCircle2 className="w-4 h-4 text-emerald-500" />;
    case 'APPLICATION':
      return <FileText className="w-4 h-4 text-blue-500" />;
    case 'WORKFLOW':
      return <Clock className="w-4 h-4 text-indigo-500" />;
    case 'OFFICER_REVIEW':
      return <AlertCircle className="w-4 h-4 text-amber-500" />;
    case 'SYSTEM':
      return <AlertCircle className="w-4 h-4 text-slate-500" />;
    default:
      return <Clock className="w-4 h-4 text-slate-500" />;
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
      <div className="flex justify-center p-12">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  if (appError || !application) {
    return (
      <div className="space-y-6">
        <Link to="/citizen/applications" className="inline-flex items-center text-sm text-primary hover:underline font-medium">
          <ArrowLeft className="w-4 h-4 mr-1" /> Back to My Applications
        </Link>
        <div className="bg-red-50 border border-red-200 text-red-800 p-6 rounded-lg flex items-start gap-4">
          <XCircle className="h-6 w-6 mt-0.5 shrink-0" />
          <div>
            <h4 className="font-bold text-lg">Application Not Found</h4>
            <p className="mt-1">We could not find the requested application or you do not have permission to view it.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <Link to="/citizen/applications" className="inline-flex items-center text-sm text-primary hover:underline font-medium">
        <ArrowLeft className="w-4 h-4 mr-1" /> Back to My Applications
      </Link>

      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            {application.serviceName || application.serviceCode}
          </h1>
          <p className="text-sm text-slate-500 mt-1 flex items-center gap-2">
            Reference Number: <span className="font-mono font-medium text-slate-900 bg-slate-100 px-2 py-0.5 rounded">{application.applicationNumber}</span>
          </p>
        </div>
        <div className="flex flex-col items-end">
          <span className="text-xs text-slate-500 mb-1 uppercase font-semibold tracking-wider">Current Status</span>
          <Badge className={`${getStatusColor(application.status)} text-sm px-3 py-1 border`}>
            {application.status.replace(/_/g, ' ')}
          </Badge>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="md:col-span-2 space-y-6">
          <Card>
            <CardHeader className="pb-4">
              <CardTitle className="flex items-center gap-2 text-lg">
                <FileText className="w-5 h-5 text-primary" />
                Application Information
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-y-6 gap-x-4">
                <div>
                  <p className="text-sm font-medium text-slate-500">Service Code</p>
                  <p className="font-mono mt-1 font-medium text-slate-900">{application.serviceCode}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Applicant ID</p>
                  <p className="font-mono mt-1 font-medium text-slate-900">{application.citizenId}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Creation Date</p>
                  <p className="mt-1 font-medium text-slate-900">{format(new Date(application.createdAt), 'PPP p')}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Submission Date</p>
                  <p className="mt-1 font-medium text-slate-900">{application.submittedAt ? format(new Date(application.submittedAt), 'PPP p') : 'Not Submitted'}</p>
                </div>
              </div>

              {!!application.verificationData && (
                <div className="border-t pt-4 mt-6">
                  <h3 className="text-sm font-semibold text-slate-900 mb-3">Verification Summary</h3>
                  <div className="bg-slate-50 border border-slate-200 rounded-lg p-4">
                    <ul className="space-y-2">
                      <li className="flex items-center gap-2 text-sm text-slate-700">
                        <CheckCircle2 className="h-4 w-4 text-emerald-500" />
                        Identity information verified successfully
                      </li>
                      <li className="flex items-center gap-2 text-sm text-slate-700">
                        <CheckCircle2 className="h-4 w-4 text-emerald-500" />
                        Eligibility criteria validated
                      </li>
                      <li className="flex items-center gap-2 text-sm text-slate-700">
                        <CheckCircle2 className="h-4 w-4 text-emerald-500" />
                        Automated checks complete
                      </li>
                    </ul>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        <Card className="h-fit">
          <CardHeader className="bg-slate-50/50 border-b border-slate-100 pb-4">
            <CardTitle className="flex items-center gap-2 text-lg">
              <Clock className="w-5 h-5 text-primary" />
              Activity History
            </CardTitle>
            <CardDescription>
              Chronological timeline of your application.
            </CardDescription>
          </CardHeader>
          <CardContent className="pt-6">
            {loadingActivity ? (
              <div className="flex justify-center p-8">
                <Loader2 className="w-6 h-6 animate-spin text-primary" />
              </div>
            ) : activity && activity.length > 0 ? (
              <div className="space-y-6 relative before:absolute before:inset-0 before:ml-2.5 before:-translate-x-px md:before:mx-auto md:before:translate-x-0 before:h-full before:w-0.5 before:bg-gradient-to-b before:from-transparent before:via-slate-200 before:to-transparent">
                {activity.map((event, index) => {
                  if (event.type === 'ACTIVITY_PARTIALLY_UNAVAILABLE') {
                    return (
                      <div key={`err-${index}`} className="relative flex items-center justify-between md:justify-normal md:odd:flex-row-reverse group is-active">
                        <div className="bg-red-50 text-red-700 text-xs px-3 py-2 rounded border border-red-200 w-full ml-8">
                          <AlertCircle className="inline h-3 w-3 mr-1" /> {event.description}
                        </div>
                      </div>
                    );
                  }

                  return (
                    <div key={event.id || index} className="relative flex items-start group">
                      <div className="flex items-center justify-center w-6 h-6 rounded-full border-2 border-white bg-white shrink-0 z-10 shadow-sm mt-0.5">
                        {getCategoryIcon(event.category)}
                      </div>
                      <div className="ml-4 flex-1">
                        <h4 className="font-semibold text-slate-900 text-sm">{event.title}</h4>
                        <time className="text-xs text-slate-500 mt-0.5 block font-medium">
                          {format(new Date(event.occurredAt), 'dd MMM yyyy, h:mm a')}
                        </time>
                        <p className="text-sm text-slate-600 mt-1">{event.description}</p>
                        {event.status && (
                          <span className={`inline-block mt-2 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wider rounded border ${getStatusColor(event.status)}`}>
                            {event.status.replace(/_/g, ' ')}
                          </span>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="text-center py-8">
                <Clock className="w-8 h-8 text-slate-300 mx-auto mb-3" />
                <p className="text-sm text-slate-500">No activity history available yet.</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
