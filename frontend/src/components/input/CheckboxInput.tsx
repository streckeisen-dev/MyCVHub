import { Checkbox, CheckboxProps } from '@heroui/react'
import { ReactNode } from 'react'

export type CheckboxInputProps = Readonly<
  Omit<CheckboxProps, 'children'> & {
    label?: string | ReactNode,
    errorMessage?: string
  }
>

export function CheckboxInput(props: CheckboxInputProps) {
  const { errorMessage, label, ...checkboxProps } = props
  const labelClasses = checkboxProps.isInvalid ? 'text-danger' : ''
  return (
    <div>
      <div className="flex">
        <Checkbox {...checkboxProps} />
        {label && (typeof label === 'string' ? <p className={labelClasses}>{label}</p> : label)}
      </div>
      {errorMessage && <p className="text-danger text-small ml-7">{errorMessage}</p>}
    </div>
  )
}
