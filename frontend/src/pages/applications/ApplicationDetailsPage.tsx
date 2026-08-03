import { Divider } from '@/components/ui/Display.tsx'
import { Button } from '@/components/ui/Button.tsx'
import { Fragment, ReactNode, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import ApplicationApi from '@/api/ApplicationApi.ts'
import { Link, useParams } from 'react-router-dom'
import { RestError } from '@/types/RestError.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { LoadingWrapper } from '@/layouts/LoadingWrapper.tsx'
import { Empty } from '@/components/Empty.tsx'
import { Attribute, AttributeList } from '@/components/AttributeList.tsx'
import { TFunction } from 'i18next'
import { ApplicationStatus } from '@/components/application/ApplicationStatus.tsx'

import { FaArrowLeft, FaPen } from 'react-icons/fa6'
import { EditApplicationModal } from '@/components/application/EditApplicationModal.tsx'
import { ExternalLink } from '@/components/ExternalLink.tsx'
import { ApplicationDetailsDto } from '@/types/application/ApplicationDetailsDto.ts'
import { ApplicationTransitionModal } from '@/components/application/ApplicationTransitionModal.tsx'
import { ApplicationTransitionDto } from '@/types/application/ApplicationTransitionDto.ts'
import { formatDateTime } from '@/helpers/DateHelper.ts'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { CvDownload } from '@/components/download/cv/CvDownload.tsx'
import { CoverLetterDownload } from '@/components/download/coverletter/CoverLetterDownload.tsx'
import { Accordion, Dropdown, Label } from '@heroui/react'
import { DropdownButton } from '@/components/ui/DropdownButton.tsx'
import { DetailTitle, Page, PageHeader } from '@/components/ui/Layout.tsx'

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
    async function loadData() {
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

    loadData()
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
        <Page size="default">
          <Button
            as={Link}
            to={getRoutePath(RouteId.ApplicationsOverview)}
            variant="tertiary"
            className="self-start"
          >
            <FaArrowLeft />
            {t('application.back')}
          </Button>
          <PageHeader>
            <DetailTitle>
              {t('application.singular')} {t('application.as')} {application.jobTitle}{' '}
              {t('application.at')} {application.company}
            </DetailTitle>
          </PageHeader>
          <div className="flex flex-col gap-4 w-full">
            {!application.isArchived && (
              <div className="flex flex-wrap items-center gap-3">
                {application.transitions.length > 0 && (
                  <Dropdown>
                    <DropdownButton>
                      <span>{t('table.actions')}</span>
                    </DropdownButton>
                    <Dropdown.Popover>
                      <Dropdown.Menu aria-label={t('table.actions')}>
                        {application.transitions.map((transition) => (
                          <Dropdown.Item
                            key={String(transition.id)}
                            id={String(transition.id)}
                            textValue={transition.label}
                            onAction={() => handleTransition(transition)}
                          >
                            <Label>{transition.label}</Label>
                          </Dropdown.Item>
                        ))}
                      </Dropdown.Menu>
                    </Dropdown.Popover>
                  </Dropdown>
                )}
                {!application.status.isTerminal && (
                  <Button
                    className="ml-auto"
                    variant="primary"
                    onPress={handleEdit}
                  >
                    <FaPen />
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
            <Accordion
              allowsMultipleExpanded
              defaultExpandedKeys={['details', 'description', 'history']}
              variant="surface"
            >
              <Accordion.Item id="details">
                <Accordion.Heading>
                  <Accordion.Trigger>
                    {t('application.details')}
                    <Accordion.Indicator />
                  </Accordion.Trigger>
                </Accordion.Heading>
                <Accordion.Panel>
                  <Accordion.Body>
                    <AttributeList
                      attributes={getApplicationAttributes(application, t)}
                      className={bgClasses}
                    />
                  </Accordion.Body>
                </Accordion.Panel>
              </Accordion.Item>
              {application.description != null && (
                <Accordion.Item id="description">
                  <Accordion.Heading>
                    <Accordion.Trigger>
                      {t('fields.description')}
                      <Accordion.Indicator />
                    </Accordion.Trigger>
                  </Accordion.Heading>
                  <Accordion.Panel>
                    <Accordion.Body>
                      <div className={bgClasses}>
                        <p className="whitespace-break-spaces">{application.description}</p>
                      </div>
                    </Accordion.Body>
                  </Accordion.Panel>
                </Accordion.Item>
              )}
              {application.status.key === 'UNSENT' && (
                <Accordion.Item id="cvDownload">
                  <Accordion.Heading>
                    <Accordion.Trigger>
                      {t('cv.title')}
                      <Accordion.Indicator />
                    </Accordion.Trigger>
                  </Accordion.Heading>
                  <Accordion.Panel>
                    <Accordion.Body>
                      <CvDownload />
                    </Accordion.Body>
                  </Accordion.Panel>
                </Accordion.Item>
              )}
              {application.status.key === 'UNSENT' && (
                <Accordion.Item id="coverLetterDownload">
                  <Accordion.Heading>
                    <Accordion.Trigger>
                      {t('coverLetter.download.title')}
                      <Accordion.Indicator />
                    </Accordion.Trigger>
                  </Accordion.Heading>
                  <Accordion.Panel>
                    <Accordion.Body>
                      <CoverLetterDownload application={application} confined />
                    </Accordion.Body>
                  </Accordion.Panel>
                </Accordion.Item>
              )}
              {application.history.length > 0 && (
                <Accordion.Item id="history" className="hidden sm:block">
                  <Accordion.Heading>
                    <Accordion.Trigger>
                      {t('application.history.title')}
                      <Accordion.Indicator />
                    </Accordion.Trigger>
                  </Accordion.Heading>
                  <Accordion.Panel>
                    <Accordion.Body>
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
                    </Accordion.Body>
                  </Accordion.Panel>
                </Accordion.Item>
              )}
            </Accordion>
          </div>
        </Page>
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
