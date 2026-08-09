import { DatePickerProps, DatePicker } from '@/components/ui/Fields.tsx'
import { ReactNode } from 'react'

import { FaXmark } from 'react-icons/fa6'
import { Button } from '@/components/ui/Button.tsx'
import clsx from 'clsx'

type DateInputProps = DatePickerProps & {
  isClearable?: boolean
  onClear?: () => void
}

export function DateInput(props: DateInputProps): ReactNode {
  const { className, isClearable, onClear, ...datePickerProps } = props
  return (
    <div className={clsx('flex w-full items-start gap-2', className)}>
      <DatePicker {...datePickerProps} className="min-w-0 flex-1" />
      {isClearable && (
        <Button
          type="button"
          variant="tertiary"
          className="mt-[1.625rem] h-10 min-w-10 shrink-0 rounded-full px-0 text-default-500"
          onPress={onClear}
        >
          <FaXmark size={18} />
        </Button>
      )}
    </div>
  )
}
