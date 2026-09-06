import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Login from './pages/Login';
import CitizenDashboard from './pages/citizen/Dashboard';
import OfficerDashboard from './pages/officer/Dashboard';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/citizen" element={<CitizenDashboard />} />
        <Route path="/officer" element={<OfficerDashboard />} />
      </Routes>
    </Router>
  );
}

export default App;
