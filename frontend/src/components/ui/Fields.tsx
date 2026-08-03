import {
  Calendar,
  Checkbox as HeroCheckbox,
  ComboBox,
  DateField,
  DatePicker as HeroDatePicker,
  Description,
  FieldError,
  Input as HeroInput,
  Label,
  ListBox,
  Slider as HeroSlider,
  Switch as HeroSwitch,
  TextArea as HeroTextArea,
  TextField
} from '@heroui/react'
import { ChangeEvent, InputHTMLAttributes, ReactNode, TextareaHTMLAttributes } from 'react'
import { Key } from '@react-types/shared'
import { DateValue } from 'react-aria-components'
import clsx from 'clsx'

export type InputProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'onChange'> & {
  description?: ReactNode
  endContent?: ReactNode
  errorMessage?: ReactNode
  isClearable?: boolean
  isInvalid?: boolean
  isRequired?: boolean
  label?: ReactNode
  labelPlacement?: string
  onChange?: (event: ChangeEvent<HTMLInputElement>) => void
  onValueChange?: (value: string) => void
  startContent?: ReactNode
}

export type TextareaProps = Omit<TextareaHTMLAttributes<HTMLTextAreaElement>, 'onChange'> & {
  description?: ReactNode
  errorMessage?: ReactNode
  isInvalid?: boolean
  isRequired?: boolean
  label?: ReactNode
  labelPlacement?: string
  maxRows?: number
  minRows?: number
  ref?: any
  onChange?: (event: any) => void
  onValueChange?: (value: string) => void
}

export function Input(props: InputProps) {
  const {
    className,
    description,
    endContent,
    errorMessage,
    isClearable: _isClearable,
    isInvalid,
    isRequired,
    label,
    labelPlacement: _labelPlacement,
    onValueChange,
    startContent,
    ...rest
  } = props

  return (
    <TextField
      isInvalid={isInvalid}
      isRequired={isRequired}
      className={clsx('flex w-full flex-col gap-1.5 px-1 py-0.5', className)}
      name={rest.name}
      type={rest.type}
      value={rest.value as string | undefined}
      onChange={(value) => onValueChange?.(value)}
      validationBehavior="aria"
    >
      {label && <Label className="text-sm font-medium leading-5 text-default-700">{label}</Label>}
      <div className="flex w-full items-center gap-2">
        {startContent}
        <HeroInput
          {...rest}
          fullWidth
          className={undefined}
          required={isRequired ?? rest.required}
          onChange={(event) => {
            rest.onChange?.(event)
            onValueChange?.(event.currentTarget.value)
          }}
        />
        {endContent}
      </div>
      {description && <Description>{description}</Description>}
      {isInvalid && errorMessage && (
        <FieldError className="text-sm text-danger">{errorMessage}</FieldError>
      )}
    </TextField>
  )
}

export function Textarea(props: TextareaProps) {
  const {
    className,
    description,
    errorMessage,
    isInvalid,
    isRequired,
    label,
    labelPlacement: _labelPlacement,
    maxRows: _maxRows,
    minRows,
    onValueChange,
    ref,
    ...rest
  } = props
  return (
    <TextField
      isInvalid={isInvalid}
      isRequired={isRequired}
      className={clsx('flex w-full flex-col gap-1.5 px-1 py-0.5', className)}
      name={rest.name}
      value={rest.value as string | undefined}
      onChange={(value) => onValueChange?.(value)}
      validationBehavior="aria"
    >
      {label && <Label className="text-sm font-medium leading-5 text-default-700">{label}</Label>}
      <div className="w-full">
        <HeroTextArea
          {...rest}
          fullWidth
          className={undefined}
          rows={rest.rows ?? minRows}
          ref={ref}
          required={isRequired ?? rest.required}
          onChange={(event) => {
            rest.onChange?.(event)
            onValueChange?.(event.currentTarget.value)
          }}
        />
      </div>
      {description && <Description>{description}</Description>}
      {isInvalid && errorMessage && (
        <FieldError className="text-sm text-danger">{errorMessage}</FieldError>
      )}
    </TextField>
  )
}

export type DatePickerProps = {
  className?: string
  errorMessage?: ReactNode
  isInvalid?: boolean
  isRequired?: boolean
  label?: ReactNode
  maxValue?: DateValue
  name?: string
  onChange?: (value: DateValue | null) => void
  showMonthAndYearPickers?: boolean
  value?: DateValue | null
}

export type CheckboxProps = {
  children?: ReactNode
  className?: string
  isDisabled?: boolean
  isIndeterminate?: boolean
  isInvalid?: boolean
  isSelected?: boolean
  label?: ReactNode
  name?: string
  onChange?: (event: ChangeEvent<HTMLInputElement>) => void
  onValueChange?: (value: boolean) => void
}

export function Checkbox(props: CheckboxProps) {
  const {
    children,
    isDisabled,
    isIndeterminate,
    isInvalid,
    isSelected,
    label,
    onChange,
    onValueChange,
    ...rest
  } = props
  return (
    <HeroCheckbox
      {...rest}
      isDisabled={isDisabled}
      isIndeterminate={isIndeterminate}
      isSelected={isSelected}
      onChange={(value) => {
        onValueChange?.(value)
        onChange?.({ currentTarget: { checked: value } } as ChangeEvent<HTMLInputElement>)
      }}
    >
      <HeroCheckbox.Content className="flex !flex-row items-start gap-3">
        <HeroCheckbox.Control className="mt-0.5 shrink-0">
          <HeroCheckbox.Indicator />
        </HeroCheckbox.Control>
        {(label || children) && (
          <span className="text-sm leading-5 text-foreground">{label ?? children}</span>
        )}
      </HeroCheckbox.Content>
    </HeroCheckbox>
  )
}

export type SwitchProps = CheckboxProps & {
  size?: 'sm' | 'md' | 'lg'
}

export function Slider(props: {
  label?: ReactNode
  maxValue?: number
  minValue?: number
  name?: string
  onChange?: (value: number | number[]) => void
  step?: number
  value?: number
}) {
  const { label, maxValue, minValue, name: _name, ...rest } = props
  return (
    <HeroSlider maxValue={maxValue} minValue={minValue} {...rest}>
      {label && <Label className="text-sm font-medium leading-5 text-default-700">{label}</Label>}
      <HeroSlider.Track>
        <HeroSlider.Fill />
        <HeroSlider.Thumb />
      </HeroSlider.Track>
    </HeroSlider>
  )
}

export function Switch(props: SwitchProps) {
  const {
    children,
    isDisabled,
    isInvalid,
    isSelected,
    label,
    onChange,
    onValueChange,
    size,
    ...rest
  } = props
  return (
    <HeroSwitch
      {...(rest as any)}
      isDisabled={isDisabled}
      isSelected={isSelected}
      size={size}
      onChange={(value) => {
        onValueChange?.(value)
        onChange?.({ currentTarget: { checked: value } } as ChangeEvent<HTMLInputElement>)
      }}
    >
      <HeroSwitch.Content className="flex !flex-row items-start gap-3">
        <HeroSwitch.Control className="mt-0.5 shrink-0">
          <HeroSwitch.Thumb />
        </HeroSwitch.Control>
        {(label || children) && (
          <span className="text-sm leading-5 text-foreground">{label ?? children}</span>
        )}
      </HeroSwitch.Content>
    </HeroSwitch>
  )
}

export function DatePicker(props: DatePickerProps) {
  const {
    className,
    errorMessage,
    isInvalid,
    isRequired,
    label,
    showMonthAndYearPickers: _showMonthAndYearPickers,
    ...rest
  } = props
  return (
    <HeroDatePicker
      className={clsx('px-1 py-0.5', className)}
      isInvalid={isInvalid}
      isRequired={isRequired}
      validationBehavior="aria"
      {...rest}
    >
      {label && <Label className="text-sm font-medium leading-5 text-default-700">{label}</Label>}
      <DateField.Group fullWidth>
        <DateField.Input>{(segment) => <DateField.Segment segment={segment} />}</DateField.Input>
        <DateField.Suffix>
          <HeroDatePicker.Trigger>
            <HeroDatePicker.TriggerIndicator />
          </HeroDatePicker.Trigger>
        </DateField.Suffix>
      </DateField.Group>
      {isInvalid && errorMessage && <FieldError>{errorMessage}</FieldError>}
      <HeroDatePicker.Popover>
        <Calendar aria-label={typeof label === 'string' ? label : 'Choose date'}>
          <Calendar.Header>
            <Calendar.YearPickerTrigger>
              <Calendar.YearPickerTriggerHeading />
              <Calendar.YearPickerTriggerIndicator />
            </Calendar.YearPickerTrigger>
            <Calendar.NavButton slot="previous" />
            <Calendar.NavButton slot="next" />
          </Calendar.Header>
          <Calendar.Grid>
            <Calendar.GridHeader>
              {(day) => <Calendar.HeaderCell>{day}</Calendar.HeaderCell>}
            </Calendar.GridHeader>
            <Calendar.GridBody>{(date) => <Calendar.Cell date={date} />}</Calendar.GridBody>
          </Calendar.Grid>
          <Calendar.YearPickerGrid>
            <Calendar.YearPickerGridBody>
              {({ year }) => <Calendar.YearPickerCell year={year} />}
            </Calendar.YearPickerGridBody>
          </Calendar.YearPickerGrid>
        </Calendar>
      </HeroDatePicker.Popover>
    </HeroDatePicker>
  )
}

export type AutocompleteProps<T = any> = {
  children?: ReactNode | ((item: T) => ReactNode)
  className?: string
  errorMessage?: ReactNode
  inputValue?: string
  isInvalid?: boolean
  isRequired?: boolean
  allowsCustomValue?: boolean
  description?: ReactNode
  items?: T[]
  label?: ReactNode
  name?: string
  onInputChange?: (value: string) => void
  onSelectionChange?: (value: Key | null) => void
  selectedKey?: Key | null
}

export function Autocomplete<T = any>(props: AutocompleteProps<T>) {
  const {
    allowsCustomValue,
    children,
    description,
    errorMessage,
    isInvalid,
    isRequired,
    label,
    onSelectionChange,
    ...rest
  } = props
  return (
    <ComboBox
      {...(rest as any)}
      allowsCustomValue={allowsCustomValue}
      className={clsx('px-1 py-0.5', rest.className)}
      isInvalid={isInvalid}
      isRequired={isRequired}
      onSelectionChange={(key) => onSelectionChange?.(key as Key | null)}
      validationBehavior="aria"
    >
      {label && <Label>{label}</Label>}
      <ComboBox.InputGroup>
        <HeroInput />
        <ComboBox.Trigger />
      </ComboBox.InputGroup>
      {description && <Description>{description}</Description>}
      {isInvalid && errorMessage && <FieldError>{errorMessage}</FieldError>}
      <ComboBox.Popover>
        <ListBox>{typeof children === 'function' ? null : children}</ListBox>
      </ComboBox.Popover>
    </ComboBox>
  )
}

export const AutocompleteItem = ({ children, ...props }: { children: ReactNode; id: Key }) => (
  <ListBox.Item id={props.id} textValue={String(children)}>
    {children}
    <ListBox.ItemIndicator />
  </ListBox.Item>
)

export type { DateValue }
