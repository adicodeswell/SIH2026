import React from 'react';
import { Link } from 'react-router-dom';
import { Card, CardContent, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { CheckCircle2, ArrowRight, ShieldCheck, FileText, ArrowLeft } from 'lucide-react';
import type { ApplicationResponse, ServiceResponse } from '@/types/service';

interface ApplicationSuccessProps {
  application: ApplicationResponse;
  service?: ServiceResponse;
  onApplyAnother?: () => void;
}

export const ApplicationSuccess: React.FC<ApplicationSuccessProps> = ({
  application,
  service,
  onApplyAnother,
}) => {
  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <Card className="border-emerald-200 shadow-md bg-white overflow-hidden">
        <div className="bg-emerald-600 text-white p-6 text-center space-y-2">
          <div className="w-14 h-14 bg-white/10 rounded-full flex items-center justify-center mx-auto mb-2 border border-white/20">
            <CheckCircle2 className="w-8 h-8 text-white" />
          </div>
          <CardTitle className="text-2xl font-bold text-white tracking-tight">
            Application Submitted Successfully
          </CardTitle>
          <CardDescription className="text-emerald-100 text-sm">
            Your application has been registered with the Government of Maharashtra service gateway.
          </CardDescription>
        </div>

        <CardContent className="p-6 space-y-6">
          {/* Reference Block */}
          <div className="bg-slate-50 border border-slate-200 rounded-xl p-5 text-center space-y-1">
            <p className="text-xs uppercase tracking-wider text-slate-500 font-semibold">
              Official Application Reference Number
            </p>
            <p className="text-2xl font-extrabold font-mono text-primary select-all">
              {application.applicationNumber}
            </p>
            <div className="pt-2 flex justify-center">
              <Badge variant="outline" className="bg-emerald-50 text-emerald-800 border-emerald-300 font-medium">
                Status: {application.status}
              </Badge>
            </div>
          </div>

          {/* Details Grid */}
          <div className="space-y-3 text-xs text-slate-700 bg-white border border-slate-200 rounded-lg p-4">
            <div className="flex justify-between items-center py-1.5 border-b border-slate-100">
              <span className="text-slate-500 font-medium">Scheme</span>
              <span className="font-semibold text-slate-900">{service?.serviceName || application.serviceCode}</span>
            </div>
            <div className="flex justify-between items-center py-1.5 border-b border-slate-100">
              <span className="text-slate-500 font-medium">Service Code</span>
              <span className="font-mono text-slate-900">{application.serviceCode}</span>
            </div>
            <div className="flex justify-between items-center py-1.5 border-b border-slate-100">
              <span className="text-slate-500 font-medium">Citizen ID</span>
              <span className="font-mono text-slate-900">{application.citizenId}</span>
            </div>
            <div className="flex justify-between items-center py-1.5">
              <span className="text-slate-500 font-medium">Submission Timestamp</span>
              <span className="text-slate-900">
                {application.submittedAt ? new Date(application.submittedAt).toLocaleString() : 'Just now'}
              </span>
            </div>
          </div>

          {/* Next Steps Notification */}
          <div className="flex items-start gap-3 bg-blue-50/70 p-4 rounded-lg border border-blue-200 text-xs text-blue-900">
            <ShieldCheck className="w-5 h-5 text-blue-700 shrink-0 mt-0.5" />
            <div className="space-y-1">
              <p className="font-semibold">Automated Interoperability Workflow Triggered</p>
              <p className="text-blue-800 leading-relaxed">
                The Camunda BPMN engine is now orchestrating verified record lookups across department databases based on your explicit consent. An officer will review your application once verification concludes.
              </p>
            </div>
          </div>

          {/* Action Links */}
          <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-2 border-t border-slate-100">
            {onApplyAnother && (
              <Button variant="outline" onClick={onApplyAnother} className="w-full sm:w-auto gap-2">
                <ArrowLeft className="w-4 h-4" /> Apply for Another Service
              </Button>
            )}
            <Link to="/citizen/applications" className="w-full sm:w-auto">
              <Button className="w-full sm:w-auto gap-2 font-medium">
                <FileText className="w-4 h-4" /> View My Applications <ArrowRight className="w-4 h-4" />
              </Button>
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
