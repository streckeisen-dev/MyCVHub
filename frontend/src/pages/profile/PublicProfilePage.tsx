import { ReactNode, useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Link, Navbar, NavbarBrand, Spinner } from '@heroui/react'
import { PublicProfileDto } from '@/types/PublicProfileDto.ts'
import { useTranslation } from 'react-i18next'
import ProfileApi from '@/api/ProfileApi.ts'
import { Empty } from '@/components/Empty.tsx'
import { RestError } from '@/types/RestError.ts'
import { ProfileTheme } from '@/config/ProfileTheme.ts'
import { getMatchingTextColor } from '@/styles/TextColor.ts'
import { h3, h4 } from '@/styles/primitives.ts'
import { WorkExperienceList } from '@/pages/profile/editor/workExperience/WorkExperienceList.tsx'
import { EducationList } from '@/pages/profile/editor/education/EducationList.tsx'
import { ProjectList } from '@/pages/profile/editor/project/ProjectList.tsx'
import { SkillList } from '@/pages/profile/editor/skill/SkillList.tsx'
import i18next, { TFunction } from 'i18next'

export function PublicProfilePage(): ReactNode {
  const { t: defaultT, i18n } = useTranslation()
  const params = useParams()

  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [loadingError, setLoadingError] = useState<string>()
  const [profile, setProfile] = useState<PublicProfileDto>()
  const t: TFunction = profile ? i18next.getFixedT(profile.language) : defaultT

  useEffect(() => {
    async function loadProfile() {
      const username = params.username
      if (!username) {
        setIsLoading(false)
        return
      }

      try {
        const result = await ProfileApi.getPublicProfile(
          username,
          i18n.language
        )
        await i18next.loadLanguages(result.language)
        setProfile(result)
      } catch (e) {
        const error = (e as RestError).errorDto
        setLoadingError(error?.message ?? t('error.genericMessage'))
      } finally {
        setIsLoading(false)
      }
    }
    loadProfile()
  }, [])

  const surfaceColor = profile?.theme?.surfaceColor ?? ProfileTheme.header
  const backgroundColor = profile?.theme?.backgroundColor ?? ProfileTheme.background
  const contentTextColor = getMatchingTextColor(backgroundColor)
  const name = `${profile?.firstName} ${profile?.lastName}`

  return isLoading ? (
    <Spinner />
  ) : !profile ? (
    <Empty
      headline={t('profile.notFound.headline')}
      title={t('profile.notFound.title')}
      text={loadingError}
    />
  ) : (
    <div className="relative flex flex-col h-screen">
      <Navbar
        maxWidth="full"
        position="sticky"
        style={{
          backgroundColor: surfaceColor,
          color: getMatchingTextColor(surfaceColor)
        }}
      >
        <NavbarBrand>
          <p className="text-xl font-bold">{name}</p>
        </NavbarBrand>
      </Navbar>
      <main
        className="w-full mx-auto flex-grow pt-16 px-6 lg:px-15 lg:pb-10"
        style={{
          backgroundColor: backgroundColor,
          color: contentTextColor
        }}
      >
        <div className="grid grid-cols-12 gap-3">
          <section className="col-span-12 lg:col-span-6 flex flex-col gap-2">
            <img
              src={profile.profilePicture}
              alt={t('fields.profilePicture')}
              className="max-w-full md:max-w-3/4 lg:max-w-1/2"
            />
            <div className="mb-2">
              <h3 className={h3()}>{name}</h3>
              <p>{profile.jobTitle}</p>
            </div>
            {profile.bio && (
              <>
                <h4 className={h4()}>{t('profile.aboutMe')}</h4>
                <p>{profile.bio}</p>
              </>
            )}

            {(profile.phone != null || profile.address != null || profile.email != null) && (
              <ContactInfo
                profile={profile}
                textColor={contentTextColor}
                t={t}
              />
            )}
          </section>
          <div className="col-span-12 lg:col-span-6 flex flex-col gap-4">
            {profile.workExperiences.length > 0 && (
              <section className="flex flex-col gap-2">
                <h4 className={h4()}>{t('workExperience.title')}</h4>
                <WorkExperienceList workExperiences={profile.workExperiences} />
              </section>
            )}
            {profile.education.length > 0 && (
              <section className="flex flex-col gap-2">
                <h4 className={h4()}>{t('education.title')}</h4>
                <EducationList education={profile.education} />
              </section>
            )}
            {profile.projects.length > 0 && (
              <section className="flex flex-col gap-2">
                <h4 className={h4()}>{t('project.title')}</h4>
                <ProjectList projects={profile.projects} />
              </section>
            )}
            {profile.skills.length > 0 && (
              <section className="flex flex-col gap-2">
                <h4 className={h4()}>{t('skills.title')}</h4>
                <SkillList skills={profile.skills} />
              </section>
            )}
          </div>
        </div>
      </main>
    </div>
  )
}

interface ContactInfoProps {
  profile: PublicProfileDto;
  textColor: string;
  t: TFunction;
}

function ContactInfo(props: ContactInfoProps): ReactNode {
  const {profile, textColor, t} = props

  return (
    <div className="flex flex-col gap-0.5">
      <h4 className={h4()}>{t('profile.contact')}</h4>
      {profile.address && (
        <>
          <p>
            {profile.firstName} {profile.lastName}
          </p>
          <p>
            {profile.address.street} {profile.address.houseNumber}
          </p>
          <p>
            {profile.address.country}-{profile.address.postcode}{' '}
            {profile.address.city}
          </p>
        </>
      )}
      {profile.email && (
        <p>
          {t('fields.email')}:{' '}
          <Link
            style={{
              color: textColor
            }}
            isExternal
            href={`mailto:${profile.email}`}
          >
            {profile.email}
          </Link>
        </p>
      )}
      {profile.phone && (
        <p>
          {t('fields.phone')}:{' '}
          <Link
            style={{
              color: textColor
            }}
            isExternal
            href={`tel:${profile.phone}`}
          >
            {profile.phone}
          </Link>
        </p>
      )}
    </div>
  )
}
