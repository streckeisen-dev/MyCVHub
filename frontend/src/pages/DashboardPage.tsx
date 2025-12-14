import { ReactNode, useEffect, useState } from 'react'
import { centerSection, h3, title } from '@/styles/primitives.ts'
import { DashboardInfoDto } from '@/types/DashboardInfoDto.ts'
import DashboardApi from '@/api/DashboardApi.ts'
import { useTranslation } from 'react-i18next'
import {
  addToast,
  Button,
  Card,
  CardBody,
  CardHeader,
  Divider,
  Spinner
} from '@heroui/react'
import { Empty } from '@/components/Empty.tsx'
import AccountApi from '@/api/AccountApi.ts'
import { RestError } from '@/types/RestError.ts'
import { Link } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { FaPen } from 'react-icons/fa6'

import classes from './DashboardPage.module.css'

function UnverifiedPage(): ReactNode {
  const { t, i18n } = useTranslation()

  async function handleGenerateToken() {
    try {
      await AccountApi.generateVerificationCode(i18n.language)
      addToast({
        title: t('account.verification.resend.success'),
        timeout: 2500,
        color: 'success'
      })
    } catch (e) {
      const error = (e as RestError).errorDto
      addToast({
        title: t('account.verification.resend.error'),
        description: error?.message ?? t('error.genericMessage'),
        color: 'danger'
      })
    }
  }

  return (
    <div className="flex flex-col gap-4 items-center">
      <h3 className={h3()}>{t('account.verification.pending.title')}</h3>
      <p>{t('account.verification.pending.description')}</p>
      <p>
        {t('account.verification.resend.description')}
        <Button
          color="primary"
          variant="light"
          onPress={handleGenerateToken}
          size="lg"
        >
          {t('account.verification.resend.action')}
        </Button>
      </p>
    </div>
  )
}

interface DashboardContentProps {
  info: DashboardInfoDto;
}

interface DashboardCardProps {
  title: string;
  content: ReactNode;
}
function DashboardCard(props: DashboardCardProps): ReactNode {
  const { title, content } = props
  return (
    <Card className="p-2">
      <CardHeader className="font-bold">{title}</CardHeader>
      <CardBody>{content}</CardBody>
    </Card>
  )
}

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
                <div
                  className={`grid grid-cols-[75%_auto_auto] ${classes.profileCard}`}
                >
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
                  <Button
                    color="primary"
                    as={Link}
                    to={getRoutePath(RouteId.EditProfile)}
                  >
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
        <DashboardCard
          title={'Job Application Tracking'}
          content={<p>Coming Soon</p>}
        />
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
        const dashboardInfo = await DashboardApi.getDashboardInfo(
          i18n.language
        )
        setInfo(dashboardInfo)
      } catch (_ignore) {
        //ignore
      } finally {
        setIsLoading(false)
      }
    }
    loadInfo()
  }, [])

  return (
    <section className={centerSection()}>
      {isLoading ? (
        <Spinner />
      ) : !info ? (
        <Empty headline={'test'} title={'test'} />
      ) : (
        <>
          <h1 className={title()}>{t('dashboard.title')}</h1>

          {!info.isVerified ? (
            <UnverifiedPage />
          ) : (
            <DashboardContent info={info} />
          )}
        </>
      )}
    </section>
  )
}
