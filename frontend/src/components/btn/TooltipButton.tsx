import { Tooltip, TooltipProps } from '@/components/ui/Tooltip.tsx'
import { PropsWithChildren, ReactNode } from 'react'

import { TableButton, TableButtonProps } from '@/components/btn/TableButton.tsx'

export type TooltipButtonProps = Readonly<
  PropsWithChildren &
    TooltipProps & {
      btnProps?: Omit<TableButtonProps, 'children'>
      onClick?: () => void
    }
>

export function TooltipButton(props: TooltipButtonProps): ReactNode {
  const { btnProps, children, onClick, color, ...tooltipProps } = props
  const textColor = `text-${color}`
  return (
    <Tooltip color={color} {...tooltipProps}>
      <TableButton
        className={`${color ? textColor : ''} ${btnProps?.className}`}
        onClick={onClick}
        {...btnProps}
      >
        {children}
      </TableButton>
    </Tooltip>
  )
}
