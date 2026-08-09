import { Card as HeroCard, Chip as HeroChip, ProgressBar, ProgressCircle } from '@heroui/react'
import { ReactNode } from 'react'

export { ProgressBar, Separator as Divider } from '@heroui/react'

export const Card = HeroCard
export const CardHeader = HeroCard.Header
export const CardBody = HeroCard.Content
export const CardFooter = HeroCard.Footer

export type ChipColor = 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'danger'
export type ChipVariantProps = Readonly<{ color?: ChipColor }>

export type ChipProps = Readonly<{
  children?: ReactNode
  className?: string
  color?: ChipColor
  radius?: string
}>

export function Chip(props: ChipProps) {
  const { children, radius: _radius, ...rest } = props
  return <HeroChip {...(rest as any)}>{children}</HeroChip>
}

export type ProgressProps = Readonly<{ className?: string; value?: number }>

export function Progress(props: ProgressProps) {
  return (
    <ProgressBar value={props.value ?? 0} className={props.className}>
      <ProgressBar.Track>
        <ProgressBar.Fill />
      </ProgressBar.Track>
    </ProgressBar>
  )
}

export type CircularProgressProps = Readonly<{
  children?: ReactNode
  className?: string
  value?: number
}>

export function CircularProgress(props: CircularProgressProps) {
  return (
    <ProgressCircle value={props.value ?? 0} className={props.className}>
      {props.children}
    </ProgressCircle>
  )
}
