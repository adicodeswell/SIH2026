import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { applicationServiceApi } from '@/services/applicationService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Loader2, ArrowLeft, Clock, FileText, XCircle } from 'lucide-react';
import type { ApplicationStatus } from '@/types/service';

const getStatusColor = (status: ApplicationStatus) => {
  switch (status) {
    case 'SUBMITTED':
      return 'bg-blue-100 text-blue-800';
    case 'APPROVED':
      return 'bg-green-100 text-green-800';
    case 'REJECTED':
    case 'FAILED':
    case 'CONSENT_DENIED':
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-amber-100 text-amber-800';
  }
};

export function CitizenApplicationDetailsPage() {
  const { id } = useParams<{ id: string }>();

  const { data: application, isLoading: loadingApp, error: appError } = useQuery({
    queryKey: ['application', id],
    queryFn: () => applicationServiceApi.getApplicationById(id!),
    enabled: !!id,
  });

  const { data: timeline, isLoading: loadingTimeline } = useQuery({
    queryKey: ['timeline', id],
    queryFn: () => applicationServiceApi.getApplicationTimeline(id!),
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
        <Link to="/citizen/applications" className="inline-flex items-center text-sm text-primary hover:underline">
          <ArrowLeft className="w-4 h-4 mr-1" /> Back to Tracker
        </Link>
        <div className="bg-red-50 border border-red-200 text-red-800 p-4 rounded-lg flex items-start gap-3"><XCircle className="h-5 w-5 mt-0.5 shrink-0" /><div><h4 className="font-semibold text-sm">Error</h4><p className="text-sm mt-1">Could not find application with reference number {id}. Please check the number and try again.</p></div></div>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <Link to="/citizen/applications" className="inline-flex items-center text-sm text-primary hover:underline">
        <ArrowLeft className="w-4 h-4 mr-1" /> Back to Tracker
      </Link>

      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Application Details
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Reference Number: <span className="font-mono font-medium">{application.applicationNumber}</span>
          </p>
        </div>
        <Badge className={getStatusColor(application.status)}>
          {application.status.replace(/_/g, ' ')}
        </Badge>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="w-5 h-5 text-primary" />
              Application Information
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <p className="text-sm font-medium text-slate-500">Service Code</p>
                <p className="font-mono mt-1">{application.serviceCode}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-slate-500">Submission Date</p>
                <p className="mt-1">{new Date(application.submittedAt).toLocaleDateString()}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-slate-500">Applicant ID</p>
                <p className="font-mono mt-1">{application.citizenId}</p>
              </div>
            </div>

            {!!application.verificationData && (
              <div className="mt-6 border-t pt-4">
                <h3 className="text-sm font-bold text-slate-900 mb-2">Verification Data</h3>
                <pre className="bg-slate-50 p-4 rounded-md text-xs overflow-x-auto border border-slate-200">
                  {JSON.stringify(application.verificationData, null, 2)}
                </pre>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="w-5 h-5 text-primary" />
              Timeline
            </CardTitle>
          </CardHeader>
          <CardContent>
            {loadingTimeline ? (
              <div className="flex justify-center p-4">
                <Loader2 className="w-6 h-6 animate-spin text-primary" />
              </div>
            ) : timeline && timeline.length > 0 ? (
              <div className="space-y-6 relative before:absolute before:inset-0 before:ml-2.5 before:w-0.5 before:bg-slate-200">
                {timeline.map((event, index) => (
                  <div key={index} className="relative flex items-start group">
                    <div className="flex items-center justify-center w-5 h-5 rounded-full border-2 border-white bg-primary text-white shrink-0 z-10 shadow mt-0.5" />
                    <div className="ml-4">
                      <h4 className="font-bold text-slate-900 text-sm">{event.eventType.replace(/_/g, ' ')}</h4>
                      <time className="text-xs text-slate-500 mt-0.5 block">{new Date(event.occurredAt).toLocaleString()}</time>
                      <p className="text-sm text-slate-600 mt-1">{event.description}</p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-slate-500 text-center py-4">No timeline events available.</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
