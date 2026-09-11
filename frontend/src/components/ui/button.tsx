import { Button as ButtonPrimitive } from "@base-ui/react/button"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "cn"

const buttonVariants = cva(
  "group/button inline-flex shrink-0 items-center justify-center rounded-md border border-transparent text-sm font-medium whitespace-nowrap transition-colors outline-none select-none focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-2 aria-invalid:ring-destructive/20 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4 cursor-pointer shadow-xs",
  {
    variants: {
      variant: {
        default: "bg-[#0B1F3A] text-white hover:bg-[#102A43] active:bg-[#0B1F3A]",
        outline:
          "border-slate-300 bg-white text-slate-800 hover:bg-slate-50 hover:text-slate-900",
        secondary:
          "border-slate-200 bg-slate-100 text-slate-800 hover:bg-slate-200",
        ghost:
          "hover:bg-slate-100 hover:text-slate-900 border-transparent shadow-none",
        destructive:
          "bg-red-600 text-white hover:bg-red-700",
        saffron:
          "bg-[#F97316] text-white hover:bg-[#EA580C]",
        success:
          "bg-[#138808] text-white hover:bg-[#0E6806]",
        link: "text-[#0B1F3A] underline-offset-4 hover:underline shadow-none",
      },
      size: {
        default: "h-9 gap-1.5 px-3.5",
        xs: "h-6 gap-1 px-2 text-xs",
        sm: "h-8 gap-1.5 px-2.5 text-xs",
        lg: "h-10 gap-2 px-5 text-sm font-semibold",
        icon: "size-9",
        "icon-xs": "size-6",
        "icon-sm": "size-8",
        "icon-lg": "size-10",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

function Button({
  className,
  variant = "default",
  size = "default",
  ...props
}: ButtonPrimitive.Props & VariantProps<typeof buttonVariants>) {
  return (
    <ButtonPrimitive
      data-slot="button"
      className={cn(buttonVariants({ variant, size, className }))}
      {...props}
    />
  )
}

export { Button, buttonVariants }
