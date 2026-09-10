import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { serviceCatalogApi } from '@/services/serviceCatalog';
import { useAuth } from '@/context/AuthContext';
import { ApplicationForm } from '@/components/citizen/ApplicationForm';
import { ApplicationSuccess } from '@/components/citizen/ApplicationSuccess';
import { PageLoader } from '@/components/feedback/Loading';
import { ErrorState, NotFound } from '@/components/feedback/States';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { notify } from '@/lib/toast';
import {
  Building2,
  ArrowLeft,
  FileCheck,
  Clock,
  Sparkles,
  ArrowRight,
  Database,
  Lock,
} from 'lucide-react';
import type { ApplicationResponse } from '@/types/service';

export const ServiceDetailPage: React.FC = () => {
  const { serviceId } = useParams<{ serviceId: string }>();
  const { user } = useAuth();
  const citizenId = user?.username || '';

  const [applyMode, setApplyMode] = useState(false);
  const [createdApplication, setCreatedApplication] = useState<ApplicationResponse | null>(null);

  // Fetch service details
  const {
    data: service,
    isLoading,
    isError,
    error,
    refetch,
  } = useQuery({
    queryKey: ['service', serviceId],
    queryFn: () => serviceCatalogApi.getServiceById(serviceId!),
    enabled: !!serviceId,
  });

  // Application creation mutation
  const applicationMutation = useMutation({
    mutationFn: async (payload: { citizenId: string; serviceCode: string }) => {
      // 1. Create application in DRAFT state
      const application = await serviceCatalogApi.createApplication({
        citizenId: payload.citizenId,
        serviceCode: payload.serviceCode,
      });

      // 2. Grant cryptographic consent bound to the application
      await serviceCatalogApi.grantConsent({
        applicationId: application.applicationNumber,
        serviceCode: payload.serviceCode,
        dataScope:
          payload.serviceCode === 'SCHOLARSHIP'
            ? 'education'
            : payload.serviceCode === 'SRV-EDU'
            ? 'education,health'
            : 'education,employment,skills',
        purpose:
          payload.serviceCode === 'SCHOLARSHIP' ? 'scholarship_verification' : 'verification',
        requestingDepartmentId: service?.departmentCode || 'DEPT-SKILLS',
      });

      // 3. Submit/activate application to start workflow
      return await serviceCatalogApi.submitApplication(application.applicationNumber);
    },
    onSuccess: (data) => {
      notify.success('Application Submitted Successfully', `Reference: ${data.applicationNumber}`);
      setCreatedApplication(data);
    },
    onError: (err: any) => {
      // Handled through normalizeApiError in lib/api
      if (err.status === 409) {
        notify.error(
          'Application Conflict',
          'An active application already exists for this scheme or state.'
        );
      } else {
        notify.error('Submission Failed', err.message || 'Unable to submit application.');
      }
    },
  });

  if (isLoading) {
    return <PageLoader message="Loading service information..." subtext="Retrieving scheme criteria from state catalog" />;
  }

  if (isError) {
    const status = (error as any)?.status;
    if (status === 404) {
      return (
        <NotFound
          title="Service Not Found"
          description={`The service scheme code "${serviceId}" could not be located in the catalog.`}
          actionHref="/citizen/services"
          actionText="Browse Available Services"
        />
      );
    }
    return (
      <ErrorState
        title="Unable to load service details"
        message={(error as any)?.message || 'Failed to fetch details for this government service.'}
        onRetry={() => refetch()}
      />
    );
  }

  if (!service) {
    return (
      <NotFound
        title="Service Not Found"
        description="The requested service does not exist or has been retired."
        actionHref="/citizen/services"
        actionText="Return to Services"
      />
    );
  }

  // View: Success Screen
  if (createdApplication) {
    return (
      <ApplicationSuccess
        application={createdApplication}
        service={service}
        onApplyAnother={() => {
          setCreatedApplication(null);
          setApplyMode(false);
        }}
      />
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Navigation Breadcrumb */}
      <div className="flex items-center justify-between">
        <Link to="/citizen/services">
          <Button variant="ghost" size="sm" className="gap-2 -ml-2 text-slate-600 hover:text-slate-900">
            <ArrowLeft className="w-4 h-4" /> Back to Services Directory
          </Button>
        </Link>
        <Badge variant="outline" className="font-mono bg-blue-50 text-blue-800 border-blue-200">
          {service.serviceCode}
        </Badge>
      </div>

      {/* Hero Service Overview Card */}
      <Card className="border-slate-200 shadow-sm bg-white overflow-hidden">
        <CardHeader className="border-b border-slate-100 bg-slate-50/50 pb-5">
          <div className="space-y-1.5">
            <div className="flex items-center gap-2 text-xs font-semibold text-slate-500 uppercase tracking-wider">
              <Building2 className="w-3.5 h-3.5" />
              <span>{service.departmentName || service.departmentCode || 'Government Department'}</span>
            </div>
            <CardTitle className="text-2xl font-bold text-slate-900 tracking-tight">
              {service.serviceName}
            </CardTitle>
            <CardDescription className="text-sm text-slate-600 mt-2 leading-relaxed">
              {service.description}
            </CardDescription>
          </div>
        </CardHeader>
        <CardContent className="p-6 space-y-6">
          {/* Key Scheme Pillars */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="bg-slate-50 border border-slate-200 rounded-lg p-3.5 space-y-1">
              <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-700">
                <FileCheck className="w-4 h-4 text-emerald-600" />
                <span>Zero Physical Scans</span>
              </div>
              <p className="text-[11px] text-slate-500">
                No document uploads needed. Academic and identity records are queried via state APIs.
              </p>
            </div>

            <div className="bg-slate-50 border border-slate-200 rounded-lg p-3.5 space-y-1">
              <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-700">
                <Lock className="w-4 h-4 text-blue-600" />
                <span>Explicit DPDP Consent</span>
              </div>
              <p className="text-[11px] text-slate-500">
                Cryptographically protected consent is recorded before any data retrieval occurs.
              </p>
            </div>

            <div className="bg-slate-50 border border-slate-200 rounded-lg p-3.5 space-y-1">
              <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-700">
                <Clock className="w-4 h-4 text-amber-600" />
                <span>BPMN Orchestration</span>
              </div>
              <p className="text-[11px] text-slate-500">
                Live automated status tracking through the Camunda workflow engine.
              </p>
            </div>
          </div>

          {/* Application Mode Toggle */}
          {!applyMode ? (
            <div className="pt-2 border-t border-slate-100 flex flex-col sm:flex-row items-center justify-between gap-4">
              <div className="text-xs text-slate-500 flex items-center gap-1.5">
                <Sparkles className="w-4 h-4 text-amber-600 shrink-0" />
                <span>Ensure your registered citizen ID matches your state records before applying.</span>
              </div>
              <Button
                onClick={() => setApplyMode(true)}
                size="lg"
                className="w-full sm:w-auto h-11 px-6 font-semibold gap-2 shadow-sm text-base"
              >
                Apply for this Service <ArrowRight className="w-4 h-4" />
              </Button>
            </div>
          ) : (
            <div className="pt-4 border-t border-slate-100 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                  <Database className="w-4 h-4 text-primary" /> Application Submission
                </h3>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setApplyMode(false)}
                  className="text-xs text-slate-500"
                >
                  Hide Application Form
                </Button>
              </div>

              <ApplicationForm
                service={service}
                citizenId={citizenId}
                isSubmitting={applicationMutation.isPending}
                onSubmit={(formData) => {
                  applicationMutation.mutate({
                    citizenId: formData.citizenId,
                    serviceCode: formData.serviceCode,
                  });
                }}
                onCancel={() => setApplyMode(false)}
              />
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
