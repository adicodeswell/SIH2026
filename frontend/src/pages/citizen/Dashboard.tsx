import { Link } from 'react-router-dom';

export default function CitizenDashboard() {
  return (
    <div className="p-8">
      <h1 className="text-3xl font-bold text-gray-800 mb-4">Citizen Dashboard</h1>
      <p className="text-gray-600 mb-6">Welcome! Apply for government schemes below without uploading any physical documents.</p>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="border border-gray-200 rounded-lg p-6 shadow-sm hover:shadow-md transition bg-white">
          <h2 className="text-xl font-bold text-blue-700 mb-2">State Youth Tech Scholarship</h2>
          <p className="text-sm text-gray-600 mb-4">Financial assistance for unemployed youth pursuing technical degrees.</p>
          <button className="px-4 py-2 bg-blue-50 text-blue-700 rounded font-semibold border border-blue-200 hover:bg-blue-100">
            Apply Now
          </button>
        </div>
      </div>
      
      <div className="mt-8">
        <Link to="/" className="text-blue-500 hover:underline">&larr; Back to Home</Link>
      </div>
    </div>
  );
}
