import { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { ExternalLink } from '@/components/ExternalLink.tsx'
import { PolicyContent } from '@/components/policy/PolicyContent.tsx'
import { SITE_CONFIG } from '@/config/RouteTree.tsx'
import { PolicyList } from '@/components/policy/PolicyList.tsx'
import { PolicySectionProps } from '@/components/policy/PolicySection.tsx'
import { PolicyText } from '@/components/policy/PolicyText.tsx'

const LAST_UPDATED = '2025-12-21'

export function PrivacyPolicyPage(): ReactNode {
  const { t } = useTranslation()

  const policy: PolicySectionProps[] = [
    {
      key: 'intro',
      title: t('privacy.intro.title'),
      content: (
        <div>
          <p>{t('privacy.intro.content')}</p>
          <p className="font-bold">{t('privacy.intro.controller.title')}</p>
          <p>{t('privacy.intro.controller.legalEntity')}</p>
          <p>{t('privacy.intro.controller.location')}</p>
          <p>
            {t('legal.contact')}:{' '}
            <ExternalLink color="foreground" href={`mailto:${SITE_CONFIG.contact}`}>
              {SITE_CONFIG.contact}
            </ExternalLink>
          </p>
        </div>
      )
    },
    {
      key: 'legalBasis',
      title: t('privacy.processing.title'),
      content: (
        <PolicyList preamble={t('privacy.processing.content')}>
          <li>{t('privacy.processing.proc1')}</li>
          <li>{t('privacy.processing.proc2')}</li>
          <li>{t('privacy.processing.proc3')}</li>
        </PolicyList>
      )
    },
    {
      key: 'data-collection',
      title: t('privacy.collection.title'),
      content: (
        <div>
          <p>{t('privacy.collection.content')}</p>
          <p className="font-bold">{t('privacy.collection.profileInfo.title')}: </p>
          <p>{t('privacy.collection.profileInfo.content')}</p>

          <p className="font-bold">{t('privacy.collection.profilePicture.title')}:</p>
          <p>{t('privacy.collection.profilePicture.content')}</p>

          <p className="font-bold">{t('privacy.collection.authData.title')}:</p>
          <p>{t('privacy.collection.authData.content')}</p>
        </div>
      )
    },
    {
      key: 'data-usage',
      title: t('privacy.usage.title'),
      content: (
        <PolicyList preamble={t('privacy.usage.content')}>
          <li>{t('privacy.usage.usage1')}</li>
          <li>{t('privacy.usage.usage2')}</li>
          <li>{t('privacy.usage.usage3')}</li>
          <li>{t('privacy.usage.usage4')}</li>
        </PolicyList>
      )
    },
    {
      key: 'data-storage',
      title: t('privacy.storage.title'),
      content: (
        <div>
          <p className="font-bold">{t('privacy.storage.userData.title')}:</p>
          <p>{t('privacy.storage.userData.content')}</p>

          <p className="font-bold">{t('privacy.storage.pictures.title')}:</p>
          <p>{t('privacy.storage.pictures.content')}</p>

          <p className="font-bold">{t('privacy.storage.security.title')}:</p>
          <p>{t('privacy.storage.security.content')}</p>
        </div>
      )
    },
    {
      key: 'retention',
      title: t('privacy.retention.title'),
      content: <p>{t('privacy.retention.content')}</p>
    },
    {
      key: 'sharing',
      title: t('privacy.sharing.title'),
      content: (
        <PolicyList preamble={t('privacy.sharing.content')}>
          <li>{t('privacy.sharing.sharing1')}</li>
          <li>{t('privacy.sharing.sharing2')}</li>
          <li>{t('privacy.sharing.sharing3')}</li>
        </PolicyList>
      )
    },
    {
      key: 'rights',
      title: t('privacy.rights.title'),
      content: (
        <PolicyList preamble={t('privacy.rights.content')}>
          <li>{t('privacy.rights.right1')}</li>
          <li>{t('privacy.rights.right2')}</li>
          <li>{t('privacy.rights.right3')}</li>
          <li>{t('privacy.rights.right4')}</li>
          <li>{t('privacy.rights.right5')}</li>
          <li>{t('privacy.rights.right6')}</li>
          <li>{t('privacy.rights.right7')}</li>
        </PolicyList>
      )
    },
    {
      key: 'breach',
      title: t('privacy.breach.title'),
      content: <PolicyText text={t('privacy.breach.content')} />
    },
    {
      key: 'third-party-providers',
      title: t('privacy.thirdParty.title'),
      content: <PolicyText text={t('privacy.thirdParty.content')} />
    },
    {
      key: 'age',
      title: t('privacy.age.title'),
      content: <PolicyText text={t('privacy.age.content')} />
    },
    {
      key: 'changes',
      title: t('privacy.changes.title'),
      content: <PolicyText text={t('privacy.changes.content')} />
    },
    {
      key: 'contact',
      title: t('legal.contact'),
      content: (
        <p>
          <span>{t('privacy.contact')} </span>
          <ExternalLink color="foreground" href={`mailto:${SITE_CONFIG.contact}`}>
            {SITE_CONFIG.contact}
          </ExternalLink>
          .
        </p>
      )
    }
  ]

  return <PolicyContent lastUpdated={LAST_UPDATED} content={policy} note={t('privacy.note')} />
}
