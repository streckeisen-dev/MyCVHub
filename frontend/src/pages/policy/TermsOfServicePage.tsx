import { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { ExternalLink } from '@/components/ExternalLink.tsx'
import { SITE_CONFIG } from '@/config/RouteTree.tsx'
import { PolicyContent, PolicySectionProps } from '@/components/policy/PolicyContent.tsx'

const CONTACT_EMAIL = 'contact@mycvhub.ch'

const UL_CLASSES = 'list-disc ml-4'

const LAST_UPDATED = '2025-12-21'

export function TermsOfServicePage(): ReactNode {
  const { t } = useTranslation()

  const terms: PolicySectionProps[] = [
    {
      key: 'acceptance',
      title: t('tos.acceptance.title'),
      content: (
        <div>
          <p>{t('tos.acceptance.content')}</p>
        </div>
      )
    },
    {
      key: 'description',
      title: t('tos.description.title'),
      content: (
        <div>
          <p>{t('tos.description.content')}</p>
          <ul className={UL_CLASSES}>
            <li>{t('tos.description.desc1')}</li>
            <li>{t('tos.description.desc2')}</li>
            <li>{t('tos.description.desc3')}</li>
            <li>{t('tos.description.desc4')}</li>
          </ul>
          <p>{t('tos.description.addendum')}</p>
        </div>
      )
    },
    {
      key: 'accounts',
      title: t('tos.accounts.title'),
      content: (
        <div>
          <p className="font-bold">{t('tos.accounts.registration.title')}</p>
          <p>{t('tos.accounts.registration.content')}</p>
          <p className="font-bold">{t('tos.accounts.security.title')}</p>
          <p>{t('tos.accounts.security.content')}</p>
          <p className="font-bold">{t('tos.accounts.age.title')}</p>
          <p>{t('tos.accounts.age.content')}</p>
        </div>
      )
    },
    {
      key: 'contentAndOwnership',
      title: t('tos.contentAndOwnership.title'),
      content: (
        <div>
          <p className="font-bold">{t('tos.contentAndOwnership.content.title')}</p>
          <p>{t('tos.contentAndOwnership.content.content')}</p>
          <p className="font-bold">{t('tos.contentAndOwnership.platform.title')}</p>
          <p>
            {t('tos.contentAndOwnership.platform.content')}
            <ExternalLink color="foreground" href={SITE_CONFIG.links.github}>
              GitHub
            </ExternalLink>
            . {t('tos.contentAndOwnership.platform.contentContinue')}
          </p>
          <p className="font-bold">{t('tos.contentAndOwnership.sharing.title')}</p>
          <p>{t('tos.contentAndOwnership.sharing.content')}</p>
        </div>
      )
    },
    {
      key: 'prohibited',
      title: t('tos.prohibited.title'),
      content: (
        <div>
          <p>{t('tos.prohibited.content')}</p>
          <ul className={UL_CLASSES}>
            <li>{t('tos.prohibited.prohib1')}</li>
            <li>{t('tos.prohibited.prohib2')}</li>
            <li>{t('tos.prohibited.prohib3')}</li>
            <li>{t('tos.prohibited.prohib4')}</li>
            <li>{t('tos.prohibited.prohib5')}</li>
            <li>{t('tos.prohibited.prohib6')}</li>
            <li>{t('tos.prohibited.prohib7')}</li>
          </ul>
          <p>{t('tos.prohibited.addendum')}</p>
        </div>
      )
    },
    {
      key: 'disclaimer',
      title: t('tos.disclaimer.title'),
      content: (
        <div>
          <p className="font-bold">{t('tos.disclaimer.asIs.title')}: </p>
          <p>{t('tos.disclaimer.asIs.content')}</p>

          <p className="font-bold">{t('tos.disclaimer.noGuarantees.title')}: </p>
          <p>{t('tos.disclaimer.noGuarantees.content')}</p>
          <ul className={UL_CLASSES}>
            <li>{t('tos.disclaimer.noGuarantees.nonGuar1')}</li>
            <li>{t('tos.disclaimer.noGuarantees.nonGuar2')}</li>
            <li>{t('tos.disclaimer.noGuarantees.nonGuar3')}</li>
            <li>{t('tos.disclaimer.noGuarantees.nonGuar4')}</li>
          </ul>

          <p className="font-bold">{t('tos.disclaimer.limitation.title')}</p>
          <p>{t('tos.disclaimer.limitation.content')}</p>
          <ul className={UL_CLASSES}>
            <li>{t('tos.disclaimer.limitation.lim1')}</li>
            <li>{t('tos.disclaimer.limitation.lim2')}</li>
            <li>{t('tos.disclaimer.limitation.lim3')}</li>
            <li>{t('tos.disclaimer.limitation.lim4')}</li>
          </ul>

          <p className="font-bold">{t('tos.disclaimer.responsibility.title')}</p>
          <p>{t('tos.disclaimer.responsibility.content')}</p>
          <ul className={UL_CLASSES}>
            <li>{t('tos.disclaimer.responsibility.resp1')}</li>
            <li>{t('tos.disclaimer.responsibility.resp2')}</li>
            <li>{t('tos.disclaimer.responsibility.resp3')}</li>
            <li>{t('tos.disclaimer.responsibility.resp4')}</li>
          </ul>
        </div>
      )
    },
    {
      key: 'privacy',
      title: t('tos.privacy.title'),
      content: (
        <div>
          <p>{t('tos.privacy.content')}</p>
        </div>
      )
    },
    {
      key: 'serviceModifications',
      title: t('tos.serviceModifications.title'),
      content: (
        <div>
          <p>{t('tos.serviceModifications.content')}</p>
          <ul className={UL_CLASSES}>
            <li>{t('tos.serviceModifications.mod1')}</li>
            <li>{t('tos.serviceModifications.mod2')}</li>
            <li>{t('tos.serviceModifications.mod3')}</li>
            <li>{t('tos.serviceModifications.mod4')}</li>
          </ul>
          <p>{t('tos.serviceModifications.addendum')}</p>
        </div>
      )
    },
    {
      key: 'termination',
      title: t('tos.termination.title'),
      content: (
        <div>
          <p className="font-bold">{t('tos.termination.byYou.title')}:</p>
          <p>{t('tos.termination.byYou.content')}</p>

          <p className="font-bold">{t('tos.termination.byUs.title')}:</p>
          <p>{t('tos.termination.byUs.content')}</p>
          <ul className={UL_CLASSES}>
            <li>{t('tos.termination.byUs.term1')}</li>
            <li>{t('tos.termination.byUs.term2')}</li>
            <li>{t('tos.termination.byUs.term3')}</li>
            <li>{t('tos.termination.byUs.term4')}</li>
          </ul>
        </div>
      )
    },
    {
      key: 'thirdParty',
      title: t('tos.thirdParty.title'),
      content: <p>{t('tos.thirdParty.content')}</p>
    },
    {
      key: 'intellectualProperty',
      title: t('tos.intellectualProperty.title'),
      content: <p>{t('tos.intellectualProperty.content')}</p>
    },
    {
      key: 'jurisdiction',
      title: t('tos.jurisdiction.title'),
      content: <p>{t('tos.jurisdiction.content')}</p>
    },
    {
      key: 'changes',
      title: t('tos.changes.title'),
      content: <p>{t('tos.changes.content')}</p>
    },
    {
      key: 'severability',
      title: t('tos.severability.title'),
      content: <p>{t('tos.severability.content')}</p>
    },
    {
      key: 'agreement',
      title: t('tos.agreement.title'),
      content: <p>{t('tos.agreement.content')}</p>
    },
    {
      key: 'contact',
      title: t('legal.contact'),
      content: (
        <div>
          <p>
            {t('tos.contact')}{' '}
            <ExternalLink href={`mailto: ${CONTACT_EMAIL}`} color="foreground">
              {CONTACT_EMAIL}
            </ExternalLink>{' '}
          </p>
        </div>
      )
    }
  ]

  return <PolicyContent lastUpdated={LAST_UPDATED} content={terms} note={t('tos.note')} />
}
