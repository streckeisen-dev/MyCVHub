import { type Composer, createI18n, type UseI18nOptions } from 'vue-i18n'
import LanguageService from '@/services/LanguageService'
import * as vuetifyLocale from 'vuetify/locale'
import type { Options } from '@vitejs/plugin-vue'

type NestedMessage = {
  [key: string]: string | NestedMessage
}

type Messages = {
  [locale: string]: NestedMessage
}

type DateFormat = {
  [key: string]: string
}

type LocaleDateFormat = {
  [locale: string]: { [name: string]: DateFormat }
}

const messages: Messages = {}
const localeDateFormats: LocaleDateFormat = {}
const langModules = import.meta.glob('../locales/*.json', { eager: true })
Object.keys(langModules).forEach((filePath) => {
  const lang = filePath.replace('../locales/', '').replace('.json', '')
  const vuetifyMessages = (vuetifyLocale as Messages)[lang]
  messages[lang] = {
    ...(langModules[filePath] as NestedMessage),
    $vuetify: vuetifyMessages
  }
})

Object.keys(messages).forEach((lang) => {
  localeDateFormats[lang] = {
    monthAndYear: {
      month: 'short',
      year: 'numeric'
    },
    simpleDate: {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }
  }
})

const i18n = createI18n({
  locale: LanguageService.getLanguage(),
  fallbackLocale: 'en',
  legacy: false,
  messages,
  datetimeFormats: localeDateFormats
})

export default i18n

export type UseI18n<Options extends UseI18nOptions = UseI18nOptions> = Composer<NonNullable<Options['messages']>, NonNullable<Options['datetimeFormats']>, NonNullable<Options['numberFormats']>, Options['locale'] extends unknown ? string : Options['locale']>