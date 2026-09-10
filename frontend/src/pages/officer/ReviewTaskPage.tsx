import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { officerServiceApi } from '@/services/officerService';
import { applicationServiceApi } from '@/services/applicationService';
import { useAuth } from '@/context/AuthContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Loader2, ArrowLeft, CheckCircle, XCircle, FileText, User, ShieldCheck, AlertCircle } from 'lucide-react';
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

  const isClaimedByMe = task?.assignee === user?.username;
  const isClaimedByOther = task?.assignee && !isClaimedByMe;

  const handleSubmitDecision = () => {
    if (!decision) {
      toast.error('Please select a decision.');
      return;
    }
    if (decision === 'REJECT' && !reason.trim()) {
      toast.error('A reason is mandatory for rejection.');
      return;
    }
    decisionMutation.mutate();
  };

  if (loadingTask || loadingApp) {
    return (
      <div className="flex justify-center p-12">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  if (taskError || !task) {
    return (
      <div className="space-y-6">
        <Link to="/officer" className="inline-flex items-center text-sm text-primary hover:underline">
          <ArrowLeft className="w-4 h-4 mr-1" /> Back to Queue
        </Link>
        <div className="bg-red-50 border border-red-200 text-red-800 p-4 rounded-lg flex items-start gap-3">
          <AlertCircle className="h-5 w-5 mt-0.5 shrink-0" />
          <div>
            <h4 className="font-semibold text-sm">Error</h4>
            <p className="text-sm mt-1">Review task not found or you do not have permission.</p>
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

  const parsedVerification = parseVerificationData(application?.verificationData);

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      <Link to="/officer" className="inline-flex items-center text-sm text-primary hover:underline">
        <ArrowLeft className="w-4 h-4 mr-1" /> Back to Queue
      </Link>

      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Application Review: {task.applicationId}
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Scheme: <span className="font-medium text-slate-700">{task.serviceCode}</span>
          </p>
        </div>
        <div className="flex items-center gap-3">
          {isClaimedByOther && (
            <Badge className="bg-amber-100 text-amber-800 border-transparent hover:bg-amber-100">
              Claimed by {task.assignee}
            </Badge>
          )}
          {isClaimedByMe && (
            <Badge className="bg-emerald-100 text-emerald-800 border-transparent hover:bg-emerald-100">
              Claimed by You
            </Badge>
          )}
          {!task.assignee && (
            <Badge variant="outline" className="text-slate-600">
              Unclaimed
            </Badge>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Details & Data */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader className="pb-4 border-b border-slate-100">
              <CardTitle className="flex items-center justify-between text-lg">
                <span className="flex items-center gap-2">
                  <User className="w-5 h-5 text-primary" />
                  Applicant Information
                </span>
                <span className="text-sm font-normal text-slate-500 font-mono">
                  ID: {task.citizenId}
                </span>
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm font-medium text-slate-500">Submitted On</p>
                  <p className="mt-1">{new Date(task.createTime).toLocaleString()}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Workflow Stage</p>
                  <p className="mt-1">{task.taskName}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-4 border-b border-slate-100">
              <CardTitle className="flex items-center gap-2 text-lg">
                <ShieldCheck className="w-5 h-5 text-emerald-600" />
                Interoperability Verification Results
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-4">
              {parsedVerification ? (
                <div className="space-y-4">
                  <div className="flex items-start text-emerald-800 bg-emerald-50 p-4 rounded-lg border border-emerald-200 text-sm">
                    <CheckCircle className="w-5 h-5 mr-3 text-emerald-600 shrink-0 mt-0.5" />
                    <div>
                      <p className="font-semibold mb-1">Source System Verified</p>
                      <p className="text-emerald-700/90">
                        This data was securely fetched directly from government source systems after DPDP consent validation.
                      </p>
                    </div>
                  </div>
                  <div className="bg-slate-900 rounded-lg overflow-hidden border border-slate-800">
                    <div className="bg-slate-800 px-4 py-2 border-b border-slate-700 flex items-center">
                      <FileText className="w-4 h-4 text-slate-400 mr-2" />
                      <span className="text-xs font-semibold text-slate-300 uppercase tracking-wider">Canonical Profile JSON</span>
                    </div>
                    <div className="p-4 overflow-x-auto">
                      <pre className="text-sm font-mono text-emerald-400">
                        {JSON.stringify(parsedVerification, null, 2)}
                      </pre>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="p-8 text-center text-amber-700 bg-amber-50 rounded-lg border border-amber-200">
                  <AlertCircle className="w-8 h-8 mx-auto mb-3 text-amber-500" />
                  <p className="font-semibold">No verification data available</p>
                  <p className="text-sm mt-1">The workflow may have failed to retrieve data from the Interoperability Service.</p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Right Column: Action Panel */}
        <div className="lg:col-span-1">
          <Card className="sticky top-6">
            <CardHeader className="border-b border-slate-100 pb-4">
              <CardTitle className="text-lg">Officer Actions</CardTitle>
            </CardHeader>
            <CardContent className="pt-6 space-y-6">
              {!isClaimedByMe ? (
                <div className="space-y-4">
                  <p className="text-sm text-slate-600">
                    {isClaimedByOther 
                      ? "This task is currently claimed by another officer. You must claim it to submit a decision."
                      : "Claim this task to begin your review process and submit a decision."}
                  </p>
                  <Button 
                    onClick={() => claimMutation.mutate()} 
                    disabled={claimMutation.isPending}
                    className="w-full"
                  >
                    {claimMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                    Claim Task
                  </Button>
                </div>
              ) : (
                <>
                  <div className="flex justify-end mb-4">
                    <Button 
                      variant="outline" 
                      size="sm" 
                      onClick={() => unclaimMutation.mutate()}
                      disabled={unclaimMutation.isPending || decisionMutation.isPending}
                      className="text-slate-500"
                    >
                      {unclaimMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                      Unclaim Task
                    </Button>
                  </div>

                  <div>
                    <label className="text-sm font-semibold text-slate-700 mb-3 block">Decision</label>
                    <div className="grid grid-cols-2 gap-3">
                      <button 
                        onClick={() => setDecision('APPROVE')}
                        className={`flex flex-col items-center justify-center p-4 rounded-lg border-2 transition ${decision === 'APPROVE' ? 'border-emerald-500 bg-emerald-50 text-emerald-700' : 'border-slate-200 bg-white text-slate-500 hover:border-emerald-200 hover:bg-emerald-50'}`}
                      >
                        <CheckCircle className="w-6 h-6 mb-2" />
                        <span className="font-bold text-sm">Approve</span>
                      </button>
                      <button 
                        onClick={() => setDecision('REJECT')}
                        className={`flex flex-col items-center justify-center p-4 rounded-lg border-2 transition ${decision === 'REJECT' ? 'border-red-500 bg-red-50 text-red-700' : 'border-slate-200 bg-white text-slate-500 hover:border-red-200 hover:bg-red-50'}`}
                      >
                        <XCircle className="w-6 h-6 mb-2" />
                        <span className="font-bold text-sm">Reject</span>
                      </button>
                    </div>
                  </div>

                  {decision === 'REJECT' && (
                    <div className="animate-in fade-in slide-in-from-top-2">
                      <label className="text-sm font-semibold text-slate-700 mb-2 block">
                        Reason for Rejection <span className="text-red-500">*</span>
                      </label>
                      <textarea 
                        value={reason}
                        onChange={(e) => setReason(e.target.value)}
                        className="w-full border border-slate-300 rounded-lg p-3 text-sm focus:ring-2 focus:ring-red-500 focus:border-red-500 outline-none"
                        rows={4}
                        placeholder="Please provide a mandatory reason for rejecting this application."
                      />
                    </div>
                  )}
                  
                  {decision === 'APPROVE' && (
                    <div className="animate-in fade-in slide-in-from-top-2">
                      <label className="text-sm font-semibold text-slate-700 mb-2 block">
                        Internal Notes (Optional)
                      </label>
                      <textarea 
                        value={reason}
                        onChange={(e) => setReason(e.target.value)}
                        className="w-full border border-slate-300 rounded-lg p-3 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 outline-none"
                        rows={3}
                        placeholder="Any notes for this approval..."
                      />
                    </div>
                  )}

                  <Button 
                    onClick={handleSubmitDecision}
                    disabled={decisionMutation.isPending || !decision}
                    className={`w-full h-12 text-base font-bold shadow-sm ${
                      decision === 'APPROVE' ? 'bg-emerald-600 hover:bg-emerald-700 text-white' 
                      : decision === 'REJECT' ? 'bg-red-600 hover:bg-red-700 text-white' 
                      : 'bg-slate-800 text-white'
                    }`}
                  >
                    {decisionMutation.isPending ? <Loader2 className="w-5 h-5 animate-spin mx-auto" /> : "Submit Final Decision"}
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
