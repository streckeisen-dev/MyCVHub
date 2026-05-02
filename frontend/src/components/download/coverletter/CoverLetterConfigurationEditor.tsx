import { ChangeEvent, ReactNode, useMemo } from 'react'
import { CoverLetterStyleDto } from '@/types/coverletter/CoverLetterStyleDto.ts'
import { Autocomplete, AutocompleteItem, Form, Input, Textarea } from '@heroui/react'
import { LanguageInput } from '@/components/input/LanguageInput.tsx'
import { Key } from '@react-types/shared'
import { SwitchInput } from '@/components/input/SwitchInput.tsx'
import { useTranslation } from 'react-i18next'
import {
  ApplicationDocument,
  ApplicationDocumentsEditor
} from '@/components/applicationTemplate/ApplicationDocumentsEditor.tsx'
import { ApplicationDetailsDto } from '@/types/application/ApplicationDetailsDto.ts'
import { h4, h5 } from '@/styles/primitives.ts'
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
  const containerMaxWidth = confined ? '' : '2xl:max-w-3/4'
  const formMaxWidth = confined ? 'max-w-9/10' : '2xl:max-w-1/2'

  return (
    <div className={`flex flex-col gap-10 w-full ${containerMaxWidth}`}>
      {templates && (
        <Autocomplete
          className="w-fit self-center"
          name="applicationTemplate"
          label={t('applicationTemplate.singular')}
          onSelectionChange={handleTemplateChange}
        >
          {templates.map((template) => (
            <AutocompleteItem key={template.id}>{template.name}</AutocompleteItem>
          ))}
        </Autocomplete>
      )}

      <CoverLetterGallery
        styles={styles}
        onSelect={handleStyleSelected}
        selectedStyle={selectedStyle?.key}
      />

      {selectedStyle && (
        <Form
          onSubmit={(e) => e.preventDefault()}
          className={`flex flex-col gap-6 self-center w-full ${formMaxWidth}`}
        >
          <LanguageInput
            isRequired
            selectedKey={config.language ?? null}
            onSelectionChange={handleLanguageChange}
          />

          <SwitchInput
            name="mirrorProfileImage"
            isSelected={config.mirrorProfileImage}
            onChange={updateConfig}
          >
            {t('coverLetter.mirrorProfileImage')}
          </SwitchInput>

          {errorMessages && (
            <>
              <h4 className={h4()}>{t('coverLetter.applicationInfo')}</h4>
              {application == null && (
                <Input
                  isRequired
                  name="jobTitle"
                  label={t('fields.jobTitle')}
                  value={config.jobTitle}
                  onChange={handleChange}
                  isInvalid={errors['application.jobTitle'] != null}
                  errorMessage={errors['application.jobTitle']}
                />
              )}

              <CheckboxInput
                label={t('coverLetter.knownContactPerson')}
                isSelected={config.contactPerson != null}
                onValueChange={handleKnownContactPersonChange}
              />

              {config.contactPerson ? (
                <>
                  <h5 className={h5()}>{t('coverLetter.contactPerson')}</h5>
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
                </>
              ) : (
                <Input
                  isRequired
                  name="addressee"
                  label={t('fields.addressee')}
                  description={t('coverLetter.addresseeHint')}
                  value={config.addressee}
                  onChange={(e: ChangeEvent<HTMLInputElement>) => handleAddresseeChange(e.currentTarget.value)}
                  isInvalid={errors['application.addressee'] != null}
                  errorMessage={errors['application.addressee']}
                />
              )}

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

              <h5 className={h5()}>{t('coverLetter.companyInfo')}</h5>

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

              <h4 className={h4()}>{t('coverLetter.content')}</h4>
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
            </>
          )}

          <ApplicationDocumentsEditor
            documents={config.documents}
            onChange={(docs) => updateConfig('documents', docs)}
            errorMessages={errorMessages ?? {}}
          />
        </Form>
      )}
    </div>
  )
}
