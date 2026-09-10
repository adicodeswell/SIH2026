import React, { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { serviceCatalogApi } from '@/services/serviceCatalog';
import { ServiceCard } from '@/components/citizen/ServiceCard';
import { Skeleton } from '@/components/feedback/Loading';
import { EmptyState, ErrorState } from '@/components/feedback/States';
import { Input } from '@/components/ui/input';
import { Search, LayoutGrid } from 'lucide-react';

export const ServicesPage: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');

  const {
    data: services,
    isLoading,
    isError,
    error,
    refetch,
  } = useQuery({
    queryKey: ['services'],
    queryFn: serviceCatalogApi.getServices,
  });

  const filteredServices = useMemo(() => {
    if (!services) return [];
    if (!searchTerm.trim()) return services;
    const lower = searchTerm.toLowerCase();
    return services.filter(
      (s) =>
        s.serviceName.toLowerCase().includes(lower) ||
        s.serviceCode.toLowerCase().includes(lower) ||
        (s.departmentName && s.departmentName.toLowerCase().includes(lower)) ||
        (s.description && s.description.toLowerCase().includes(lower))
    );
  }, [services, searchTerm]);

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 border-b border-slate-200 pb-5">
        <div>
          <div className="flex items-center gap-2">
            <LayoutGrid className="w-6 h-6 text-primary" />
            <h1 className="text-2xl font-bold tracking-tight text-slate-900">Government Services & Schemes</h1>
          </div>
          <p className="text-sm text-slate-500 mt-1">
            Browse verified welfare programs available for automated zero-document application through Ekikrit.
          </p>
        </div>

        {/* Search Input */}
        <div className="w-full sm:w-72 relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
          <Input
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search schemes or departments..."
            className="pl-9 text-sm bg-white"
          />
        </div>
      </div>

      {/* Loading State */}
      {isLoading && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6" data-testid="services-loading">
          {[1, 2, 3, 4, 5, 6].map((idx) => (
            <div key={idx} className="border border-slate-200 rounded-xl p-5 bg-white space-y-4">
              <Skeleton className="h-5 w-2/3" />
              <Skeleton className="h-4 w-1/3" />
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-9 w-full rounded-lg" />
            </div>
          ))}
        </div>
      )}

      {/* Error State */}
      {isError && (
        <ErrorState
          title="Unable to load government schemes"
          message={
            (error as any)?.message ||
            'Failed to communicate with the application service. Please verify your connection or try again.'
          }
          onRetry={() => refetch()}
        />
      )}

      {/* Empty State */}
      {!isLoading && !isError && filteredServices.length === 0 && (
        <EmptyState
          title={searchTerm ? 'No matching schemes found' : 'No government schemes available'}
          description={
            searchTerm
              ? `No active services matched "${searchTerm}". Try searching for a different keyword or department.`
              : 'There are currently no active public services published in the catalog.'
          }
          action={
            searchTerm
              ? {
                  label: 'Clear Search Filter',
                  onClick: () => setSearchTerm(''),
                }
              : undefined
          }
        />
      )}

      {/* Services Grid */}
      {!isLoading && !isError && filteredServices.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredServices.map((service) => (
            <ServiceCard key={service.serviceCode} service={service} />
          ))}
        </div>
      )}
    </div>
  );
};
