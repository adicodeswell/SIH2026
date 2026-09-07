import { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { interoperabilityApi, applicationApi, workflowApi } from '@/lib/api';
import { Link } from 'react-router-dom';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, LogOut, CheckCircle2, FileText, ArrowRight, Lock, LayoutGrid } from 'lucide-react';

export default function CitizenDashboard() {
  const { token, logout } = useAuth();
  const username = token ? JSON.parse(atob(token.split('.')[1])).preferred_username.toUpperCase() : '';
  
  const [schemes, setSchemes] = useState<any[]>([]);
  const [loadingSchemes, setLoadingSchemes] = useState(true);
  
  const [selectedScheme, setSelectedScheme] = useState<any | null>(null);
  
  const [data, setData] = useState<any>(null); // merged data
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  // Fetch schemes on mount
  useEffect(() => {
    const fetchSchemes = async () => {
      try {
        const response = await applicationApi.get('/api/v1/services');
        setSchemes(response.data);
      } catch (error) {
        toast.error("Failed to load schemes");
      } finally {
        setLoadingSchemes(false);
      }
    };
    fetchSchemes();
  }, []);

  const handleStartApplication = async (scheme: any) => {
    setSelectedScheme(scheme);
    setLoading(true);
    setSubmitted(false);
    setData(null);
    toast.info("Connecting to National Exchange...", { description: "Fetching secure records across government departments." });
    try {
      const response = await interoperabilityApi.get(`/api/v1/interop/fetch/all/${username}`);
      
      // Merge array of partial canonical data into one consolidated profile
      const mergedData = response.data.reduce((acc: any, current: any) => {
        Object.keys(current).forEach(key => {
          if (current[key] !== null && current[key] !== undefined) {
            acc[key] = current[key];
          }
        });
        return acc;
      }, {});

      setData(mergedData);
      toast.success("Verification Complete", { description: "Your records have been successfully retrieved and validated." });
    } catch (error) {
      toast.error("Interop Verification Failed", { description: "Could not contact Interoperability Service (Port 8082)." });
      setSelectedScheme(null);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitApplication = async () => {
    if (!selectedScheme) return;
    setSubmitting(true);
    const toastId = toast.loading("Processing Application...");
    try {
      // 1. Explicitly grant cryptographic consent
      await workflowApi.post('/api/v1/consents', {
        dataScope: 'education,employment,skills',
        purpose: 'verification',
        requestingDepartmentId: selectedScheme.departmentCode || 'DEPT_EMP'
      });
      toast.success("Consent Granted", { id: toastId, description: "Your cryptographic consent has been recorded." });

      // 2. Submit application for the specifically chosen service
      const appToast = toast.loading("Routing to Officer...");
      await applicationApi.post('/api/v1/applications', {
        citizenId: username,
        serviceCode: selectedScheme.serviceCode
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

        {/* Scheme Catalog */}
        {!selectedScheme && (
          <div className="space-y-4">
            <h2 className="text-xl font-bold flex items-center gap-2"><LayoutGrid className="w-5 h-5"/> Available Government Schemes</h2>
            {loadingSchemes ? (
              <div className="flex justify-center p-8"><Loader2 className="w-8 h-8 animate-spin text-primary" /></div>
            ) : (
              <div className="grid grid-cols-1 gap-4">
                {schemes.map((scheme) => (
                  <Card key={scheme.serviceCode} className="border-primary/20 shadow-sm hover:shadow-md transition">
                    <CardHeader className="bg-primary/5 border-b py-4">
                      <div className="flex justify-between items-start">
                        <div>
                          <CardTitle className="text-lg text-primary">{scheme.serviceName}</CardTitle>
                          <p className="text-sm text-muted-foreground mt-1">{scheme.departmentName}</p>
                        </div>
                        <Badge variant="secondary" className="bg-blue-100 text-blue-800">{scheme.serviceCode}</Badge>
                      </div>
                    </CardHeader>
                    <CardContent className="p-4">
                      <p className="text-sm text-slate-700 mb-4">{scheme.description}</p>
                      <Button onClick={() => handleStartApplication(scheme)} className="w-full sm:w-auto shadow-sm">
                        Apply Now (Zero Documents) <ArrowRight className="w-4 h-4 ml-2" />
                      </Button>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Application Wizard */}
        {selectedScheme && (
          <Card className="border-primary/20 shadow-md">
            <CardHeader className="bg-primary/5 border-b">
              <div className="flex justify-between items-start">
                <div>
                  <Button variant="ghost" size="sm" onClick={() => setSelectedScheme(null)} className="mb-2 -ml-2 text-muted-foreground">← Back to Catalog</Button>
                  <CardTitle className="text-xl text-primary flex items-center gap-2">
                    <FileText className="w-5 h-5" /> {selectedScheme.serviceName}
                  </CardTitle>
                  <CardDescription className="mt-2 text-base">{selectedScheme.description}</CardDescription>
                </div>
                <Badge variant="secondary" className="bg-blue-100 text-blue-800">{selectedScheme.serviceCode}</Badge>
              </div>
            </CardHeader>
            <CardContent className="p-6">
              
              {loading && (
                <div className="flex flex-col items-center justify-center p-12 text-primary font-medium bg-primary/5 rounded-lg border border-primary/20">
                  <Loader2 className="w-10 h-10 mb-4 animate-spin" />
                  <p>Querying external interoperability APIs...</p>
                  <p className="text-sm text-muted-foreground mt-2">Aggregating records from Health, Education, and Employment departments.</p>
                </div>
              )}

              {/* Display Merged Canonical Data as a Verified Read-Only Form */}
              {data && !submitted && (
                <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
                  <div className="flex items-center text-emerald-700 bg-emerald-50 p-4 rounded-lg border border-emerald-200">
                    <CheckCircle2 className="w-5 h-5 mr-3" />
                    <span className="font-semibold">Records Successfully Retrieved & Verified</span>
                  </div>
                  
                  {/* Verified Form Section */}
                  <div className="bg-white p-6 rounded-xl border shadow-sm space-y-6">
                    <h3 className="text-lg font-semibold border-b pb-2 flex items-center">
                      <FileText className="w-5 h-5 mr-2 text-primary" />
                      Consolidated Citizen Profile
                    </h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      {/* Identity Data */}
                      <div className="space-y-2">
                        <Label className="text-muted-foreground text-xs uppercase tracking-wider">Citizen ID (SSO)</Label>
                        <div className="relative">
                          <Input value={data.citizenId || username} disabled className="bg-slate-50 border-slate-200 text-slate-700 pr-10 font-medium" />
                          <Lock className="w-4 h-4 text-slate-400 absolute right-3 top-3" />
                        </div>
                      </div>
                      
                      <div className="space-y-2">
                        <Label className="text-muted-foreground text-xs uppercase tracking-wider">Full Name</Label>
                        <div className="relative">
                          <Input value={data.fullName || "Not Found"} disabled className="bg-emerald-50/50 border-emerald-200 text-slate-800 pr-10 font-medium" />
                          {data.fullName && <CheckCircle2 className="w-4 h-4 text-emerald-500 absolute right-3 top-3" />}
                        </div>
                        {data.fullName && <p className="text-[10px] text-emerald-600 font-medium flex items-center justify-end"><CheckCircle2 className="w-3 h-3 mr-1"/> Verified by Auth DB</p>}
                      </div>

                      <div className="space-y-2">
                        <Label className="text-muted-foreground text-xs uppercase tracking-wider">Date of Birth</Label>
                        <div className="relative">
                          <Input value={data.dateOfBirth || "Not Found"} disabled className="bg-emerald-50/50 border-emerald-200 text-slate-800 pr-10 font-medium" />
                          {data.dateOfBirth && <CheckCircle2 className="w-4 h-4 text-emerald-500 absolute right-3 top-3" />}
                        </div>
                      </div>

                      {/* Academic Data */}
                      <div className="space-y-2">
                        <Label className="text-muted-foreground text-xs uppercase tracking-wider">Highest Degree</Label>
                        <div className="relative">
                          <Input value={data.highestDegree || "Not Found"} disabled className="bg-emerald-50/50 border-emerald-200 text-slate-800 pr-10 font-medium" />
                          {data.highestDegree && <CheckCircle2 className="w-4 h-4 text-emerald-500 absolute right-3 top-3" />}
                        </div>
                        {data.highestDegree && <p className="text-[10px] text-emerald-600 font-medium flex items-center justify-end"><CheckCircle2 className="w-3 h-3 mr-1"/> Verified by Dept of Education</p>}
                      </div>

                      <div className="space-y-2">
                        <Label className="text-muted-foreground text-xs uppercase tracking-wider">Graduation Year</Label>
                        <div className="relative">
                          <Input value={data.graduationYear || "Not Found"} disabled className="bg-emerald-50/50 border-emerald-200 text-slate-800 pr-10 font-medium" />
                          {data.graduationYear && <CheckCircle2 className="w-4 h-4 text-emerald-500 absolute right-3 top-3" />}
                        </div>
                      </div>

                      {/* Employment Data */}
                      <div className="space-y-2">
                        <Label className="text-muted-foreground text-xs uppercase tracking-wider">Employment Status</Label>
                        <div className="relative">
                          <Input value={data.employmentStatus || "Not Found"} disabled className="bg-emerald-50/50 border-emerald-200 text-slate-800 pr-10 font-medium" />
                          {data.employmentStatus && <CheckCircle2 className="w-4 h-4 text-emerald-500 absolute right-3 top-3" />}
                        </div>
                        {data.employmentStatus && <p className="text-[10px] text-emerald-600 font-medium flex items-center justify-end"><CheckCircle2 className="w-3 h-3 mr-1"/> Verified by Dept of Employment</p>}
                      </div>

                    </div>

                    {/* Grievance Note */}
                    <div className="mt-4 pt-4 border-t border-slate-100 flex items-start gap-2 text-xs text-muted-foreground">
                      <div className="mt-0.5"><Lock className="w-3 h-3 text-slate-400" /></div>
                      <p>
                        This data is securely fetched from interconnected government databases. You cannot edit it manually. 
                        <button className="text-primary hover:underline ml-1 font-medium">Is this data incorrect? Raise a grievance.</button>
                      </p>
                    </div>
                  </div>

                  {/* DPDP Consent */}
                  <div className="bg-amber-50 border border-amber-200 p-4 rounded-lg flex items-start gap-3 mt-6">
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
                    className="w-full bg-emerald-600 hover:bg-emerald-700 text-white shadow-lg text-lg h-14 mt-4"
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
                  <p className="text-emerald-700 mb-6">Your application for {selectedScheme.serviceName} has been securely routed to the Camunda BPMN Engine.</p>
                  <Button variant="outline" className="border-emerald-200 text-emerald-700 hover:bg-emerald-100" onClick={() => { setSubmitted(false); setData(null); setSelectedScheme(null); }}>
                    Apply for Another Scheme
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
        )}

        <div className="text-center pt-8">
          <Link to="/">
            <Button variant="link" className="text-muted-foreground">Return to Homepage</Button>
          </Link>
        </div>
      </div>
    </div>
  );
}
