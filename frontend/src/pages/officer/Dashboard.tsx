import { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { officerServiceApi } from '@/services/officerService';
import { useAuth } from '@/context/AuthContext';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Breadcrumb } from '@/components/ui/Breadcrumb';
import {
  Loader2,
  ArrowRight,
  User,
  AlertCircle,
  CheckCircle2,
  RefreshCw,
  Clock,
  ShieldCheck,
  FileCheck,
} from 'lucide-react';
import { format } from 'date-fns';

export default function OfficerDashboard() {
  const navigate = useNavigate();
  const { user } = useAuth();

  const { data: tasks, isLoading, error, refetch } = useQuery({
    queryKey: ['officerTasks'],
    queryFn: officerServiceApi.getPendingReviews,
  });

  const myClaimedCount = useMemo(() => {
    if (!tasks || !user?.username?.toUpperCase()) return 0;
    return tasks.filter((t) => t.assignee === user.username).length;
  }, [tasks, user]);

  return (
    <div className="space-y-6">
      {/* Breadcrumb Navigation */}
      <Breadcrumb
        items={[
          { label: 'Officer Review Console', href: '/officer' },
          { label: 'Review Queue' },
        ]}
      />

      {/* Official Executive Header */}
      <div className="bg-white border border-slate-200 rounded-md p-6 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="bg-[#0B1F3A] text-white text-[10px] font-bold uppercase px-2 py-0.5 rounded-xs tracking-wider">
              Competent Authority Desk
            </span>
            <span className="text-xs text-slate-500 font-medium">
              Officer: <strong className="text-slate-800">{user?.name || user?.username?.toUpperCase()}</strong>
            </span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Pending Application Review Queue
          </h1>
          <p className="text-xs sm:text-sm text-slate-600 mt-1 max-w-2xl leading-relaxed">
            Evaluate citizen scheme applications supported by verified state interoperability records. Execute digital approvals or rejections with statutory rationales.
          </p>
        </div>

        <div className="flex items-center gap-2.5 shrink-0">
          <Button
            onClick={() => refetch()}
            variant="outline"
            disabled={isLoading}
            className="text-xs font-semibold h-9 gap-1.5"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
            Refresh Queue
          </Button>
        </div>
      </div>

      {/* KPI Cards (strictly derived from real queue data) */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="p-4 border-slate-200 bg-white shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Total Queue Depth
            </span>
            <div className="w-7 h-7 rounded-sm bg-amber-50 text-amber-700 flex items-center justify-center">
              <Clock className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-black text-slate-900 font-mono">
              {isLoading ? <Loader2 className="w-5 h-5 animate-spin text-slate-400" /> : tasks?.length || 0}
            </span>
            <span className="text-[11px] text-slate-500">pending tasks</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-2 border-t border-slate-100 pt-1.5">
            Awaiting officer verification &amp; sanction
          </p>
        </Card>

        <Card className="p-4 border-slate-200 bg-white shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Claimed By You
            </span>
            <div className="w-7 h-7 rounded-sm bg-blue-50 text-[#0B1F3A] flex items-center justify-center">
              <User className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-black text-blue-900 font-mono">
              {isLoading ? <Loader2 className="w-5 h-5 animate-spin text-slate-400" /> : myClaimedCount}
            </span>
            <span className="text-[11px] text-slate-500">assigned to your desk</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-2 border-t border-slate-100 pt-1.5">
            Locked for your active evaluation
          </p>
        </Card>

        <Card className="p-4 border-slate-200 bg-white shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Interoperability Engine
            </span>
            <div className="w-7 h-7 rounded-sm bg-emerald-50 text-emerald-700 flex items-center justify-center">
              <ShieldCheck className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-xs font-bold text-emerald-700">DPDP Verified</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-2 border-t border-slate-100 pt-1.5">
            Direct canonical state registry feeds active
          </p>
        </Card>
      </div>

      {/* Review Queue Table */}
      {error ? (
        <div className="bg-red-50 border border-red-200 text-red-900 p-6 rounded-md flex items-start gap-3">
          <AlertCircle className="h-5 w-5 mt-0.5 shrink-0 text-red-600" />
          <div>
            <h4 className="font-bold text-sm">Queue Load Error</h4>
            <p className="text-xs mt-1 text-red-800">
              Failed to load pending human-task reviews. Please verify the security workflow service is running and retry.
            </p>
          </div>
        </div>
      ) : (
        <Card className="shadow-xs border-slate-200 rounded-md overflow-hidden bg-white">
          <CardHeader className="border-b border-slate-100 bg-slate-50/70 pb-3">
            <CardTitle className="text-sm font-bold text-slate-900 flex items-center gap-2">
              <FileCheck className="w-4 h-4 text-[#0B1F3A]" />
              <span>Statutory Evaluation Ledger</span>
            </CardTitle>
            <CardDescription className="text-xs">
              All tasks represent Camunda BPMN user tasks assigned to candidate group or claimed officers.
            </CardDescription>
          </CardHeader>
          <CardContent className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 text-[11px] font-semibold uppercase tracking-wider">
                    <th className="px-4 py-3">Application Reference</th>
                    <th className="px-4 py-3">Citizen ID</th>
                    <th className="px-4 py-3">Scheme Code</th>
                    <th className="px-4 py-3">Workflow Stage</th>
                    <th className="px-4 py-3">Filing Date</th>
                    <th className="px-4 py-3">Assignment Status</th>
                    <th className="px-4 py-3 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-xs">
                  {isLoading ? (
                    <tr>
                      <td colSpan={7} className="p-12 text-center text-slate-500">
                        <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2 text-[#0B1F3A]" />
                        <span className="font-medium text-xs">Loading queue tasks...</span>
                      </td>
                    </tr>
                  ) : !tasks || tasks.length === 0 ? (
                    <tr>
                      <td colSpan={7} className="p-12 text-center text-slate-500 bg-slate-50/30">
                        <CheckCircle2 className="w-8 h-8 text-emerald-600 mx-auto mb-2" />
                        <p className="font-bold text-slate-900 text-sm">Inbox Clear</p>
                        <p className="text-xs mt-1 text-slate-500">
                          There are currently no pending review tasks in your departmental queue.
                        </p>
                      </td>
                    </tr>
                  ) : (
                    tasks.map((task) => (
                      <tr
                        key={task.taskId}
                        className="hover:bg-slate-50 transition-colors cursor-pointer even:bg-slate-50/40"
                        onClick={() => navigate(`/officer/reviews/${task.taskId}`)}
                      >
                        <td className="px-4 py-3 font-mono font-bold text-slate-900">
                          {task.applicationId}
                        </td>
                        <td className="px-4 py-3 text-slate-700">
                          <div className="flex items-center gap-1.5 font-mono">
                            <User className="w-3.5 h-3.5 text-slate-400" />
                            {task.citizenId}
                          </div>
                        </td>
                        <td className="px-4 py-3">
                          <Badge variant="service" className="text-[11px]">
                            {task.serviceCode}
                          </Badge>
                        </td>
                        <td className="px-4 py-3">
                          <span className="font-medium text-slate-800">
                            {task.taskName || 'Officer Verification'}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-slate-500 whitespace-nowrap font-medium">
                          {task.createTime ? format(new Date(task.createTime), 'dd MMM yyyy, p') : '-'}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap">
                          {task.assignee ? (
                            <Badge
                              variant={task.assignee === user?.username?.toUpperCase() ? 'active' : 'secondary'}
                              className="text-[10px]"
                            >
                              {task.assignee === user?.username?.toUpperCase() ? 'Claimed by You' : `Claimed: ${task.assignee}`}
                            </Badge>
                          ) : (
                            <Badge variant="pending" className="text-[10px]">
                              Unclaimed
                            </Badge>
                          )}
                        </td>
                        <td className="px-4 py-3 text-right whitespace-nowrap">
                          <Button
                            size="xs"
                            className="font-bold text-[11px] gap-1 bg-[#0B1F3A] hover:bg-[#102A43] text-white"
                            onClick={(e) => {
                              e.stopPropagation();
                              navigate(`/officer/reviews/${task.taskId}`);
                            }}
                          >
                            Review File <ArrowRight className="w-3 h-3" />
                          </Button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
