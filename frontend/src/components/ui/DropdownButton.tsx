import clsx from 'clsx'
import { ReactNode } from 'react'
import { FaChevronDown } from 'react-icons/fa6'
import { Dropdown } from '@heroui/react'

export type DropdownButtonProps = Readonly<{
  children: ReactNode
  className?: string
  showChevron?: boolean
  type?: 'button' | 'submit' | 'reset'
}>

export function DropdownButton(props: DropdownButtonProps): ReactNode {
  const { children, className, showChevron = true, type = 'button' } = props

  return (
    <Dropdown.Trigger
      type={type}
      className={clsx(
        'inline-flex min-h-10 items-center justify-center gap-2 rounded-lg border border-default-300 bg-transparent px-4 py-2 text-sm font-medium text-foreground transition-colors hover:bg-default/10 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary',
        className
      )}
    >
      {children}
      {showChevron && <FaChevronDown size={14} className="shrink-0 text-default-500" />}
    </Dropdown.Trigger>
  )
}
