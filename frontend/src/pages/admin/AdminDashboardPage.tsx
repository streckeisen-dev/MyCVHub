import AdminAuthApi from '@/api/AdminAuthApi.ts'
import AdminDashboardApi from '@/api/AdminDashboardApi.ts'
import { Button } from '@/components/ui/Button.tsx'
import { Card, CardBody, CardHeader, Chip } from '@/components/ui/Display.tsx'
import { Page, PageHeader, PageTitle } from '@/components/ui/Layout.tsx'
import { AdminAuthorizationContext } from '@/context/AdminAuthorizationContext.tsx'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { LoadingWrapper } from '@/layouts/LoadingWrapper.tsx'
import { AdminDashboardDto, AdminDashboardMetricPointDto } from '@/types/admin/AdminDashboardDto.ts'
import { RestError } from '@/types/RestError.ts'
import clsx from 'clsx'
import { ReactNode, use, useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { FaArrowRightFromBracket, FaChartLine, FaClock, FaUserCheck, FaUsers } from 'react-icons/fa6'
import { useNavigate } from 'react-router-dom'

type DashboardRange = '7d' | '30d' | '90d'

const ranges: DashboardRange[] = ['7d', '30d', '90d']

export function AdminDashboardPage(): ReactNode {
  const { t, i18n } = useTranslation()
  const { admin, handleAdminLogout } = use(AdminAuthorizationContext)
  const [dashboard, setDashboard] = useState<AdminDashboardDto>()
  const [range, setRange] = useState<DashboardRange>('30d')
  const [isLoading, setIsLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    let ignore = false

    async function loadDashboard() {
      setIsLoading(true)
      try {
        const result = await AdminDashboardApi.getDashboard(i18n.language, range)
        if (!ignore) {
          setDashboard(result)
        }
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('admin.dashboard.error'), error?.message ?? t('error.genericMessage'))
      } finally {
        if (!ignore) {
          setIsLoading(false)
        }
      }
    }

    loadDashboard()
    return () => {
      ignore = true
    }
  }, [i18n.language, range, t])

  async function handleLogout() {
    await AdminAuthApi.logout(i18n.language)
    handleAdminLogout()
    navigate('/admin/login', { replace: true })
  }

  return (
    <Page size="wide">
      <PageHeader className="flex-row items-start justify-between gap-4">
        <div className="flex flex-col gap-2">
          <PageTitle>{t('admin.dashboard.title')}</PageTitle>
          <div className="flex flex-wrap items-center gap-2 text-sm text-default-600">
            <span>{admin?.username}</span>
            {admin?.role && <Chip>{admin.role}</Chip>}
          </div>
        </div>
        <Button variant="outline" onPress={handleLogout}>
          <FaArrowRightFromBracket aria-hidden />
          {t('admin.logout')}
        </Button>
      </PageHeader>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex rounded-full border border-default-200 bg-content1 p-1">
          {ranges.map((item) => (
            <Button
              key={item}
              className={clsx('min-h-8 px-3 py-1', item === range && 'bg-default-foreground text-background')}
              variant={item === range ? 'primary' : 'ghost'}
              onPress={() => setRange(item)}
            >
              {t(`admin.dashboard.range.${item}`)}
            </Button>
          ))}
        </div>
        {dashboard && (
          <div className="flex items-center gap-2 text-sm text-default-600">
            <FaClock aria-hidden />
            <span>{t('admin.dashboard.asOf', { value: formatDateTime(dashboard.asOf, i18n.language) })}</span>
          </div>
        )}
      </div>

      <LoadingWrapper isLoading={isLoading} className="w-full">
        {dashboard && <DashboardContent dashboard={dashboard} locale={i18n.language} />}
      </LoadingWrapper>
    </Page>
  )
}

function DashboardContent(props: Readonly<{ dashboard: AdminDashboardDto; locale: string }>): ReactNode {
  const { t } = useTranslation()
  const { dashboard, locale } = props
  const verifiedTotal = dashboard.verifiedAccountCount + dashboard.unverifiedAccountCount
  const verifiedPercent = verifiedTotal === 0 ? 0 : Math.round((dashboard.verifiedAccountCount / verifiedTotal) * 100)
  const oauthTotal = dashboard.oauthSignupCount + dashboard.passwordSignupCount
  const oauthPercent = oauthTotal === 0 ? 0 : Math.round((dashboard.oauthSignupCount / oauthTotal) * 100)

  return (
    <div className="flex w-full flex-col gap-5">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <MetricCard icon={<FaUsers aria-hidden />} label={t('admin.dashboard.totalUsers')} value={dashboard.totalRegisteredUsers} />
        <MetricCard icon={<FaChartLine aria-hidden />} label={t('admin.dashboard.dailyActiveUsers')} value={dashboard.dailyActiveUsers} />
        <MetricCard icon={<FaChartLine aria-hidden />} label={t('admin.dashboard.weeklyActiveUsers')} value={dashboard.weeklyActiveUsers} />
        <MetricCard icon={<FaChartLine aria-hidden />} label={t('admin.dashboard.monthlyActiveUsers')} value={dashboard.monthlyActiveUsers} />
      </div>

      <div className="grid grid-cols-1 gap-5 xl:grid-cols-2">
        <ChartCard title={t('admin.dashboard.activeUserSeries')} series={dashboard.activeUserSeries} locale={locale} colorClass="text-accent" />
        <ChartCard title={t('admin.dashboard.signupSeries')} series={dashboard.signupSeries} locale={locale} colorClass="text-success" />
      </div>

      <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
        <SplitCard
          title={t('admin.dashboard.accountVerification')}
          primaryLabel={t('admin.dashboard.verified')}
          primaryValue={dashboard.verifiedAccountCount}
          secondaryLabel={t('admin.dashboard.unverified')}
          secondaryValue={dashboard.unverifiedAccountCount}
          percent={verifiedPercent}
        />
        <SplitCard
          title={t('admin.dashboard.signupSource')}
          primaryLabel={t('admin.dashboard.oauth')}
          primaryValue={dashboard.oauthSignupCount}
          secondaryLabel={t('admin.dashboard.password')}
          secondaryValue={dashboard.passwordSignupCount}
          percent={oauthPercent}
        />
      </div>
    </div>
  )
}

function MetricCard(props: Readonly<{ icon: ReactNode; label: string; value: number }>): ReactNode {
  return (
    <Card className="rounded-lg border border-default-200">
      <CardBody className="flex flex-row items-center gap-4 p-5">
        <div className="flex size-10 items-center justify-center rounded-full bg-default-100 text-default-700">
          {props.icon}
        </div>
        <div className="flex min-w-0 flex-col">
          <span className="text-sm text-default-600">{props.label}</span>
          <strong className="text-2xl font-semibold text-foreground">{formatNumber(props.value)}</strong>
        </div>
      </CardBody>
    </Card>
  )
}

function ChartCard(props: Readonly<{
  title: string
  series: AdminDashboardMetricPointDto[]
  locale: string
  colorClass: string
}>): ReactNode {
  return (
    <Card className="rounded-lg border border-default-200">
      <CardHeader className="px-5 pt-5">
        <h2 className="text-base font-semibold text-foreground">{props.title}</h2>
      </CardHeader>
      <CardBody className="px-5 pb-5">
        <MiniChart series={props.series} locale={props.locale} colorClass={props.colorClass} />
      </CardBody>
    </Card>
  )
}

function SplitCard(props: Readonly<{
  title: string
  primaryLabel: string
  primaryValue: number
  secondaryLabel: string
  secondaryValue: number
  percent: number
}>): ReactNode {
  return (
    <Card className="rounded-lg border border-default-200">
      <CardBody className="flex flex-col gap-4 p-5">
        <div className="flex items-center gap-2">
          <FaUserCheck aria-hidden className="text-default-500" />
          <h2 className="text-base font-semibold text-foreground">{props.title}</h2>
        </div>
        <div className="h-3 overflow-hidden rounded-full bg-default-100">
          <div className="h-full rounded-full bg-accent" style={{ width: `${props.percent}%` }} />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <SplitValue label={props.primaryLabel} value={props.primaryValue} />
          <SplitValue label={props.secondaryLabel} value={props.secondaryValue} />
        </div>
      </CardBody>
    </Card>
  )
}

function SplitValue(props: Readonly<{ label: string; value: number }>): ReactNode {
  return (
    <div className="flex flex-col">
      <span className="text-sm text-default-600">{props.label}</span>
      <strong className="text-xl font-semibold text-foreground">{formatNumber(props.value)}</strong>
    </div>
  )
}

function MiniChart(props: Readonly<{
  series: AdminDashboardMetricPointDto[]
  locale: string
  colorClass: string
}>): ReactNode {
  const max = useMemo(() => Math.max(1, ...props.series.map((point) => point.value)), [props.series])
  const points = useMemo(() => {
    if (props.series.length === 0) return ''
    return props.series
      .map((point, index) => {
        const x = props.series.length === 1 ? 0 : (index / (props.series.length - 1)) * 100
        const y = 100 - (point.value / max) * 88
        return `${x},${y}`
      })
      .join(' ')
  }, [max, props.series])
  const lastPoint = props.series.at(-1)

  return (
    <div className="flex flex-col gap-3">
      <div className="h-48 w-full">
        <svg className={clsx('h-full w-full', props.colorClass)} role="img" viewBox="0 0 100 100" preserveAspectRatio="none">
          <polyline
            fill="none"
            stroke="currentColor"
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2.5"
            points={points}
            vectorEffect="non-scaling-stroke"
          />
        </svg>
      </div>
      {lastPoint && (
        <div className="flex items-center justify-between text-sm text-default-600">
          <span>{formatDate(lastPoint.date, props.locale)}</span>
          <strong className="text-foreground">{formatNumber(lastPoint.value)}</strong>
        </div>
      )}
    </div>
  )
}

function formatNumber(value: number): string {
  return new Intl.NumberFormat().format(value)
}

function formatDate(value: string, locale: string): string {
  return new Intl.DateTimeFormat(locale, { month: 'short', day: 'numeric' }).format(new Date(value))
}

function formatDateTime(value: string, locale: string): string {
  return new Intl.DateTimeFormat(locale, {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value))
}
