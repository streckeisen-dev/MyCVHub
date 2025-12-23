import { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { ExternalLink } from '@/components/ExternalLink.tsx'
import { SITE_CONFIG } from '@/config/RouteTree.tsx'
import { PolicyContent } from '@/components/policy/PolicyContent.tsx'
import { PolicyList } from '@/components/policy/PolicyList.tsx'
import { PolicySectionProps } from '@/components/policy/PolicySection.tsx'
import { PolicyText } from '@/components/policy/PolicyText.tsx'

const CONTACT_EMAIL = 'contact@mycvhub.ch'

const LAST_UPDATED = '2025-12-21'

export function TermsOfServicePage(): ReactNode {
  const { t } = useTranslation()

  const terms: PolicySectionProps[] = [
    {
      key: 'acceptance',
      title: t('tos.acceptance.title'),
      content: <PolicyText text={t('tos.acceptance.content')} />
    },
    {
      key: 'description',
      title: t('tos.description.title'),
      content: (
        <PolicyList
          preamble={t('tos.description.content')}
          addendum={t('tos.description.addendum')}
        >
          <li>{t('tos.description.desc1')}</li>
          <li>{t('tos.description.desc2')}</li>
          <li>{t('tos.description.desc3')}</li>
          <li>{t('tos.description.desc4')}</li>
        </PolicyList>
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
        <PolicyList preamble={t('tos.prohibited.content')} addendum={t('tos.prohibited.addendum')}>
          <li>{t('tos.prohibited.prohib1')}</li>
          <li>{t('tos.prohibited.prohib2')}</li>
          <li>{t('tos.prohibited.prohib3')}</li>
          <li>{t('tos.prohibited.prohib4')}</li>
          <li>{t('tos.prohibited.prohib5')}</li>
          <li>{t('tos.prohibited.prohib6')}</li>
          <li>{t('tos.prohibited.prohib7')}</li>
        </PolicyList>
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
          <PolicyList preamble={t('tos.disclaimer.noGuarantees.content')}>
            <li>{t('tos.disclaimer.noGuarantees.nonGuar1')}</li>
            <li>{t('tos.disclaimer.noGuarantees.nonGuar2')}</li>
            <li>{t('tos.disclaimer.noGuarantees.nonGuar3')}</li>
            <li>{t('tos.disclaimer.noGuarantees.nonGuar4')}</li>
          </PolicyList>

          <p className="font-bold">{t('tos.disclaimer.limitation.title')}</p>
          <PolicyList preamble={t('tos.disclaimer.limitation.content')}>
            <li>{t('tos.disclaimer.limitation.lim1')}</li>
            <li>{t('tos.disclaimer.limitation.lim2')}</li>
            <li>{t('tos.disclaimer.limitation.lim3')}</li>
            <li>{t('tos.disclaimer.limitation.lim4')}</li>
          </PolicyList>

          <p className="font-bold">{t('tos.disclaimer.responsibility.title')}</p>
          <PolicyList preamble={t('tos.disclaimer.responsibility.content')}>
            <li>{t('tos.disclaimer.responsibility.resp1')}</li>
            <li>{t('tos.disclaimer.responsibility.resp2')}</li>
            <li>{t('tos.disclaimer.responsibility.resp3')}</li>
            <li>{t('tos.disclaimer.responsibility.resp4')}</li>
          </PolicyList>
        </div>
      )
    },
    {
      key: 'privacy',
      title: t('tos.privacy.title'),
      content: <PolicyText text={t('tos.privacy.content')} />
    },
    {
      key: 'serviceModifications',
      title: t('tos.serviceModifications.title'),
      content: (
        <PolicyList
          preamble={t('tos.serviceModifications.content')}
          addendum={t('tos.serviceModifications.addendum')}
        >
          <li>{t('tos.serviceModifications.mod1')}</li>
          <li>{t('tos.serviceModifications.mod2')}</li>
          <li>{t('tos.serviceModifications.mod3')}</li>
          <li>{t('tos.serviceModifications.mod4')}</li>
        </PolicyList>
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
          <PolicyList preamble={t('tos.termination.byUs.content')}>
            <li>{t('tos.termination.byUs.term1')}</li>
            <li>{t('tos.termination.byUs.term2')}</li>
            <li>{t('tos.termination.byUs.term3')}</li>
            <li>{t('tos.termination.byUs.term4')}</li>
          </PolicyList>
        </div>
      )
    },
    {
      key: 'thirdParty',
      title: t('tos.thirdParty.title'),
      content: <PolicyText text={t('tos.thirdParty.content')} />
    },
    {
      key: 'intellectualProperty',
      title: t('tos.intellectualProperty.title'),
      content: <PolicyText text={t('tos.intellectualProperty.content')} />
    },
    {
      key: 'jurisdiction',
      title: t('tos.jurisdiction.title'),
      content: <PolicyText text={t('tos.jurisdiction.content')} />
    },
    {
      key: 'changes',
      title: t('tos.changes.title'),
      content: <PolicyText text={t('tos.changes.content')} />
    },
    {
      key: 'severability',
      title: t('tos.severability.title'),
      content: <PolicyText text={t('tos.severability.content')} />
    },
    {
      key: 'agreement',
      title: t('tos.agreement.title'),
      content: <PolicyText text={t('tos.agreement.content')} />
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
