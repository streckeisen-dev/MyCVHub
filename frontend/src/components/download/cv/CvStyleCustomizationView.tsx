import { CVStyleOptionDto } from '@/types/cv/CVStyleDto.ts'
import { KeyValueObject } from '@/types/KeyValueObject.ts'
import { ColorPicker } from '@/components/input/ColorPicker.tsx'
import { Input } from '@heroui/react'
import { CVStyleOptionType } from '@/types/cv/CVStyleOptionType.ts'

export type CvStyleCustomizationViewProps = Readonly<{
  options: CVStyleOptionDto[];
  value: KeyValueObject<string>;
  onChange: (name: string, value: string) => void;
}>

export function CvStyleCustomizationView(props: CvStyleCustomizationViewProps) {
  const {options, value, onChange} = props

  function handleChange(name: string, value: string) {
    onChange(name, value)
  }

  return (
    <div className="min-w-lg">
      {options.map((option) => {
        if (option.type === CVStyleOptionType.COLOR) {
          return (
            <ColorPicker
              key={option.key}
              className="items-center"
              label={option.name}
              color={value[option.key]}
              onChange={(c) => handleChange(option.key, c.hex)}
            />
          )
        }
        return (
          <Input
            key={option.key}
            label={option.name}
            value={value[option.key]}
            onChange={(e) => handleChange(option.key, e.target.value)}
          />
        )
      })}
    </div>
  )
}
