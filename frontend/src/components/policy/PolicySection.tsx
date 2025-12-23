import { ReactNode } from 'react'
import { h4 } from '@/styles/primitives.ts'

export type PolicySectionProps = Readonly<{
  key: string
  title: string
  content: ReactNode
}>

export function PolicySection(props: PolicySectionProps): ReactNode {
  const { key, title, content } = props
  return (
    <section key={key} className="flex flex-col gap-2">
      <h4 className={h4()}>{title}</h4>
      <div>{content}</div>
    </section>
  )
}
