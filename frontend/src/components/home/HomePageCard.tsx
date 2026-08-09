import { Card, CardBody, CardHeader } from '@/components/ui/Display.tsx'
import { ReactNode } from 'react'

export type HomePageCardProps = Readonly<{
  icon?: ReactNode
  title: string
  description: string
}>

export function HomePageCard(props: HomePageCardProps): ReactNode {
  const { icon, title, description } = props
  return (
    <Card className="h-full border border-default-200 bg-surface">
      <CardHeader className="flex items-start gap-3 px-5 pb-2 pt-5">
        {icon && (
          <span className="mt-0.5 inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-accent/10 text-accent">
            {icon}
          </span>
        )}
        <h3 className="text-base font-semibold leading-6 text-foreground">{title}</h3>
      </CardHeader>
      <CardBody className="px-5 pb-5 pt-2">
        <p className="text-sm leading-6 text-default-600">{description}</p>
      </CardBody>
    </Card>
  )
}
