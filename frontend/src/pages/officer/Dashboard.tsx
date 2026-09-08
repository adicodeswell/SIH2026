import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { officerApi, applicationApi } from '../../lib/api';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Loader2, ArrowLeft, CheckCircle, XCircle, FileText, User, ShieldCheck } from 'lucide-react';

export default function OfficerDashboard() {
  const { logout } = useAuth();
  const [tasks, setTasks] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
const parseAndMergeData = (rawData: any) => {
  if (!rawData) return null;
  let parsed = rawData;
  if (typeof rawData === "string") {
    try {
      parsed = JSON.parse(rawData);
    } catch(e) {
      return rawData;
    }
  }
  if (Array.isArray(parsed)) {
    return parsed.reduce((acc: any, current: any) => {
      Object.keys(current).forEach(key => {
        if (current[key] !== null && current[key] !== undefined) {
          acc[key] = current[key];
        }
      });
      return acc;
    }, {});
  }
  return parsed;
};
  const [error, setError] = useState('');
  
  // Split View State
  const [selectedTask, setSelectedTask] = useState<any | null>(null);
  const [applicationDetails, setApplicationDetails] = useState<any | null>(null);
  const [loadingDetails, setLoadingDetails] = useState(false);
  
  // Action State
  const [decision, setDecision] = useState<'APPROVE' | 'REJECT' | null>(null);
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetchTasks = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await officerApi.get('/api/v1/officer/reviews');
      setTasks(res.data || []);
    } catch (err: any) {
      console.error(err);
      setError('Failed to fetch pending applications.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTasks();
  }, []);

  const handleSelectTask = async (task: any) => {
    setSelectedTask(task);
    setDecision(null);
    setReason('');
    setLoadingDetails(true);
    try {
      const res = await applicationApi.get(`/api/v1/applications/${task.applicationId}`);
      setApplicationDetails(res.data);
    } catch (err) {
      toast.error("Could not fetch application details.");
      setSelectedTask(null);
    } finally {
      setLoadingDetails(false);
    }
  };

  const handleSubmitDecision = async () => {
    if (!decision) {
      toast.error("Please select a decision.");
      return;
    }
    if (decision === 'REJECT' && !reason.trim()) {
      toast.error("A reason is mandatory for rejection.");
      return;
    }

    setSubmitting(true);
    try {
      await officerApi.post(`/api/v1/officer/reviews/${selectedTask.taskId}/decision`, {
        decision: decision,
        reason: reason || "Officer reviewed data and found it satisfactory"
      });
      toast.success(`Application ${decision === 'APPROVE' ? 'Approved' : 'Rejected'} Successfully`);
      setSelectedTask(null);
      fetchTasks();
    } catch (err: any) {
      toast.error('Failed to submit decision: ' + (err.response?.data?.message || err.message));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <header className="bg-slate-900 text-white shadow-sm px-8 py-4 flex justify-between items-center border-b border-slate-800">
        <div className="flex items-center gap-3">
          <ShieldCheck className="w-6 h-6 text-emerald-400" />
          <h1 className="text-xl font-bold tracking-tight">Ekikrit Officer Console</h1>
        </div>
        <button onClick={logout} className="text-slate-300 hover:text-white font-semibold text-sm transition">
          Sign Out
        </button>
      </header>
      
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 md:p-8">
        
        {/* VIEW 1: TASK INBOX */}
        {!selectedTask && (
          <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
            <div className="flex justify-between items-center mb-6">
              <div>
                <h2 className="text-2xl font-bold text-slate-800 tracking-tight">Pending Reviews</h2>
                <p className="text-slate-500 text-sm mt-1">Review applications backed by verified interoperability data.</p>
              </div>
              <Button onClick={fetchTasks} variant="outline" className="bg-white border-slate-200 text-slate-700 shadow-sm">
                Refresh Inbox
              </Button>
            </div>

            {error && <div className="p-4 mb-6 bg-red-50 text-red-700 rounded-lg border border-red-200">{error}</div>}

            <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50 text-slate-600 text-xs uppercase tracking-wider">
                    <th className="p-4 border-b font-semibold">Application ID</th>
                    <th className="p-4 border-b font-semibold">Citizen ID</th>
                    <th className="p-4 border-b font-semibold">Scheme</th>
                    <th className="p-4 border-b font-semibold">Received</th>
                    <th className="p-4 border-b text-right font-semibold">Action</th>
                  </tr>
                </thead>
                <tbody>
                  {loading && tasks.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="p-12 text-center text-slate-500">
                        <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2 text-slate-400" />
                        Fetching tasks...
                      </td>
                    </tr>
                  ) : tasks.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="p-12 text-center text-slate-500 bg-slate-50/50">
                        <CheckCircle className="w-8 h-8 text-emerald-500 mx-auto mb-2" />
                        Inbox Zero! No pending tasks for review.
                      </td>
                    </tr>
                  ) : (
                    tasks.map(task => (
                      <tr key={task.taskId} className="hover:bg-slate-50 border-b border-slate-100 last:border-0 transition cursor-pointer" onClick={() => handleSelectTask(task)}>
                        <td className="p-4 text-sm font-semibold text-slate-800">{task.applicationId}</td>
                        <td className="p-4 text-sm text-slate-600 flex items-center gap-2"><User className="w-4 h-4 text-slate-400"/> {task.citizenId}</td>
                        <td className="p-4 text-sm text-slate-600">
                          <span className="bg-blue-50 text-blue-700 px-2 py-1 rounded border border-blue-100 text-xs font-medium">{task.serviceCode}</span>
                        </td>
                        <td className="p-4 text-sm text-slate-500">{new Date(task.createTime).toLocaleString()}</td>
                        <td className="p-4 text-right">
                          <Button size="sm" variant="secondary" className="bg-slate-100 hover:bg-slate-200 text-slate-700">
                            Review File &rarr;
                          </Button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* VIEW 2: SPLIT-VIEW WORKSPACE */}
        {selectedTask && (
          <div className="h-full animate-in fade-in zoom-in-95 duration-300">
            <div className="mb-4">
              <Button variant="ghost" onClick={() => setSelectedTask(null)} className="-ml-4 text-slate-500 hover:text-slate-800">
                <ArrowLeft className="w-4 h-4 mr-2" /> Back to Inbox
              </Button>
            </div>
            
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              
              {/* LEFT SIDE: Secure Data View */}
              <div className="lg:col-span-2 space-y-6">
                <div className="bg-white border rounded-xl shadow-sm p-6">
                  <div className="flex justify-between items-start border-b pb-4 mb-4">
                    <div>
                      <h2 className="text-xl font-bold tracking-tight text-slate-900">Application File: {selectedTask.applicationId}</h2>
                      <p className="text-slate-500 text-sm mt-1">Scheme: <span className="font-medium text-slate-700">{selectedTask.serviceCode}</span></p>
                    </div>
                    <div className="text-right">
                      <p className="text-xs uppercase tracking-wider text-slate-400 font-semibold mb-1">Applicant ID</p>
                      <p className="font-mono text-sm text-slate-700 bg-slate-100 px-2 py-1 rounded">{selectedTask.citizenId}</p>
                    </div>
                  </div>

                  {loadingDetails ? (
                    <div className="p-12 text-center text-slate-500">
                      <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2 text-slate-400" />
                      Loading securely fetched Interoperability Records...
                    </div>
                  ) : applicationDetails?.verificationData ? (
                    <div className="space-y-4">
                      <div className="flex items-center text-emerald-800 bg-emerald-50 p-3 rounded border border-emerald-200 text-sm font-medium">
                        <CheckCircle className="w-4 h-4 mr-2 text-emerald-600" />
                        This data was securely fetched directly from government source systems after DPDP consent.
                      </div>
                      <div className="bg-slate-900 rounded-lg overflow-hidden border border-slate-800 shadow-inner">
                        <div className="bg-slate-800 px-4 py-2 border-b border-slate-700 flex items-center">
                          <FileText className="w-4 h-4 text-slate-400 mr-2" />
                          <span className="text-xs font-semibold text-slate-300 uppercase tracking-wider">Canonical Profile JSON</span>
                        </div>
                        <div className="p-4 overflow-x-auto">
                          <pre className="text-sm font-mono text-emerald-400">
                            {JSON.stringify(parseAndMergeData(applicationDetails.verificationData), null, 2)}
                          </pre>
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className="p-12 text-center text-amber-700 bg-amber-50 rounded border border-amber-200">
                      No verification data found for this application. The workflow may have failed or no data was returned by the Interoperability Service.
                    </div>
                  )}
                </div>
              </div>

              {/* RIGHT SIDE: Action Panel */}
              <div className="lg:col-span-1">
                <div className="bg-white border rounded-xl shadow-sm p-6 sticky top-6">
                  <h3 className="text-lg font-bold text-slate-900 border-b pb-4 mb-4">Officer Decision</h3>
                  
                  <div className="space-y-4">
                    <div>
                      <label className="text-sm font-semibold text-slate-700 mb-2 block">Action</label>
                      <div className="grid grid-cols-2 gap-3">
                        <button 
                          onClick={() => setDecision('APPROVE')}
                          className={`flex flex-col items-center justify-center p-4 rounded-lg border-2 transition ${decision === 'APPROVE' ? 'border-emerald-500 bg-emerald-50 text-emerald-700' : 'border-slate-200 bg-white text-slate-500 hover:border-emerald-200 hover:bg-emerald-50/50'}`}
                        >
                          <CheckCircle className="w-6 h-6 mb-2" />
                          <span className="font-bold text-sm">Approve</span>
                        </button>
                        <button 
                          onClick={() => setDecision('REJECT')}
                          className={`flex flex-col items-center justify-center p-4 rounded-lg border-2 transition ${decision === 'REJECT' ? 'border-red-500 bg-red-50 text-red-700' : 'border-slate-200 bg-white text-slate-500 hover:border-red-200 hover:bg-red-50/50'}`}
                        >
                          <XCircle className="w-6 h-6 mb-2" />
                          <span className="font-bold text-sm">Reject</span>
                        </button>
                      </div>
                    </div>

                    {decision === 'REJECT' && (
                      <div className="animate-in fade-in slide-in-from-top-2">
                        <label className="text-sm font-semibold text-slate-700 mb-2 block">Reason for Rejection <span className="text-red-500">*</span></label>
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
                        <label className="text-sm font-semibold text-slate-700 mb-2 block">Notes (Optional)</label>
                        <textarea 
                          value={reason}
                          onChange={(e) => setReason(e.target.value)}
                          className="w-full border border-slate-300 rounded-lg p-3 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 outline-none"
                          rows={3}
                          placeholder="Any internal notes for approval..."
                        />
                      </div>
                    )}

                    <Button 
                      onClick={handleSubmitDecision}
                      disabled={submitting || !decision}
                      className={`w-full h-12 text-base font-bold shadow-sm ${decision === 'APPROVE' ? 'bg-emerald-600 hover:bg-emerald-700 text-white' : decision === 'REJECT' ? 'bg-red-600 hover:bg-red-700 text-white' : 'bg-slate-800 text-white'}`}
                    >
                      {submitting ? <Loader2 className="w-5 h-5 animate-spin mx-auto" /> : "Submit Final Decision"}
                    </Button>
                  </div>
                </div>
              </div>

            </div>
          </div>
        )}
      </main>
    </div>
  );
}
