import { PropsWithChildren, ReactNode } from 'react'
import { Card, CardBody, CardHeader } from '@heroui/react'

export type DashboardCardProps = Readonly<PropsWithChildren & {
  title: string
}>

export function DashboardCard(props: DashboardCardProps): ReactNode {
  const { title, children } = props
  return (
    <Card className="p-2">
      <CardHeader className="font-bold">{title}</CardHeader>
      <CardBody>{children}</CardBody>
    </Card>
  )
}
