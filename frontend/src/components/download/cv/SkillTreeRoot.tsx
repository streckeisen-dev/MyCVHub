import { Checkbox } from '@/components/ui/Fields.tsx'
import { ReactNode, useState } from 'react'

import { FaCaretDown, FaCaretRight } from 'react-icons/fa6'
import { useTranslation } from 'react-i18next'

export interface SkillCategories {
  title: string
  children: SkillLeaf[]
}

export interface SkillLeaf {
  id: number
  title: string
  selected: boolean
}

export type SkillTreeRootProps = Readonly<{
  content: SkillCategories[]
  onChange: (selected: number[]) => void
  disabled: boolean
  errorMessage?: string
}>

export function SkillTreeRoot(props: SkillTreeRootProps): ReactNode {
  const { t } = useTranslation()
  const { content, onChange, disabled, errorMessage } = props

  const [isExpanded, setIsExpanded] = useState(false)
  const isRootSelected = content.every((c) => c.children.every((leaf) => leaf.selected))
  const toggleExpand = () => setIsExpanded((prev) => !prev)
  const Icon = isExpanded ? FaCaretDown : FaCaretRight

  function handleRootChange(isSelected: boolean) {
    if (disabled) return
    if (isSelected) {
      onChange(content.flatMap((c) => c.children.map((l) => l.id)))
    } else {
      onChange([])
    }
  }

  function handleSelected(selected: number[]) {
    if (disabled) return
    const alreadySelected: number[] = content.flatMap((c) =>
      c.children.filter((l) => l.selected).map((l) => l.id)
    )
    onChange([...alreadySelected, ...selected])
  }

  function handleDeselected(deselected: number[]) {
    if (disabled) return
    const selected: number[] = content
      .flatMap((c) => c.children.filter((l) => l.selected).map((l) => l.id))
      .filter((id) => !deselected.includes(id))
    onChange(selected)
  }

  return (
    <div>
      <div className="flex items-center gap-3 rounded-md px-2 py-2 text-sm hover:bg-default/5">
        <Icon onClick={toggleExpand} className="shrink-0 cursor-pointer text-default-500" />
        <Checkbox
          isDisabled={disabled}
          isSelected={isRootSelected}
          onValueChange={handleRootChange}
          isIndeterminate={
            !isRootSelected && content.some((c) => c.children.some((leaf) => leaf.selected))
          }
        />
        <p className="min-w-0 font-medium text-foreground">{t('skills.title')}</p>
      </div>
      {errorMessage && <p className="px-2 text-sm text-danger">{errorMessage}</p>}
      {isExpanded && (
        <div className="flex flex-col gap-1">
          {content.map((category) => (
            <SkillCategoryLeaf
              key={category.title}
              title={category.title}
              content={category.children}
              onSelect={handleSelected}
              onDeselect={handleDeselected}
              disabled={disabled}
            />
          ))}
        </div>
      )}
    </div>
  )
}

type SkillCategoryLeafProps = Readonly<{
  title: string
  content: SkillLeaf[]
  onSelect: (selected: number[]) => void
  onDeselect: (deselected: number[]) => void
  disabled: boolean
}>

function SkillCategoryLeaf(props: SkillCategoryLeafProps): ReactNode {
  const { title, content, onSelect, onDeselect, disabled } = props

  const [isExpanded, setIsExpanded] = useState(false)
  const isCategorySelected = content.every((leaf) => leaf.selected)
  const toggleExpand = () => setIsExpanded((prev) => !prev)
  const Icon = isExpanded ? FaCaretDown : FaCaretRight

  function handleCategoryChange(isSelected: boolean) {
    if (disabled) return
    const ids = content.map((l) => l.id)
    if (isSelected) {
      onSelect(ids)
    } else {
      onDeselect(ids)
    }
  }

  function handleLeafChange(id: number, isSelected: boolean) {
    if (disabled) return
    const ids = [id]
    if (isSelected) {
      onSelect(ids)
    } else {
      onDeselect(ids)
    }
  }

  return (
    <>
      <div className="flex items-center gap-3 rounded-md px-2 py-2 pl-8 text-sm hover:bg-default/5">
        <Icon onClick={toggleExpand} className="shrink-0 cursor-pointer text-default-500" />
        <Checkbox
          isDisabled={disabled}
          isSelected={isCategorySelected}
          onValueChange={handleCategoryChange}
          isIndeterminate={!isCategorySelected && content.some((leaf) => leaf.selected)}
        />
        <p className="min-w-0 font-medium text-foreground">{title}</p>
      </div>
      {isExpanded && (
        <div className="flex flex-col gap-1">
          {content.map((leaf) => (
            <div
              key={leaf.id}
              className="flex items-center gap-3 rounded-md px-2 py-2 pl-16 text-sm hover:bg-default/5"
            >
              <Checkbox
                isDisabled={disabled}
                isSelected={leaf.selected}
                onValueChange={(isSelected) => handleLeafChange(leaf.id, isSelected)}
              />
              <p className="min-w-0 text-foreground">{leaf.title}</p>
            </div>
          ))}
        </div>
      )}
    </>
  )
}
