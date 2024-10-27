import * as validators from '@vuelidate/validators'
import i18n from '@/plugins/i18n'
import { type MessageParams } from '@vuelidate/validators'

const { createI18nMessage } = validators
const messageParams = (params: MessageParams) => ({
	...params,
	label: i18n.global.t(`fields.${params.property}`)
})

export const withI18nMessage = createI18nMessage({ t: i18n.global.t.bind(i18n), messageParams })
export const required = withI18nMessage(validators.required)
export const email = withI18nMessage(validators.email)
