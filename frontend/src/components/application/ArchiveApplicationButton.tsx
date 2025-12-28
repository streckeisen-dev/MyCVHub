import {
  Button,
  Modal,
  ModalBody,
  ModalContent,
  ModalFooter,
  ModalHeader,
  Tooltip
} from '@heroui/react'
import { FaBoxArchive } from 'react-icons/fa6'
import { useTranslation } from 'react-i18next'
import { useState } from 'react'
import { h3 } from '@/styles/primitives.ts'
import ApplicationApi from '@/api/ApplicationApi.ts'
import { RestError } from '@/types/RestError.ts'
import { addErrorToast, addSuccessToast } from '@/helpers/ToastHelper.ts'
import { TableButton } from '@/components/TableButton.tsx'

export type ArchiveApplicationButtonProps = Readonly<{
  id: number
  onArchive: () => void
}>

export function ArchiveApplicationButton(props: ArchiveApplicationButtonProps) {
  const { t, i18n } = useTranslation()
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false)
  const [isLoading, setIsLoading] = useState<boolean>(false)
  const { id, onArchive } = props

  async function handleArchive() {
    setIsLoading(true)
    try {
      await ApplicationApi.archive(id, i18n.language)
      addSuccessToast(t('application.editor.archive.archiveSuccess'))
      setIsModalOpen(false)
      onArchive()
    } catch (e) {
      const error = (e as RestError).errorDto
      addErrorToast(
        t('application.editor.archive.archiveError'),
        error?.message ?? t('error.genericMessage')
      )
    } finally {
      setIsLoading(false)
    }
  }

  function handleModalOpen() {
    setIsModalOpen(true)
  }

  function handleModalClose() {
    setIsModalOpen(false)
  }

  return (
    <>
      <Tooltip color="warning" content={t('application.editor.archive.title')} closeDelay={0}>
        <TableButton onClick={handleModalOpen} className="text-warning">
          <FaBoxArchive />
        </TableButton>
      </Tooltip>
      <Modal isOpen={isModalOpen} onClose={handleModalClose}>
        <ModalContent>
          {(onClose) => (
            <>
              <ModalHeader>
                <h3 className={h3()}>{t('application.editor.archive.title')}</h3>
              </ModalHeader>
              <ModalBody>
                <p>{t('application.editor.archive.confirmationText')}</p>
              </ModalBody>
              <ModalFooter>
                <Button variant="light" onPress={onClose} disabled={isLoading}>
                  {t('forms.cancel')}
                </Button>
                <Button color="warning" onPress={handleArchive} isLoading={isLoading}>
                  {t('application.editor.archive.title')}
                </Button>
              </ModalFooter>
            </>
          )}
        </ModalContent>
      </Modal>
    </>
  )
}
