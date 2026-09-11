import * as React from "react"
import { Input as InputPrimitive } from "@base-ui/react/input"
import { cn } from "cn"

function Input({ className, type, ...props }: React.ComponentProps<"input">) {
  return (
    <InputPrimitive
      type={type}
      data-slot="input"
      className={cn(
        "h-9 w-full min-w-0 rounded-md border border-slate-300 bg-white px-3 py-1.5 text-sm text-slate-900 transition-colors outline-none placeholder:text-slate-400 focus-visible:border-[#0B1F3A] focus-visible:ring-1 focus-visible:ring-[#0B1F3A] disabled:pointer-events-none disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-500 aria-invalid:border-red-500 aria-invalid:ring-1 aria-invalid:ring-red-500",
        className
      )}
      {...props}
    />
  )
}

export { Input }
