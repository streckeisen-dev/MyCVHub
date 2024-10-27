<template>
	<v-row>
		<v-col cols="12">
			<h3>{{ t('account.passwordRequirements') }}</h3>
		</v-col>
	</v-row>
	<v-row>
		<v-list density="comfortable">
			<v-list-item
				v-for="requirement in passwordRequirements"
				:key="requirement.name"
				:title="requirement.name"
				:class="requirement.predicate() ? 'pw-requirement-fulfilled' : 'pw-requirement-error'" />
		</v-list>
	</v-row>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ValidatorFn } from '@vuelidate/core'
import { withI18nMessage } from '@/validation/validators'

type PasswordData = {
	password?: string
	confirmPassword?: string
}

type ValidationRules = { [key: string]: { [validator: string]: ValidatorFn } }

const formState = defineModel<PasswordData>('formState', {
	required: true
})

const rules = defineModel<ValidationRules>('rules', {
	required: true
})

const { t } = useI18n()

const passwordRequirements = computed(() => [
	{
		name: t('passwordRequirements.length', { length: '8' }),
		predicate: () => {
			if (formState.value.password == null) {
				return false
			}
			const pw = formState.value.password as string
			return pw.length >= 8
		}
	},
	{
		name: t('passwordRequirements.whitespaces'),
		predicate: () => {
			if (formState.value.password == null) {
				return false
			}
			const pw = formState.value.password as string
			return pw != '' && !pw.includes(' ')
		}
	},
	{
		name: t('passwordRequirements.digits'),
		predicate: () => {
			if (formState.value.password == null) {
				return false
			}
			const pw = formState.value.password as string
			return /\d/.test(pw)
		}
	},
	{
		name: t('passwordRequirements.specialChars'),
		predicate: () => {
			if (formState.value.password == null) {
				return false
			}
			const pw = formState.value.password as string
			return /\W/.test(pw)
		}
	},
	{
		name: t('passwordRequirements.uppercase'),
		predicate: () => {
			if (formState.value.password == null) {
				return false
			}
			const pw = formState.value.password as string
			return pw.toLowerCase() !== pw
		}
	},
	{
		name: t('passwordRequirements.lowercase'),
		predicate: () => {
			if (formState.value.password == null) {
				return false
			}
			const pw = formState.value.password as string
			return pw.toUpperCase() !== pw
		}
	},
	{
		name: t('passwordRequirements.match'),
		predicate: () => {
			if (formState.value.password == null) {
				return false
			}
			const pw = formState.value.password as string
			return pw === formState.value.confirmPassword
		}
	}
])

const passwordValidator = withI18nMessage(() =>
	passwordRequirements.value.every((r) => r.predicate())
)

rules.value.password.passwordValidator = passwordValidator
</script>

<style scoped lang="scss">
div.v-list-item {
	min-height: auto;

	&.pw-requirement-fulfilled {
		color: green;
	}

	&.pw-requirement-error {
		color: red;
	}
}
</style>
