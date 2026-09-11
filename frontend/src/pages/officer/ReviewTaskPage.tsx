import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { officerServiceApi } from '@/services/officerService';
import { applicationServiceApi } from '@/services/applicationService';
import { useAuth } from '@/context/AuthContext';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Breadcrumb } from '@/components/ui/Breadcrumb';
import { WorkflowProcessVisualizer } from '@/components/ui/WorkflowProcessVisualizer';
import {
  Loader2,
  ArrowLeft,
  CheckCircle,
  XCircle,
  FileText,
  User,
  ShieldCheck,
  AlertCircle,
  Building2,
} from 'lucide-react';
import { toast } from 'sonner';

export function OfficerReviewTaskPage() {
  const { taskId } = useParams<{ taskId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const queryClient = useQueryClient();

  const [decision, setDecision] = useState<'APPROVE' | 'REJECT' | null>(null);
  const [reason, setReason] = useState('');

  // Fetch Task Metadata
  const { data: task, isLoading: loadingTask, error: taskError } = useQuery({
    queryKey: ['officerTask', taskId],
    queryFn: () => officerServiceApi.getReviewTask(taskId!),
    enabled: !!taskId,
  });

  // Fetch Application Details (for verificationData)
  const { data: application, isLoading: loadingApp } = useQuery({
    queryKey: ['application', task?.applicationId],
    queryFn: () => applicationServiceApi.getApplicationById(task!.applicationId),
    enabled: !!task?.applicationId,
  });

  // Mutations
  const claimMutation = useMutation({
    mutationFn: () => officerServiceApi.claimTask(taskId!),
    onSuccess: () => {
      toast.success('Task claimed successfully');
      queryClient.invalidateQueries({ queryKey: ['officerTask', taskId] });
      queryClient.invalidateQueries({ queryKey: ['officerTasks'] });
    },
    onError: (err: any) => toast.error(err?.message || 'Failed to claim task'),
  });

  const unclaimMutation = useMutation({
    mutationFn: () => officerServiceApi.unclaimTask(taskId!),
    onSuccess: () => {
      toast.success('Task unclaimed successfully');
      queryClient.invalidateQueries({ queryKey: ['officerTask', taskId] });
      queryClient.invalidateQueries({ queryKey: ['officerTasks'] });
    },
    onError: (err: any) => toast.error(err?.message || 'Failed to unclaim task'),
  });

  const decisionMutation = useMutation({
    mutationFn: () => officerServiceApi.submitDecision(taskId!, { decision: decision!, reason }),
    onSuccess: () => {
      toast.success(`Application ${decision === 'APPROVE' ? 'Approved' : 'Rejected'} Successfully`);
      queryClient.invalidateQueries({ queryKey: ['officerTasks'] });
      navigate('/officer');
    },
    onError: (err: any) => toast.error(err?.message || 'Failed to submit decision'),
  });

  const isClaimedByMe = task?.assignee === user?.username?.toUpperCase();
  const isClaimedByOther = task?.assignee && !isClaimedByMe;

  const handleSubmitDecision = () => {
    if (!decision) {
      toast.error('Please select a statutory decision (Approve or Reject).');
      return;
    }
    if (decision === 'REJECT' && !reason.trim()) {
      toast.error('A statutory justification is mandatory for rejecting an application.');
      return;
    }
    decisionMutation.mutate();
  };

  if (loadingTask || loadingApp) {
    return (
      <div className="flex flex-col items-center justify-center p-16 space-y-3 bg-white border border-slate-200 rounded-md">
        <Loader2 className="w-8 h-8 animate-spin text-[#0B1F3A]" />
        <p className="text-xs font-semibold text-slate-700">Loading statutory review file dossier...</p>
      </div>
    );
  }

  if (taskError || !task) {
    return (
      <div className="space-y-4">
        <Link to="/officer" className="inline-flex items-center text-xs text-[#0B1F3A] hover:underline font-semibold">
          <ArrowLeft className="w-3.5 h-3.5 mr-1" /> Back to Review Queue
        </Link>
        <div className="bg-red-50 border border-red-200 text-red-900 p-6 rounded-md flex items-start gap-3">
          <AlertCircle className="h-5 w-5 mt-0.5 shrink-0 text-red-600" />
          <div>
            <h4 className="font-bold text-sm">Review Task Unavailable</h4>
            <p className="text-xs mt-1 text-red-800">
              Review task was not found or your officer credentials do not hold permissions for this candidate group.
            </p>
          </div>
        </div>
      </div>
    );
  }

  const parseVerificationData = (data: any) => {
    if (!data) return null;
    try {
      if (typeof data === 'string') return JSON.parse(data);
      return data;
    } catch {
      return data;
    }
  };

  const rawVerification = task?.verificationData ?? application?.verificationData;
  const parsedVerification = parseVerificationData(rawVerification);

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Breadcrumbs */}
      <Breadcrumb
        items={[
          { label: 'Officer Review Console', href: '/officer' },
          { label: 'Review Queue', href: '/officer' },
          { label: task.applicationId },
        ]}
      />

      {/* Official Dossier Header Banner */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 bg-white p-6 rounded-md border border-slate-200 shadow-xs">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="bg-[#0B1F3A] text-white text-[10px] font-bold uppercase px-2 py-0.5 rounded-xs tracking-wider">
              Human-Task Evaluation
            </span>
            <span className="text-xs text-slate-500 font-mono">
              App ID: <strong className="text-slate-900">{task.applicationId}</strong>
            </span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Dossier Review: {task.applicationId}
          </h1>
          <p className="text-xs text-slate-500 flex items-center gap-2">
            <span>Scheme: <strong className="text-slate-800">{task.serviceCode}</strong></span>
            <span>•</span>
            <span>Stage: <strong className="text-slate-800">{task.taskName}</strong></span>
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          {isClaimedByOther && (
            <Badge variant="pending" className="text-xs">
              Claimed by {task.assignee}
            </Badge>
          )}
          {isClaimedByMe && (
            <Badge variant="active" className="text-xs">
              Claimed by You
            </Badge>
          )}
          {!task.assignee && (
            <Badge variant="outline" className="text-xs">
              Unclaimed Queue Task
            </Badge>
          )}
        </div>
      </div>

      {/* Workflow Process Visualizer */}
      <WorkflowProcessVisualizer currentStatus="PENDING_OFFICER_REVIEW" />

      {/* Two Column Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Applicant Info & Interoperability Data */}
        <div className="lg:col-span-2 space-y-6">
          <Card className="rounded-md border-slate-200 shadow-xs bg-white">
            <CardHeader className="pb-3 border-b border-slate-100 bg-slate-50/70">
              <CardTitle className="text-sm font-bold text-slate-900 flex items-center justify-between">
                <span className="flex items-center gap-2">
                  <User className="w-4 h-4 text-[#0B1F3A]" />
                  Applicant Canonical Identity
                </span>
                <span className="text-xs font-mono text-slate-500 font-normal">
                  Citizen ID: {task.citizenId}
                </span>
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-4">
              <div className="grid grid-cols-2 gap-4 text-xs">
                <div>
                  <span className="text-slate-500 font-medium">Filing Timestamp</span>
                  <p className="mt-1 font-semibold text-slate-900">
                    {new Date(task.createTime).toLocaleString()}
                  </p>
                </div>
                <div>
                  <span className="text-slate-500 font-medium">BPMN Workflow Task</span>
                  <p className="mt-1 font-semibold text-slate-900">{task.taskName}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Verification Results Dossier */}
          <Card className="rounded-md border-slate-200 shadow-xs bg-white">
            <CardHeader className="pb-3 border-b border-slate-100 bg-slate-50/70">
              <CardTitle className="flex items-center gap-2 text-sm font-bold text-slate-900">
                <ShieldCheck className="w-4 h-4 text-emerald-700" />
                <span>Interoperability Verification Inspection</span>
              </CardTitle>
              <CardDescription className="text-xs">
                Verified records pulled from canonical state department databases under DPDP consent
              </CardDescription>
            </CardHeader>
            <CardContent className="pt-4 space-y-4">
              {parsedVerification ? (
                <div className="space-y-4">
                  <div className="flex items-start text-emerald-900 bg-emerald-50/80 p-3.5 rounded-sm border border-emerald-300 text-xs">
                    <CheckCircle className="w-4 h-4 mr-2.5 text-emerald-700 shrink-0 mt-0.5" />
                    <div>
                      <p className="font-bold mb-0.5">Authoritative State System Verified</p>
                      <p className="text-emerald-800 leading-relaxed">
                        This payload was retrieved directly from state registers (Education Board / Revenue / Employment).
                        No physical scans or unverified user uploads were utilized.
                      </p>
                    </div>
                  </div>

                  {/* Canonical Profile Data Display */}
                  <div className="bg-[#0B1F3A] rounded-sm overflow-hidden border border-slate-800 text-xs">
                    <div className="bg-[#102A43] px-3.5 py-2 border-b border-slate-700 flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <FileText className="w-3.5 h-3.5 text-slate-300" />
                        <span className="font-bold text-slate-200 uppercase tracking-wider text-[11px]">
                          Canonical Verification JSON Payload
                        </span>
                      </div>
                      <span className="text-[10px] text-emerald-400 font-mono">CRYPTOGRAPHICALLY SIGNED</span>
                    </div>
                    <div className="p-4 overflow-x-auto max-h-96">
                      <pre className="font-mono text-emerald-400 text-xs leading-relaxed">
                        {JSON.stringify(parsedVerification, null, 2)}
                      </pre>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="p-8 text-center text-amber-900 bg-amber-50 rounded-sm border border-amber-200">
                  <AlertCircle className="w-6 h-6 mx-auto mb-2 text-amber-600" />
                  <p className="font-bold text-sm">No verification payload available</p>
                  <p className="text-xs mt-1 text-amber-800">
                    The workflow may not have populated verification data or the interoperability query is pending.
                  </p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Right Column: Officer Statutory Decision Panel */}
        <div className="lg:col-span-1">
          <Card className="sticky top-20 rounded-md border-slate-200 shadow-xs bg-white">
            <CardHeader className="border-b border-slate-100 bg-slate-50/70 pb-3">
              <CardTitle className="text-sm font-bold text-slate-900 flex items-center gap-1.5">
                <Building2 className="w-4 h-4 text-[#0B1F3A]" />
                <span>Statutory Authority Actions</span>
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-4 space-y-4 text-xs">
              {!isClaimedByMe ? (
                <div className="space-y-3">
                  <p className="text-slate-600 leading-relaxed">
                    {isClaimedByOther
                      ? `This dossier is currently claimed by officer "${task.assignee}". You must claim it to submit a formal resolution.`
                      : 'Claim this application dossier to lock it for your evaluation and submit a formal sanction decision.'}
                  </p>
                  <Button
                    onClick={() => claimMutation.mutate()}
                    disabled={claimMutation.isPending}
                    className="w-full h-9 font-bold text-xs bg-[#0B1F3A] hover:bg-[#102A43] text-white"
                  >
                    {claimMutation.isPending && <Loader2 className="w-3.5 h-3.5 mr-1.5 animate-spin" />}
                    Claim File for Review
                  </Button>
                </div>
              ) : (
                <>
                  <div className="flex justify-between items-center pb-2 border-b border-slate-100">
                    <span className="text-slate-500 font-medium">Assigned to: <strong>You</strong></span>
                    <Button
                      variant="outline"
                      size="xs"
                      onClick={() => unclaimMutation.mutate()}
                      disabled={unclaimMutation.isPending || decisionMutation.isPending}
                      className="text-slate-600 hover:text-slate-900"
                    >
                      {unclaimMutation.isPending && <Loader2 className="w-3 h-3 mr-1 animate-spin" />}
                      Release Claim
                    </Button>
                  </div>

                  <div className="space-y-2">
                    <label className="text-xs font-bold text-slate-800 block">
                      Statutory Determination <span className="text-red-600">*</span>
                    </label>
                    <div className="grid grid-cols-2 gap-2">
                      <button
                        type="button"
                        onClick={() => setDecision('APPROVE')}
                        className={`flex flex-col items-center justify-center p-3 rounded-sm border-2 transition-all cursor-pointer ${
                          decision === 'APPROVE'
                            ? 'border-emerald-600 bg-emerald-50 text-emerald-900'
                            : 'border-slate-200 bg-white text-slate-600 hover:border-emerald-300'
                        }`}
                      >
                        <CheckCircle className="w-5 h-5 mb-1 text-emerald-600" />
                        <span className="font-bold text-xs">Approve Scheme</span>
                      </button>

                      <button
                        type="button"
                        onClick={() => setDecision('REJECT')}
                        className={`flex flex-col items-center justify-center p-3 rounded-sm border-2 transition-all cursor-pointer ${
                          decision === 'REJECT'
                            ? 'border-red-600 bg-red-50 text-red-900'
                            : 'border-slate-200 bg-white text-slate-600 hover:border-red-300'
                        }`}
                      >
                        <XCircle className="w-5 h-5 mb-1 text-red-600" />
                        <span className="font-bold text-xs">Reject Scheme</span>
                      </button>
                    </div>
                  </div>

                  {decision === 'REJECT' && (
                    <div className="space-y-1">
                      <label className="text-xs font-bold text-slate-800 block">
                        Mandatory Statutory Justification <span className="text-red-600">*</span>
                      </label>
                      <textarea
                        value={reason}
                        onChange={(e) => setReason(e.target.value)}
                        className="w-full border border-slate-300 rounded-sm p-2.5 text-xs focus:border-red-500 focus:ring-1 focus:ring-red-500 outline-none"
                        rows={4}
                        placeholder="State legal grounds or eligibility criteria discrepancy..."
                      />
                    </div>
                  )}

                  {decision === 'APPROVE' && (
                    <div className="space-y-1">
                      <label className="text-xs font-bold text-slate-800 block">
                        Sanction Notes (Optional)
                      </label>
                      <textarea
                        value={reason}
                        onChange={(e) => setReason(e.target.value)}
                        className="w-full border border-slate-300 rounded-sm p-2.5 text-xs focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 outline-none"
                        rows={3}
                        placeholder="Optional remarks regarding sanction..."
                      />
                    </div>
                  )}

                  <Button
                    onClick={handleSubmitDecision}
                    disabled={decisionMutation.isPending || !decision}
                    className={`w-full h-10 text-xs font-bold text-white shadow-xs ${
                      decision === 'APPROVE'
                        ? 'bg-emerald-700 hover:bg-emerald-800'
                        : decision === 'REJECT'
                        ? 'bg-red-700 hover:bg-red-800'
                        : 'bg-[#0B1F3A] hover:bg-[#102A43]'
                    }`}
                  >
                    {decisionMutation.isPending ? (
                      <Loader2 className="w-4 h-4 animate-spin mx-auto" />
                    ) : (
                      'Submit Statutory Resolution'
                    )}
                  </Button>
                </>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
