import { centerSection, title } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'
import {
  ApplicationTable,
  ApplicationTableControls
} from '@/components/application/ApplicationTable.tsx'
import { useEffect, useRef, useState } from 'react'
import { Button } from '@heroui/react'
import ApplicationApi from '@/api/ApplicationApi.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { EditApplicationModal } from '@/components/application/EditApplicationModal.tsx'
import { ApplicationStatusDto } from '@/types/application/ApplicationStatusDto.ts'

export function ApplicationsPage() {
  const { t, i18n } = useTranslation()

  const [applicationStatuses, setApplicationStatuses] = useState<ApplicationStatusDto[]>([])
  const [showModal, setShowModal] = useState<boolean>(false)

  const applicationsTable = useRef<ApplicationTableControls>(null)

  useEffect(() => {
    async function loadApplicationStatus() {
      try {
        const result = await ApplicationApi.getApplicationStatuses(i18n.language)
        setApplicationStatuses(result)
      } catch (_ignore) {
        addErrorToast(t('application.statusLoadingError'))
      }
    }
    loadApplicationStatus()
  }, [])

  function handleAdd() {
    setShowModal(true)
  }

  function handleModalClose() {
    setShowModal(false)
  }

  function handleSave() {
    applicationsTable.current?.refresh()
  }

  return (
    <section className={centerSection()}>
      <h1 className={title()}>{t('application.title')}</h1>

      <div className="w-full flex flex-col items-end">
        <Button color="primary" onPress={handleAdd}>
          {t('application.editor.add')}
        </Button>
      </div>

      <ApplicationTable ref={applicationsTable} statuses={applicationStatuses} />

      {showModal && <EditApplicationModal onClose={handleModalClose} onSave={handleSave} />}
    </section>
  )
}
