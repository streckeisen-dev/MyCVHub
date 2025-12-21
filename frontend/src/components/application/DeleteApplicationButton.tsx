import { ReactNode, useState, MouseEvent } from 'react'
import { FaTrash } from 'react-icons/fa6'
import {
  Button,
  Modal,
  ModalBody,
  ModalContent,
  ModalFooter,
  ModalHeader,
  Tooltip
} from '@heroui/react'
import { useTranslation } from 'react-i18next'
import { h3 } from '@/styles/primitives.ts'
import ApplicationApi from '@/api/ApplicationApi.ts'
import { addErrorToast, addSuccessToast } from '@/helpers/ToastHelper.ts'
import { RestError } from '@/types/RestError.ts'

export type DeleteApplicationButtonProps = Readonly<{
  id: number,
  onDelete: () => void
}>

export function DeleteApplicationButton(props: DeleteApplicationButtonProps): ReactNode {
  const { t, i18n } = useTranslation()
  const { id, onDelete } = props

  const [isModalOpen, setIsModalOpen] = useState<boolean>(false)

  function handleModalOpen(e: MouseEvent<HTMLSpanElement>) {
    e.stopPropagation()
    setIsModalOpen(true)
  }

  function handleModalClose() {
    setIsModalOpen(false)
  }

  async function handleDelete() {
    try {
      await ApplicationApi.deleteApplication(id, i18n.language)
      setIsModalOpen(false)
      addSuccessToast(t('application.editor.delete.deleteSuccess'))
      onDelete()
    } catch (e) {
      const error = (e as RestError).errorDto
      addErrorToast(t('application.editor.delete.deleteError'), error?.message ?? t('error.genericMessage'))
    }
  }

  return (
    <>
      <Tooltip color="danger" content={t('application.editor.delete.title')} closeDelay={0}>
        <button
          type="button"
          className="text-lg text-danger cursor-pointer z-10"
          onClick={handleModalOpen}
        >
          <FaTrash />
        </button>
      </Tooltip>
      <Modal isOpen={isModalOpen} backdrop="blur" onClose={handleModalClose}>
        <ModalContent>
          {(onClose) => (
            <>
              <ModalHeader>
                <h3 className={h3()}>{t('application.editor.delete.title')}</h3>
              </ModalHeader>
              <ModalBody>
                <p>{t('application.editor.delete.confirmationText')}</p>
              </ModalBody>
              <ModalFooter>
                <Button variant="light" onPress={onClose}>
                  {t('forms.cancel')}
                </Button>
                <Button color="danger" onPress={handleDelete}>
                  {t('application.editor.delete.title')}
                </Button>
              </ModalFooter>
            </>
          )}
        </ModalContent>
      </Modal>
    </>
  )
}