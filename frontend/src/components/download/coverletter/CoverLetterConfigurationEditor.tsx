import { AutocompleteItem, Input, Textarea, Autocomplete } from '@/components/ui/Fields.tsx'
import { ChangeEvent, ReactNode, useMemo } from 'react'
import { CoverLetterStyleDto } from '@/types/coverletter/CoverLetterStyleDto.ts'
import { Form } from '@heroui/react'
import { LanguageInput } from '@/components/input/LanguageInput.tsx'
import { Key } from '@react-types/shared'
import { SwitchInput } from '@/components/input/SwitchInput.tsx'
import { useTranslation } from 'react-i18next'
import {
  ApplicationDocument,
  ApplicationDocumentsEditor
} from '@/components/applicationTemplate/ApplicationDocumentsEditor.tsx'
import { ApplicationDetailsDto } from '@/types/application/ApplicationDetailsDto.ts'
import { CheckboxInput } from '@/components/input/CheckboxInput.tsx'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { CoverLetterGallery } from '@/components/download/coverletter/CoverLetterGallery.tsx'
import { ApplicationTemplateDto } from '@/types/applicationTemplate/ApplicationTemplateDto.ts'
import { v7 as uuid } from 'uuid'

export interface CoverLetterConfigurationData {
  language: string | undefined
  style: string | undefined
  mirrorProfileImage: boolean
  jobTitle: string
  company: string
  contactPerson:
    | {
        firstName: string
        lastName: string
      }
    | undefined
  addressee: string
  salutation: string
  companyStreet: string
  companyPostcode: string
  companyCity: string
  coverLetterContent: string
  closing: string
  documents: ApplicationDocument[]
}

export type CoverLetterEditorProps = Readonly<{
  styles: CoverLetterStyleDto[]
  config: CoverLetterConfigurationData
  onChange: (config: CoverLetterConfigurationData) => void
  errorMessages?: ErrorMessages
  application?: ApplicationDetailsDto
  templates?: ApplicationTemplateDto[]
  confined?: boolean
}>

export function CoverLetterConfigurationEditor(props: CoverLetterEditorProps): ReactNode {
  const { styles, config, onChange, errorMessages, application, templates, confined } = props
  const { t } = useTranslation()

  const selectedStyle = styles.find((s) => s.key === config.style)

  const errors: ErrorMessages = useMemo(() => {
    return errorMessages ?? {}
  }, [errorMessages])

  function handleStyleSelected(key: string) {
    const style = styles.find((s) => s.key === key)
    if (!style) return
    onChange({
      ...config,
      style: style.key
    })
  }

  function handleLanguageChange(value: Key | null) {
    onChange({
      ...config,
      language: (value as string) ?? undefined
    })
  }

  function handleChange(e: ChangeEvent<HTMLInputElement>) {
    const name = e.currentTarget.name
    const value = e.currentTarget.value
    updateConfig(name, value)
  }

  function updateConfig(name: string, value: unknown) {
    Object.keys(errors)
      .filter((errorKey) => errorKey.endsWith(name))
      .forEach((errorKey) => (errors[errorKey] = undefined))
    onChange({
      ...config,
      [name]: value
    })
  }

  function handleKnownContactPersonChange(value: boolean) {
    onChange({
      ...config,
      contactPerson: value
        ? {
            firstName: '',
            lastName: ''
          }
        : undefined
    })
  }

  function handleAddresseeChange(value: string) {
    onChange({
      ...config,
      addressee: value,
      contactPerson: undefined
    })
  }

  function handleContactPersonChange(name: string, value: string) {
    onChange({
      ...config,
      addressee: '',
      contactPerson: {
        firstName: config.contactPerson?.firstName ?? '',
        lastName: config.contactPerson?.lastName ?? '',
        [name]: value
      }
    })
  }

  function handleTemplateChange(templateKey: Key | null) {
    if (!templateKey) return

    const template = templates?.find(
      (template) => template.id === Number.parseInt(templateKey as string)
    )
    if (!template) return

    onChange({
      ...config,
      style: template.coverLetterConfiguration.style,
      language: template.coverLetterConfiguration.language,
      mirrorProfileImage: template.coverLetterConfiguration.mirrorProfileImage,
      coverLetterContent: template.coverLetterConfiguration.content,
      closing: template.coverLetterConfiguration.closing,
      documents:
        template.coverLetterConfiguration.documents?.map((doc) => ({ id: uuid(), name: doc })) ?? []
    })
  }
  const containerMaxWidth = confined ? 'max-w-5xl' : 'max-w-5xl'
  const formMaxWidth = confined ? 'max-w-5xl' : 'max-w-5xl'

  return (
    <div className={`mx-auto flex w-full flex-col gap-7 ${containerMaxWidth}`}>
      {templates && (
        <div className="w-fit 2xl:pl-5">
          <Autocomplete
            name="applicationTemplate"
            label={t('applicationTemplate.singular')}
            onSelectionChange={handleTemplateChange}
            description={t('applicationTemplate.usageHint')}
          >
            {templates.map((template) => (
              <AutocompleteItem key={template.id} id={template.id}>
                {template.name}
              </AutocompleteItem>
            ))}
          </Autocomplete>
        </div>
      )}

      <div className="w-full">
        <CoverLetterGallery
          styles={styles}
          onSelect={handleStyleSelected}
          selectedStyle={selectedStyle?.key}
        />
      </div>

      {selectedStyle && (
        <Form
          onSubmit={(e) => e.preventDefault()}
          className={`flex w-full flex-col gap-6 self-center ${formMaxWidth}`}
        >
          <div className="grid w-full gap-5 rounded-lg border border-default-200 bg-surface p-5 md:grid-cols-[minmax(0,20rem)_minmax(0,1fr)] md:items-start">
            <LanguageInput
              isRequired
              selectedKey={config.language ?? null}
              onSelectionChange={handleLanguageChange}
            />

            <div className="md:pt-7">
              <SwitchInput
                name="mirrorProfileImage"
                isSelected={config.mirrorProfileImage}
                onChange={updateConfig}
              >
                {t('coverLetter.mirrorProfileImage')}
              </SwitchInput>
            </div>
          </div>

          {errorMessages && (
            <>
              <div className="flex w-full flex-col gap-5 rounded-lg border border-default-200 bg-surface p-5">
                <p className="text-base font-semibold text-foreground">
                  {t('coverLetter.applicationInfo')}
                </p>

                {application == null && (
                  <div className="max-w-3xl">
                    <Input
                      isRequired
                      name="jobTitle"
                      label={t('fields.jobTitle')}
                      value={config.jobTitle}
                      onChange={handleChange}
                      isInvalid={errors['application.jobTitle'] != null}
                      errorMessage={errors['application.jobTitle']}
                    />
                  </div>
                )}

                <CheckboxInput
                  label={t('coverLetter.knownContactPerson')}
                  isSelected={config.contactPerson != null}
                  onValueChange={handleKnownContactPersonChange}
                />

                {config.contactPerson ? (
                  <div className="grid w-full max-w-3xl gap-4 md:grid-cols-2">
                    <p className="text-sm font-semibold text-default-600 md:col-span-2">
                      {t('coverLetter.contactPerson')}
                    </p>
                    <Input
                      isRequired
                      name="firstName"
                      label={t('fields.firstName')}
                      value={config.contactPerson.firstName}
                      onChange={(e: ChangeEvent<HTMLInputElement>) =>
                        handleContactPersonChange(e.currentTarget.name, e.currentTarget.value)
                      }
                      isInvalid={errors['application.contactPerson.firstName'] != null}
                      errorMessage={errors['application.contactPerson.firstName']}
                    />
                    <Input
                      isRequired
                      name="lastName"
                      label={t('fields.lastName')}
                      value={config.contactPerson.lastName}
                      onChange={(e: ChangeEvent<HTMLInputElement>) =>
                        handleContactPersonChange(e.currentTarget.name, e.currentTarget.value)
                      }
                      isInvalid={errors['application.contactPerson.lastName'] != null}
                      errorMessage={errors['application.contactPerson.lastName']}
                    />
                  </div>
                ) : (
                  <div className="max-w-3xl">
                    <Input
                      isRequired
                      name="addressee"
                      label={t('fields.addressee')}
                      description={t('coverLetter.addresseeHint')}
                      value={config.addressee}
                      onChange={(e: ChangeEvent<HTMLInputElement>) =>
                        handleAddresseeChange(e.currentTarget.value)
                      }
                      isInvalid={errors['application.addressee'] != null}
                      errorMessage={errors['application.addressee']}
                    />
                  </div>
                )}

                <div className="max-w-3xl">
                  <Input
                    isRequired
                    name="salutation"
                    label={t('fields.salutation')}
                    description={
                      config.contactPerson
                        ? t('coverLetter.salutationContactPersonHint')
                        : t('coverLetter.salutationAddresseeHint')
                    }
                    value={config.salutation}
                    onChange={handleChange}
                    isInvalid={errors['application.salutation'] != null}
                    errorMessage={errors['application.salutation']}
                  />
                </div>

                <div className="grid w-full gap-4 md:grid-cols-2">
                  <p className="text-sm font-semibold text-default-600 md:col-span-2">
                    {t('coverLetter.companyInfo')}
                  </p>

                  {application == null && (
                    <Input
                      isRequired
                      name="company"
                      label={t('fields.company')}
                      value={config.company}
                      onChange={handleChange}
                      isInvalid={errors['application.company'] != null}
                      errorMessage={errors['application.company']}
                    />
                  )}

                  <Input
                    isRequired
                    name="companyStreet"
                    label={t('fields.street')}
                    value={config.companyStreet}
                    onChange={handleChange}
                    isInvalid={errors['application.companyAddress.street'] != null}
                    errorMessage={errors['application.companyAddress.street']}
                  />

                  <Input
                    isRequired
                    name="companyPostcode"
                    label={t('fields.postcode')}
                    value={config.companyPostcode}
                    onChange={handleChange}
                    isInvalid={errors['application.companyAddress.postcode'] != null}
                    errorMessage={errors['application.companyAddress.postcode']}
                  />

                  <Input
                    isRequired
                    name="companyCity"
                    label={t('fields.city')}
                    value={config.companyCity}
                    onChange={handleChange}
                    isInvalid={errors['application.companyAddress.city'] != null}
                    errorMessage={errors['application.companyAddress.city']}
                  />
                </div>
              </div>

              <div className="flex w-full flex-col gap-5 rounded-lg border border-default-200 bg-surface p-5">
                <p className="text-base font-semibold text-foreground">
                  {t('coverLetter.content')}
                </p>
                <Textarea
                  isRequired
                  minRows={20}
                  maxRows={50}
                  name="coverLetterContent"
                  label={t('fields.content')}
                  description={
                    <p className="whitespace-break-spaces">{t('coverLetter.coverLetterHint')}</p>
                  }
                  value={config.coverLetterContent}
                  onChange={handleChange}
                  isInvalid={errors['application.coverLetterContent'] != null}
                  errorMessage={errors['application.coverLetterContent']}
                />

                <div className="max-w-3xl">
                  <Input
                    isRequired
                    name="closing"
                    label={t('fields.closing')}
                    description={t('coverLetter.closingHint')}
                    value={config.closing}
                    onChange={handleChange}
                    isInvalid={errors['application.closing'] != null}
                    errorMessage={errors['application.closing']}
                  />
                </div>
              </div>
            </>
          )}

          <div className="w-full rounded-lg border border-default-200 bg-surface p-5">
            <ApplicationDocumentsEditor
              documents={config.documents}
              onChange={(docs) => updateConfig('documents', docs)}
              errorMessages={errorMessages ?? {}}
            />
          </div>
        </Form>
      )}
    </div>
  )
}
