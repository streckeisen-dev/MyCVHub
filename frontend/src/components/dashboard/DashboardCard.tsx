import { Card, CardBody, CardHeader } from '@/components/ui/Display.tsx'
import { PropsWithChildren, ReactNode } from 'react'

export type DashboardCardProps = Readonly<
  PropsWithChildren & {
    title: string
  }
>

export function DashboardCard(props: DashboardCardProps): ReactNode {
  const { title, children } = props
  return (
    <Card className="h-full border border-default-100 bg-[var(--surface)] p-0">
      <CardHeader className="px-6 pb-3 pt-5 text-lg font-semibold leading-6">{title}</CardHeader>
      <CardBody className="px-6 pb-6 pt-0">{children}</CardBody>
    </Card>
  )
}
