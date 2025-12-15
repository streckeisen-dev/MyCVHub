import { centerSection, h2, h4 } from '@/styles/primitives.ts'

export type EmptyProps = Readonly<{
  headline: string
  title?: string
  text?: string
}>

export function Empty(props: EmptyProps) {
  const { headline, title, text } = props
  return (
    <section className={centerSection()}>
      <h2 className={h2()}>{headline}</h2>
      { title && <h4 className={h4()}>{title}</h4> }
      { text && <p>{text}</p> }
    </section>
  )
}