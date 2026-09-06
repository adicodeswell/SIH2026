import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 text-gray-900 p-4">
      <h1 className="text-4xl font-bold text-blue-700 mb-2">MahaSetu Portal</h1>
      <p className="text-lg text-gray-600 mb-8 text-center max-w-lg">
        Seamless E-Governance and Interoperability Platform for the citizens and officers of Maharashtra.
      </p>
      <div className="flex gap-4">
        <Link to="/citizen" className="px-6 py-3 bg-blue-600 text-white rounded-lg shadow hover:bg-blue-700 font-semibold transition">
          Citizen Portal
        </Link>
        <Link to="/officer" className="px-6 py-3 bg-green-600 text-white rounded-lg shadow hover:bg-green-700 font-semibold transition">
          Officer Dashboard
        </Link>
      </div>
    </div>
  );
}
