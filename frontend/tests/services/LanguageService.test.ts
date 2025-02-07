import { describe, expect, it } from 'vitest'
import LanguageService, { LANGUAGE_KEY } from '../../src/services/LanguageService'

describe('getLanguage', () => {
  it('should return navigator language when no language is set', () => {
    const lang = LanguageService.getLanguage()

    expect(lang).toBe(navigator.language.split('-')[0])
  })

  it('should return language from local storage if set', () => {
    const lang = 'de'
    localStorage.setItem(LANGUAGE_KEY, lang)

    const result = LanguageService.getLanguage()

    expect(result).toBe(lang)
  })
})

describe('setLanguage', () => {
  it('should set language in local storage', () => {
    const lang = 'jp'

    LanguageService.setLanguage(lang)
    const result = localStorage.getItem(LANGUAGE_KEY)

    expect(result).toBe(lang)
  })
})