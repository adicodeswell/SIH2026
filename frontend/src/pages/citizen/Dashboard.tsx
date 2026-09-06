import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { interoperabilityApi, applicationApi, workflowApi } from '../../lib/api';
import { Link } from 'react-router-dom';

export default function CitizenDashboard() {
  const { token, logout } = useAuth();
  
  // Keycloak sometimes returns preferred_username in lowercase. 
  // We must uppercase it because the backend mock database expects 'MH1001'.
  const username = token ? JSON.parse(atob(token.split('.')[1])).preferred_username.toUpperCase() : '';
  
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const handleFetchData = async () => {
    setLoading(true);
    try {
      // 1. Fetch cross-departmental data magically
      const response = await interoperabilityApi.get(`/api/v1/interop/fetch/all/${username}`);
      setData(response.data);
    } catch (error) {
      console.error("Failed to fetch interop data", error);
      alert("Error contacting Interoperability Service. Is it running on 8082?");
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitApplication = async () => {
    try {
      // 1. Explicitly grant cryptographic consent for the exact scope the workflow needs
      await workflowApi.post('/api/v1/consents', {
        dataScope: 'education,employment,skills',
        purpose: 'verification',
        requestingDepartmentId: 'DEPT_EMP'
      });

      // 2. Submit the application, triggering the workflow which will now find valid consent!
      await applicationApi.post('/api/v1/applications', {
        citizenId: username,
        serviceCode: "SKILL_BENEFIT"
      });
      setSubmitted(true);
    } catch (error) {
      console.error("Failed to submit application", error);
      alert("Error submitting application. Is Application Service running on 8081?");
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-4xl mx-auto bg-white p-8 rounded-xl shadow-sm border border-gray-100">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-gray-800">Citizen Portal</h1>
          <button onClick={logout} className="text-red-500 font-semibold hover:underline">Logout</button>
        </div>

        <p className="text-gray-600 mb-6">
          Welcome <span className="font-bold text-blue-600">{username}</span>! Apply for government schemes below. 
          Zero document uploads required.
        </p>
        
        {/* Scheme Selection */}
        <div className="border border-blue-200 rounded-lg p-6 bg-blue-50 mb-8">
          <h2 className="text-xl font-bold text-blue-800 mb-2">State Youth Tech Scholarship</h2>
          <p className="text-sm text-blue-600 mb-4">Financial assistance for youth pursuing technical degrees.</p>
          
          {!data && !loading && (
            <button 
              onClick={handleFetchData}
              className="px-6 py-2 bg-blue-600 text-white rounded font-semibold shadow hover:bg-blue-700 transition"
            >
              Start Application & Fetch My Records
            </button>
          )}

          {loading && (
            <div className="text-blue-600 font-semibold animate-pulse">
              Contacting Health, Education, and Employment departments...
            </div>
          )}
        </div>

        {/* Display Fetched Canonical Data */}
        {data && !submitted && (
          <div className="animate-in fade-in duration-500">
            <h3 className="text-lg font-bold text-green-700 mb-4">✅ Records Successfully Fetched!</h3>
            
            <div className="bg-gray-100 p-4 rounded-lg text-sm font-mono overflow-x-auto mb-6 text-gray-800">
              <pre>{JSON.stringify(data, null, 2)}</pre>
            </div>

            <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4 mb-6">
              <p className="text-yellow-800 text-sm font-semibold">
                Consent Declaration: I hereby authorize the Govt of Maharashtra to use my fetched data to evaluate this scheme.
              </p>
            </div>

            <button 
              onClick={handleSubmitApplication}
              className="w-full px-6 py-3 bg-green-600 text-white rounded font-bold shadow-lg hover:bg-green-700 transition"
            >
              Grant Consent & Submit Final Application
            </button>
          </div>
        )}

        {/* Success State */}
        {submitted && (
          <div className="bg-green-100 border border-green-300 text-green-800 p-6 rounded-lg text-center animate-in zoom-in duration-500">
            <h3 className="text-2xl font-bold mb-2">Application Submitted!</h3>
            <p>Your application has been routed to the Officer Workflow Engine for final approval.</p>
          </div>
        )}

        <div className="mt-8 text-center border-t pt-4">
          <Link to="/" className="text-gray-500 hover:underline">Return to Homepage</Link>
        </div>
      </div>
    </div>
  );
}
