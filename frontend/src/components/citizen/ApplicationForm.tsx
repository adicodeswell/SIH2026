import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Shield, ShieldAlert, CheckCircle2, Loader2, AlertCircle, FileText } from 'lucide-react';
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
      newErrors.citizenId = 'Citizen Identifier must not be blank';
    } else if (enteredCitizenId.trim().length < 3) {
      newErrors.citizenId = 'Citizen Identifier must be at least 3 characters';
    }

    if (!consentAcknowledged) {
      newErrors.consent = 'You must grant explicit consent under the DPDP Act 2023 to proceed';
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
    <form onSubmit={handleSubmit} noValidate className="space-y-5">
      {/* Scheme Identification Form Card */}
      <Card className="border-slate-200 shadow-xs bg-white rounded-md">
        <CardHeader className="border-b border-slate-100 bg-slate-50/70 pb-3">
          <CardTitle className="text-sm font-bold text-slate-900 flex items-center gap-2">
            <FileText className="w-4 h-4 text-[#0B1F3A]" />
            <span>Application Parameters &amp; Identity Verification</span>
          </CardTitle>
          <CardDescription className="text-xs text-slate-500">
            Authenticated via Single Sign-On and National Interoperability Framework
          </CardDescription>
        </CardHeader>
        <CardContent className="pt-4 space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1">
              <Label htmlFor="serviceCode" className="text-xs font-semibold text-slate-700">
                Service Scheme Code
              </Label>
              <Input
                id="serviceCode"
                value={service.serviceCode}
                disabled
                className="bg-slate-100 font-mono text-xs text-slate-700 cursor-not-allowed h-9"
              />
            </div>

            <div className="space-y-1">
              <Label htmlFor="departmentCode" className="text-xs font-semibold text-slate-700">
                Administering Department
              </Label>
              <Input
                id="departmentCode"
                value={service.departmentName || service.departmentCode}
                disabled
                className="bg-slate-100 text-xs text-slate-700 cursor-not-allowed h-9"
              />
            </div>
          </div>

          <div className="space-y-1">
            <Label htmlFor="serviceName" className="text-xs font-semibold text-slate-700">
              Scheme Name
            </Label>
            <Input
              id="serviceName"
              value={service.serviceName}
              disabled
              className="bg-slate-100 text-xs font-medium text-slate-700 cursor-not-allowed h-9"
            />
          </div>

          <div className="space-y-1">
            <div className="flex justify-between items-center">
              <Label htmlFor="citizenId" className="text-xs font-semibold text-slate-700">
                Applicant Citizen Identifier <span className="text-red-600" aria-hidden="true">*</span>
              </Label>
              <span className="text-[11px] text-slate-400">Canonical Registry ID</span>
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
              placeholder="e.g. MH1001"
              aria-invalid={!!errors.citizenId}
              aria-describedby={errors.citizenId ? 'citizenId-error' : undefined}
              className={`font-mono text-xs h-9 ${errors.citizenId ? 'border-red-500 focus-visible:ring-red-400' : ''}`}
            />
            {errors.citizenId && (
              <p id="citizenId-error" className="text-xs text-red-600 flex items-center gap-1 mt-1">
                <AlertCircle className="w-3.5 h-3.5" />
                {errors.citizenId}
              </p>
            )}
            <p className="text-[11px] text-slate-500">
              This identifier maps directly to your canonical citizen records in state databases.
            </p>
          </div>
        </CardContent>
      </Card>

      {/* DPDP Act 2023 Cryptographic Consent Declaration Card */}
      <Card className="border-amber-300 bg-amber-50/50 shadow-xs rounded-md">
        <CardHeader className="border-b border-amber-200/80 pb-3 bg-amber-50/80">
          <div className="flex items-center gap-2">
            <ShieldAlert className="w-5 h-5 text-amber-800" />
            <CardTitle className="text-sm font-bold text-amber-950">
              DPDP Act 2023 Cryptographic Consent Declaration
            </CardTitle>
          </div>
          <CardDescription className="text-xs text-amber-900">
            Mandatory prior digital authorization for zero-document interoperability data retrieval
          </CardDescription>
        </CardHeader>
        <CardContent className="pt-4 space-y-3.5 text-xs text-slate-700">
          <p className="leading-relaxed text-slate-800">
            By applying for <strong>{service.serviceName}</strong>, you grant cryptographic authorization to the{' '}
            <strong>{service.departmentName || service.departmentCode || 'competent government department'}</strong>{' '}
            to query state canonical registries exclusively for validating your eligibility criteria.
          </p>

          <div className="bg-white p-3 rounded border border-amber-200/80 space-y-1.5 text-slate-800">
            <div className="flex items-center gap-2 font-medium">
              <Shield className="w-4 h-4 text-emerald-600 shrink-0" />
              <span>Consent Purpose: Verification &amp; Evaluation for {service.serviceName}</span>
            </div>
            <div className="flex items-center gap-2 font-medium">
              <CheckCircle2 className="w-4 h-4 text-blue-600 shrink-0" />
              <span>Data Scope: Academic records, employment history, certified skill credentials &amp; welfare records</span>
            </div>
          </div>

          <div className="pt-1">
            <label className="flex items-start gap-2.5 cursor-pointer select-none">
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
                className="mt-0.5 h-4 w-4 rounded-xs border-slate-300 text-[#0B1F3A] focus:ring-[#0B1F3A]"
                aria-describedby={errors.consent ? 'consent-error' : undefined}
              />
              <span className="text-xs text-slate-900 font-semibold leading-relaxed">
                I hereby grant explicit, verifiable cryptographic consent under the Digital Personal Data Protection
                (DPDP) Act 2023 for Ekikrit to fetch my verified records on my behalf. <span className="text-red-600">*</span>
              </span>
            </label>
            {errors.consent && (
              <p id="consent-error" className="text-xs text-red-600 flex items-center gap-1 mt-1.5 font-medium">
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
          className="w-full sm:w-auto text-xs h-9"
        >
          Cancel &amp; Return
        </Button>
        <Button
          type="submit"
          disabled={isSubmitting}
          className="w-full sm:w-auto min-w-[210px] h-9 font-bold text-xs bg-[#0B1F3A] hover:bg-[#102A43] text-white gap-2 shadow-xs"
        >
          {isSubmitting ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              Submitting Application...
            </>
          ) : (
            <>
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
              Submit Application &amp; Consent
            </>
          )}
        </Button>
      </div>
    </form>
  );
};
