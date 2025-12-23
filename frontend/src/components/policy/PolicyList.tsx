import { PropsWithChildren } from 'react'

const UL_CLASSES = 'list-disc ml-4'

export type PolicyListProps = Readonly<
  PropsWithChildren & {
    preamble: string
    addendum?: string
  }
>

export function PolicyList(props: PolicyListProps) {
  const { preamble, addendum, children } = props
  return (
    <div>
      <p>{preamble}</p>
      <ul className={UL_CLASSES}>{children}</ul>
      {addendum && <p>{addendum}</p>}
    </div>
  )
}
