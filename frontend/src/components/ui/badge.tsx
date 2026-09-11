import { mergeProps } from "@base-ui/react/merge-props"
import { useRender } from "@base-ui/react/use-render"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "cn"

const badgeVariants = cva(
  "group/badge inline-flex items-center justify-center gap-1 overflow-hidden rounded-sm border px-2 py-0.5 text-xs font-semibold uppercase tracking-wider whitespace-nowrap transition-colors focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/50 [&>svg]:pointer-events-none [&>svg]:size-3!",
  {
    variants: {
      variant: {
        default: "border-slate-800 bg-[#0B1F3A] text-white",
        secondary: "border-slate-200 bg-slate-100 text-slate-800",
        destructive: "border-red-300 bg-red-50 text-red-800",
        outline: "border-slate-300 bg-white text-slate-700",
        ghost: "border-transparent bg-transparent text-slate-700 hover:bg-slate-100",
        link: "border-transparent text-[#0B1F3A] underline-offset-4 hover:underline",
        // Official Government Status Badges
        active: "border-emerald-300 bg-emerald-50 text-emerald-800",
        success: "border-emerald-300 bg-emerald-50 text-emerald-800",
        pending: "border-amber-300 bg-amber-50 text-amber-800",
        review: "border-amber-300 bg-amber-50 text-amber-800",
        failed: "border-red-300 bg-red-50 text-red-800",
        rejected: "border-red-300 bg-red-50 text-red-800",
        info: "border-blue-300 bg-blue-50 text-blue-800",
        service: "border-sky-300 bg-sky-50 text-sky-900 font-mono",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  }
)

function Badge({
  className,
  variant = "default",
  render,
  ...props
}: useRender.ComponentProps<"span"> & VariantProps<typeof badgeVariants>) {
  return useRender({
    defaultTagName: "span",
    props: mergeProps<"span">(
      {
        className: cn(badgeVariants({ variant }), className),
      },
      props
    ),
    render,
    state: {
      slot: "badge",
      variant,
    },
  })
}

export { Badge, badgeVariants }
