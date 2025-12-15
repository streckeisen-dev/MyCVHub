import { ReactNode, useEffect, useState } from 'react'
import { centerSection, title } from '@/styles/primitives.ts'
import { DashboardInfoDto } from '@/types/DashboardInfoDto.ts'
import DashboardApi from '@/api/DashboardApi.ts'
import { useTranslation } from 'react-i18next'
import { Button, Divider, Spinner } from '@heroui/react'
import { Empty } from '@/components/Empty.tsx'
import { Link } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { FaPen } from 'react-icons/fa6'

import classes from '../DashboardPage.module.css'
import { UnverifiedPage } from '@/pages/dashboard/UnverifiedPage.tsx'
import { DashboardCard } from '@/pages/dashboard/DashboardCard.tsx'

type DashboardContentProps = Readonly<{
  info: DashboardInfoDto
}>

function DashboardContent(props: DashboardContentProps): ReactNode {
  const { t } = useTranslation()
  const { info } = props

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-5 w-full">
      {info.profile ? (
        <DashboardCard
          title={t('dashboard.profile')}
          content={
            <div className="flex flex-col gap-4">
              <div className={`grid grid-cols-[75%_auto_auto] ${classes.profileCard}`}>
                <Divider className="col-span-3" />
                <p>{t('workExperience.title')}</p>
                <p>{info.profile.experienceCount}</p>
                <Link to={getRoutePath(RouteId.EditProfile, 'experience')}>
                  <FaPen />
                </Link>

                <Divider className="col-span-3" />

                <p>{t('education.title')}</p>
                <p>{info.profile.educationCount}</p>
                <Link to={getRoutePath(RouteId.EditProfile, 'education')}>
                  <FaPen />
                </Link>

                <Divider className="col-span-3" />

                <p>{t('project.title')}</p>
                <p>{info.profile.projectCount}</p>
                <Link to={getRoutePath(RouteId.EditProfile, 'projects')}>
                  <FaPen />
                </Link>

                <Divider className="col-span-3" />

                <p>{t('skills.title')}</p>
                <p>{info.profile.skillCount}</p>
                <Link to={getRoutePath(RouteId.EditProfile, 'skills')}>
                  <FaPen />
                </Link>

                <Divider className="col-span-3" />
              </div>
              <div>
                <Button color="primary" as={Link} to={getRoutePath(RouteId.EditProfile)}>
                  {t('account.profile.edit')}
                </Button>
              </div>
            </div>
          }
        />
      ) : (
        <DashboardCard
          title={t('dashboard.profile')}
          content={
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
          }
        />
      )}
      <DashboardCard title={'Job Application Tracking'} content={<p>Coming Soon</p>} />
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

  const content = info ? (
    <>
      <h1 className={title()}>{t('dashboard.title')}</h1>

      {info.isVerified ? <DashboardContent info={info} /> : <UnverifiedPage />}
    </>
  ) : (
    <Empty headline={t('dashboard.loadingError')} />
  )

  return (
    <section className={centerSection()}>
      {isLoading ? (
        <Spinner />
      ) : content
      }
    </section>
  )
}
