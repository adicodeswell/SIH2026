import { useState } from 'react';
import { useAuth } from '@/context/AuthContext';
import { interoperabilityApi, applicationApi, workflowApi } from '@/lib/api';
import { Link } from 'react-router-dom';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Loader2, LogOut, CheckCircle2, FileText, ArrowRight } from 'lucide-react';

export default function CitizenDashboard() {
  const { token, logout } = useAuth();
  
  const username = token ? JSON.parse(atob(token.split('.')[1])).preferred_username.toUpperCase() : '';
  
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const handleFetchData = async () => {
    setLoading(true);
    toast.info("Connecting to National Exchange...", { description: "Fetching secure records from Education and Employment departments." });
    try {
      const response = await interoperabilityApi.get(`/api/v1/interop/fetch/all/${username}`);
      setData(response.data);
      toast.success("Verification Complete", { description: "Your records have been successfully retrieved and validated." });
    } catch (error) {
      toast.error("Interop Verification Failed", { description: "Could not contact Interoperability Service (Port 8082)." });
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitApplication = async () => {
    setSubmitting(true);
    const toastId = toast.loading("Processing Application...");
    try {
      // 1. Explicitly grant cryptographic consent
      await workflowApi.post('/api/v1/consents', {
        dataScope: 'education,employment,skills',
        purpose: 'verification',
        requestingDepartmentId: 'DEPT_EMP'
      });
      toast.success("Consent Granted", { id: toastId, description: "Your cryptographic consent has been recorded." });

      // 2. Submit application
      const appToast = toast.loading("Routing to Officer...");
      await applicationApi.post('/api/v1/applications', {
        citizenId: username,
        serviceCode: "SKILL_BENEFIT"
      });
      setSubmitted(true);
      toast.success("Application Submitted Successfully", { id: appToast, description: "Your application is now under officer review." });
    } catch (error) {
      toast.error("Submission Failed", { id: toastId, description: "Could not route application to workflow engine." });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-neutral-50 p-4 md:p-8">
      <div className="max-w-4xl mx-auto space-y-6">
        
        {/* Header */}
        <div className="flex justify-between items-center bg-white p-6 rounded-xl border shadow-sm">
          <div>
            <h1 className="text-2xl font-bold tracking-tight">Citizen Portal</h1>
            <p className="text-muted-foreground">Welcome back, <span className="font-semibold text-primary">{username}</span></p>
          </div>
          <Button variant="ghost" className="text-destructive hover:text-destructive/90 hover:bg-destructive/10" onClick={logout}>
            <LogOut className="w-4 h-4 mr-2" /> Logout
          </Button>
        </div>

        {/* Scheme Catalog Simulation */}
        <Card className="border-primary/20 shadow-md">
          <CardHeader className="bg-primary/5 border-b">
            <div className="flex justify-between items-start">
              <div>
                <CardTitle className="text-xl text-primary flex items-center gap-2">
                  <FileText className="w-5 h-5" /> State Youth Tech Scholarship
                </CardTitle>
                <CardDescription className="mt-2 text-base">Financial assistance for youth pursuing technical degrees. Zero document uploads required.</CardDescription>
              </div>
              <Badge variant="secondary" className="bg-blue-100 text-blue-800 hover:bg-blue-100">Active Scheme</Badge>
            </div>
          </CardHeader>
          <CardContent className="p-6">
            {!data && !loading && (
              <Button size="lg" onClick={handleFetchData} className="w-full sm:w-auto shadow-md">
                Start Application & Verify Records <ArrowRight className="w-4 h-4 ml-2" />
              </Button>
            )}

            {loading && (
              <div className="flex items-center text-primary font-medium p-4 bg-primary/5 rounded-lg border border-primary/20">
                <Loader2 className="w-5 h-5 mr-3 animate-spin" />
                Querying external interoperability APIs (Health, Education, Employment)...
              </div>
            )}

            {/* Display Fetched Canonical Data */}
            {data && !submitted && (
              <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
                <div className="flex items-center text-emerald-700 bg-emerald-50 p-4 rounded-lg border border-emerald-200">
                  <CheckCircle2 className="w-5 h-5 mr-3" />
                  <span className="font-semibold">Verified Canonical Data Retrieved</span>
                </div>
                
                <div className="bg-slate-950 p-4 rounded-lg text-sm font-mono overflow-x-auto text-emerald-400 border shadow-inner">
                  <pre>{JSON.stringify(data, null, 2)}</pre>
                </div>

                <div className="bg-amber-50 border border-amber-200 p-4 rounded-lg flex items-start gap-3">
                  <div className="bg-amber-100 p-2 rounded-full mt-0.5">
                    <CheckCircle2 className="w-4 h-4 text-amber-700" />
                  </div>
                  <div>
                    <h4 className="font-semibold text-amber-900">DPDP Consent Declaration</h4>
                    <p className="text-amber-800 text-sm mt-1">
                      I authorize the Government of Maharashtra to cryptographically sign and verify my education and employment data for evaluating this scheme.
                    </p>
                  </div>
                </div>

                <Button 
                  size="lg" 
                  className="w-full bg-emerald-600 hover:bg-emerald-700 text-white shadow-lg text-lg h-14"
                  onClick={handleSubmitApplication}
                  disabled={submitting}
                >
                  {submitting ? (
                    <><Loader2 className="w-5 h-5 mr-2 animate-spin" /> Granting Consent & Submitting...</>
                  ) : (
                    "Grant Cryptographic Consent & Submit Application"
                  )}
                </Button>
              </div>
            )}

            {/* Success State */}
            {submitted && (
              <div className="bg-emerald-50 border border-emerald-200 p-8 rounded-xl text-center animate-in zoom-in-95 duration-500">
                <div className="bg-emerald-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                  <CheckCircle2 className="w-8 h-8 text-emerald-600" />
                </div>
                <h3 className="text-2xl font-bold text-emerald-900 mb-2">Application Successfully Submitted!</h3>
                <p className="text-emerald-700 mb-6">Your application and cryptographic consent have been securely routed to the Camunda BPMN Engine.</p>
                <Button variant="outline" className="border-emerald-200 text-emerald-700 hover:bg-emerald-100" onClick={() => { setSubmitted(false); setData(null); }}>
                  Apply for Another Scheme
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        <div className="text-center pt-8">
          <Link to="/">
            <Button variant="link" className="text-muted-foreground">Return to Homepage</Button>
          </Link>
        </div>
      </div>
    </div>
  );
}
