export default function Login() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-xl shadow-md w-full max-w-md text-center">
        <h2 className="text-2xl font-bold mb-6 text-gray-800">Secure Login</h2>
        <p className="text-sm text-gray-500 mb-6">Redirecting to Keycloak Authentication...</p>
        <button className="w-full px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition">
          Login via Keycloak
        </button>
      </div>
    </div>
  );
}
