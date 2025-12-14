import { ReactNode } from 'react'
import { Switch, SwitchProps } from '@heroui/react'

export type SwitchInputProps = Omit<Omit<SwitchProps, 'onChange'>, 'name'> & {
  name: string;
  hint?: string | undefined;
  onChange?: (name: string, val: boolean) => void;
};

export function SwitchInput(props: SwitchInputProps): ReactNode {
  const { name, hint, onChange, ...switchProps } = props

  function handleChange(val: boolean) {
    if (onChange) {
      onChange(name, val)
    }
  }
  return (
    <div>
      <Switch onValueChange={handleChange} {...switchProps} />
      {hint && <p className="text-gray-400 ml-14">{hint}</p>}
    </div>
  )
}
