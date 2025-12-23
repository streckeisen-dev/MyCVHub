import { Button } from '@heroui/react'

import { centerSection, h3, h4, subtitle, title } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate } from 'react-router-dom'
import { use, useEffect } from 'react'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { HomePageCard } from '@/components/home/HomePageCard.tsx'
import { HomePageList } from '@/components/home/HomePageList.tsx'

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
    <section className={centerSection()}>
      <div className="inline-block max-w-2xl text-center justify-center">
        <h1 className={title()}>{t('home.welcome.title')}</h1>
        <div className={subtitle({ class: 'mt-4' })}>{t('home.welcome.message')}</div>
      </div>

      <h3 className={h3()}>{t('home.features.title')}</h3>
      <div className="flex flex-wrap gap-3 gap-x-6 justify-center">
        <HomePageCard
          title={t('home.features.create.title')}
          description={t('home.features.create.description')}
        />

        <HomePageCard
          title={t('home.features.generate.title')}
          description={t('home.features.generate.description')}
        />

        <HomePageCard
          title={t('home.features.tracking.title')}
          description={t('home.features.tracking.description')}
        />
      </div>

      <h3 className={h3()}>{t('home.why.title')}</h3>
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

      <h4 className={h4()}>{t('home.callToAction.title')}</h4>
      <div className="flex flex-col items-center gap-2">
        <p>{t('home.callToAction.description')}</p>
        <Button as={Link} to="register" color="primary">
          {t('home.callToAction.btn')}
        </Button>
      </div>
    </section>
  )
}
