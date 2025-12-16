import React, { useState } from 'react'
import {
  Button,
  Modal,
  ModalBody,
  ModalContent,
  ModalFooter,
  ModalHeader,
  useDisclosure
} from '@heroui/react'
import { useTranslation } from 'react-i18next'
import AccountApi from '@/api/AccountApi.ts'
import { RestError } from '@/types/RestError.ts'
import { FaTrash } from 'react-icons/fa6'
import { useNavigate } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { addErrorToast } from '@/helpers/ToastHelper.ts'

export function AccountDeleteButton(): React.ReactNode {
  const { t, i18n } = useTranslation()
  const { isOpen, onOpen, onOpenChange } = useDisclosure()
  const [isDeleting, setIsDeleting] = useState(false)
  const navigate = useNavigate()

  async function handleDelete() {
    setIsDeleting(true)
    try {
      await AccountApi.deleteAccount(i18n.language)
      navigate(getRoutePath(RouteId.Logout))
    } catch (e) {
      const error = (e as RestError).errorDto
      addErrorToast(
        t('account.accountMgmt.delete.error'),
        error?.message ?? t('error.genericMessage')
      )
    } finally {
      setIsDeleting(false)
    }
  }

  return (
    <>
      <Button color="danger" startContent={<FaTrash />} onPress={onOpen}>
        {t('account.accountMgmt.delete.action')}
      </Button>
      <Modal isOpen={isOpen} onOpenChange={onOpenChange}>
        <ModalContent>
          {(onClose) => (
            <>
              <ModalHeader className="flex flex-col gap-1">
                {t('account.accountMgmt.delete.action')}
              </ModalHeader>
              <ModalBody>
                <p>{t('account.accountMgmt.delete.confirmationText')}</p>
              </ModalBody>
              <ModalFooter>
                <Button color="default" variant="light" onPress={onClose}>
                  {t('forms.cancel')}
                </Button>
                <Button color="danger" onPress={handleDelete} isLoading={isDeleting}>
                  {t('account.accountMgmt.delete.action')}
                </Button>
              </ModalFooter>
            </>
          )}
        </ModalContent>
      </Modal>
    </>
  )
}
