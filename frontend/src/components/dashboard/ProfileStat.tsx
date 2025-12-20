import { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { FaPen } from 'react-icons/fa6'
import { Divider } from '@heroui/react'

export type ProfileStatProps = Readonly<{
  title: string
  count: number
  type: string
}>

export function ProfileStat(props: ProfileStatProps): ReactNode {
  const { title, count, type } = props
  return (
    <>
      <p>{title}</p>
      <p>{count}</p>
      <Link to={getRoutePath(RouteId.EditProfile, type)}>
        <FaPen />
      </Link>
      <Divider className="col-span-3" />
    </>
  )
}
