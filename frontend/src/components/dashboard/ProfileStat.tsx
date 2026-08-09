import { Divider } from '@/components/ui/Display.tsx'
import { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { FaPen } from 'react-icons/fa6'

export type ProfileStatProps = Readonly<{
  ariaLabel: string
  title: string
  count: number
  type: string
}>

export function ProfileStat(props: ProfileStatProps): ReactNode {
  const { ariaLabel, title, count, type } = props
  return (
    <div className="grid grid-cols-[minmax(0,1fr)_auto_auto] items-center gap-4 py-3">
      <p className="min-w-0 text-default-600">{title}</p>
      <p className="tabular-nums font-semibold text-foreground">{count}</p>
      <Link
        className="inline-flex h-8 w-8 items-center justify-center rounded-md text-default-500 transition-colors hover:bg-default/10 hover:text-primary"
        aria-label={ariaLabel}
        to={getRoutePath(RouteId.EditProfile, type)}
      >
        <FaPen size={14} />
      </Link>
      <Divider className="col-span-3" />
    </div>
  )
}
