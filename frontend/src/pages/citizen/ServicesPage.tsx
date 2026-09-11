import React, { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { serviceCatalogApi } from '@/services/serviceCatalog';
import { ServiceCard } from '@/components/citizen/ServiceCard';
import { Skeleton } from '@/components/feedback/Loading';
import { EmptyState, ErrorState } from '@/components/feedback/States';
import { Breadcrumb } from '@/components/ui/Breadcrumb';
import { Input } from '@/components/ui/input';
import { Search, Building2, X } from 'lucide-react';

export const ServicesPage: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedDept, setSelectedDept] = useState<string>('ALL');

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

  // Extract unique departments from existing data
  const departments = useMemo(() => {
    if (!services) return [];
    const depts = new Set<string>();
    services.forEach((s) => {
      if (s.departmentName) depts.add(s.departmentName);
      else if (s.departmentCode) depts.add(s.departmentCode);
    });
    return Array.from(depts);
  }, [services]);

  const filteredServices = useMemo(() => {
    if (!services) return [];
    return services.filter((s) => {
      const matchesSearch =
        !searchTerm.trim() ||
        s.serviceName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        s.serviceCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (s.departmentName && s.departmentName.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (s.description && s.description.toLowerCase().includes(searchTerm.toLowerCase()));

      const matchesDept =
        selectedDept === 'ALL' ||
        s.departmentName === selectedDept ||
        s.departmentCode === selectedDept;

      return matchesSearch && matchesDept;
    });
  }, [services, searchTerm, selectedDept]);

  return (
    <div className="space-y-6">
      {/* Breadcrumb Navigation */}
      <Breadcrumb
        items={[
          { label: 'Citizen Services', href: '/citizen' },
          { label: 'Schemes Directory' },
        ]}
      />

      {/* Page Header */}
      <div className="bg-white border border-slate-200 rounded-md p-6 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="bg-[#0B1F3A] text-white text-[10px] font-bold uppercase px-2 py-0.5 rounded-xs tracking-wider">
              State Scheme Catalog
            </span>
            <span className="text-xs text-slate-500 font-medium">
              Zero-Document Application Enabled
            </span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Government Services &amp; Schemes Directory
          </h1>
          <p className="text-xs sm:text-sm text-slate-600 mt-1 max-w-2xl leading-relaxed">
            Browse state welfare schemes integrated with Ekikrit. Applications are evaluated using canonical state
            source databases upon granting your explicit cryptographic consent.
          </p>
        </div>

        {/* Search Input Box */}
        <div className="w-full md:w-80 relative shrink-0">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
          <Input
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search by scheme or keyword..."
            className="pl-9 pr-8 text-xs bg-white h-9"
          />
          {searchTerm && (
            <button
              onClick={() => setSearchTerm('')}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-0.5"
              aria-label="Clear search query"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>
      </div>

      {/* Department Filter Chips (Based strictly on real data) */}
      {departments.length > 0 && (
        <div className="flex items-center gap-2 flex-wrap text-xs">
          <span className="text-slate-500 font-medium mr-1 flex items-center gap-1">
            <Building2 className="w-3.5 h-3.5" /> Filter:
          </span>
          <button
            type="button"
            onClick={() => setSelectedDept('ALL')}
            className={`px-3 py-1 rounded-xs font-semibold uppercase text-[11px] transition-colors border ${
              selectedDept === 'ALL'
                ? 'bg-[#0B1F3A] text-white border-[#0B1F3A]'
                : 'bg-white text-slate-700 border-slate-200 hover:bg-slate-50'
            }`}
          >
            All Departments ({services?.length || 0})
          </button>
          {departments.map((dept) => {
            const count = services?.filter((s) => s.departmentName === dept || s.departmentCode === dept).length || 0;
            return (
              <button
                key={dept}
                type="button"
                onClick={() => setSelectedDept(dept)}
                className={`px-3 py-1 rounded-xs font-semibold text-[11px] transition-colors border ${
                  selectedDept === dept
                    ? 'bg-[#0B1F3A] text-white border-[#0B1F3A]'
                    : 'bg-white text-slate-700 border-slate-200 hover:bg-slate-50'
                }`}
              >
                {dept} ({count})
              </button>
            );
          })}
        </div>
      )}

      {/* Loading Skeletons */}
      {isLoading && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4" data-testid="services-loading">
          {[1, 2, 3, 4, 5, 6].map((idx) => (
            <div key={idx} className="border border-slate-200 rounded-md p-5 bg-white space-y-3">
              <Skeleton className="h-4 w-2/3" />
              <Skeleton className="h-3 w-1/3" />
              <Skeleton className="h-14 w-full" />
              <Skeleton className="h-8 w-full rounded-sm" />
            </div>
          ))}
        </div>
      )}

      {/* Error State */}
      {isError && (
        <ErrorState
          title="Unable to communicate with Scheme Service"
          message={
            (error as any)?.message ||
            'Failed to load published schemes. Please ensure the backend application service is accessible.'
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
              ? `No active services matched "${searchTerm}". Try another search term or reset filters.`
              : 'There are currently no active public services published in the catalog.'
          }
          action={
            searchTerm || selectedDept !== 'ALL'
              ? {
                  label: 'Reset Filters',
                  onClick: () => {
                    setSearchTerm('');
                    setSelectedDept('ALL');
                  },
                }
              : undefined
          }
        />
      )}

      {/* Services Grid */}
      {!isLoading && !isError && filteredServices.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredServices.map((service) => (
            <ServiceCard key={service.serviceCode} service={service} />
          ))}
        </div>
      )}
    </div>
  );
};
