import { ReactNode } from 'react'
import { Card, CardBody, CardHeader } from '@heroui/react'

export type DashboardCardProps = Readonly<{
  title: string
  content: ReactNode
}>

export function DashboardCard(props: DashboardCardProps): ReactNode {
  const { title, content } = props
  return (
    <Card className="p-2">
      <CardHeader className="font-bold">{title}</CardHeader>
      <CardBody>{content}</CardBody>
    </Card>
  )
}
