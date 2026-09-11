import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { consentServiceApi } from '@/services/consentService';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Breadcrumb } from '@/components/ui/Breadcrumb';
import { Loader2, XCircle, ShieldCheck, Building2, Lock } from 'lucide-react';
import { toast } from 'sonner';

export function CitizenConsentsPage() {
  const queryClient = useQueryClient();

  const { data: consents, isLoading } = useQuery({
    queryKey: ['consents'],
    queryFn: consentServiceApi.getConsents,
  });

  const revokeMutation = useMutation({
    mutationFn: consentServiceApi.revokeConsent,
    onSuccess: () => {
      toast.success('Consent revoked successfully');
      queryClient.invalidateQueries({ queryKey: ['consents'] });
    },
    onError: () => {
      toast.error('Failed to revoke consent');
    },
  });

  const handleRevoke = (id: string) => {
    if (
      window.confirm(
        'Are you sure you want to revoke this consent? Future automated verification for this service will be halted.'
      )
    ) {
      revokeMutation.mutate(id);
    }
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Breadcrumbs */}
      <Breadcrumb
        items={[
          { label: 'Citizen Services', href: '/citizen' },
          { label: 'DPDP Data Consents' },
        ]}
      />

      {/* Page Header */}
      <div className="bg-white border border-slate-200 rounded-md p-6 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="bg-[#0B1F3A] text-white text-[10px] font-bold uppercase px-2 py-0.5 rounded-xs tracking-wider">
              DPDP Act 2023 Ledger
            </span>
            <span className="text-xs text-slate-500 font-medium">
              Data Principal Control Center
            </span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Cryptographic Data Sharing Consents
          </h1>
          <p className="text-xs sm:text-sm text-slate-600 mt-1 max-w-2xl leading-relaxed">
            Under the Digital Personal Data Protection Act 2023, you hold complete sovereignty over your data.
            Review and manage all authorizations granted to state departments for zero-document verification.
          </p>
        </div>
      </div>

      {/* Consents List Card */}
      <Card className="border-slate-200 shadow-xs bg-white rounded-md">
        <CardHeader className="border-b border-slate-100 bg-slate-50/70 pb-3">
          <CardTitle className="text-sm font-bold text-slate-900 flex items-center gap-2">
            <Lock className="w-4 h-4 text-[#0B1F3A]" />
            <span>Active &amp; Historical Consent Declarations</span>
          </CardTitle>
          <CardDescription className="text-xs text-slate-500">
            Each entry represents an explicit, signed authorization bound to an application dossier.
          </CardDescription>
        </CardHeader>
        <CardContent className="p-4 sm:p-6">
          {isLoading ? (
            <div className="flex flex-col items-center justify-center p-12 space-y-2">
              <Loader2 className="w-6 h-6 animate-spin text-[#0B1F3A]" />
              <p className="text-xs text-slate-500 font-medium">Querying state consent records...</p>
            </div>
          ) : !consents || consents.length === 0 ? (
            <div className="text-center py-12">
              <ShieldCheck className="w-10 h-10 text-slate-300 mx-auto mb-2" />
              <h3 className="text-sm font-bold text-slate-900">No Consents on Record</h3>
              <p className="text-xs text-slate-500 mt-1 max-w-xs mx-auto">
                You have not granted any data sharing permissions yet. Applying for a welfare scheme will generate an explicit consent token.
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {consents.map((consent) => (
                <div
                  key={consent.id}
                  className="p-4 rounded-md border border-slate-200 bg-white flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center hover:border-slate-300 transition-all shadow-2xs"
                >
                  <div className="space-y-1.5 text-xs">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-slate-900 text-sm">{consent.purpose}</span>
                      <Badge variant={consent.status === 'ACTIVE' ? 'active' : 'failed'}>
                        {consent.status}
                      </Badge>
                    </div>

                    <div className="flex items-center gap-1.5 text-slate-600">
                      <strong className="text-slate-700">Data Scope:</strong>
                      <span className="font-mono bg-slate-100 px-1.5 py-0.5 rounded text-[11px] text-slate-800">
                        {consent.dataScope.split(',').join(', ')}
                      </span>
                    </div>

                    <p className="text-slate-500 flex items-center gap-1 text-[11px]">
                      <Building2 className="w-3 h-3 text-slate-400" />
                      Authorized Department: <span className="font-semibold text-slate-800">{consent.requestingDepartmentId}</span>
                      <span className="text-slate-300">•</span>
                      <span>Granted on {new Date(consent.grantedAt).toLocaleDateString()}</span>
                    </p>

                    {consent.revokedAt && (
                      <p className="text-[11px] text-red-600 font-medium">
                        Revocation timestamp: {new Date(consent.revokedAt).toLocaleString()}
                      </p>
                    )}
                  </div>

                  {consent.status === 'ACTIVE' && (
                    <Button
                      variant="destructive"
                      size="xs"
                      onClick={() => handleRevoke(consent.id)}
                      disabled={revokeMutation.isPending}
                      className="font-bold text-xs h-8 px-3 shrink-0"
                    >
                      {revokeMutation.isPending ? (
                        <Loader2 className="w-3.5 h-3.5 mr-1 animate-spin" />
                      ) : (
                        <XCircle className="w-3.5 h-3.5 mr-1" />
                      )}
                      Revoke Consent
                    </Button>
                  )}
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
