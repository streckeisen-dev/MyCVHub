import { PropsWithChildren, ReactNode } from 'react'
import { centerSection } from '@/styles/primitives.ts'
import { Spinner } from '@heroui/react'

export type LoadingWrapperProps = Readonly<
  PropsWithChildren & {
    isLoading: boolean
  }
>

export function LoadingWrapper(props: LoadingWrapperProps): ReactNode {
  const { isLoading, children } = props

  return <section className={centerSection()}>{isLoading ? <Spinner /> : children}</section>
}
