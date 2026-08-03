import { Card as HeroCard, Chip as HeroChip, ProgressBar, ProgressCircle, Separator } from '@heroui/react'
import { ReactNode } from 'react'

export const Card = HeroCard
export const CardHeader = HeroCard.Header
export const CardBody = HeroCard.Content
export const CardFooter = HeroCard.Footer

export const Divider = Separator

export type ChipColor = 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'danger'
export type ChipVariantProps = { color?: ChipColor }

export function Chip(props: { children?: ReactNode; className?: string; color?: ChipColor; radius?: string }) {
  const { children, radius: _radius, ...rest } = props
  return <HeroChip {...(rest as any)}>{children}</HeroChip>
}

export function Progress(props: { className?: string; value?: number }) {
  return (
    <ProgressBar value={props.value ?? 0} className={props.className}>
      <ProgressBar.Track>
        <ProgressBar.Fill />
      </ProgressBar.Track>
    </ProgressBar>
  )
}

export function CircularProgress(props: { className?: string; value?: number; children?: ReactNode }) {
  return <ProgressCircle value={props.value ?? 0} className={props.className}>{props.children}</ProgressCircle>
}
