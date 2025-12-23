import { ReactNode, useEffect, useState } from 'react'
import { title } from '@/styles/primitives.ts'
import { ApplicationInfoDto, DashboardInfoDto } from '@/types/dashboard/DashboardInfoDto.ts'
import DashboardApi from '@/api/DashboardApi.ts'
import { useTranslation } from 'react-i18next'
import { Button, Divider } from '@heroui/react'
import { Empty } from '@/components/Empty.tsx'
import { Link } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'

import classes from './DashboardPage.module.css'
import { UnverifiedView } from '@/components/dashboard/UnverifiedView.tsx'
import { DashboardCard } from '@/components/dashboard/DashboardCard.tsx'
import { ProfileStat } from '@/components/dashboard/ProfileStat.tsx'
import { ApplicationStat } from '@/components/dashboard/ApplicationStat.tsx'
import { LoadingWrapper } from '@/layouts/LoadingWrapper.tsx'

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
    <div className="grid grid-cols-1 md:grid-cols-2 gap-5 w-full">
      {info.profile ? (
        <DashboardCard title={t('dashboard.profile')}>
          <div className="flex flex-col gap-4">
            <div className={`grid grid-cols-[75%_auto_auto] ${classes.profileCard}`}>
              <Divider className="col-span-3" />

              <ProfileStat
                title={t('workExperience.title')}
                count={info.profile.experienceCount}
                type="experience"
              />

              <ProfileStat
                title={t('education.title')}
                count={info.profile.educationCount}
                type="education"
              />

              <ProfileStat
                title={t('project.title')}
                count={info.profile.educationCount}
                type="projects"
              />

              <ProfileStat
                title={t('skills.title')}
                count={info.profile.skillCount}
                type="skills"
              />
            </div>

            <Button
              className="w-min"
              color="primary"
              as={Link}
              to={getRoutePath(RouteId.EditProfile)}
            >
              {t('account.profile.edit')}
            </Button>
          </div>
        </DashboardCard>
      ) : (
        <DashboardCard title={t('dashboard.profile')}>
          <div className="flex flex-col gap-2">
            <p>{t('dashboard.noProfile')}</p>
            <Button
              color="primary"
              className="w-min"
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
          <div className="flex flex-col gap-4">
            <div className="grid grid-cols-2 gap-1.5">
              <Divider className="col-span-2" />
              {info.applications.map((stat) => (
                <ApplicationStat key={stat.status.key} stat={stat} />
              ))}
            </div>
            <Button
              className="w-min"
              color="primary"
              as={Link}
              to={getRoutePath(RouteId.ApplicationsOverview)}
            >
              {t('dashboard.viewApplications')}
            </Button>
          </div>
        ) : (
          <p>{t('application.noEntries')}</p>
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
    <LoadingWrapper isLoading={isLoading}>
      {info ? (
        <>
          <h1 className={title()}>{t('dashboard.title')}</h1>

          {info.isVerified ? <DashboardContent info={info} /> : <UnverifiedView />}
        </>
      ) : (
        <Empty headline={t('dashboard.loadingError')} />
      )}
    </LoadingWrapper>
  )
}
