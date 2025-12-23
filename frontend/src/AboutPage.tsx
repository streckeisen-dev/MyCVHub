import { ReactNode } from 'react'
import { centerSection, title } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'
import { ExternalLink } from '@/components/ExternalLink.tsx'
import { SITE_CONFIG } from '@/config/RouteTree.tsx'
import { FaGithub } from 'react-icons/fa6'

export function AboutPage(): ReactNode {
  const {t} = useTranslation()

  return <section className={`${centerSection()} max-w-5xl mx-auto`}>
    <h1 className={title()}>{t('about.title')}</h1>

    <p>{t('about.what')}</p>

    <p>{t('about.about')}</p>

    <p>{t('about.openSource')}</p>
    <ExternalLink color="foreground" href={SITE_CONFIG.links.github} className="flex gap-1">
      <FaGithub size={25} /> <span>MyCVHub</span>
    </ExternalLink>
  </section>
}