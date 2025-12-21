import { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { centerSection, h4, title } from '@/styles/primitives.ts'
import { ExternalLink } from '@/components/ExternalLink.tsx'
import { formatDate } from '@/helpers/DateHelper.ts'

const CONTACT_EMAIL = 'contact@mycvhub.ch'

const UL_CLASSES = 'list-disc ml-4'

const LAST_UPDATED = '2025-12-21'

type PolicySectionProps = Readonly<{
  title: string
  content: ReactNode
}>

function PolicySection(props: PolicySectionProps): ReactNode {
  const { title, content } = props
  return (
    <section className="flex flex-col gap-2">
      <h4 className={h4()}>{title}</h4>
      <div>{content}</div>
    </section>
  )
}

export function PrivacyPolicyPage(): ReactNode {
  const { t } = useTranslation()

  const policy: (PolicySectionProps & { key: string })[] = [
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
            <ExternalLink color="foreground" href={`mailto:${CONTACT_EMAIL}`}>
              {CONTACT_EMAIL}
            </ExternalLink>
          </p>
        </div>
      )
    },
    {
      key: 'legalBasis',
      title: t('privacy.processing.title'),
      content: (
        <div>
          <p>{t('privacy.processing.content')}</p>
          <ul className={UL_CLASSES}>
            <li>{t('privacy.processing.proc1')}</li>
            <li>{t('privacy.processing.proc2')}</li>
            <li>{t('privacy.processing.proc3')}</li>
          </ul>
        </div>
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
        <>
          <p>{t('privacy.usage.content')}</p>
          <ul className={UL_CLASSES}>
            <li>{t('privacy.usage.usage1')}</li>
            <li>{t('privacy.usage.usage2')}</li>
            <li>{t('privacy.usage.usage3')}</li>
            <li>{t('privacy.usage.usage4')}</li>
          </ul>
        </>
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
        <div>
          <p>{t('privacy.sharing.content')}</p>
          <ul className={UL_CLASSES}>
            <li>{t('privacy.sharing.sharing1')}</li>
            <li>{t('privacy.sharing.sharing2')}</li>
            <li>{t('privacy.sharing.sharing3')}</li>
          </ul>
        </div>
      )
    },
    {
      key: 'rights',
      title: t('privacy.rights.title'),
      content: (
        <div>
          <p>{t('privacy.rights.content')}</p>
          <ul className={UL_CLASSES}>
            <li>{t('privacy.rights.right1')}</li>
            <li>{t('privacy.rights.right2')}</li>
            <li>{t('privacy.rights.right3')}</li>
            <li>{t('privacy.rights.right4')}</li>
            <li>{t('privacy.rights.right5')}</li>
            <li>{t('privacy.rights.right6')}</li>
            <li>{t('privacy.rights.right7')}</li>
          </ul>
        </div>
      )
    },
    {
      key: 'breach',
      title: t('privacy.breach.title'),
      content: <p>{t('privacy.breach.content')}</p>
    },
    {
      key: 'third-party-providers',
      title: t('privacy.thirdParty.title'),
      content: <p>{t('privacy.thirdParty.content')}</p>
    },
    {
      key: 'age',
      title: t('privacy.age.title'),
      content: <p>{t('privacy.age.content')}</p>
    },
    {
      key: 'changes',
      title: t('privacy.changes.title'),
      content: <p>{t('privacy.changes.content')}</p>
    },
    {
      key: 'contact',
      title: t('legal.contact'),
      content: (
        <p>
          <span>{t('privacy.contact')} </span>
          <ExternalLink color="foreground" href={`mailto:${CONTACT_EMAIL}`}>
            {CONTACT_EMAIL}
          </ExternalLink>
          .
        </p>
      )
    }
  ]

  return (
    <div className={centerSection()}>
      <h1 className={title()}>{t('privacy.title')}</h1>
      <p>
        {t('legal.lastUpdated')}: {formatDate(LAST_UPDATED)}
      </p>

      <div className="max-w-5xl flex flex-col gap-2">
        {policy.map((section, index) => (
          <PolicySection
            key={section.key}
            title={index + 1 + '. ' + section.title}
            content={section.content}
          />
        ))}

        <div>
          <p className="font-bold">{t('legal.note')}:</p>
          <p>{t('privacy.note')}</p>
        </div>
      </div>
    </div>
  )
}
