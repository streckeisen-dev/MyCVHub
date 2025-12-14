import { ReactNode, useEffect, useState } from 'react'
import ProfileApi from '@/api/ProfileApi.ts'
import { useTranslation } from 'react-i18next'
import { ProfileDto } from '@/types/ProfileDto.ts'
import { addToast, Spinner } from '@heroui/react'
import { Empty } from '@/components/Empty.tsx'
import { ProfileEditor } from '@/pages/profile/editor/ProfileEditor.tsx'
import { RestError } from '@/types/RestError.ts'

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
          addToast({
            title: loadingError,
            color: 'danger'
          })
        }
      } finally {
        setIsLoading(false)
      }
    }
    loadProfile()
  }, [])

  return isLoading ? (
    <Spinner />
  ) : !profile ? (
    <Empty
      headline={t('error.genericMessageTitle')}
      title={t('profile.loadingError')}
    />
  ) : (
    <ProfileEditor initialValue={profile} />
  )
}
