with open('./frontend/src/pages/citizen/Dashboard.tsx', 'r') as f:
    content = f.read()

import re

# Add useQuery and applicationServiceApi
new_imports = """import { applicationServiceApi } from '@/services/applicationService';
import { useQuery } from '@tanstack/react-query';
import { FileText } from 'lucide-react';
"""

content = content.replace("import { useState, useEffect } from 'react';", "import { useState, useEffect } from 'react';\n" + new_imports)

# Add useQuery to CitizenDashboard
new_query = """
  const { data: myApps, isLoading: loadingApps } = useQuery({
    queryKey: ['citizen-applications'],
    queryFn: () => applicationServiceApi.getMyApplications(),
  });
"""

content = content.replace("  const [schemes, setSchemes] = useState<any[]>([]);", new_query + "\n  const [schemes, setSchemes] = useState<any[]>([]);")

# Add My Applications to the Dashboard below Welcome Banner
my_apps_section = """
      {/* My Applications Section */}
      <div className="bg-white border border-slate-200 shadow-sm rounded-xl overflow-hidden">
        <div className="flex flex-col sm:flex-row items-center justify-between p-6">
          <div className="flex items-start gap-4">
            <div className="bg-blue-50 p-3 rounded-full hidden sm:block">
              <FileText className="w-6 h-6 text-primary" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-slate-900">My Applications</h2>
              <p className="text-sm text-slate-500 mt-1">Track the status of all your submitted schemes and view activity history.</p>
              {!loadingApps && myApps && (
                <Badge variant="secondary" className="mt-2 bg-blue-50 text-blue-800 hover:bg-blue-100">
                  {myApps.length} Application{myApps.length !== 1 ? 's' : ''}
                </Badge>
              )}
            </div>
          </div>
          <Link to="/citizen/applications" className="mt-4 sm:mt-0 w-full sm:w-auto">
            <Button variant="outline" className="w-full sm:w-auto gap-2">
              View My Applications <ArrowRight className="w-4 h-4" />
            </Button>
          </Link>
        </div>
      </div>
"""

content = content.replace("      {/* Featured Schemes Section */}", my_apps_section + "\n      {/* Featured Schemes Section */}")

with open('./frontend/src/pages/citizen/Dashboard.tsx', 'w') as f:
    f.write(content)
