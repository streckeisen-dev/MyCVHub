import { Button } from '@/components/ui/Button.tsx'
import { ReactNode, useEffect, useState } from 'react'
import { ApplicationInfoDto, DashboardInfoDto } from '@/types/dashboard/DashboardInfoDto.ts'
import DashboardApi from '@/api/DashboardApi.ts'
import { useTranslation } from 'react-i18next'

import { Empty } from '@/components/Empty.tsx'
import { Link } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'

import { UnverifiedView } from '@/components/dashboard/UnverifiedView.tsx'
import { DashboardCard } from '@/components/dashboard/DashboardCard.tsx'
import { ProfileStat } from '@/components/dashboard/ProfileStat.tsx'
import { ApplicationStat } from '@/components/dashboard/ApplicationStat.tsx'
import { LoadingWrapper } from '@/layouts/LoadingWrapper.tsx'
import { Page, PageHeader, PageTitle } from '@/components/ui/Layout.tsx'

type DashboardContentProps = Readonly<{
  info: DashboardInfoDto
}>

function sortApplicationStats(a: ApplicationInfoDto, b: ApplicationInfoDto) {
  return b.count - a.count
}

function DashboardContent(props: DashboardContentProps): ReactNode {
  const { t } = useTranslation()
  const { info } = props

  info.applications.sort(sortApplicationStats)

  return (
    <div className="grid w-full max-w-5xl grid-cols-1 gap-5 md:grid-cols-2 md:gap-6">
      {info.profile ? (
        <DashboardCard title={t('dashboard.profile')}>
          <div className="flex h-full flex-col gap-5">
            <div className="flex flex-col">
              <ProfileStat
                ariaLabel={`${t('account.profile.edit')}: ${t('workExperience.title')}`}
                title={t('workExperience.title')}
                count={info.profile.experienceCount}
                type="experience"
              />

              <ProfileStat
                ariaLabel={`${t('account.profile.edit')}: ${t('education.title')}`}
                title={t('education.title')}
                count={info.profile.educationCount}
                type="education"
              />

              <ProfileStat
                ariaLabel={`${t('account.profile.edit')}: ${t('project.title')}`}
                title={t('project.title')}
                count={info.profile.projectCount}
                type="projects"
              />

              <ProfileStat
                ariaLabel={`${t('account.profile.edit')}: ${t('skills.title')}`}
                title={t('skills.title')}
                count={info.profile.skillCount}
                type="skills"
              />
            </div>

            <Button
              className="mt-auto w-fit"
              variant="primary"
              as={Link}
              to={getRoutePath(RouteId.EditProfile)}
            >
              {t('account.profile.edit')}
            </Button>
          </div>
        </DashboardCard>
      ) : (
        <DashboardCard title={t('dashboard.profile')}>
          <div className="flex h-full flex-col gap-4">
            <p className="text-default-600">{t('dashboard.noProfile')}</p>
            <Button
              variant="primary"
              className="mt-auto w-fit"
              as={Link}
              to={getRoutePath(RouteId.CreateProfile)}
            >
              {t('account.profile.create')}
            </Button>
          </div>
        </DashboardCard>
      )}
      <DashboardCard title={t('dashboard.applications')}>
        {info.applications.length > 0 ? (
          <div className="flex h-full flex-col gap-5">
            <div className="flex flex-col">
              {info.applications.map((stat) => (
                <ApplicationStat key={stat.status.key} stat={stat} />
              ))}
            </div>
            <Button
              className="mt-auto w-fit"
              variant="primary"
              as={Link}
              to={getRoutePath(RouteId.ApplicationsOverview)}
            >
              {t('dashboard.viewApplications')}
            </Button>
          </div>
        ) : (
          <p className="text-default-600">{t('application.noEntries')}</p>
        )}
      </DashboardCard>
    </div>
  )
}

export function DashboardPage(): ReactNode {
  const { t, i18n } = useTranslation()
  const [info, setInfo] = useState<DashboardInfoDto>()
  const [isLoading, setIsLoading] = useState<boolean>(true)

  useEffect(() => {
    async function loadInfo() {
      try {
        const dashboardInfo = await DashboardApi.getDashboardInfo(i18n.language)
        setInfo(dashboardInfo)
      } finally {
        setIsLoading(false)
      }
    }
    loadInfo()
  }, [])

  return (
    <LoadingWrapper isLoading={isLoading} className="w-full">
      {info ? (
        <Page size="wide">
          <PageHeader align="center">
            <PageTitle>{t('dashboard.title')}</PageTitle>
          </PageHeader>

          <div className="flex w-full justify-center">
            {info.isVerified ? <DashboardContent info={info} /> : <UnverifiedView />}
          </div>
        </Page>
      ) : (
        <Empty headline={t('dashboard.loadingError')} />
      )}
    </LoadingWrapper>
  )
}
