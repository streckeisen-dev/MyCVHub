import { ReactNode } from 'react'
import { Card, CardBody, CardHeader } from '@heroui/react'
import { h4 } from '@/styles/primitives.ts'

export type HomePageCardProps = Readonly<{
  title: string
  description: string
}>

export function HomePageCard(props: HomePageCardProps): ReactNode {
  const { title, description } = props
  return (
    <Card className="p-4 max-w-lg">
      <CardHeader>
        <h4 className={h4()}>{title}</h4>
      </CardHeader>
      <CardBody>
        <p>{description}</p>
      </CardBody>
    </Card>
  )
}
