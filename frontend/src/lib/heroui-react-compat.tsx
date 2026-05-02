import {
  AnchorHTMLAttributes,
  ChangeEvent,
  ComponentPropsWithRef,
  InputHTMLAttributes,
  ReactNode,
  TextareaHTMLAttributes,
  createContext,
  forwardRef,
  useContext,
  useState
} from 'react'

type AnyProps = Record<string, any>
type Key = string | number

export type SortDescriptor = {
  column?: Key
  direction: 'ascending' | 'descending'
}

export type SharedSelection = Set<Key> & {
  currentKey?: string
}

export type DateValue = any
export type ButtonProps = ComponentPropsWithRef<'button'> & {
  as?: any
  color?: string
  endContent?: ReactNode
  isDisabled?: boolean
  isIconOnly?: boolean
  isLoading?: boolean
  onPress?: () => void
  startContent?: ReactNode
  variant?: string
  [key: string]: any
}
export type InputProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'onChange'> & {
  description?: ReactNode
  endContent?: ReactNode
  errorMessage?: ReactNode
  isInvalid?: boolean
  isRequired?: boolean
  label?: ReactNode
  onChange?: (event: ChangeEvent<HTMLInputElement>) => void
  startContent?: ReactNode
  onValueChange?: (value: string) => void
  [key: string]: any
}
export type CheckboxProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'onChange'> & {
  errorMessage?: ReactNode
  isDisabled?: boolean
  isIndeterminate?: boolean
  isInvalid?: boolean
  isSelected?: boolean
  label?: ReactNode
  onChange?: (event: ChangeEvent<HTMLInputElement>) => void
  onValueChange?: (value: boolean) => void
  [key: string]: any
}
export type SwitchProps = Omit<CheckboxProps, 'size'> & {
  size?: string
}
export type DatePickerProps<T = any> = Omit<InputHTMLAttributes<HTMLInputElement>, 'onChange' | 'value'> & {
  errorMessage?: ReactNode
  isInvalid?: boolean
  isRequired?: boolean
  label?: ReactNode
  maxValue?: T
  onChange?: (value: T | null) => void
  value?: T | null
  [key: string]: any
}
export type AutocompleteProps<T = any> = Omit<ComponentPropsWithRef<'select'>, 'children' | 'onChange'> & {
  items?: T[]
  label?: ReactNode
  errorMessage?: ReactNode
  isInvalid?: boolean
  isRequired?: boolean
  selectedKey?: Key | null
  inputValue?: string
  allowsCustomValue?: boolean
  children?: ReactNode | ((item: T) => ReactNode)
  onInputChange?: (value: string) => void
  onSelectionChange?: (value: Key | null) => void
  [key: string]: any
}
export type TextareaProps = Omit<TextareaHTMLAttributes<HTMLTextAreaElement>, 'onChange'> & {
  description?: ReactNode
  errorMessage?: ReactNode
  isInvalid?: boolean
  isRequired?: boolean
  label?: ReactNode
  onChange?: (event: any) => void
  onValueChange?: (value: string) => void
  [key: string]: any
}
export type TooltipProps = AnyProps
export type ModalProps = AnyProps & {
  children?: ReactNode
  onClose?: () => void
}
export type LinkProps = AnchorHTMLAttributes<HTMLAnchorElement>
export type ChipVariantProps = {
  color?: 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'danger'
}

function cx(...values: Array<string | false | undefined>) {
  return values.filter(Boolean).join(' ')
}

function buttonVariant(props: AnyProps) {
  if (props.color === 'danger' && props.variant === 'flat') return 'danger-soft'
  if (props.color === 'danger') return 'danger'
  if (props.color === 'secondary') return 'secondary'
  if (props.variant === 'bordered') return 'outline'
  if (props.variant === 'light' || props.variant === 'flat') return 'tertiary'
  if (props.variant === 'ghost') return 'ghost'
  return props.color === 'primary' || !props.variant ? 'primary' : props.variant
}

function selectionFromValue(value: string): SharedSelection {
  const selection = new Set<Key>(value ? [value] : []) as SharedSelection
  selection.currentKey = value || undefined
  return selection
}

export const HeroUIProvider = ({ children }: AnyProps & { children: ReactNode }) => <>{children}</>

export const Button = forwardRef<HTMLElement, ButtonProps>(function Button(props, ref) {
  const {
    as,
    children,
    className,
    color: _color,
    endContent,
    isDisabled,
    isIconOnly,
    isLoading,
    onPress,
    startContent,
    variant: _variant,
    ...rest
  } = props
  const Component = as ?? 'button'
  const variant = buttonVariant(props)
  return (
    <Component
      {...rest}
      ref={ref}
      className={cx(
        'inline-flex items-center justify-center gap-2 rounded-md px-4 py-2 text-sm font-medium disabled:opacity-50',
        isIconOnly && 'h-10 w-10 px-0',
        variant === 'primary' && 'bg-accent text-accent-foreground',
        variant === 'secondary' && 'border border-default bg-surface',
        variant === 'tertiary' && 'bg-transparent',
        variant === 'outline' && 'border border-default bg-transparent',
        variant === 'ghost' && 'bg-transparent',
        variant === 'danger' && 'bg-danger text-danger-foreground',
        variant === 'danger-soft' && 'bg-danger-soft text-danger',
        className
      )}
      disabled={isDisabled || isLoading}
      onClick={onPress}
    >
      {isLoading ? <Spinner size="sm" /> : startContent}
      {children}
      {endContent}
    </Component>
  )
})

function FieldWrapper(props: {
  children: ReactNode
  className?: string
  errorMessage?: ReactNode
  isInvalid?: boolean
  label?: ReactNode
}) {
  const { children, className, errorMessage, isInvalid, label } = props
  return (
    <label className={cx('flex flex-col gap-1', className)}>
      {label && <span className="text-sm text-muted">{label}</span>}
      {children}
      {isInvalid && errorMessage && <span className="text-xs text-danger">{errorMessage}</span>}
    </label>
  )
}

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(props, ref) {
  const {
    className,
    endContent,
    errorMessage,
    isInvalid,
    isRequired,
    label,
    onValueChange,
    startContent,
    ...rest
  } = props
  return (
    <FieldWrapper className={className} errorMessage={errorMessage} isInvalid={isInvalid} label={label}>
      <span className="flex items-center gap-2 rounded-md border border-default bg-surface px-3 py-2">
        {startContent}
        <input
          {...(rest as InputHTMLAttributes<HTMLInputElement>)}
          ref={ref}
          required={isRequired ?? rest.required}
          className="min-w-0 flex-1 bg-transparent outline-none"
          onChange={(event) => {
            rest.onChange?.(event)
            onValueChange?.(event.currentTarget.value)
          }}
        />
        {endContent}
      </span>
    </FieldWrapper>
  )
})

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(function Textarea(props, ref) {
  const { className, errorMessage, isInvalid, isRequired, label, onValueChange, ...rest } = props
  return (
    <FieldWrapper className={className} errorMessage={errorMessage} isInvalid={isInvalid} label={label}>
      <textarea
        {...(rest as TextareaHTMLAttributes<HTMLTextAreaElement>)}
        ref={ref}
        required={isRequired ?? rest.required}
        className="min-h-24 rounded-md border border-default bg-surface px-3 py-2 outline-none"
        onChange={(event) => {
          rest.onChange?.(event)
          onValueChange?.(event.currentTarget.value)
        }}
      />
    </FieldWrapper>
  )
})

export const Form = forwardRef<HTMLFormElement, ComponentPropsWithRef<'form'>>(function Form(props, ref) {
  return <form {...props} ref={ref} />
})

export const Checkbox = (props: CheckboxProps) => {
  const { children, className, isDisabled, isIndeterminate: _isIndeterminate, isSelected, label, onValueChange, ...rest } = props
  return (
    <label className={cx('inline-flex items-center gap-2', className)}>
      <input
        {...rest}
        checked={isSelected ?? rest.checked}
        disabled={isDisabled ?? rest.disabled}
        type="checkbox"
        onChange={(event) => {
          rest.onChange?.(event)
          onValueChange?.(event.currentTarget.checked)
        }}
      />
      <span>{label ?? children}</span>
    </label>
  )
}

export const Switch = ({ size: _size, ...props }: SwitchProps) => <Checkbox {...props} />

export function DatePicker<T = any>(props: DatePickerProps<T>) {
  const { errorMessage, isInvalid, isRequired, label, maxValue: _maxValue, onChange, value, ...rest } = props
  return (
    <FieldWrapper className={rest.className} errorMessage={errorMessage} isInvalid={isInvalid} label={label}>
      <input
        {...rest}
        type="date"
        value={value?.toString?.() ?? ''}
        required={isRequired ?? rest.required}
        className="rounded-md border border-default bg-surface px-3 py-2 outline-none"
        onChange={(event) => onChange?.(event.currentTarget.value as T)}
      />
    </FieldWrapper>
  )
}

export function Autocomplete<T = any>(props: AutocompleteProps<T>) {
  const { children, className, errorMessage, isInvalid, isRequired, items, label, onInputChange, onSelectionChange, ...rest } = props
  return (
    <FieldWrapper className={className} errorMessage={errorMessage} isInvalid={isInvalid} label={label}>
      <select
        {...rest}
        required={isRequired ?? rest.required}
        className="rounded-md border border-default bg-surface px-3 py-2 outline-none"
        onChange={(event) => {
          onInputChange?.(event.currentTarget.value)
          onSelectionChange?.(event.currentTarget.value)
        }}
      >
        <option value="" />
        {items
          ? items.map((item) => {
              const option = item as AnyProps
              return <option key={option.key ?? option.id} value={option.key ?? option.id}>{option.name ?? option.label ?? option.key}</option>
            })
          : typeof children === 'function' ? null : children}
      </select>
    </FieldWrapper>
  )
}

export const AutocompleteItem = ({ children, ...props }: AnyProps) => <option {...props} value={props.key}>{children}</option>

export type SelectProps<T = any> = Omit<ComponentPropsWithRef<'select'>, 'children' | 'onChange'> & {
  children?: ReactNode | ((item: T) => ReactNode)
  isClearable?: boolean
  items?: T[]
  label?: ReactNode
  onSelectionChange?: (selection: SharedSelection) => void
  selectedKeys?: string[] | SharedSelection
  selectedKey?: Key | null
  description?: ReactNode
  [key: string]: any
}

export function Select<T = any>(props: SelectProps<T>) {
  const { children, className, items, label, onSelectionChange, selectedKeys, ...rest } = props
  const selected = Array.isArray(selectedKeys) ? selectedKeys[0] : selectedKeys?.currentKey
  return (
    <FieldWrapper className={className} label={label}>
      <select
        {...rest}
        value={selected ?? ''}
        className="rounded-md border border-default bg-surface px-3 py-2 outline-none"
        onChange={(event) => onSelectionChange?.(selectionFromValue(event.currentTarget.value))}
      >
        {rest.isClearable && <option value="" />}
        {items
          ? items.map((item) => {
              const option = item as AnyProps
              return <option key={option.key ?? option.id} value={option.key ?? option.id}>{option.name ?? option.label ?? option.key}</option>
            })
          : typeof children === 'function' ? null : children}
      </select>
    </FieldWrapper>
  )
}

export const SelectItem = ({ children, ...props }: AnyProps) => <option {...props} value={props.key}>{children}</option>

export const Chip = ({ children, className, color, ...rest }: AnyProps) => (
  <span {...rest} className={cx('inline-flex items-center rounded-full px-2 py-1 text-xs', color === 'danger' && 'bg-danger text-danger-foreground', color === 'warning' && 'bg-warning text-warning-foreground', color === 'success' && 'bg-success text-success-foreground', className)}>
    {children}
  </span>
)

export const Spinner = ({ className }: AnyProps) => <span className={cx('inline-block h-5 w-5 animate-spin rounded-full border-2 border-current border-r-transparent', className)} />
export const Progress = ({ className, value = 0, ...rest }: AnyProps) => <progress {...rest} className={cx('h-2 w-full', className)} value={value} max={100} />
export const CircularProgress = Progress
export type SliderProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'onChange' | 'size' | 'value'> & {
  label?: ReactNode
  maxValue?: number
  minValue?: number
  onChange?: (value: number) => void
  size?: string
  value?: number
}
export const Slider = ({ maxValue, minValue, onChange, size: _size, value, ...rest }: SliderProps) => (
  <input
    {...rest}
    max={maxValue ?? rest.max}
    min={minValue ?? rest.min}
    type="range"
    value={value}
    onChange={(event) => onChange?.(Number(event.currentTarget.value))}
  />
)
export const Divider = ({ className, ...rest }: AnyProps) => <hr {...rest} className={cx('border-default', className)} />

export const Card = ({ children, className, ...rest }: AnyProps) => <div {...rest} className={cx('rounded-lg border border-default bg-surface', className)}>{children}</div>
export const CardHeader = ({ children, className, ...rest }: AnyProps) => <div {...rest} className={cx('p-4', className)}>{children}</div>
export const CardBody = ({ children, className, ...rest }: AnyProps) => <div {...rest} className={cx('p-4', className)}>{children}</div>
export const CardFooter = ({ children, className, ...rest }: AnyProps) => <div {...rest} className={cx('p-4', className)}>{children}</div>

export const Tooltip = ({ children, content, ...rest }: AnyProps) => <span {...rest} title={typeof content === 'string' ? content : undefined}>{children}</span>

export const User = ({ avatarProps, className, name, ...rest }: AnyProps) => (
  <div {...rest} className={cx('inline-flex items-center gap-2', className)}>
    {avatarProps?.src && <img alt="" className="h-8 w-8 rounded-full" src={avatarProps.src} />}
    {name && <span>{name}</span>}
  </div>
)

export const Navbar = ({ children, className, ...rest }: AnyProps) => <nav {...rest} className={cx('flex items-center gap-4', className)}>{children}</nav>
export const NavbarBrand = ({ children, className, ...rest }: AnyProps) => <div {...rest} className={cx('flex items-center', className)}>{children}</div>
export const NavbarContent = ({ children, className, ...rest }: AnyProps) => <div {...rest} className={cx('flex items-center gap-4', className)}>{children}</div>
export const NavbarItem = ({ children, className, ...rest }: AnyProps) => <div {...rest} className={className}>{children}</div>
export const NavbarMenu = ({ children, className, ...rest }: AnyProps) => <div {...rest} className={className}>{children}</div>
export const NavbarMenuItem = ({ children, className, ...rest }: AnyProps) => <div {...rest} className={className}>{children}</div>
export const NavbarMenuToggle = (props: AnyProps) => <button {...props} type="button" />

export const Dropdown = ({ children }: AnyProps) => <div className="relative inline-block">{children}</div>
export const DropdownTrigger = ({ children }: AnyProps) => <>{children}</>
export const DropdownMenu = ({ children, ...rest }: AnyProps) => <div {...rest} className="absolute right-0 z-50 mt-2 min-w-40 rounded-md border border-default bg-overlay p-2 shadow-lg">{children}</div>
export const DropdownItem = ({ children, className, onPress, ...rest }: AnyProps) => <button {...rest} className={cx('block w-full px-3 py-2 text-left', className)} onClick={onPress} type="button">{children}</button>

export function useDisclosure(defaultOpen = false) {
  const [isOpen, setIsOpen] = useState(defaultOpen)
  return {
    isOpen,
    onOpen: () => setIsOpen(true),
    onClose: () => setIsOpen(false),
    onOpenChange: setIsOpen
  }
}

const ModalCloseContext = createContext<(() => void) | undefined>(undefined)

export const Modal = ({ children, isOpen = true, onClose, ...rest }: AnyProps) => {
  if (!isOpen) return null
  return (
    <div {...rest} className={cx('fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4', rest.className)}>
      <ModalCloseContext.Provider value={onClose}>
        {typeof children === 'function' ? children(onClose) : children}
      </ModalCloseContext.Provider>
    </div>
  )
}

export const ModalContent = ({ children, className, ...rest }: AnyProps & {
  children?: ReactNode | ((onClose: () => void) => ReactNode)
}) => {
  const onClose = useContext(ModalCloseContext) ?? (() => undefined)
  return <div {...rest} className={cx('max-h-full max-w-3xl overflow-auto rounded-lg bg-overlay p-4 shadow-xl', className)}>{typeof children === 'function' ? children(onClose) : children}</div>
}

export const ModalHeader = ({ children, className, ...rest }: AnyProps) => <div {...rest} className={cx('mb-3 text-lg font-semibold', className)}>{children}</div>
export const ModalBody = ({ children, className, ...rest }: AnyProps) => <div {...rest} className={cx('mb-4', className)}>{children}</div>
export const ModalFooter = ({ children, className, ...rest }: AnyProps) => <div {...rest} className={cx('flex justify-end gap-2', className)}>{children}</div>

export const Accordion = ({ children, ...rest }: AnyProps) => <div {...rest}>{children}</div>
export const AccordionItem = ({ children, title, ...rest }: AnyProps) => <section {...rest}><h3>{title}</h3>{children}</section>
export const Tabs = ({ children, ...rest }: AnyProps) => <div {...rest}>{children}</div>
export const Tab = ({ children, title, ...rest }: AnyProps) => <section {...rest}><h3>{title}</h3>{children}</section>

export const Table = ({ bottomContent, children, className, topContent, ...rest }: AnyProps) => <div {...rest} className={className}>{topContent}<table className="w-full">{children}</table>{bottomContent}</div>
export const TableHeader = ({ children, columns = [] }: AnyProps & {
  children?: ReactNode | ((column: AnyProps) => ReactNode)
}) => <thead><tr>{typeof children === 'function' ? columns.map((column: AnyProps) => children(column)) : children}</tr></thead>
export const TableColumn = ({ children, ...rest }: AnyProps) => <th {...rest}>{children}</th>
export function TableBody<T = any>({ children, emptyContent, isLoading, items = [], loadingContent }: AnyProps & {
  children?: ReactNode | ((item: T) => ReactNode)
  items?: T[]
}) {
  return <tbody>{isLoading && <tr><td>{loadingContent}</td></tr>}{!isLoading && items.length === 0 && <tr><td>{emptyContent}</td></tr>}{!isLoading && items.map((item: T) => (typeof children === 'function' ? children(item) : children))}</tbody>
}
export const TableRow = ({ children, ...rest }: AnyProps & {
  children?: ReactNode | ((column: Key) => ReactNode)
}) => <tr {...rest}>{typeof children === 'function' ? children('actions') : children}</tr>
export const TableCell = ({ children, ...rest }: AnyProps) => <td {...rest}>{children}</td>

export const Pagination = ({ onChange, page, total }: AnyProps) => <div className="inline-flex items-center gap-2"><Button isDisabled={page <= 1} onPress={() => onChange?.(page - 1)}>Prev</Button><span>{page} / {total}</span><Button isDisabled={page >= total} onPress={() => onChange?.(page + 1)}>Next</Button></div>

export const ToastProvider = ({ children }: AnyProps & { children?: ReactNode }) => <>{children}</>
export function addToast(toast: { title?: ReactNode; description?: ReactNode; color?: string; timeout?: number }) {
  const message = [toast.title, toast.description].filter(Boolean).join(': ')
  if (message) console.info(message)
}

export function link(options?: { class?: string; className?: string; color?: string }) {
  return options?.className ?? options?.class ?? ''
}

export const Link = forwardRef<HTMLAnchorElement, LinkProps>(function Link(props, ref) {
  return <a {...props} ref={ref} />
}) as any
