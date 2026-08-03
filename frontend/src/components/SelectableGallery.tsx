import { Card, CardBody, CardFooter, CardHeader } from '@/components/ui/Display.tsx'
import { Button } from '@/components/ui/Button.tsx'
import { ReactNode } from 'react'

import sanitizeHtml from 'sanitize-html'
import { useTranslation } from 'react-i18next'
import { FaCheck } from 'react-icons/fa6'
import clsx from 'clsx'

export interface GalleryItems {
  key: string
  name: string
  image: string
  alt: string
  description: string
}

export type SelectableGalleryProps = Readonly<{
  items: GalleryItems[]
  compact?: boolean
  disabled?: boolean
  selected: string | undefined
  onSelect?: (key: string) => void
}>

function sanitize(html: string): string {
  return sanitizeHtml(html, {
    allowedTags: ['p', 'a']
  })
}

export function SelectableGallery(props: SelectableGalleryProps): ReactNode {
  const { items, compact = false, selected, disabled, onSelect } = props
  const { t } = useTranslation()

  function handleSelected(key: string) {
    if (disabled || !onSelect) return
    onSelect(key)
  }

  return (
    <div
      className={clsx(
        'grid w-full grid-cols-1 gap-5',
        compact ? 'max-w-5xl justify-items-start' : 'justify-items-center',
        !compact && items.length === 1 && 'mx-auto',
        items.length > 1 ? 'sm:grid-cols-2' : compact ? 'max-w-sm' : 'max-w-lg'
      )}
    >
      {items.map((item) => {
        const isSelected = item.key === selected
        return (
          <Card
            key={item.key}
            className={clsx(
              'relative flex h-full w-full overflow-hidden border transition-colors',
              disabled || compact ? 'lg:max-w-sm' : 'lg:max-w-lg',
              isSelected
                ? 'border-2 border-[#0072F5] bg-[#0072F5]/8 shadow-md ring-2 ring-[#0072F5]/25'
                : 'border-default-200 bg-surface hover:border-default-300',
              disabled && 'bg-surface'
            )}
          >
            {isSelected && (
              <div className="absolute right-3 top-3 z-10 inline-flex h-9 w-9 items-center justify-center rounded-full border-2 border-white bg-[#0072F5] text-white shadow-md">
                <FaCheck size={16} />
              </div>
            )}
            <CardHeader className="flex items-start justify-between gap-3 px-5 pb-2 pt-5">
              <p className="pr-10 text-lg font-semibold leading-6">{item.name}</p>
            </CardHeader>
            <CardBody className="flex flex-1 flex-col gap-4 px-5 py-3">
              <div className="overflow-hidden rounded-md border border-default-200 bg-default/5">
                <img
                  className={clsx(
                    'block aspect-[3/4] w-full',
                    disabled || compact ? 'bg-white object-contain' : 'object-cover'
                  )}
                  src={item.image}
                  alt={item.alt}
                />
              </div>
              <p
                className="text-sm leading-6 text-default-600"
                dangerouslySetInnerHTML={{
                  __html: sanitize(item.description)
                }}
              />
            </CardBody>
            {!disabled && (
              <CardFooter className="px-5 pb-5 pt-2">
                <Button
                  variant="primary"
                  className={clsx(isSelected && 'bg-[#0072F5] text-white ring-1 ring-[#0072F5]/30')}
                  onPress={() => handleSelected(item.key)}
                >
                  {t('gallery.select')}
                </Button>
              </CardFooter>
            )}
          </Card>
        )
      })}
    </div>
  )
}
