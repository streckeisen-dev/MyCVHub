export type PolicyTextProps = Readonly<{
  text: string
}>

export function PolicyText(props: PolicyTextProps) {
  const { text } = props
  return <p>{text}</p>
}
