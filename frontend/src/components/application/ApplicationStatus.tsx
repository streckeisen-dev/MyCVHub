import { ReactNode } from 'react'
import { Chip, ChipVariantProps } from '@heroui/react'
import { ApplicationStatusDto } from '@/types/application/ApplicationStatusDto.ts'

const STATUS_COLOR_MAP: Record<string, ChipVariantProps['color']> = {
  UNSENT: 'primary',
  WAITING_FOR_FIRST_RESPONSE: 'primary',
  REJECTED: 'danger',
  INTERVIEW_SCHEDULED: 'secondary',
  WAITING_FOR_INTERVIEW_FEEDBACK: 'secondary',
  OFFER_RECEIVED: 'secondary',
  OFFER_DECLINED: 'default',
  WITHDRAWN: 'default',
  HIRED: 'success'
}

export type ApplicationStatusProps = Readonly<{
  status: ApplicationStatusDto
}>

export function ApplicationStatus(props: ApplicationStatusProps): ReactNode {
  const { status } = props
  const color = STATUS_COLOR_MAP[status.key]
  return (
    <Chip color={color} className="whitespace-break-spaces h-fit min-h-7 text-center" radius="lg">
      {status.name}
    </Chip>
  )
}
