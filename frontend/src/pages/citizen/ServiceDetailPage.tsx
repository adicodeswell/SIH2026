import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
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
import { Breadcrumb } from '@/components/ui/Breadcrumb';
import { WorkflowProcessVisualizer } from '@/components/ui/WorkflowProcessVisualizer';
import { notify } from '@/lib/toast';
import {
  Building2,
  FileCheck,
  Clock,
  Sparkles,
  ArrowRight,
  Database,
  Lock,
  ChevronUp,
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
      const result = await serviceCatalogApi.submitApplication(application.applicationNumber);
      return result;
    },
    onSuccess: (data) => {
      notify.success('Application Submitted Successfully', `Reference: ${data.applicationNumber}`);
      setCreatedApplication(data);
    },
    onError: (err: any) => {
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
    return (
      <PageLoader
        message="Loading government scheme dossier..."
        subtext="Retrieving verified eligibility parameters from state catalog"
      />
    );
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
        title="Unable to load scheme details"
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
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Breadcrumb Navigation */}
      <Breadcrumb
        items={[
          { label: 'Citizen Services', href: '/citizen' },
          { label: 'Schemes Directory', href: '/citizen/services' },
          { label: service.serviceName },
        ]}
      />

      {/* Scheme Dossier Hero Card */}
      <Card className="border-slate-200 shadow-xs bg-white rounded-md overflow-hidden">
        <CardHeader className="border-b border-slate-100 bg-slate-50/70 p-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="space-y-1">
              <div className="flex items-center gap-2 text-xs font-semibold text-slate-600">
                <Building2 className="w-3.5 h-3.5 text-[#0B1F3A]" />
                <span>{service.departmentName || service.departmentCode || 'Government Department'}</span>
              </div>
              <CardTitle className="text-2xl font-bold text-slate-900 tracking-tight">
                {service.serviceName}
              </CardTitle>
            </div>
            <div className="flex items-center gap-2 shrink-0">
              <Badge variant="service" className="text-xs px-2.5 py-1">
                Scheme Code: {service.serviceCode}
              </Badge>
            </div>
          </div>
          <CardDescription className="text-sm text-slate-600 mt-2 leading-relaxed">
            {service.description}
          </CardDescription>
        </CardHeader>

        <CardContent className="p-6 space-y-6">
          {/* Key Scheme Pillars Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="bg-slate-50 border border-slate-200 rounded-sm p-4 space-y-1">
              <div className="flex items-center gap-1.5 text-xs font-bold text-slate-800">
                <FileCheck className="w-4 h-4 text-emerald-700" />
                <span>Zero Physical Scans</span>
              </div>
              <p className="text-[11px] text-slate-500 leading-relaxed">
                No paper uploads. Academic, caste, or income records are queried via canonical state APIs.
              </p>
            </div>

            <div className="bg-slate-50 border border-slate-200 rounded-sm p-4 space-y-1">
              <div className="flex items-center gap-1.5 text-xs font-bold text-slate-800">
                <Lock className="w-4 h-4 text-blue-700" />
                <span>Explicit DPDP Consent</span>
              </div>
              <p className="text-[11px] text-slate-500 leading-relaxed">
                Cryptographically protected consent token recorded before any data retrieval occurs.
              </p>
            </div>

            <div className="bg-slate-50 border border-slate-200 rounded-sm p-4 space-y-1">
              <div className="flex items-center gap-1.5 text-xs font-bold text-slate-800">
                <Clock className="w-4 h-4 text-amber-700" />
                <span>BPMN Workflow</span>
              </div>
              <p className="text-[11px] text-slate-500 leading-relaxed">
                Automated multi-department orchestration tracked via Camunda human-task engine.
              </p>
            </div>
          </div>

          {/* Workflow Process Flow Visualization */}
          <WorkflowProcessVisualizer currentStatus="DRAFT" />

          {/* Application Action Toggle */}
          {!applyMode ? (
            <div className="pt-4 border-t border-slate-100 flex flex-col sm:flex-row items-center justify-between gap-4">
              <div className="text-xs text-slate-500 flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-[#F97316] shrink-0" />
                <span>Your registered identifier is auto-linked to ensure zero-document processing.</span>
              </div>
              <Button
                onClick={() => setApplyMode(true)}
                className="w-full sm:w-auto h-10 px-6 font-bold text-xs bg-[#0B1F3A] hover:bg-[#102A43] text-white gap-2 shadow-xs"
              >
                Apply Online for this Scheme <ArrowRight className="w-4 h-4" />
              </Button>
            </div>
          ) : (
            <div className="pt-4 border-t border-slate-100 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
                  <Database className="w-4 h-4 text-[#0B1F3A]" />
                  <span>Online Application Submission</span>
                </h3>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setApplyMode(false)}
                  className="text-xs text-slate-600 gap-1"
                >
                  Hide Form <ChevronUp className="w-3.5 h-3.5" />
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
