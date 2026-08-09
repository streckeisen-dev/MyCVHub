import { Button } from '@/components/ui/Button.tsx'
import { Input } from '@/components/ui/Fields.tsx'
import { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { ErrorMessages } from '@/types/ErrorMessages.ts'

import { FaPlus, FaTrash } from 'react-icons/fa6'
import { v7 as uuid } from 'uuid'

export interface ApplicationDocument {
  id: string
  name: string
}

export type ApplicationDocumentsEditorProps = Readonly<{
  documents: ApplicationDocument[]
  errorMessages: ErrorMessages
  onChange: (documents: ApplicationDocument[]) => void
}>

export function ApplicationDocumentsEditor(props: ApplicationDocumentsEditorProps): ReactNode {
  const { documents, errorMessages, onChange } = props
  const { t } = useTranslation()

  function handleAddDocument() {
    onChange([...documents, { id: uuid(), name: '' }])
  }

  function handleDocumentChange(id: string, value: string) {
    onChange(documents.map((doc) => (doc.id === id ? { ...doc, name: value } : doc)))
  }

  function handleRemoveDocument(id: string) {
    onChange(documents.filter((doc) => doc.id !== id))
  }

  return (
    <div className="w-full max-w-3xl">
      <label className="text-default-500">{t('applicationTemplate.applicationDocuments')}</label>
      <p className="text-default-400">{t('applicationTemplate.applicationDocumentsHint')}</p>
      <div className="mt-4 flex flex-col gap-3">
        {documents.map((doc, index) => (
          <div
            key={doc.id}
            className="grid grid-cols-[minmax(0,1fr)_2.5rem_2.5rem] items-end gap-3"
          >
            <Input
              isRequired
              label={t('fields.documentName')}
              value={doc.name}
              onValueChange={(val: string) => handleDocumentChange(doc.id, val)}
            />
            <Button
              isIconOnly
              variant="danger"
              onPress={() => handleRemoveDocument(doc.id)}
            >
              <FaTrash />
            </Button>
            {index === documents.length - 1 ? (
              <Button isIconOnly variant="primary" onPress={handleAddDocument}>
                <FaPlus />
              </Button>
            ) : (
              <span aria-hidden="true" className="h-10 w-10" />
            )}
          </div>
        ))}
        {documents.length === 0 && (
          <Button
            className="self-start"
            isIconOnly
            variant="primary"
            aria-label={t('applicationTemplate.applicationDocuments')}
            onPress={handleAddDocument}
          >
            <FaPlus />
          </Button>
        )}
      </div>
      {errorMessages.documents && (
        <p className="text-danger text-sm mt-1">{errorMessages.documents}</p>
      )}
    </div>
  )
}
