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
    <Card className="border-slate-200 shadow-xs hover:border-[#0B1F3A]/40 transition-all flex flex-col justify-between bg-white rounded-md overflow-hidden group">
      <div>
        <CardHeader className="pb-3 border-b border-slate-100 bg-slate-50/70">
          <div className="flex items-start justify-between gap-2.5">
            <div className="space-y-1">
              <div className="flex items-center gap-1.5 text-[11px] text-slate-500 font-medium">
                <Building2 className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                <span className="truncate">{service.departmentName || service.departmentCode || 'Government Department'}</span>
              </div>
              <CardTitle className="text-sm font-bold text-slate-900 leading-snug group-hover:text-[#0B1F3A]">
                {service.serviceName}
              </CardTitle>
            </div>
            <Badge variant="service" className="text-[11px] shrink-0 font-mono">
              {service.serviceCode}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="pt-3.5 pb-3.5 space-y-3">
          <p className="text-xs text-slate-600 leading-relaxed line-clamp-3">
            {service.description || 'Verified government welfare scheme providing targeted citizen benefits.'}
          </p>

          <div className="flex items-center gap-1.5 text-[11px] text-emerald-800 bg-emerald-50 px-2 py-1 rounded-xs border border-emerald-200/80">
            <ShieldCheck className="w-3.5 h-3.5 text-emerald-700 shrink-0" />
            <span className="truncate">Zero-document verification via DPDP consent</span>
          </div>
        </CardContent>
      </div>

      <div className="p-3.5 pt-0 border-t border-slate-100 bg-white flex items-center justify-between gap-2 mt-auto">
        <Link to={`/citizen/services/${service.serviceCode}`} className="w-full">
          <Button className="w-full justify-center gap-1.5 font-semibold text-xs h-8.5 bg-[#0B1F3A] hover:bg-[#102A43] text-white">
            Apply Online <ArrowRight className="w-3.5 h-3.5" />
          </Button>
        </Link>
      </div>
    </Card>
  );
};
