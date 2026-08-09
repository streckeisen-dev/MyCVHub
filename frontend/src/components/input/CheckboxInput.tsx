import { Checkbox, CheckboxProps } from '@/components/ui/Fields.tsx'
import { ReactNode } from 'react'

export type CheckboxInputProps = Readonly<
  Omit<CheckboxProps, 'children'> & {
    label?: string | ReactNode
    errorMessage?: string
  }
>

export function CheckboxInput(props: CheckboxInputProps) {
  const { errorMessage, label, ...checkboxProps } = props
  const labelClasses = checkboxProps.isInvalid ? 'text-danger' : ''
  return (
    <div>
      <Checkbox
        {...checkboxProps}
        label={
          label &&
          (typeof label === 'string' ? <span className={labelClasses}>{label}</span> : label)
        }
      />
      {errorMessage && <p className="text-danger text-small ml-8 mt-1">{errorMessage}</p>}
    </div>
  )
}
