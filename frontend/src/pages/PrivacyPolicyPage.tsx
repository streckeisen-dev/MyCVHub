import { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { h4, title } from '@/styles/primitives.ts'
import { ExternalLink } from '@/components/ExternalLink.tsx'

const CONTACT_EMAIL = 'contact@mycvhub.ch'

const UL_CLASSES = 'list-disc ml-4'

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
      content: <p>{t('privacy.intro.content')}</p>
    },
    {
      key: 'data-collection',
      title: t('privacy.collection.title'),
      content: (
        <>
          <p>{t('privacy.collection.content')}</p>
          <ul className={UL_CLASSES}>
            <li>
              <strong>{t('privacy.collection.profileInfo.title')}</strong>:
              <p>{t('privacy.collection.profileInfo.content')}</p>
            </li>
            <li>
              <strong>{t('privacy.collection.profilePicture.title')}</strong>:
              <p>{t('privacy.collection.profilePicture.content')}</p>
            </li>
          </ul>
        </>
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
          </ul>
        </>
      )
    },
    {
      key: 'data-storage',
      title: t('privacy.storage.title'),
      content: (
        <>
          <p>
            <strong>{t('privacy.storage.userData.title')}</strong>:
            <p>{t('privacy.storage.userData.content')}</p>
          </p>
          <p>
            <strong>{t('privacy.storage.pictures.title')}</strong>:
            <p>{t('privacy.storage.pictures.content')}</p>
          </p>
        </>
      )
    },
    {
      key: 'data-access',
      title: t('privacy.access.title'),
      content: <p>{t('privacy.access.content')}</p>
    },
    {
      key: 'data-sharing',
      title: t('privacy.sharing.title'),
      content: <p>{t('privacy.sharing.content')}</p>
    },
    {
      key: 'third-party-providers',
      title: t('privacy.thirdParty.title'),
      content: <p>{t('privacy.thirdParty.content')}</p>
    },
    {
      key: 'contact',
      title: t('privacy.contact.title'),
      content: (
        <p>
          <span>{t('privacy.contact.content')} </span>
          <ExternalLink href={`mailto:${CONTACT_EMAIL}`}>{CONTACT_EMAIL}</ExternalLink>.
        </p>
      )
    }
  ]

  return (
    <div className="flex flex-col gap-2">
      <h1 className={title() + ' text-center'}>{t('privacy.title')}</h1>
      <p className="text-center">{t('privacy.description')}</p>

      {policy.map((section, index) => (
        <PolicySection
          key={section.key}
          title={index + 1 + '. ' + section.title}
          content={section.content}
        />
      ))}
    </div>
  )
}
