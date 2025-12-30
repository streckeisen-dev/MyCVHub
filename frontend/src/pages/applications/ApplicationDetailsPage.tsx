import { Fragment, ReactNode, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import ApplicationApi from '@/api/ApplicationApi.ts'
import { Link, useParams } from 'react-router-dom'
import { RestError } from '@/types/RestError.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { LoadingWrapper } from '@/layouts/LoadingWrapper.tsx'
import { Empty } from '@/components/Empty.tsx'
import { h2 } from '@/styles/primitives.ts'
import { Attribute, AttributeList } from '@/components/AttributeList.tsx'
import { TFunction } from 'i18next'
import { ApplicationStatus } from '@/components/application/ApplicationStatus.tsx'
import {
  Accordion,
  AccordionItem,
  Button,
  Divider,
  Dropdown,
  DropdownItem,
  DropdownMenu,
  DropdownTrigger
} from '@heroui/react'
import { FaArrowLeft, FaPen } from 'react-icons/fa6'
import { EditApplicationModal } from '@/components/application/EditApplicationModal.tsx'
import { ExternalLink } from '@/components/ExternalLink.tsx'
import { ApplicationDetailsDto } from '@/types/application/ApplicationDetailsDto.ts'
import { ApplicationTransitionModal } from '@/components/application/ApplicationTransitionModal.tsx'
import { ApplicationTransitionDto } from '@/types/application/ApplicationTransitionDto.ts'
import { formatDateTime } from '@/helpers/DateHelper.ts'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'

function getApplicationAttributes(application: ApplicationDetailsDto, t: TFunction): Attribute[] {
  const attributes: Attribute[] = [
    {
      name: t('fields.status'),
      value: <ApplicationStatus status={application.status} />
    }
  ]

  if (application.source) {
    attributes.push({
      name: t('fields.source'),
      value: <ExternalLink href={application.source}>{application.source}</ExternalLink>
    })
  }
  attributes.push({
    name: t('application.createdAt'),
    value: formatDateTime(application.createdAt)
  })
  return attributes
}

export function ApplicationDetailsPage(): ReactNode {
  const { t, i18n } = useTranslation()
  const params = useParams()

  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [application, setApplication] = useState<ApplicationDetailsDto>()
  const [showEditModal, setShowEditModal] = useState<boolean>(false)
  const [currentTransition, setCurrentTransition] = useState<ApplicationTransitionDto | undefined>(
    undefined
  )

  useEffect(() => {
    async function loadApplication() {
      if (!params.id) {
        setIsLoading(false)
        return
      }
      try {
        const result = await ApplicationApi.getApplication(
          Number.parseInt(params.id),
          i18n.language
        )
        setApplication(result)
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('application.loadingError'), error?.message ?? t('error.genericMessage'))
      } finally {
        setIsLoading(false)
      }
    }

    loadApplication()
  }, [])

  function handleEdit() {
    setShowEditModal(true)
  }

  function handleModalClose() {
    setShowEditModal(false)
  }

  function handleSave(update: ApplicationDetailsDto) {
    setApplication(update)
  }

  function handleTransition(transition: ApplicationTransitionDto) {
    setCurrentTransition(transition)
  }

  function handleTransitionCancel() {
    setCurrentTransition(undefined)
  }

  const bgClasses = 'bg-default-100 p-5 rounded-lg'

  return (
    <LoadingWrapper isLoading={isLoading}>
      {application ? (
        <div className="w-full md:w-3/4 xl:w-1/2 items-center flex flex-col gap-6">
          <Button
            as={Link}
            to={getRoutePath(RouteId.ApplicationsOverview)}
            variant="light"
            className="self-start"
            startContent={<FaArrowLeft />}
          >
            {t('application.back')}
          </Button>
          <h2 className={h2()}>
            {t('application.singular')} {t('application.as')} {application.jobTitle}{' '}
            {t('application.at')} {application.company}
          </h2>
          <div className="flex flex-col gap-4 w-full">
            {!application.isArchived && (
              <div className="flex flex-wrap gap-5">
                {application.transitions.length > 0 && (
                  <Dropdown>
                    <DropdownTrigger>
                      <Button color="primary" variant="bordered">
                        {t('table.actions')}
                      </Button>
                    </DropdownTrigger>
                    <DropdownMenu>
                      {application.transitions.map((transition) => (
                        <DropdownItem
                          as={Button}
                          color="primary"
                          className="bg-primary h-10 mb-1.5"
                          key={transition.id}
                          onPress={() => handleTransition(transition)}
                        >
                          {transition.label}
                        </DropdownItem>
                      ))}
                    </DropdownMenu>
                  </Dropdown>
                )}
                {!application.status.isTerminal && (
                  <Button
                    className="ml-auto"
                    startContent={<FaPen />}
                    color="primary"
                    onPress={handleEdit}
                  >
                    {t('application.editor.edit')}
                  </Button>
                )}
              </div>
            )}
            {application.isArchived && (
              <div className="bg-warning-400 rounded-lg w-full p-2 text-center">
                <p>{t('application.archivedDescription')}</p>
              </div>
            )}
            <Accordion selectionMode="multiple" defaultExpandedKeys="all" variant="bordered">
              <AccordionItem key="details" title={t('application.details')}>
                <AttributeList
                  attributes={getApplicationAttributes(application, t)}
                  className={bgClasses}
                />
              </AccordionItem>
              <AccordionItem
                key="description"
                title={t('fields.description')}
                hidden={application.description == null}
              >
                <div className={bgClasses}>
                  <p className="whitespace-break-spaces">{application.description}</p>
                </div>
              </AccordionItem>
              <AccordionItem
                key="history"
                title={t('application.history.title')}
                hidden={application.history.length === 0}
                className="hidden sm:block"
              >
                <div className={bgClasses}>
                  <div className="grid grid-cols-4 gap-4">
                    <p className="font-bold">{t('application.history.from')}</p>
                    <p className="font-bold">{t('application.history.to')}</p>
                    <p className="font-bold">{t('fields.comment')}</p>
                    <p className="font-bold">{t('application.history.timestamp')}</p>

                    <Divider className="col-span-4" />

                    {application.history.map((historyEntry) => (
                      <Fragment key={historyEntry.id}>
                        <ApplicationStatus status={historyEntry.from} />
                        <ApplicationStatus status={historyEntry.to} />
                        <p className="whitespace-break-spaces">
                          {historyEntry.comment ?? <>&mdash;</>}
                        </p>
                        <p>{formatDateTime(historyEntry.timestamp)}</p>

                        <Divider className="col-span-4" />
                      </Fragment>
                    ))}
                  </div>
                </div>
              </AccordionItem>
            </Accordion>
          </div>
        </div>
      ) : (
        <Empty headline={t('application.notFound')} />
      )}
      {showEditModal && (
        <EditApplicationModal
          initialValue={application}
          onClose={handleModalClose}
          onSave={handleSave}
        />
      )}
      {application && currentTransition != null && (
        <ApplicationTransitionModal
          application={application}
          transition={currentTransition}
          onSave={handleSave}
          onClose={handleTransitionCancel}
        />
      )}
    </LoadingWrapper>
  )
}
