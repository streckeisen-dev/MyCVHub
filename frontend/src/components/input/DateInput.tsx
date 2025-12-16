import { ReactNode } from 'react'
import { DatePicker, DatePickerProps } from '@heroui/react'
import { FaXmark } from 'react-icons/fa6'

type DateInputProps = DatePickerProps & {
  isClearable?: boolean;
  onClear?: () => void;
};

export function DateInput(props: DateInputProps): ReactNode {
  const { isClearable, onClear, ...datePickerProps } = props
  return (
    <div className="flex w-full gap-2">
      <DatePicker
        {...datePickerProps}
      />
      {isClearable && (
        <FaXmark
          className="mt-5.5 cursor-pointer hover:bg-default rounded-full p-1 w-min"
          size={30}
          color={'gray'}
          onClick={onClear}
        />
      )}
    </div>
  )
}
