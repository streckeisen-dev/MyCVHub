import { ChangeEvent, FormEvent, ReactNode, useState } from 'react'
import {useTranslation} from 'react-i18next'
import {useNavigate} from 'react-router-dom'
import {ErrorMessages} from '@/types/ErrorMessages.ts'
import defaultProfilePicture from '@/assets/default_profile_picture.png'
import {getRoutePath, RouteId} from '@/config/RouteTree.tsx'
import {ProfileUpdateRequestDto} from '@/types/ProfileUpdateRequestDto.ts'
import ProfileApi from '@/api/ProfileApi.ts'
import {RestError} from '@/types/RestError.ts'
import {addToast, Form, Input, Textarea} from '@heroui/react'
import {twoColumnForm} from '@/styles/primitives.ts'
import {SwitchInput} from '@/components/SwitchInput.tsx'
import {FaCamera} from 'react-icons/fa6'
import {FormButtons} from '@/components/FormButtons.tsx'

interface GeneralProfileInfo {
  profilePicture?: string;
  jobTitle?: string;
  bio?: string;
  isProfilePublic?: boolean;
  isEmailPublic?: boolean;
  isPhonePublic?: boolean;
  isAddressPublic?: boolean;
  hideDescriptions?: boolean;
}

export interface GeneralEditorProps {
  initialValue: GeneralProfileInfo;
}

export function GeneralEditor(props: GeneralEditorProps): ReactNode {
  const { t, i18n } = useTranslation()
  const {initialValue} = props
  const navigate = useNavigate()

  const [info, setInfo] = useState<GeneralProfileInfo>(initialValue)
  const [uploadedProfilePicture, setUploadedProfilePicture] = useState<File | undefined>()
  const [isSaving, setIsSaving] = useState<boolean>(false)
  const [errorMessages, setErrorMessages] = useState<ErrorMessages>({})

  const profilePicture = uploadedProfilePicture
    ? URL.createObjectURL(uploadedProfilePicture)
    : info.profilePicture ?? defaultProfilePicture

  function handleChange(e: ChangeEvent<HTMLInputElement>) {
    const name = e.currentTarget.name
    const value = e.currentTarget.value
    updateValue(name, value)
  }

  function updateValue(name: string, value: unknown | undefined) {
    setInfo((prev) => {
      return {
        ...prev,
        [name]: value
      }
    })
  }

  function handleImageChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.currentTarget.files ? e.currentTarget.files[0] : undefined
    setUploadedProfilePicture(file)
  }

  function handleCancel() {
    navigate(getRoutePath(RouteId.Dashboard))
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setIsSaving(true)

    const update: ProfileUpdateRequestDto = {
      ...info,
      profilePicture: uploadedProfilePicture
    }
    try {
      const result = await ProfileApi.saveGeneralInformation(
        update,
        i18n.language
      )
      setInfo({ ...result })
      setUploadedProfilePicture(undefined)
      addToast({
        title: t('profile.editor.saveSuccessTitle'),
        color: 'success'
      })
    } catch (e) {
      const error = (e as RestError).errorDto
      const messages = error?.errors ?? {}
      setErrorMessages(messages)
      if (Object.keys(messages).length === 0) {
        addToast({
          title: t('profile.editor.saveErrorTitle'),
          description: error?.message ?? t('error.genericMessage'),
          color: 'danger'
        })
      }
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <Form className={twoColumnForm()} onSubmit={handleSubmit}>
      <div className="flex flex-col gap-6">
        <Input
          name="jobTitle"
          label={t('fields.jobTitle')}
          value={info.jobTitle}
          onChange={handleChange}
          isInvalid={errorMessages.jobTitle != null}
          errorMessage={errorMessages.jobTitle}
          isRequired
        />

        <Textarea
          name="bio"
          label={t('fields.bio')}
          value={info.bio ?? ''}
          onChange={handleChange}
          isInvalid={errorMessages.bio != null}
          errorMessage={errorMessages.bio}
        />

        <SwitchInput
          name="isProfilePublic"
          hint={t('profile.editor.publicProfileHint')}
          isSelected={info.isProfilePublic}
          onChange={updateValue}
        >
          {t('profile.editor.publicProfile')}
        </SwitchInput>

        <SwitchInput
          name="isEmailPublic"
          hint={t('profile.editor.publicEmailHint')}
          isSelected={info.isEmailPublic}
          onChange={updateValue}
          isDisabled={!info.isProfilePublic}
        >
          {t('profile.editor.publicEmail')}
        </SwitchInput>

        <SwitchInput
          name="isPhonePublic"
          hint={t('profile.editor.publicPhoneHint')}
          isSelected={info.isPhonePublic}
          onChange={updateValue}
          isDisabled={!info.isProfilePublic}
        >
          {t('profile.editor.publicPhone')}
        </SwitchInput>

        <SwitchInput
          name="isAddressPublic"
          hint={t('profile.editor.publicAddressHint')}
          isSelected={info.isAddressPublic}
          onChange={updateValue}
          isDisabled={!info.isProfilePublic}
        >
          {t('profile.editor.publicAddress')}
        </SwitchInput>

        <SwitchInput
          name="hideDescriptions"
          hint={t('profile.editor.hideDescriptionsHint')}
          isSelected={info.hideDescriptions}
          onChange={updateValue}
          isDisabled={!info.isProfilePublic}
        >
          {t('profile.editor.hideDescriptions')}
        </SwitchInput>
      </div>
      <div className="flex flex-col gap-2">
        <img
          src={profilePicture}
          alt={t('fields.profilePicture')}
          className="max-w-full md:max-w-150"
        />
        <div>
          <Input
            name="profilePicture"
            type="file"
            accept=".png, .jpeg, .jpg"
            startContent={<FaCamera size={20} />}
            label={t('fields.profilePicture')}
            placeholder={'test'}
            onChange={handleImageChange}
            isInvalid={errorMessages.profilePicture != null}
            errorMessage={errorMessages.profilePicture}
          />
          <p className="text-gray-400 ml-2">
            {t('profile.editor.pictureHint')}
          </p>
        </div>
      </div>
      <FormButtons onCancel={handleCancel} isSaving={isSaving} />
    </Form>
  )
}