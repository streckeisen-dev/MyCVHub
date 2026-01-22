import { Fragment, ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { Button, Input } from '@heroui/react'
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
    onChange([...documents.filter((doc) => doc.id !== id), { id, name: value }])
  }

  function handleRemoveDocument(id: string) {
    onChange(documents.filter((doc) => doc.id !== id))
  }

  return (
    <div>
      <label className="text-default-500">{t('applicationTemplate.applicationDocuments')}</label>
      <p className="text-default-400">{t('applicationTemplate.applicationDocumentsHint')}</p>
      <div className="grid grid-cols-12 gap-2 items-center mt-4">
        {documents.map((doc) => (
          <Fragment key={doc.id}>
            <Input
              isRequired
              className="col-span-10"
              label={t('fields.documentName')}
              value={doc.name}
              onValueChange={(val) => handleDocumentChange(doc.id, val)}
            />
            <Button
              className="col-span-2"
              isIconOnly
              color="danger"
              onPress={() => handleRemoveDocument(doc.id)}
              radius="full"
            >
              <FaTrash />
            </Button>
          </Fragment>
        ))}
        <Button
          className="col-span-12"
          isIconOnly
          color="primary"
          onPress={handleAddDocument}
          radius="full"
        >
          <FaPlus />
        </Button>
      </div>
      {errorMessages.documents && (
        <p className="text-danger text-sm mt-1">{errorMessages.documents}</p>
      )}
    </div>
  )
}
