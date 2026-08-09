import { ReactNode } from 'react'
import { SelectableGallery } from '@/components/SelectableGallery.tsx'
import { CoverLetterStyleDto } from '@/types/coverletter/CoverLetterStyleDto.ts'
import { KeyValueObject } from '@/types/KeyValueObject.ts'
import ModernCoverLetter from '@/assets/cl_styles/modern.png'

const coverLetterImages: KeyValueObject<string> = {
  modern: ModernCoverLetter
}

export type CoverLetterGalleryProps = Readonly<{
  styles: CoverLetterStyleDto[]
  compact?: boolean
  selectedStyle: string | undefined
  onSelect?: (style: string) => void
  disabled?: boolean
}>

export function CoverLetterGallery(props: CoverLetterGalleryProps): ReactNode {
  const { styles, compact = false, selectedStyle, onSelect, disabled } = props

  function handleStyleSelected(key: string) {
    if (disabled || !onSelect) return
    onSelect(key)
  }
  return (
    <SelectableGallery
      items={styles.map((style) => ({
        key: style.key,
        name: style.name,
        image: coverLetterImages[style.key],
        alt: `Example of ${style.name} cover letter style`,
        description: style.description
      }))}
      selected={selectedStyle}
      compact={compact}
      onSelect={handleStyleSelected}
      disabled={disabled}
    />
  )
}
