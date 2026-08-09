import { Tooltip as HeroTooltip } from '@heroui/react'
import { ReactNode } from 'react'
import clsx from 'clsx'

type TooltipColor = 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'danger'

const TOOLTIP_COLOR_CLASSES: Record<TooltipColor, string> = {
  default: '',
  primary: 'text-primary',
  secondary: 'text-secondary',
  success: 'text-success',
  warning: 'text-warning',
  danger: 'text-danger'
}

export type TooltipProps = Readonly<{
  children?: ReactNode
  closeDelay?: number
  color?: TooltipColor
  content?: ReactNode
}>

export function Tooltip(props: TooltipProps) {
  const { children, closeDelay, color = 'default', content } = props
  return (
    <HeroTooltip closeDelay={closeDelay}>
      {children}
      <HeroTooltip.Content className={clsx(TOOLTIP_COLOR_CLASSES[color])}>
        {content}
      </HeroTooltip.Content>
    </HeroTooltip>
  )
}
