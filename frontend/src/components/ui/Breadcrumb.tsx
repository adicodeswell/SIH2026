import React from 'react';
import { Link } from 'react-router-dom';
import { ChevronRight, Home } from 'lucide-react';

export interface BreadcrumbItem {
  label: string;
  href?: string;
}

interface BreadcrumbProps {
  items: BreadcrumbItem[];
}

export const Breadcrumb: React.FC<BreadcrumbProps> = ({ items }) => {
  return (
    <nav aria-label="Breadcrumb" className="select-none mb-4">
      <ol className="flex items-center space-x-1.5 text-xs text-slate-500 flex-wrap">
        <li className="flex items-center">
          <Link
            to="/"
            className="flex items-center gap-1 hover:text-[#0B1F3A] transition-colors font-medium"
          >
            <Home className="w-3.5 h-3.5" />
            <span className="sr-only">Home</span>
          </Link>
        </li>
        {items.map((item, index) => {
          const isLast = index === items.length - 1;
          return (
            <li key={index} className="flex items-center space-x-1.5">
              <ChevronRight className="w-3.5 h-3.5 text-slate-400 shrink-0" />
              {item.href && !isLast ? (
                <Link
                  to={item.href}
                  className="hover:text-[#0B1F3A] transition-colors font-medium text-slate-600"
                >
                  {item.label}
                </Link>
              ) : (
                <span className="font-semibold text-slate-800 truncate" aria-current={isLast ? 'page' : undefined}>
                  {item.label}
                </span>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
};
