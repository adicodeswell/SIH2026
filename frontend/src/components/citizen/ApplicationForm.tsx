import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Shield, ShieldAlert, CheckCircle2, Loader2, AlertCircle } from 'lucide-react';
import type { ServiceResponse } from '@/types/service';

interface ApplicationFormProps {
  service: ServiceResponse;
  citizenId: string;
  isSubmitting: boolean;
  onSubmit: (formData: { citizenId: string; serviceCode: string; grantConsent: boolean }) => void;
  onCancel: () => void;
}

export const ApplicationForm: React.FC<ApplicationFormProps> = ({
  service,
  citizenId,
  isSubmitting,
  onSubmit,
  onCancel,
}) => {
  const [enteredCitizenId, setEnteredCitizenId] = useState(citizenId);
  const [consentAcknowledged, setConsentAcknowledged] = useState(false);
  const [errors, setErrors] = useState<{ citizenId?: string; consent?: string }>({});

  const validate = () => {
    const newErrors: { citizenId?: string; consent?: string } = {};

    if (!enteredCitizenId.trim()) {
      newErrors.citizenId = 'Citizen ID must not be blank';
    } else if (enteredCitizenId.trim().length < 3) {
      newErrors.citizenId = 'Citizen ID must be at least 3 characters';
    }

    if (!consentAcknowledged) {
      newErrors.consent = 'You must grant explicit consent under the DPDP Act to proceed';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    onSubmit({
      citizenId: enteredCitizenId.trim(),
      serviceCode: service.serviceCode,
      grantConsent: consentAcknowledged,
    });
  };

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-6">
      {/* Application Fields Card */}
      <Card className="border-slate-200 shadow-sm bg-white">
        <CardHeader className="border-b border-slate-100 bg-slate-50/50 pb-4">
          <CardTitle className="text-base font-semibold text-slate-900">
            Applicant & Scheme Identification
          </CardTitle>
          <CardDescription className="text-xs text-slate-500">
            Verified with National Single Sign-On and State Registry
          </CardDescription>
        </CardHeader>
        <CardContent className="pt-5 space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="serviceCode" className="text-xs font-semibold text-slate-700">
              Selected Service Code
            </Label>
            <Input
              id="serviceCode"
              value={service.serviceCode}
              disabled
              className="bg-slate-100 font-mono text-sm text-slate-700 cursor-not-allowed"
            />
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="serviceName" className="text-xs font-semibold text-slate-700">
              Service Scheme Name
            </Label>
            <Input
              id="serviceName"
              value={service.serviceName}
              disabled
              className="bg-slate-100 text-sm text-slate-700 cursor-not-allowed"
            />
          </div>

          <div className="space-y-1.5">
            <div className="flex justify-between items-center">
              <Label htmlFor="citizenId" className="text-xs font-semibold text-slate-700">
                Citizen Identifier <span className="text-red-500" aria-hidden="true">*</span>
              </Label>
              <span className="text-[11px] text-slate-400">Authenticated via Keycloak SSO</span>
            </div>
            <Input
              id="citizenId"
              value={enteredCitizenId}
              onChange={(e) => {
                setEnteredCitizenId(e.target.value);
                if (errors.citizenId) {
                  setErrors((prev) => ({ ...prev, citizenId: undefined }));
                }
              }}
              placeholder="e.g. MH1001 or preferred username"
              aria-invalid={!!errors.citizenId}
              aria-describedby={errors.citizenId ? 'citizenId-error' : undefined}
              className={`font-mono ${errors.citizenId ? 'border-red-500 focus-visible:ring-red-400' : ''}`}
            />
            {errors.citizenId && (
              <p id="citizenId-error" className="text-xs text-red-600 flex items-center gap-1 mt-1">
                <AlertCircle className="w-3.5 h-3.5" />
                {errors.citizenId}
              </p>
            )}
            <p className="text-[11px] text-slate-500">
              This identifier maps to your canonical citizen profile in the state registry.
            </p>
          </div>
        </CardContent>
      </Card>

      {/* Cryptographic Consent Card */}
      <Card className="border-amber-200 bg-amber-50/40 shadow-sm">
        <CardHeader className="border-b border-amber-200/60 pb-3">
          <div className="flex items-center gap-2">
            <ShieldAlert className="w-5 h-5 text-amber-700" />
            <CardTitle className="text-base font-semibold text-amber-950">
              DPDP Act 2023 Cryptographic Consent Declaration
            </CardTitle>
          </div>
          <CardDescription className="text-xs text-amber-800">
            Mandatory prior authorization for zero-document interoperability data retrieval
          </CardDescription>
        </CardHeader>
        <CardContent className="pt-4 space-y-4 text-sm text-slate-700">
          <p className="text-xs leading-relaxed text-slate-700">
            By applying for <strong>{service.serviceName}</strong>, you grant cryptographic authorization to the{' '}
            <strong>{service.departmentName || service.departmentCode || 'requesting government department'}</strong>{' '}
            to query state canonical registries solely for verifying your eligibility.
          </p>

          <div className="bg-white p-3.5 rounded-lg border border-amber-200 text-xs space-y-1.5">
            <div className="flex items-center gap-2 text-slate-800 font-medium">
              <Shield className="w-4 h-4 text-emerald-600 shrink-0" />
              <span>Consent Purpose: Verification for {service.serviceName}</span>
            </div>
            <div className="flex items-center gap-2 text-slate-800 font-medium">
              <CheckCircle2 className="w-4 h-4 text-blue-600 shrink-0" />
              <span>Data Scope: Academic qualifications, employment, skill certifications, & health records</span>
            </div>
          </div>

          <div className="pt-2">
            <label className="flex items-start gap-3 cursor-pointer select-none">
              <input
                type="checkbox"
                id="consentCheckbox"
                checked={consentAcknowledged}
                onChange={(e) => {
                  setConsentAcknowledged(e.target.checked);
                  if (errors.consent) {
                    setErrors((prev) => ({ ...prev, consent: undefined }));
                  }
                }}
                className="mt-1 h-4 w-4 rounded border-slate-300 text-primary focus:ring-primary"
                aria-describedby={errors.consent ? 'consent-error' : undefined}
              />
              <span className="text-xs text-slate-800 font-medium leading-relaxed">
                I hereby grant explicit, verifiable cryptographic consent under the Digital Personal Data Protection
                (DPDP) Act 2023 for Ekikrit to fetch my verified records on my behalf. <span className="text-red-500">*</span>
              </span>
            </label>
            {errors.consent && (
              <p id="consent-error" className="text-xs text-red-600 flex items-center gap-1 mt-2">
                <AlertCircle className="w-3.5 h-3.5" />
                {errors.consent}
              </p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Action Buttons */}
      <div className="flex flex-col-reverse sm:flex-row items-center justify-end gap-3 pt-2">
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isSubmitting}
          className="w-full sm:w-auto"
        >
          Cancel & Return
        </Button>
        <Button
          type="submit"
          disabled={isSubmitting}
          className="w-full sm:w-auto min-w-[200px] h-10 font-semibold gap-2 shadow-sm"
        >
          {isSubmitting ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              Submitting Application...
            </>
          ) : (
            <>
              <CheckCircle2 className="w-4 h-4" />
              Submit Application
            </>
          )}
        </Button>
      </div>
    </form>
  );
};
