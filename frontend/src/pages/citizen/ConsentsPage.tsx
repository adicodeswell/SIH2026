import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { consentServiceApi } from '@/services/consentService';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Loader2, ShieldAlert, XCircle } from 'lucide-react';
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
    if (window.confirm('Are you sure you want to revoke this consent? Future processing for this service will be blocked.')) {
      revokeMutation.mutate(id);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Data Sharing Consents</h1>
        <p className="text-sm text-slate-500 mt-1">
          Manage the data sharing permissions you have granted to government departments.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <ShieldAlert className="w-5 h-5 text-primary" />
            Active & Historical Consents
          </CardTitle>
          <CardDescription>
            You have full control over your data. You can revoke active consents at any time.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex justify-center p-8">
              <Loader2 className="w-8 h-8 animate-spin text-primary" />
            </div>
          ) : !consents || consents.length === 0 ? (
            <div className="text-center py-12">
              <ShieldAlert className="w-12 h-12 text-slate-300 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-slate-900">No Consents Found</h3>
              <p className="text-slate-500 mt-1">You haven't granted any data sharing permissions yet.</p>
            </div>
          ) : (
            <div className="space-y-4">
              {consents.map((consent) => (
                <div key={consent.id} className="p-4 rounded-lg border border-slate-200 bg-white flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-slate-900">{consent.purpose}</span>
                      <Badge variant="outline" className={consent.status === 'ACTIVE' ? 'border-green-200 bg-green-50 text-green-700' : 'border-red-200 bg-red-50 text-red-700'}>
                        {consent.status}
                      </Badge>
                    </div>
                    <p className="text-sm text-slate-600">
                      <span className="font-medium">Data Scope:</span> {consent.dataScope.split(',').join(', ')}
                    </p>
                    <p className="text-xs text-slate-500">
                      Granted to <span className="font-medium">{consent.requestingDepartmentId}</span> on {new Date(consent.grantedAt).toLocaleDateString()}
                    </p>
                    {consent.revokedAt && (
                      <p className="text-xs text-red-600 mt-1">
                        Revoked on {new Date(consent.revokedAt).toLocaleString()}
                      </p>
                    )}
                  </div>
                  
                  {consent.status === 'ACTIVE' && (
                    <Button 
                      variant="destructive" 
                      size="sm"
                      onClick={() => handleRevoke(consent.id)}
                      disabled={revokeMutation.isPending}
                    >
                      {revokeMutation.isPending ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : <XCircle className="w-4 h-4 mr-2" />}
                      Revoke
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
