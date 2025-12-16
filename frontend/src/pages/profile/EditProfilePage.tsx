import { ReactNode, useEffect, useState } from 'react'
import ProfileApi from '@/api/ProfileApi.ts'
import { useTranslation } from 'react-i18next'
import { ProfileDto } from '@/types/ProfileDto.ts'
import { Spinner } from '@heroui/react'
import { Empty } from '@/components/Empty.tsx'
import { ProfileEditor } from '@/components/profile/editor/ProfileEditor.tsx'
import { RestError } from '@/types/RestError.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'

export function EditProfilePage(): ReactNode {
  const { t, i18n } = useTranslation()
  const [profile, setProfile] = useState<ProfileDto>()
  const [isLoading, setIsLoading] = useState<boolean>(true)

  useEffect(() => {
    async function loadProfile() {
      try {
        const result = await ProfileApi.getProfile(i18n.language)
        setProfile(result)
      } catch (e) {
        const error = (e as RestError).errorDto
        const loadingError = error?.message ?? t('error.genericMessage')
        if (loadingError) {
          addErrorToast(loadingError)
        }
      } finally {
        setIsLoading(false)
      }
    }
    loadProfile()
  }, [])

  const content = profile ? (
    <ProfileEditor initialValue={profile} />
  ) : (
    <Empty headline={t('error.genericMessageTitle')} title={t('profile.loadingError')} />
  )

  return isLoading ? <Spinner /> : content
}
