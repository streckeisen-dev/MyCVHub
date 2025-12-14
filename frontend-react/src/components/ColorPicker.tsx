import { ReactNode } from 'react'
import { ChromePicker, ChromePickerProps } from 'react-color'

export type ColorPickerProps = ChromePickerProps & {
  label?: string;
  isRequired?: boolean;
  errorMessage?: string;
};

export function ColorPicker(props: ColorPickerProps): ReactNode {
  const { label, isRequired, errorMessage, className , ...pickerProps } = props
  return (
    <div className={`flex flex-col gap-1 ${className}`}>
      {label && (
        <label className="text-foreground-500 flex gap-1">
          <p>{label}</p>
          {isRequired && <span className="text-danger">*</span>}
        </label>
      )}
      <ChromePicker {...pickerProps} />
      { errorMessage && <p className="text-danger text-small">{errorMessage}</p> }
    </div>
  )
}
