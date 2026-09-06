import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { officerApi } from '../../lib/api';

export default function OfficerDashboard() {
  const { logout } = useAuth();
  const [tasks, setTasks] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

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

  const handleDecision = async (taskId: string, decision: 'APPROVE' | 'REJECT') => {
    try {
      await officerApi.post(`/api/v1/officer/reviews/${taskId}/decision`, {
        decision: decision,
        reason: "Officer reviewed data and found it satisfactory"
      });
      // Remove from list or refresh
      fetchTasks();
    } catch (err: any) {
      console.error(err);
      alert('Failed to submit decision: ' + (err.response?.data?.message || err.message));
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="bg-white shadow-sm px-8 py-4 flex justify-between items-center border-b">
        <h1 className="text-2xl font-bold text-gray-800">GovSecure Officer Console</h1>
        <button onClick={logout} className="text-red-600 hover:text-red-700 font-semibold text-sm">
          Sign Out
        </button>
      </header>
      
      <main className="flex-1 max-w-6xl w-full mx-auto p-8">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h2 className="text-xl font-semibold text-gray-800">Pending Approvals</h2>
            <p className="text-gray-500 text-sm mt-1">Review applications backed by verified interoperability data.</p>
          </div>
          <button onClick={fetchTasks} className="px-4 py-2 bg-blue-100 text-blue-700 rounded-lg hover:bg-blue-200 text-sm font-semibold transition">
            Refresh Tasks
          </button>
        </div>

        {error && <div className="p-4 mb-6 bg-red-50 text-red-700 rounded-lg border border-red-200">{error}</div>}

        <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-100 text-gray-700 text-sm">
                <th className="p-4 border-b">App ID</th>
                <th className="p-4 border-b">Citizen ID</th>
                <th className="p-4 border-b">Scheme</th>
                <th className="p-4 border-b">Created</th>
                <th className="p-4 border-b text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              {loading && tasks.length === 0 ? (
                <tr>
                  <td colSpan={5} className="p-4 text-center text-gray-500 py-8">Loading tasks...</td>
                </tr>
              ) : tasks.length === 0 ? (
                <tr>
                  <td colSpan={5} className="p-4 text-center text-gray-500 py-8">No pending tasks for review! Inbox zero!</td>
                </tr>
              ) : (
                tasks.map(task => (
                  <tr key={task.taskId} className="hover:bg-gray-50">
                    <td className="p-4 border-b text-sm font-medium">{task.applicationId}</td>
                    <td className="p-4 border-b text-sm text-gray-600">{task.citizenId}</td>
                    <td className="p-4 border-b text-sm text-gray-600">{task.serviceCode}</td>
                    <td className="p-4 border-b text-sm text-gray-500">{new Date(task.createTime).toLocaleString()}</td>
                    <td className="p-4 border-b text-right space-x-2">
                      <button 
                        onClick={() => handleDecision(task.taskId, 'APPROVE')}
                        className="px-3 py-1.5 bg-green-600 text-white rounded hover:bg-green-700 text-xs font-semibold shadow-sm transition"
                      >
                        Approve
                      </button>
                      <button 
                        onClick={() => handleDecision(task.taskId, 'REJECT')}
                        className="px-3 py-1.5 bg-red-600 text-white rounded hover:bg-red-700 text-xs font-semibold shadow-sm transition"
                      >
                        Reject
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
}
