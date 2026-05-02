import { CVStyleOptionDto } from '@/types/cv/CVStyleDto.ts'
import { KeyValueObject } from '@/types/KeyValueObject.ts'
import { ColorPicker } from '@/components/input/ColorPicker.tsx'
import { Input } from '@heroui/react'
import { CVStyleOptionType } from '@/types/cv/CVStyleOptionType.ts'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { ChangeEvent } from 'react'

export type CvStyleCustomizationViewProps = Readonly<{
  options: CVStyleOptionDto[]
  value: KeyValueObject<string>
  onChange: (name: string, value: string) => void
  errorMessages?: ErrorMessages
}>

export function CvStyleCustomizationView(props: CvStyleCustomizationViewProps) {
  const { options, value, onChange, errorMessages } = props

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
              errorMessage={errorMessages?.[option.key]}
            />
          )
        }
        return (
          <Input
            key={option.key}
            label={option.name}
            value={value[option.key]}
            onChange={(e: ChangeEvent<HTMLInputElement>) => handleChange(option.key, e.target.value)}
            isInvalid={errorMessages?.[option.key] != null}
            errorMessage={errorMessages?.[option.key]}
          />
        )
      })}
    </div>
  )
}
