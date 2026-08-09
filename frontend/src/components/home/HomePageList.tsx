import { ReactNode } from 'react'

export interface HomePageListEntry {
  title: string
  description: string
}

export type HomePageListProps = Readonly<{
  entries: HomePageListEntry[]
}>

export function HomePageList(props: HomePageListProps): ReactNode {
  const { entries } = props
  return (
    <div className="grid gap-4 md:grid-cols-3">
      {entries.map((entry, index) => (
        <div key={entry.title} className="rounded-lg border border-default-200 bg-surface p-5">
          <div className="mb-3 inline-flex h-8 w-8 items-center justify-center rounded-full bg-default text-sm font-semibold text-default-foreground">
            {index + 1}
          </div>
          <p className="font-semibold text-foreground">{entry.title}</p>
          <p className="mt-2 text-sm leading-6 text-default-600">{entry.description}</p>
        </div>
      ))}
    </div>
  )
}
