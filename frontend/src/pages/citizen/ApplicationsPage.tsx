import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Search, Info } from 'lucide-react';

export function CitizenApplicationsPage() {
  const [searchId, setSearchId] = useState('');
  const navigate = useNavigate();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchId.trim()) {
      navigate(`/citizen/applications/${searchId.trim()}`);
    }
  };

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Track Application</h1>
        <p className="text-sm text-slate-500 mt-1">
          Enter your Application Reference Number to view its current status and timeline.
        </p>
      </div>

      <div className="bg-blue-50 border border-blue-200 text-blue-800 p-4 rounded-lg flex items-start gap-3"><Info className="h-5 w-5 mt-0.5 shrink-0" /><div><h4 className="font-semibold text-sm">Backend Limitation Notice</h4><p className="text-sm mt-1">The Ekikrit platform backend currently does not provide an API endpoint to list all applications for a citizen. 
          Until this capability is added to the backend services, please use your specific Application Reference Number 
          (e.g., MH-2024-XXXX) to track its progress.</p></div></div>

      <Card>
        <CardHeader>
          <CardTitle>Application Tracker</CardTitle>
          <CardDescription>
            You can find your reference number on the success screen when you submitted your application, or in your registered email/SMS.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSearch} className="flex gap-4">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
              <Input
                placeholder="Enter Reference Number (e.g., MH-2024-1234)"
                className="pl-9"
                value={searchId}
                onChange={(e) => setSearchId(e.target.value)}
                required
              />
            </div>
            <Button type="submit" disabled={!searchId.trim()}>
              Track
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
