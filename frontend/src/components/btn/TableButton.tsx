import { HTMLAttributes, ReactNode } from 'react'

export type TableButtonProps = Readonly<
  HTMLAttributes<HTMLButtonElement> & {
    children: ReactNode
  }
>

export function TableButton(props: TableButtonProps): ReactNode {
  const { children, className, ...remainingProps } = props
  return (
    <button type="button" className={`cursor-pointer text-lg ${className}`} {...remainingProps}>
      {children}
    </button>
  )
}
