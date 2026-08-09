import { Button } from '@/components/ui/Button.tsx'
import { title } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate } from 'react-router-dom'
import { use, useEffect } from 'react'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { HomePageCard } from '@/components/home/HomePageCard.tsx'
import { HomePageList } from '@/components/home/HomePageList.tsx'
import { Page } from '@/components/ui/Layout.tsx'
import { FaBriefcase, FaFilePdf, FaUser } from 'react-icons/fa6'

export default function HomePage() {
  const { t } = useTranslation()
  const { user } = use(AuthorizationContext)
  const navigate = useNavigate()

  useEffect(() => {
    if (user) {
      navigate(getRoutePath(RouteId.Dashboard))
    }
  }, [user])

  return (
    <Page size="wide" className="gap-12">
      <section className="grid min-h-[calc(100dvh-12rem)] items-center gap-8 lg:grid-cols-[minmax(0,1fr)_minmax(22rem,28rem)]">
        <div className="flex max-w-3xl flex-col items-start gap-6">
          <h1 className={title({ size: 'md', class: 'block' })}>{t('home.welcome.title')}</h1>
          <p className="max-w-2xl text-base leading-7 text-default-600 md:text-lg">
            {t('home.welcome.message')}
          </p>
          <p className="max-w-2xl text-sm leading-6 text-default-500">
            {t('home.welcome.positioning')}
          </p>
          <Button as={Link} to={getRoutePath(RouteId.Signup)} variant="primary">
            {t('home.callToAction.btn')}
          </Button>
        </div>

        <div className="rounded-lg border border-default-200 bg-surface p-5">
          <p className="text-sm font-semibold uppercase text-default-500">
            {t('home.features.title')}
          </p>
          <div className="mt-5 flex flex-col gap-4">
            <HomePageCard
              icon={<FaUser size={18} />}
              title={t('home.features.create.title')}
              description={t('home.features.create.description')}
            />
            <HomePageCard
              icon={<FaFilePdf size={18} />}
              title={t('home.features.generate.title')}
              description={t('home.features.generate.description')}
            />
            <HomePageCard
              icon={<FaBriefcase size={18} />}
              title={t('home.features.tracking.title')}
              description={t('home.features.tracking.description')}
            />
          </div>
        </div>
      </section>

      <section className="rounded-lg border border-default-200 bg-surface p-5">
        <p className="text-sm font-semibold uppercase text-default-500">
          {t('home.workflow.title')}
        </p>
        <div className="mt-4 grid gap-3 md:grid-cols-4">
          {[
            t('home.workflow.profile'),
            t('home.workflow.include'),
            t('home.workflow.generate'),
            t('home.workflow.track')
          ].map((step, index) => (
            <div key={step} className="flex items-center gap-3 rounded-md bg-default/40 p-3">
              <span className="inline-flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-accent text-sm font-semibold text-accent-foreground">
                {index + 1}
              </span>
              <span className="text-sm font-medium text-foreground">{step}</span>
            </div>
          ))}
        </div>
      </section>

      <section className="flex flex-col gap-5">
        <h2 className="text-2xl font-semibold text-foreground">{t('home.why.title')}</h2>
        <HomePageList
          entries={[
            {
              title: t('home.why.allInOne.title'),
              description: t('home.why.allInOne.description')
            },
            {
              title: t('home.why.alwaysUpToDate.title'),
              description: t('home.why.alwaysUpToDate.description')
            },
            {
              title: t('home.why.organized.title'),
              description: t('home.why.organized.description')
            }
          ]}
        />
      </section>

      <section className="flex flex-col items-start gap-4 rounded-lg border border-default-200 bg-surface p-6 md:flex-row md:items-center md:justify-between">
        <div>
          <h2 className="text-xl font-semibold text-foreground">{t('home.callToAction.title')}</h2>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-default-600">
            {t('home.callToAction.description')}
          </p>
          <p className="mt-1 max-w-2xl text-sm leading-6 text-default-500">
            {t('home.callToAction.reassurance')}
          </p>
        </div>
        <Button as={Link} to={getRoutePath(RouteId.Signup)} variant="primary">
          {t('home.callToAction.btn')}
        </Button>
      </section>
    </Page>
  )
}
