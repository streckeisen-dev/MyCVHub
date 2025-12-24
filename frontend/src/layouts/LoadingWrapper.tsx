import { PropsWithChildren, ReactNode } from 'react'
import { centerSection } from '@/styles/primitives.ts'
import { Spinner } from '@heroui/react'

export type LoadingWrapperProps = Readonly<
  PropsWithChildren & {
    isLoading: boolean
    className?: string
  }
>

export function LoadingWrapper(props: LoadingWrapperProps): ReactNode {
  const { isLoading, className, children } = props

  return (
    <section className={className ?? centerSection()}>{isLoading ? <Spinner /> : children}</section>
  )
}
