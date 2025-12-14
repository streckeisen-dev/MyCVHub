import { ReactNode, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { ProfileEditor } from '@/pages/profile/editor/ProfileEditor.tsx'
import ProfileApi from '@/api/ProfileApi.ts'
import { useNavigate } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'

export function CreateProfilePage(): ReactNode {
  const { i18n } = useTranslation()
  const navigate = useNavigate()

  useEffect(() => {
    async function loadProfile() {
      try {
        await ProfileApi.getProfile(i18n.language)
        navigate(getRoutePath(RouteId.EditProfile))
      } catch (_ignore) {
        // no profile -> stay on page
      }
    }
    loadProfile()
  }, [])

  return <ProfileEditor />
}
