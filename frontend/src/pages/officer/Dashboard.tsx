import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { officerServiceApi } from '@/services/officerService';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Loader2, ArrowRight, User, AlertCircle, CheckCircle2 } from 'lucide-react';

export default function OfficerDashboard() {
  const navigate = useNavigate();

  const { data: tasks, isLoading, error, refetch } = useQuery({
    queryKey: ['officerTasks'],
    queryFn: officerServiceApi.getPendingReviews,
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Review Queue</h1>
          <p className="text-sm text-slate-500 mt-1">
            Evaluate applications backed by verified interoperability data.
          </p>
        </div>
        <Button onClick={() => refetch()} variant="outline" className="bg-white border-slate-200 text-slate-700 shadow-sm">
          Refresh Queue
        </Button>
      </div>

      {error ? (
        <div className="bg-red-50 border border-red-200 text-red-800 p-4 rounded-lg flex items-start gap-3">
          <AlertCircle className="h-5 w-5 mt-0.5 shrink-0" />
          <div>
            <h4 className="font-semibold text-sm">Error</h4>
            <p className="text-sm mt-1">Failed to load pending reviews. Please try again.</p>
          </div>
        </div>
      ) : (
        <Card className="shadow-sm border-slate-200">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 text-xs uppercase tracking-wider">
                  <th className="p-4 font-semibold">Application Reference</th>
                  <th className="p-4 font-semibold">Citizen ID</th>
                  <th className="p-4 font-semibold">Scheme/Service</th>
                  <th className="p-4 font-semibold">Status</th>
                  <th className="p-4 font-semibold">Submitted</th>
                  <th className="p-4 text-right font-semibold">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white">
                {isLoading ? (
                  <tr>
                    <td colSpan={6} className="p-12 text-center text-slate-500">
                      <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2 text-primary" />
                      Loading queue...
                    </td>
                  </tr>
                ) : !tasks || tasks.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="p-12 text-center text-slate-500 bg-slate-50/50">
                      <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2" />
                      <p className="font-medium text-slate-900">Inbox Zero</p>
                      <p className="text-sm mt-1">No pending tasks for review.</p>
                    </td>
                  </tr>
                ) : (
                  tasks.map((task) => (
                    <tr 
                      key={task.taskId} 
                      className="hover:bg-slate-50 transition-colors cursor-pointer group" 
                      onClick={() => navigate(`/officer/reviews/${task.taskId}`)}
                    >
                      <td className="p-4 text-sm font-semibold text-slate-900 font-mono">
                        {task.applicationId}
                      </td>
                      <td className="p-4 text-sm text-slate-600">
                        <div className="flex items-center gap-1.5">
                          <User className="w-4 h-4 text-slate-400" />
                          {task.citizenId}
                        </div>
                      </td>
                      <td className="p-4 text-sm text-slate-600">
                        <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-200 font-medium">
                          {task.serviceCode}
                        </Badge>
                      </td>
                      <td className="p-4 text-sm">
                        <Badge className="bg-amber-100 text-amber-800 hover:bg-amber-100 border-transparent">
                          {task.status.replace(/_/g, ' ')}
                        </Badge>
                        {task.assignee && (
                          <div className="text-xs text-slate-500 mt-1 flex items-center gap-1">
                            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                            Claimed
                          </div>
                        )}
                      </td>
                      <td className="p-4 text-sm text-slate-500 whitespace-nowrap">
                        {new Date(task.createTime).toLocaleDateString()}
                      </td>
                      <td className="p-4 text-right">
                        <Button 
                          size="sm" 
                          variant="secondary" 
                          className="bg-slate-100 hover:bg-slate-200 text-slate-700 group-hover:bg-primary group-hover:text-white transition-colors"
                        >
                          Review File <ArrowRight className="w-4 h-4 ml-1.5" />
                        </Button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </Card>
      )}
    </div>
  );
}
