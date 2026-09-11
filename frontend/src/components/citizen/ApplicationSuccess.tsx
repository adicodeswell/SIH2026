import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Card, CardContent, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { CheckCircle2, ArrowRight, ShieldCheck, FileText, ArrowLeft, Copy, Check } from 'lucide-react';
import type { ApplicationResponse, ServiceResponse } from '@/types/service';
import { toast } from 'sonner';

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
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(application.applicationNumber);
    setCopied(true);
    toast.success('Application reference number copied to clipboard');
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="max-w-2xl mx-auto space-y-5">
      <Card className="border-slate-200 shadow-xs bg-white rounded-md overflow-hidden">
        {/* Official Header Banner */}
        <div className="bg-[#0B1F3A] text-white p-6 text-center space-y-2 border-b-4 border-[#138808]">
          <div className="w-12 h-12 bg-white/10 rounded-full flex items-center justify-center mx-auto mb-1 border border-white/20">
            <CheckCircle2 className="w-7 h-7 text-emerald-400" />
          </div>
          <CardTitle className="text-xl font-bold text-white tracking-tight">
            Official Application Acknowledgement Receipt
          </CardTitle>
          <CardDescription className="text-slate-300 text-xs">
            Government Interoperability Platform • Ekikrit Service Gateway
          </CardDescription>
        </div>

        <CardContent className="p-6 space-y-6">
          {/* Reference Number Box */}
          <div className="bg-slate-50 border border-slate-200 rounded-sm p-4 text-center space-y-1 relative">
            <p className="text-[11px] uppercase tracking-wider text-slate-500 font-bold">
              Application Dossier Reference Identifier
            </p>
            <div className="flex items-center justify-center gap-2">
              <p className="text-2xl font-black font-mono text-[#0B1F3A] tracking-wider select-all">
                {application.applicationNumber}
              </p>
              <button
                type="button"
                onClick={handleCopy}
                className="p-1 rounded text-slate-400 hover:text-slate-700 hover:bg-slate-200 transition-colors"
                title="Copy reference number"
                aria-label="Copy reference number"
              >
                {copied ? <Check className="w-4 h-4 text-emerald-600" /> : <Copy className="w-4 h-4" />}
              </button>
            </div>
            <div className="pt-2 flex justify-center">
              <Badge variant="active" className="text-xs font-semibold px-2.5 py-0.5">
                Status: {application.status}
              </Badge>
            </div>
          </div>

          {/* Acknowledgement Ledger Breakdown */}
          <div className="space-y-2.5 text-xs text-slate-700 bg-white border border-slate-200 rounded-sm p-4">
            <div className="flex justify-between items-center py-1.5 border-b border-slate-100">
              <span className="text-slate-500 font-medium">Applied Scheme:</span>
              <span className="font-bold text-slate-900">{service?.serviceName || application.serviceCode}</span>
            </div>
            <div className="flex justify-between items-center py-1.5 border-b border-slate-100">
              <span className="text-slate-500 font-medium">Service Scheme Code:</span>
              <span className="font-mono text-slate-900 font-semibold">{application.serviceCode}</span>
            </div>
            <div className="flex justify-between items-center py-1.5 border-b border-slate-100">
              <span className="text-slate-500 font-medium">Citizen Identifier:</span>
              <span className="font-mono text-slate-900 font-semibold">{application.citizenId}</span>
            </div>
            <div className="flex justify-between items-center py-1.5">
              <span className="text-slate-500 font-medium">Filing Timestamp:</span>
              <span className="text-slate-900 font-medium">
                {application.submittedAt ? new Date(application.submittedAt).toLocaleString() : 'Just now'}
              </span>
            </div>
          </div>

          {/* Institutional Next Steps Notification */}
          <div className="flex items-start gap-3 bg-blue-50/80 p-4 rounded-sm border border-blue-200 text-xs text-blue-950">
            <ShieldCheck className="w-5 h-5 text-[#0B1F3A] shrink-0 mt-0.5" />
            <div className="space-y-1">
              <p className="font-bold text-slate-900">Zero-Document Interoperability Workflow Triggered</p>
              <p className="text-slate-600 leading-relaxed">
                The Camunda BPMN workflow engine has initiated automated query requests against canonical department databases under your DPDP Act 2023 cryptographic consent. An assigned department review officer will evaluate the verified records.
              </p>
            </div>
          </div>

          {/* Action Navigation */}
          <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-2 border-t border-slate-100">
            {onApplyAnother && (
              <Button
                variant="outline"
                onClick={onApplyAnother}
                className="w-full sm:w-auto text-xs h-9 gap-1.5 font-semibold"
              >
                <ArrowLeft className="w-3.5 h-3.5" /> Apply for Another Scheme
              </Button>
            )}
            <Link to="/citizen/applications" className="w-full sm:w-auto">
              <Button className="w-full sm:w-auto text-xs h-9 gap-1.5 font-bold bg-[#0B1F3A] hover:bg-[#102A43] text-white">
                <FileText className="w-3.5 h-3.5" /> View My Applications <ArrowRight className="w-3.5 h-3.5" />
              </Button>
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
