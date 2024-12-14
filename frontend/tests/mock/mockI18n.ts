import { createI18n } from 'vue-i18n'

const i18n = createI18n({
  legacy: false,
  locale: 'en',
  messages: {
    en: {
      date: {
        today: 'Today'
      }
    }
  },
  datetimeFormats: {
    en: {
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
  }
})

export default i18n