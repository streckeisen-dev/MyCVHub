import { ReactNode } from 'react'
import { Button, Card, CardBody, CardFooter, CardHeader } from '@heroui/react'
import sanitizeHtml from 'sanitize-html'
import { useTranslation } from 'react-i18next'

export interface GalleryItems {
  key: string
  name: string
  image: string
  alt: string
  description: string
}

export type SelectableGalleryProps = Readonly<{
  items: GalleryItems[]
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
  const { items, selected, disabled, onSelect} = props
  const { t } = useTranslation()

  function handleSelected(key: string) {
    if (disabled || !onSelect) return
    onSelect(key)
  }

  return (
    <div className="flex flex-col sm:flex-row justify-center gap-4">
      {items.map((item) => (
        <Card
          key={item.key}
          className="w-full lg:max-w-lg p-2"
          style={{
            border: item.key === selected ? '2px solid hsl(var(--heroui-primary))' : 'none'
          }}
        >
          <CardHeader>
            <p className="font-bold text-large">{item.name}</p>
          </CardHeader>
          <CardBody className="flex flex-col gap-2">
            <img src={item.image} alt={item.alt} />
            <p
              className={'text-default-600'}
              dangerouslySetInnerHTML={{
                __html: sanitize(item.description)
              }}
            />
          </CardBody>
          {!disabled && (
            <CardFooter>
              <Button color="primary" onPress={() => handleSelected(item.key)}>
                {t('cv.select')}
              </Button>
            </CardFooter>
          )}
        </Card>
      ))}
    </div>
  )
}