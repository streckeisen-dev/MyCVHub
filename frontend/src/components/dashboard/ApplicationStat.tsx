import { Divider } from '@/components/ui/Display.tsx'
import { ReactNode } from 'react'
import { ApplicationInfoDto } from '@/types/dashboard/DashboardInfoDto.ts'

export type ApplicationStatProps = Readonly<{
  stat: ApplicationInfoDto
}>

export function ApplicationStat(props: ApplicationStatProps): ReactNode {
  const { stat } = props
  return (
    <div className="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-4 py-3">
      <p className="min-w-0 text-default-600">{stat.status.name}</p>
      <p className="tabular-nums font-semibold text-foreground">{stat.count}</p>
      <Divider className="col-span-2" />
    </div>
  )
}
