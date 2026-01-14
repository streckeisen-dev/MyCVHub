import { cloneElement, ReactElement, ReactNode, useState } from 'react'
import {
  Button,
  ButtonProps,
  Modal,
  ModalBody,
  ModalContent,
  ModalFooter,
  ModalHeader,
  TooltipProps
} from '@heroui/react'
import { h3 } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'
import ApplicationTemplateApi from '@/api/ApplicationTemplateApi.ts'
import { addErrorToast, addSuccessToast } from '@/helpers/ToastHelper.ts'
import { RestError } from '@/types/RestError.ts'
import { TableButtonProps } from '@/components/btn/TableButton.tsx'

export type DeleteApplicationTemplateModalProps = Readonly<{
  id: number
  trigger: ReactElement<ButtonProps | TableButtonProps | TooltipProps>
  onDelete: () => void
}>

export function DeleteApplicationTemplateModal(
  props: DeleteApplicationTemplateModalProps
): ReactNode {
  const { id, trigger, onDelete } = props
  const { t, i18n } = useTranslation()

  const [isModalOpen, setIsModalOpen] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)

  function handleModalOpen() {
    setIsModalOpen(true)
  }

  function handleModalClose() {
    setIsModalOpen(false)
  }

  async function handleDelete() {
    try {
      await ApplicationTemplateApi.deleteApplicationTemplate(id, i18n.language)
      addSuccessToast(t('applicationTemplate.delete.deleteSuccess'))
      onDelete()
      handleModalClose()
    } catch (e) {
      const error = (e as RestError).errorDto
      addErrorToast(
        t('applicationTemplate.delete.deleteError'),
        error?.message ?? t('error.genericMessage')
      )
    } finally {
      setIsDeleting(false)
    }
  }

  const triggerButtonProps = Object.hasOwn(trigger.props, 'onPress')
    ? {
        ...trigger.props,
        onPress: handleModalOpen
      }
    : {
        ...trigger.props,
        onClick: handleModalOpen
      }

  // eslint-disable-next-line @eslint-react/no-clone-element
  const triggerButton = cloneElement(trigger, triggerButtonProps)

  return (
    <>
      {triggerButton}
      <Modal
        isOpen={isModalOpen}
        onClose={handleModalClose}
        backdrop="blur"
        size="lg"
        className="p-6"
      >
        <ModalContent>
          {(onClose) => (
            <>
              <ModalHeader>
                <h3 className={h3()}>{t('applicationTemplate.delete.title')}</h3>
              </ModalHeader>
              <ModalBody>
                <p>{t('applicationTemplate.delete.confirmationText')}</p>
              </ModalBody>
              <ModalFooter>
                <Button color="default" variant="light" onPress={onClose} isDisabled={isDeleting}>
                  {t('forms.cancel')}
                </Button>
                <Button
                  color="danger"
                  onPress={handleDelete}
                  isLoading={isDeleting}
                  isDisabled={isDeleting}
                >
                  {t('applicationTemplate.delete.title')}
                </Button>
              </ModalFooter>
            </>
          )}
        </ModalContent>
      </Modal>
    </>
  )
}
