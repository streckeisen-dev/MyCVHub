import { Tooltip as HeroTooltip } from '@heroui/react'
import { ReactNode } from 'react'

export type TooltipProps = {
  children?: ReactNode
  closeDelay?: number
  color?: string
  content?: ReactNode
}

export function Tooltip(props: TooltipProps) {
  const { children, content } = props
  return (
    <HeroTooltip>
      {children}
      <HeroTooltip.Content>{content}</HeroTooltip.Content>
    </HeroTooltip>
  )
}
