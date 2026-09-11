import React from 'react';
import { CheckCircle2, Clock, ShieldCheck, ArrowRight, Server, UserCheck, FileCheck } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface WorkflowStep {
  id: string;
  title: string;
  department: string;
  status: 'completed' | 'active' | 'upcoming' | 'rejected';
  description?: string;
  timestamp?: string;
}

interface WorkflowProcessVisualizerProps {
  currentStatus?: string;
  className?: string;
}

export const WorkflowProcessVisualizer: React.FC<WorkflowProcessVisualizerProps> = ({
  currentStatus = 'SUBMITTED',
  className,
}) => {
  // Compute stage based on actual application status
  const getStepStatus = (stepIndex: number): 'completed' | 'active' | 'upcoming' | 'rejected' => {
    if (currentStatus === 'REJECTED' || currentStatus === 'FAILED' || currentStatus === 'CONSENT_DENIED') {
      if (stepIndex === 3) return 'rejected';
      if (stepIndex < 3) return 'completed';
      return 'upcoming';
    }
    if (currentStatus === 'APPROVED' || currentStatus === 'COMPLETED') {
      return 'completed';
    }
    if (currentStatus === 'PENDING_OFFICER_REVIEW' || currentStatus === 'PENDING_REVIEW') {
      if (stepIndex < 3) return 'completed';
      if (stepIndex === 3) return 'active';
      return 'upcoming';
    }
    if (currentStatus === 'SUBMITTED' || currentStatus === 'IN_PROGRESS' || currentStatus === 'PENDING_VERIFICATION') {
      if (stepIndex < 2) return 'completed';
      if (stepIndex === 2) return 'active';
      return 'upcoming';
    }
    // DRAFT
    if (stepIndex === 0) return 'active';
    return 'upcoming';
  };

  const steps: { label: string; sub: string; icon: React.ReactNode }[] = [
    {
      label: '1. Citizen Consent',
      sub: 'DPDP Cryptographic Authorization',
      icon: <ShieldCheck className="w-4 h-4" />,
    },
    {
      label: '2. Registry Verification',
      sub: 'Canonical Education/State APIs',
      icon: <Server className="w-4 h-4" />,
    },
    {
      label: '3. Workflow Engine',
      sub: 'BPMN Human Task Routing',
      icon: <Clock className="w-4 h-4" />,
    },
    {
      label: '4. Officer Review',
      sub: 'Competent Authority Evaluation',
      icon: <UserCheck className="w-4 h-4" />,
    },
    {
      label: '5. Sanction / Order',
      sub: 'Official Digital Resolution',
      icon: <FileCheck className="w-4 h-4" />,
    },
  ];

  return (
    <div className={cn('bg-white border border-slate-200 rounded-md p-5 shadow-xs', className)}>
      <div className="flex items-center justify-between border-b border-slate-100 pb-3 mb-4">
        <div>
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-800 flex items-center gap-1.5">
            <Clock className="w-3.5 h-3.5 text-[#0B1F3A]" />
            <span>Automated Interoperability Workflow Pipeline</span>
          </h4>
          <p className="text-[11px] text-slate-500 mt-0.5">
            State governance process tracking executed under the DPDP Act 2023 zero-document verification mandate
          </p>
        </div>
        <div className="hidden sm:flex items-center gap-1 text-[11px] bg-slate-100 text-slate-700 px-2 py-0.5 rounded font-mono">
          <span>Camunda BPMN 7.20</span>
        </div>
      </div>

      {/* Responsive Process Flow Steps */}
      <div className="grid grid-cols-1 md:grid-cols-5 gap-3 relative">
        {steps.map((step, idx) => {
          const status = getStepStatus(idx);

          return (
            <div
              key={idx}
              className={cn(
                'relative p-3 rounded-sm border transition-all flex flex-col justify-between text-left',
                status === 'completed' && 'bg-emerald-50/50 border-emerald-300 text-emerald-950',
                status === 'active' && 'bg-blue-50/70 border-blue-400 text-blue-950 ring-1 ring-blue-300',
                status === 'rejected' && 'bg-red-50/60 border-red-300 text-red-950',
                status === 'upcoming' && 'bg-slate-50/60 border-slate-200 text-slate-500 opacity-80'
              )}
            >
              {/* Step Header */}
              <div className="flex items-center justify-between mb-2">
                <span
                  className={cn(
                    'w-6 h-6 rounded-xs flex items-center justify-center text-xs font-bold shrink-0',
                    status === 'completed' && 'bg-emerald-600 text-white',
                    status === 'active' && 'bg-[#0B1F3A] text-white',
                    status === 'rejected' && 'bg-red-600 text-white',
                    status === 'upcoming' && 'bg-slate-200 text-slate-600'
                  )}
                >
                  {status === 'completed' ? <CheckCircle2 className="w-4 h-4" /> : idx + 1}
                </span>

                <span className="text-[10px] uppercase font-bold tracking-wider">
                  {status === 'completed' && <span className="text-emerald-700">Verified</span>}
                  {status === 'active' && <span className="text-blue-700 animate-pulse font-extrabold">In Progress</span>}
                  {status === 'rejected' && <span className="text-red-700">Rejected</span>}
                  {status === 'upcoming' && <span className="text-slate-400">Queued</span>}
                </span>
              </div>

              {/* Step Details */}
              <div>
                <p className="text-xs font-bold tracking-tight text-slate-900 leading-snug">
                  {step.label}
                </p>
                <p className="text-[11px] text-slate-500 mt-0.5 leading-tight">
                  {step.sub}
                </p>
              </div>

              {/* Arrow indicator for desktop layout */}
              {idx < steps.length - 1 && (
                <div
                  className="hidden md:block absolute -right-2.5 top-1/2 -translate-y-1/2 z-10 bg-white rounded-full p-0.5 border border-slate-200 text-slate-400"
                  aria-hidden="true"
                >
                  <ArrowRight className="w-3 h-3" />
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};
