import { ReactNode } from 'react'
import { ApplicationInfoDto } from '@/types/dashboard/DashboardInfoDto.ts'
import { Divider } from '@heroui/react'

export type ApplicationStatProps = Readonly<{
  stat: ApplicationInfoDto
}>

export function ApplicationStat(props: ApplicationStatProps): ReactNode {
  const { stat } = props
  return (
    <>
      <p>{stat.status.name}</p>
      <p>{stat.count}</p>
      <Divider className="col-span-2" />
    </>
  )
}
