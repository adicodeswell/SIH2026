import React from 'react';
import { Link } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { ArrowRight, Building2, ShieldCheck } from 'lucide-react';
import type { ServiceResponse } from '@/types/service';

interface ServiceCardProps {
  service: ServiceResponse;
}

export const ServiceCard: React.FC<ServiceCardProps> = ({ service }) => {
  return (
    <Card className="border-slate-200 shadow-sm hover:border-slate-300 transition-all flex flex-col justify-between bg-white">
      <div>
        <CardHeader className="pb-3 border-b border-slate-100 bg-slate-50/50">
          <div className="flex items-start justify-between gap-3">
            <div className="space-y-1">
              <CardTitle className="text-base font-semibold text-slate-900 leading-snug">
                {service.serviceName}
              </CardTitle>
              <div className="flex items-center gap-1.5 text-xs text-slate-500 font-medium">
                <Building2 className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                <span>{service.departmentName || service.departmentCode || 'Government Department'}</span>
              </div>
            </div>
            <Badge variant="outline" className="bg-blue-50 text-blue-800 border-blue-200 text-xs shrink-0 font-mono">
              {service.serviceCode}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="pt-4 pb-4">
          <p className="text-sm text-slate-600 leading-relaxed line-clamp-3">
            {service.description || 'Verified government welfare scheme providing targeted citizen benefits.'}
          </p>
          <div className="mt-4 flex items-center gap-1.5 text-xs text-emerald-700 bg-emerald-50/80 px-2.5 py-1.5 rounded border border-emerald-200/60">
            <ShieldCheck className="w-3.5 h-3.5 shrink-0" />
            <span>Zero-document verification via DPDP consent</span>
          </div>
        </CardContent>
      </div>
      <div className="p-4 pt-0 border-t border-slate-100 bg-white flex items-center justify-between gap-2 mt-auto">
        <Link to={`/citizen/services/${service.serviceCode}`} className="w-full">
          <Button className="w-full justify-center gap-2 font-medium text-sm">
            View & Apply <ArrowRight className="w-4 h-4" />
          </Button>
        </Link>
      </div>
    </Card>
  );
};
