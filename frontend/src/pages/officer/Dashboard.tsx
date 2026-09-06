import { Link } from 'react-router-dom';

export default function OfficerDashboard() {
  return (
    <div className="p-8">
      <h1 className="text-3xl font-bold text-gray-800 mb-4">Officer Dashboard</h1>
      <p className="text-gray-600 mb-6">Review and approve citizen applications backed by verified interoperability data.</p>
      
      <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-100 text-gray-700 text-sm">
              <th className="p-4 border-b">App ID</th>
              <th className="p-4 border-b">Citizen ID</th>
              <th className="p-4 border-b">Scheme</th>
              <th className="p-4 border-b">Status</th>
              <th className="p-4 border-b">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td className="p-4 text-gray-500 italic" colSpan={5}>No pending tasks yet. (Will fetch from Workflow API)</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div className="mt-8">
        <Link to="/" className="text-blue-500 hover:underline">&larr; Back to Home</Link>
      </div>
    </div>
  );
}
