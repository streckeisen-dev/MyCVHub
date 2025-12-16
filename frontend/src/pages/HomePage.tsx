import { Button, Card, CardBody, CardHeader} from '@heroui/react'

import { h3, h4, subtitle, title } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate } from 'react-router-dom'
import { use, useEffect } from 'react'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'

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
      <section className="flex flex-col items-center justify-center gap-4 py-8 md:py-10">
        <div className="inline-block max-w-lg text-center justify-center">
          <span className={title()}>{t('home.welcome.title')}</span>
          <div className={subtitle({ class: 'mt-4' })}>
            {t('home.welcome.message')}
          </div>
        </div>

        <div className="flex gap-3">
          <Card className="p-3">
            <CardHeader>
              <h4 className={h4()}>{t('home.features.create.title')}</h4>
            </CardHeader>
            <CardBody>
              <p>{t('home.features.create.description')}</p>
            </CardBody>
          </Card>

          <Card className="p-3">
            <CardHeader>
              <h4 className={h4()}>{t('home.features.share.title')}</h4>
            </CardHeader>
            <CardBody>
              <p>{t('home.features.share.description')}</p>
            </CardBody>
          </Card>
        </div>

        <div className="flex flex-col items-center gap-5">
          <h2 className={h3()}>{t('home.callToAction.title')}</h2>
          <Button as={Link} to="register" color="primary">
            {t('home.callToAction.btn')}
          </Button>
        </div>
      </section>
  )
}
