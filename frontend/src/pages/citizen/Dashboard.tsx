import { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { applicationApi, workflowApi } from '@/lib/api';
import { Link } from 'react-router-dom';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Loader2, LogOut, FileText, ArrowRight, Lock, LayoutGrid, ShieldAlert, CheckCircle2, RefreshCw } from 'lucide-react';

// Helper to parse double-escaped strings and merge the canonical array into a single clean profile
const parseAndMergeData = (rawData: any) => {
  if (!rawData) return null;
  let parsed = rawData;
  if (typeof rawData === 'string') {
    try {
      parsed = JSON.parse(rawData);
    } catch(e) {
      return rawData;
    }
  }
  if (Array.isArray(parsed)) {
    return parsed.reduce((acc: any, current: any) => {
      Object.keys(current).forEach(key => {
        if (current[key] !== null && current[key] !== undefined) {
          acc[key] = current[key];
        }
      });
      return acc;
    }, {});
  }
  return parsed;
};

export default function CitizenDashboard() {
  const { token, logout } = useAuth();
  const username = token ? JSON.parse(atob(token.split('.')[1])).preferred_username.toUpperCase() : '';
  
  const [schemes, setSchemes] = useState<any[]>([]);
  const [loadingSchemes, setLoadingSchemes] = useState(true);
  
  const [selectedScheme, setSelectedScheme] = useState<any | null>(null);
  
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  
  // Real-time polling state
  const [applicationNumber, setApplicationNumber] = useState<string | null>(null);
  const [applicationStatus, setApplicationStatus] = useState<string>('');
  const [verificationData, setVerificationData] = useState<any>(null);

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

  // Poll for application status updates
  useEffect(() => {
    let interval: any;
    if (applicationNumber && applicationStatus !== 'PENDING_OFFICER_REVIEW' && applicationStatus !== 'FAILED' && applicationStatus !== 'CONSENT_DENIED') {
      interval = setInterval(async () => {
        try {
          const res = await applicationApi.get(`/api/v1/applications/${applicationNumber}`);
          setApplicationStatus(res.data.status);
          if (res.data.status === 'PENDING_OFFICER_REVIEW' && res.data.verificationData) {
            setVerificationData(parseAndMergeData(res.data.verificationData));
            toast.success("Records Verified", { description: "Interoperability check complete." });
          } else if (res.data.status === 'FAILED' || res.data.status === 'CONSENT_DENIED') {
            toast.error("Workflow Failed", { description: "The system could not verify your records." });
          }
        } catch (error) {
          console.error("Polling error", error);
        }
      }, 3000);
    }
    return () => clearInterval(interval);
  }, [applicationNumber, applicationStatus]);

  const handleStartApplication = (scheme: any) => {
    setSelectedScheme(scheme);
    setSubmitted(false);
    setApplicationNumber(null);
    setApplicationStatus('');
    setVerificationData(null);
  };

  const handleSubmitApplication = async () => {
    if (!selectedScheme) return;
    setSubmitting(true);
    const toastId = toast.loading("Processing Application...");
    try {
      // 1. Explicitly grant cryptographic consent FIRST
      await workflowApi.post('/api/v1/consents', {
        dataScope: selectedScheme.serviceCode === 'SCHOLARSHIP' ? 'education' : selectedScheme.serviceCode === 'SRV-EDU' ? 'education,health' : 'education,employment,skills',
        purpose: selectedScheme.serviceCode === 'SCHOLARSHIP' ? 'scholarship_verification' : 'verification',
        requestingDepartmentId: selectedScheme.departmentCode || 'DEPT_EMP'
      });
      toast.success("Consent Granted", { id: toastId, description: "Your cryptographic consent has been securely recorded." });

      // 2. Submit application for the specifically chosen service
      const appToast = toast.loading("Routing to Workflow Engine...");
      const res = await applicationApi.post('/api/v1/applications', {
        citizenId: username,
        serviceCode: selectedScheme.serviceCode
      });
      
      setApplicationNumber(res.data.applicationNumber);
      setApplicationStatus(res.data.status);
      setSubmitted(true);
      toast.success("Application Submitted Successfully", { id: appToast, description: "Your application is now being processed by the Camunda BPMN engine." });
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
          <div className="space-y-4 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <h2 className="text-xl font-bold flex items-center gap-2"><LayoutGrid className="w-5 h-5"/> Available Government Schemes</h2>
            {loadingSchemes ? (
              <div className="flex justify-center p-8"><Loader2 className="w-8 h-8 animate-spin text-primary" /></div>
            ) : (
              <div className="grid grid-cols-1 gap-4">
                {schemes.map((scheme: any) => (
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

        {/* Application Wizard - Consent Stage */}
        {selectedScheme && !submitted && (
          <Card className="border-primary/20 shadow-md animate-in fade-in slide-in-from-bottom-4 duration-500">
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
              
              <div className="space-y-6">
                <div className="flex items-center text-amber-900 bg-amber-50 p-4 rounded-lg border border-amber-200">
                  <ShieldAlert className="w-6 h-6 mr-3 text-amber-700" />
                  <div>
                    <span className="font-semibold block">Data Access Consent Required</span>
                    <span className="text-sm text-amber-800">Before we can evaluate your eligibility, you must grant permission for Ekikrit to securely fetch your records. No protected records have been retrieved yet.</span>
                  </div>
                </div>
                
                <div className="bg-white p-6 rounded-xl border shadow-sm space-y-4">
                  <h3 className="text-lg font-semibold border-b pb-2 flex items-center">
                    <Lock className="w-5 h-5 mr-2 text-slate-500" />
                    Consent Details
                  </h3>
                  
                  <div className="space-y-3 text-sm text-slate-700">
                    <div className="grid grid-cols-3 gap-2 border-b border-slate-100 pb-2">
                      <span className="font-medium text-slate-900">Purpose</span>
                      <span className="col-span-2">Eligibility verification for {selectedScheme.serviceName}.</span>
                    </div>
                    <div className="grid grid-cols-3 gap-2 border-b border-slate-100 pb-2">
                      <span className="font-medium text-slate-900">Requesting Dept</span>
                      <span className="col-span-2">{selectedScheme.departmentName || 'Employment Department'}</span>
                    </div>
                    <div className="grid grid-cols-3 gap-2">
                      <span className="font-medium text-slate-900">Data Scope</span>
                      <span className="col-span-2">
                        <ul className="list-disc list-inside space-y-1">
                          <li>Education & Qualifications</li>
                          <li>Employment Status & Income</li>
                          <li>Health & Disability Records (if applicable)</li>
                        </ul>
                      </span>
                    </div>
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
                    "Grant Cryptographic Consent & Continue"
                  )}
                </Button>
                <div className="text-center">
                  <Button variant="ghost" onClick={() => setSelectedScheme(null)} disabled={submitting}>
                    Cancel & Return
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Live Processing State */}
        {submitted && selectedScheme && !verificationData && (
          <div className="bg-white border p-8 rounded-xl text-center animate-in zoom-in-95 duration-500 shadow-sm mt-6">
            <div className="bg-blue-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
              <RefreshCw className="w-8 h-8 text-blue-600 animate-spin" />
            </div>
            <h3 className="text-2xl font-bold text-slate-900 mb-2">Consent Recorded ✓</h3>
            <p className="text-slate-600 mb-2">Your application has been submitted and securely routed to the Camunda Workflow Engine.</p>
            
            <div className="my-6 bg-slate-50 p-4 rounded-lg border border-slate-200 text-left max-w-sm mx-auto">
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Live Status</p>
              <div className="flex items-center gap-3">
                <Loader2 className="w-4 h-4 text-primary animate-spin" />
                <span className="text-sm font-medium">{applicationStatus || 'INITIALIZING'}</span>
              </div>
              <p className="text-xs text-slate-500 mt-2">Securely retrieving records from government departments. Please wait...</p>
            </div>
          </div>
        )}

        {/* Verification Result Display */}
        {submitted && selectedScheme && verificationData && (
          <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500 mt-6">
            <div className="flex items-center justify-between text-emerald-800 bg-emerald-50 p-6 rounded-xl border border-emerald-200 shadow-sm">
              <div className="flex items-center">
                <CheckCircle2 className="w-6 h-6 mr-3 text-emerald-600" />
                <div>
                  <span className="font-bold block text-lg">Records Retrieved & Verified</span>
                  <span className="text-sm">Your application has been safely sent for officer review.</span>
                </div>
              </div>
              <Badge variant="outline" className="bg-white text-emerald-700 border-emerald-300">Status: {applicationStatus}</Badge>
            </div>
            
            {/* Safe Data Display */}
            <div className="bg-white p-6 rounded-xl border shadow-sm space-y-4">
              <h3 className="text-lg font-semibold border-b pb-2 flex items-center">
                <FileText className="w-5 h-5 mr-2 text-primary" />
                Your Secure Verification Profile
              </h3>
              
              {/* Display nicely formatted JSON instead of double escaped array */}
              <div className="bg-slate-950 p-4 rounded-lg text-sm font-mono overflow-x-auto text-emerald-400 border shadow-inner">
                <pre>{JSON.stringify(verificationData, null, 2)}</pre>
              </div>

              <div className="mt-4 pt-4 border-t border-slate-100 flex items-start gap-2 text-xs text-muted-foreground">
                <div className="mt-0.5"><Lock className="w-3 h-3 text-slate-400" /></div>
                <p>
                  This data was securely fetched via Ekikrit Interoperability ONLY AFTER your explicit DPDP consent was recorded.
                </p>
              </div>
            </div>

            <Button size="lg" variant="outline" className="w-full h-14 border-slate-300 text-slate-700 hover:bg-slate-50" onClick={() => { setSubmitted(false); setSelectedScheme(null); setVerificationData(null); }}>
              Return to Scheme Catalog
            </Button>
          </div>
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
